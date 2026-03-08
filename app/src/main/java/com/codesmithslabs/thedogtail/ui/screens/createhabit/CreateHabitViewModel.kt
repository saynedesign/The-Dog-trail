package com.codesmithslabs.thedogtail.ui.screens.createhabit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codesmithslabs.thedogtail.data.HabitDao
import com.codesmithslabs.thedogtail.data.HabitEntity
import com.codesmithslabs.thedogtail.util.AwardXpUseCase
import com.codesmithslabs.thedogtail.util.LevelSystem
import com.codesmithslabs.thedogtail.util.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateHabitViewModel @Inject constructor(
    private val habitDao: HabitDao,
    private val notificationScheduler: NotificationScheduler,
    private val awardXpUseCase: AwardXpUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(CreateHabitContract.State())
    val state: StateFlow<CreateHabitContract.State> = _state.asStateFlow()

    private val _effect = Channel<CreateHabitContract.Effect>()
    val effect = _effect.receiveAsFlow()

    init {
        val habitId = savedStateHandle.get<Long>("habitId")
        if (habitId != null && habitId != -1L) {
            loadHabit(habitId)
        }
    }

    private fun loadHabit(habitId: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val habit = habitDao.getHabitById(habitId)
                if (habit != null) {
                    _state.value = _state.value.copy(
                        habitId = habit.id,
                        habitName = habit.title,
                        description = habit.description,
                        habitIcon = habit.icon.ifEmpty { "📝" },
                        habitType = try {
                            CreateHabitContract.HabitType.valueOf(habit.type)
                        } catch (e: Exception) {
                            CreateHabitContract.HabitType.YES_NO
                        },
                        habitColor = habit.color,
                        isOneTime = habit.isOneTime,
                        frequency = try {
                            CreateHabitContract.Frequency.valueOf(habit.frequency)
                        } catch (e: Exception) {
                            CreateHabitContract.Frequency.DAILY
                        },
                        timeOfDay = try {
                            CreateHabitContract.TimeOfDay.valueOf(habit.timeOfDay.uppercase())
                        } catch (e: Exception) {
                            CreateHabitContract.TimeOfDay.ANYTIME
                        },
                        scheduledDate = habit.scheduledDate ?: System.currentTimeMillis(),
                        endDate = habit.endDate,
                        endDateEnabled = habit.endDate != null,
                        
                        target = if (habit.type == "TIMER") habit.targetValue.toLong().toString() else habit.targetValue.toInt().toString(),
                        unitName = habit.unit,
                        isAtLeast = habit.isAtLeast,
                        reminderEnabled = habit.reminderEnabled,
                        reminderTime = habit.reminderTime,
                        selectedDays = habit.selectedDays.split(",").mapNotNull { it.toIntOrNull() }.toSet(),
                        isLoading = false
                    )
                } else {
                    _state.value = _state.value.copy(isLoading = false, isError = true)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, isError = true)
            }
        }
    }

    fun handleEvent(event: CreateHabitContract.Event) {
        when (event) {
            is CreateHabitContract.Event.OnNameChange -> {
                _state.value = _state.value.copy(habitName = event.name)
            }
            is CreateHabitContract.Event.OnDescriptionChange -> {
                _state.value = _state.value.copy(description = event.description)
            }
            is CreateHabitContract.Event.OnTypeChange -> {
                _state.value = _state.value.copy(habitType = event.type)
            }
            
            // New Events
            is CreateHabitContract.Event.OnToggleOneTime -> {
                _state.value = _state.value.copy(isOneTime = event.isOneTime)
            }
            is CreateHabitContract.Event.OnColorChange -> {
                _state.value = _state.value.copy(habitColor = event.color)
            }
            is CreateHabitContract.Event.OnIconChange -> {
                _state.value = _state.value.copy(habitIcon = event.icon)
            }
            is CreateHabitContract.Event.OnFrequencyChange -> {
                _state.value = _state.value.copy(frequency = event.frequency)
                // Auto-select days based on frequency if needed
                if (event.frequency == CreateHabitContract.Frequency.DAILY) {
                    _state.value = _state.value.copy(selectedDays = setOf(1, 2, 3, 4, 5, 6, 7))
                }
            }
            is CreateHabitContract.Event.OnTimeOfDayChange -> {
                _state.value = _state.value.copy(timeOfDay = event.timeOfDay)
            }
            is CreateHabitContract.Event.OnDateChange -> {
                _state.value = _state.value.copy(scheduledDate = event.date)
            }
            is CreateHabitContract.Event.OnEndDateToggle -> {
                _state.value = _state.value.copy(endDateEnabled = event.enabled)
                if (!event.enabled) {
                    _state.value = _state.value.copy(endDate = null)
                } else if (_state.value.endDate == null) {
                    _state.value = _state.value.copy(endDate = System.currentTimeMillis())
                }
            }
            is CreateHabitContract.Event.OnEndDateChange -> {
                _state.value = _state.value.copy(endDate = event.date)
            }
            
            is CreateHabitContract.Event.OnReminderToggle -> {
                _state.value = _state.value.copy(reminderEnabled = event.enabled)
            }
            is CreateHabitContract.Event.OnReminderTimeChange -> {
                _state.value = _state.value.copy(reminderTime = event.time)
            }
            is CreateHabitContract.Event.OnDayToggle -> {
                val currentDays = _state.value.selectedDays.toMutableSet()
                if (currentDays.contains(event.day)) {
                    if (currentDays.size > 1) { // Prevent empty days
                        currentDays.remove(event.day)
                    }
                } else {
                    currentDays.add(event.day)
                }
                _state.value = _state.value.copy(selectedDays = currentDays)
            }
            is CreateHabitContract.Event.OnToggleIconPicker -> {
                _state.value = _state.value.copy(showIconPicker = event.show)
            }
            is CreateHabitContract.Event.OnSaveClicked -> {
                saveHabit()
            }
            is CreateHabitContract.Event.OnBackClicked -> {
                viewModelScope.launch {
                    _effect.send(CreateHabitContract.Effect.NavigateBack)
                }
            }
        }
    }

    private fun saveHabit() {
        val currentState = _state.value
        if (currentState.habitName.isBlank()) {
            // TODO: Show error via Effect
            return
        }

        viewModelScope.launch {
            _state.value = currentState.copy(isLoading = true)
            try {
                val habit = HabitEntity(
                    id = currentState.habitId ?: 0L,
                    title = currentState.habitName,
                    description = currentState.description,
                    type = currentState.habitType.name,
                    targetValue = currentState.target.toFloatOrNull() ?: 1f,
                    unit = currentState.unitName,
                    isAtLeast = currentState.isAtLeast,
                    
                    icon = currentState.habitIcon,
                    color = currentState.habitColor,
                    
                    // New Fields
                    isOneTime = currentState.isOneTime,
                    frequency = currentState.frequency.name,
                    timeOfDay = currentState.timeOfDay.name,
                    scheduledDate = if (currentState.isOneTime) currentState.scheduledDate else null,
                    endDate = if (currentState.endDateEnabled) currentState.endDate else null,
                    
                    reminderEnabled = currentState.reminderEnabled,
                    reminderTime = currentState.reminderTime,
                    selectedDays = currentState.selectedDays.sorted().joinToString(","),
                    createdTimestamp = System.currentTimeMillis()
                )
                
                if (currentState.habitId != null) {
                    // Update existing
                    val existingHabit = habitDao.getHabitById(currentState.habitId)
                    if (existingHabit != null) {
                        // Merge fields if necessary, but here we overwrite mostly
                        val updatedHabit = habit.copy(
                            createdTimestamp = existingHabit.createdTimestamp, // Preserve creation time
                            isCompletedToday = existingHabit.isCompletedToday // Preserve status
                        )
                        habitDao.updateHabit(updatedHabit)
                        scheduleNotification(currentState, currentState.habitId)
                    }
                } else {
                    // Insert new
                    val habitId = habitDao.insertHabit(habit)
                    scheduleNotification(currentState, habitId)
                    // Award XP for creating a new habit
                    awardXpUseCase.award(LevelSystem.XpRewards.HABIT_CREATED, LevelSystem.XpReasons.HABIT_CREATED, habitId)
                }
                
                _effect.send(CreateHabitContract.Effect.NavigateBack)
            } catch (e: Exception) {
                _state.value = currentState.copy(isLoading = false, isError = true)
            }
        }
    }
    
    private fun scheduleNotification(state: CreateHabitContract.State, habitId: Long) {
        if (state.reminderEnabled) {
            if (state.isOneTime) {
                notificationScheduler.scheduleOneTimeReminder(
                    habitId = habitId,
                    habitName = state.habitName,
                    scheduledDate = state.scheduledDate,
                    time = state.reminderTime
                )
            } else {
                notificationScheduler.scheduleReminder(
                    habitId = habitId,
                    habitName = state.habitName,
                    time = state.reminderTime,
                    days = state.selectedDays
                )
            }
        } else {
            notificationScheduler.cancelReminder(habitId)
        }
    }
}
