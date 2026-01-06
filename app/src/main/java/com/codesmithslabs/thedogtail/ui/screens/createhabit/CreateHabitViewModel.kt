package com.codesmithslabs.thedogtail.ui.screens.createhabit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codesmithslabs.thedogtail.data.HabitDao
import com.codesmithslabs.thedogtail.data.HabitEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.codesmithslabs.thedogtail.util.NotificationScheduler

@HiltViewModel
class CreateHabitViewModel @Inject constructor(
    private val habitDao: HabitDao,
    private val notificationScheduler: NotificationScheduler,
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
                        habitIcon = habit.icon,
                        habitType = try {
                            CreateHabitContract.HabitType.valueOf(habit.type)
                        } catch (e: Exception) {
                            CreateHabitContract.HabitType.NUMERIC
                        },
                        target = if (habit.type == "TIMER") habit.targetValue.toLong().toString() else habit.targetValue.toInt().toString(), // Handle float/int conversion
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
            is CreateHabitContract.Event.OnTargetChange -> {
                _state.value = _state.value.copy(target = event.target)
            }
            is CreateHabitContract.Event.OnUnitNameChange -> {
                _state.value = _state.value.copy(unitName = event.unitName)
            }
            is CreateHabitContract.Event.OnGoalTypeChange -> {
                _state.value = _state.value.copy(isAtLeast = event.isAtLeast)
            }
            is CreateHabitContract.Event.OnToggleAdvancedOptions -> {
                _state.value = _state.value.copy(isAdvancedOptionsExpanded = !_state.value.isAdvancedOptionsExpanded)
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
            // TODO: Show error
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
                    targetValue = currentState.target.toFloatOrNull() ?: 0f,
                    unit = currentState.unitName,
                    isAtLeast = currentState.isAtLeast,
                    // Default values for now
                    icon = currentState.habitIcon ?: "",
                    color = 0xFF4B68FF, // BrandBlue
                    reminderEnabled = currentState.reminderEnabled,
                    reminderTime = currentState.reminderTime,
                    selectedDays = currentState.selectedDays.sorted().joinToString(","),
                    createdTimestamp = System.currentTimeMillis() // Only used for new habits
                )
                
                if (currentState.habitId != null) {
                    val existingHabit = habitDao.getHabitById(currentState.habitId)
                    if (existingHabit != null) {
                        val updatedHabit = existingHabit.copy(
                            title = currentState.habitName,
                            description = currentState.description,
                            type = currentState.habitType.name,
                            targetValue = currentState.target.toFloatOrNull() ?: 0f,
                            unit = currentState.unitName,
                            isAtLeast = currentState.isAtLeast,
                            icon = currentState.habitIcon ?: existingHabit.icon,
                            reminderEnabled = currentState.reminderEnabled,
                            reminderTime = currentState.reminderTime,
                            selectedDays = currentState.selectedDays.sorted().joinToString(",")
                        )
                        habitDao.updateHabit(updatedHabit)
                        
                        // Re-schedule reminder if updated
                        if (currentState.reminderEnabled) {
                            notificationScheduler.scheduleReminder(
                                habitId = currentState.habitId,
                                habitName = currentState.habitName,
                                time = currentState.reminderTime,
                                days = currentState.selectedDays
                            )
                        }
                    }
                } else {
                    val habitId = habitDao.insertHabit(habit)
                    
                    if (currentState.reminderEnabled) {
                        notificationScheduler.scheduleReminder(
                            habitId = habitId,
                            habitName = currentState.habitName,
                            time = currentState.reminderTime,
                            days = currentState.selectedDays
                        )
                    }
                }
                
                _effect.send(CreateHabitContract.Effect.NavigateBack)
            } catch (e: Exception) {
                _state.value = currentState.copy(isLoading = false, isError = true)
                // _effect.send(CreateHabitContract.Effect.ShowToast("Error saving habit"))
            }
        }
    }
}
