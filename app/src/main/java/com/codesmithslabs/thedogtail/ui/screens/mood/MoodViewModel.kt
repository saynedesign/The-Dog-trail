package com.codesmithslabs.thedogtail.ui.screens.mood

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codesmithslabs.thedogtail.data.MoodDao
import com.codesmithslabs.thedogtail.data.MoodEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class MoodViewModel @Inject constructor(
    private val moodDao: MoodDao
) : ViewModel() {

    private val _state = MutableStateFlow(MoodContract.State())
    val state: StateFlow<MoodContract.State> = _state.asStateFlow()

    private val _effect = Channel<MoodContract.Effect>()
    val effect = _effect.receiveAsFlow()

    init {
        loadMoodsForMonth(_state.value.selectedMonth)
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            moodDao.getAllMoods().collect { moods ->
                _state.value = _state.value.copy(moodHistory = moods)
            }
        }
    }

    private fun loadMoodsForMonth(month: YearMonth) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val start = month.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val end = month.atEndOfMonth().plusDays(1).atStartOfDay(ZoneId.systemDefault()).minusNanos(1).toInstant().toEpochMilli()

            moodDao.getMoodsForRange(start, end).collect { moods ->
                val moodMap = moods.associateBy {
                     java.time.Instant.ofEpochMilli(it.timestamp).atZone(ZoneId.systemDefault()).toLocalDate().dayOfMonth
                }
                
                _state.value = _state.value.copy(
                    moods = moodMap,
                    isLoading = false
                )
            }
        }
    }

    fun handleEvent(event: MoodContract.Event) {
        when (event) {
            is MoodContract.Event.OnMonthChanged -> {
                _state.value = _state.value.copy(selectedMonth = event.month)
                loadMoodsForMonth(event.month)
            }
            is MoodContract.Event.OnDayClicked -> {
                 val date = _state.value.selectedMonth.atDay(event.day)
                 if (date.isAfter(LocalDate.now())) return
                 
                 _state.value = _state.value.copy(
                     showAddMoodDialog = true,
                     selectedDateForMood = event.day,
                     currentStep = 1,
                     selectedMoodType = null,
                     selectedMoodEmoji = null,
                     selectedFeeling = null
                 )
            }
            is MoodContract.Event.OnAddTodayMoodClicked -> {
                val today = LocalDate.now()
                val newState = _state.value.copy(
                    showAddMoodDialog = true,
                    selectedDateForMood = today.dayOfMonth,
                    currentStep = 1,
                    selectedMoodType = null,
                    selectedMoodEmoji = null,
                    selectedFeeling = null
                )

                if (today.year == _state.value.selectedMonth.year && today.month == _state.value.selectedMonth.month) {
                    _state.value = newState
                } else {
                    val currentMonth = YearMonth.now()
                    _state.value = newState.copy(selectedMonth = currentMonth)
                    loadMoodsForMonth(currentMonth)
                }
            }
            is MoodContract.Event.OnMoodOptionSelected -> {
                _state.value = _state.value.copy(
                    selectedMoodType = event.moodType,
                    selectedMoodEmoji = event.emoji
                )
            }
            is MoodContract.Event.OnSubmitMood -> {
                if (_state.value.selectedMoodType != null) {
                    _state.value = _state.value.copy(currentStep = 2)
                }
            }
            is MoodContract.Event.OnFeelingOptionSelected -> {
                 _state.value = _state.value.copy(selectedFeeling = event.feeling)
            }
            is MoodContract.Event.OnSubmitFeeling -> {
                saveMood()
            }
            is MoodContract.Event.OnBackStep -> {
                _state.value = _state.value.copy(currentStep = 1)
            }
            is MoodContract.Event.OnDismissDialog -> {
                _state.value = _state.value.copy(showAddMoodDialog = false, selectedDateForMood = null, currentStep = 1, selectedMoodType = null, selectedFeeling = null)
            }
            is MoodContract.Event.OnBackClicked -> {
                viewModelScope.launch {
                    _effect.send(MoodContract.Effect.NavigateBack)
                }
            }
            is MoodContract.Event.OnHistoryClicked -> {
                _state.value = _state.value.copy(showHistory = true)
            }
            is MoodContract.Event.OnCloseHistory -> {
                _state.value = _state.value.copy(showHistory = false)
            }
        }
    }

    private fun saveMood() {
        val day = _state.value.selectedDateForMood ?: return
        val moodType = _state.value.selectedMoodType ?: return
        val moodEmoji = _state.value.selectedMoodEmoji ?: return
        val feeling = _state.value.selectedFeeling ?: ""

        val date = _state.value.selectedMonth.atDay(day)
        val timestamp = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        viewModelScope.launch {
            val mood = MoodEntity(
                moodType = moodType,
                moodEmoji = moodEmoji,
                timestamp = timestamp,
                feeling = feeling
            )
            moodDao.insertMood(mood)
            _state.value = _state.value.copy(showAddMoodDialog = false, selectedDateForMood = null)
        }
    }
}
