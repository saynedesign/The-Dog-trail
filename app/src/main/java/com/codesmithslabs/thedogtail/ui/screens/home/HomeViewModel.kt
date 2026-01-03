package com.codesmithslabs.thedogtail.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codesmithslabs.thedogtail.data.HabitDao
import com.codesmithslabs.thedogtail.data.UserDao
import com.codesmithslabs.thedogtail.data.HabitLogDao
import com.codesmithslabs.thedogtail.data.HabitLogEntity
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val habitDao: HabitDao,
    private val userDao: UserDao,
    private val habitLogDao: HabitLogDao
) : ViewModel() {

    private val _state = MutableStateFlow(HomeContract.State())
    val state: StateFlow<HomeContract.State> = _state.asStateFlow()

    private val _effect = Channel<HomeContract.Effect>()
    val effect = _effect.receiveAsFlow()

    private var logsCache: List<HabitLogEntity> = emptyList()

    init {
        loadUserData()
        loadHabits()
        loadLogs()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            // Get the single user (Flow<UserEntity?>)
            userDao.getUser().collect { user ->
                if (user != null) {
                    _state.value = _state.value.copy(
                        userName = user.name,
                        userImageUri = user.profileImageUri
                    )
                }
            }
        }
    }

    private fun loadHabits() {
        viewModelScope.launch {
            habitDao.getAllHabits().collect { habits ->
                _state.value = _state.value.copy(habits = habits)
            }
        }
    }

    private fun loadLogs() {
        viewModelScope.launch {
            habitLogDao.getAllLogs().collect { logs ->
                logsCache = logs
                updateCompletedForSelectedDate()
            }
        }
    }

    private fun updateCompletedForSelectedDate() {
        val day = _state.value.selectedEpochDay
        val completed = logsCache.asSequence()
            .filter { it.dateEpochDay == day }
            .map { it.habitId }
            .toSet()
        _state.value = _state.value.copy(completedForSelectedDate = completed)
    }

    private fun epochDayFor(fullDateString: String): Long {
        val formatter = DateTimeFormatter.ofPattern("EEE d", Locale.getDefault())
        val today = LocalDate.now()
        for (offset in -30..0) {
            val date = today.plusDays(offset.toLong())
            if (date.format(formatter) == fullDateString) {
                return date.toEpochDay()
            }
        }
        return today.toEpochDay()
    }

    fun handleEvent(event: HomeContract.Event) {
        when (event) {
            is HomeContract.Event.OnAddHabitClicked -> {
                sendEffect(HomeContract.Effect.NavigateToAddHabit)
            }
            is HomeContract.Event.OnHabitClicked -> {
                sendEffect(HomeContract.Effect.NavigateToHabitDetails(event.habitId))
            }
            is HomeContract.Event.OnDateSelected -> {
                val epoch = epochDayFor(event.date)
                _state.value = _state.value.copy(selectedDate = event.date, selectedEpochDay = epoch)
                updateCompletedForSelectedDate()
            }
            is HomeContract.Event.OnToggleHabit -> {
                toggleHabit(event.habitId, event.isDone)
            }
            is HomeContract.Event.OnProfileClicked -> {
                sendEffect(HomeContract.Effect.NavigateToProfile)
            }
        }
    }

    private fun toggleHabit(habitId: Long, isDone: Boolean) {
        viewModelScope.launch {
            val day = _state.value.selectedEpochDay
            val existing = habitLogDao.getLogForDay(habitId, day)
            if (isDone && existing == null) {
                habitLogDao.insertLog(HabitLogEntity(habitId = habitId, dateEpochDay = day))
            } else if (!isDone && existing != null) {
                habitLogDao.deleteLog(existing)
            }
        }
    }

    private fun sendEffect(effect: HomeContract.Effect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}
