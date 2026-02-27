package com.codesmithslabs.thedogtail.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codesmithslabs.thedogtail.data.HabitDao
import com.codesmithslabs.thedogtail.data.UserDao
import com.codesmithslabs.thedogtail.data.HabitLogDao
import com.codesmithslabs.thedogtail.data.HabitLogEntity
import com.codesmithslabs.thedogtail.data.HabitEntity
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
    private var allHabitsCache: List<HabitEntity> = emptyList()

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
                allHabitsCache = habits
                updateHabitsForSelectedDate()
            }
        }
    }

    private fun loadLogs() {
        viewModelScope.launch {
            habitLogDao.getAllLogs().collect { logs ->
                logsCache = logs
                updateHabitLogsForSelectedDate()
            }
        }
    }

    private fun updateHabitsForSelectedDate() {
        val selectedDate = LocalDate.ofEpochDay(_state.value.selectedEpochDay)
        // LocalDate.dayOfWeek.value returns 1 (Mon) to 7 (Sun)
        val dayOfWeek = selectedDate.dayOfWeek.value 
        
        val filteredHabits = allHabitsCache.filter { habit ->
             // Parse "1,2,3" string. If empty or null, assume everyday or handle gracefully.
             // Based on HabitEntity default "1,2,3,4,5,6,7", it should be fine.
             val scheduledDays = habit.selectedDays.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
             scheduledDays.contains(dayOfWeek)
        }
        _state.value = _state.value.copy(habits = filteredHabits)
    }

    private fun updateHabitLogsForSelectedDate() {
        val day = _state.value.selectedEpochDay
        val logsForDay = logsCache.asSequence()
            .filter { it.dateEpochDay == day }
            .associateBy { it.habitId }
        _state.value = _state.value.copy(habitLogs = logsForDay)
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
                updateHabitsForSelectedDate()
                updateHabitLogsForSelectedDate()
            }
            is HomeContract.Event.OnToggleHabit -> {
                toggleHabit(event.habitId, event.isDone)
            }
            is HomeContract.Event.OnUpdateHabitValue -> {
                updateHabitValue(event.habitId, event.newValue)
            }
            is HomeContract.Event.OnTimerClicked -> {
                sendEffect(HomeContract.Effect.NavigateToTimer(event.habitId))
            }
            is HomeContract.Event.OnProfileClicked -> {
                sendEffect(HomeContract.Effect.NavigateToProfile)
            }
            is HomeContract.Event.OnMoodClicked -> {
                _state.value = _state.value.copy(currentTab = HomeContract.HomeTab.MOOD)
            }
            is HomeContract.Event.OnHomeClicked -> {
                _state.value = _state.value.copy(currentTab = HomeContract.HomeTab.HABITS)
            }
            is HomeContract.Event.OnEditHabitClicked -> {
                _state.value = _state.value.copy(
                    showEditDialog = true,
                    selectedHabitId = event.habitId
                )
            }
            is HomeContract.Event.OnDeleteHabitClicked -> {
                _state.value = _state.value.copy(
                    showDeleteDialog = true,
                    selectedHabitId = event.habitId
                )
            }
            is HomeContract.Event.OnDismissDialog -> {
                _state.value = _state.value.copy(
                    showDeleteDialog = false,
                    showEditDialog = false,
                    selectedHabitId = null
                )
            }
            is HomeContract.Event.OnConfirmDelete -> {
                val habitId = _state.value.selectedHabitId
                if (habitId != null) {
                    viewModelScope.launch {
                        val habit = habitDao.getHabitById(habitId)
                        if (habit != null) {
                            habitDao.deleteHabit(habit)
                        }
                    }
                }
                _state.value = _state.value.copy(
                    showDeleteDialog = false,
                    selectedHabitId = null
                )
            }
            is HomeContract.Event.OnConfirmEdit -> {
                val habitId = _state.value.selectedHabitId
                if (habitId != null) {
                    sendEffect(HomeContract.Effect.NavigateToEditHabit(habitId))
                }
                _state.value = _state.value.copy(
                    showEditDialog = false,
                    selectedHabitId = null
                )
            }
        }
    }

    private fun toggleHabit(habitId: Long, isDone: Boolean) {
        viewModelScope.launch {
            val day = _state.value.selectedEpochDay
            val existing = habitLogDao.getLogForDay(habitId, day)
            if (isDone && existing == null) {
                habitLogDao.insertLog(HabitLogEntity(habitId = habitId, dateEpochDay = day, value = 1f))
            } else if (!isDone && existing != null) {
                habitLogDao.deleteLog(existing)
            }
        }
    }

    private fun updateHabitValue(habitId: Long, newValue: Float) {
        viewModelScope.launch {
            val day = _state.value.selectedEpochDay
            val existing = habitLogDao.getLogForDay(habitId, day)
            if (existing != null) {
                if (newValue <= 0 && existing.value != null && existing.value <= 0) {
                     // Maybe delete if 0? For now just update
                     habitLogDao.insertLog(existing.copy(value = newValue))
                } else {
                    habitLogDao.insertLog(existing.copy(value = newValue))
                }
            } else {
                habitLogDao.insertLog(HabitLogEntity(habitId = habitId, dateEpochDay = day, value = newValue))
            }
        }
    }

    private fun sendEffect(effect: HomeContract.Effect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}
