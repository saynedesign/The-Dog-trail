package com.codesmithslabs.thedogtail.ui.screens.preferences

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PreferencesViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(PreferencesContract.State())
    val state = _state.asStateFlow()

    private val _effect = Channel<PreferencesContract.Effect>()
    val effect = _effect.receiveAsFlow()

    fun handleEvent(event: PreferencesContract.Event) {
        when (event) {
            PreferencesContract.Event.OnBackClicked -> {
                viewModelScope.launch { _effect.send(PreferencesContract.Effect.NavigateBack) }
            }
            is PreferencesContract.Event.OnTimeClick -> {
                _state.update { it.copy(showTimePicker = true, timePickerType = event.type) }
            }
            is PreferencesContract.Event.OnTimeSelected -> {
                _state.update { currentState ->
                    val newState = when (currentState.timePickerType) {
                        PreferencesContract.TimePickerType.MORNING -> currentState.copy(morningTime = event.time)
                        PreferencesContract.TimePickerType.AFTERNOON -> currentState.copy(afternoonTime = event.time)
                        PreferencesContract.TimePickerType.EVENING -> currentState.copy(eveningTime = event.time)
                        PreferencesContract.TimePickerType.REMINDER -> currentState.copy(reminderTime = event.time)
                        null -> currentState
                    }
                    newState.copy(showTimePicker = false, timePickerType = null)
                }
            }
            PreferencesContract.Event.OnTimePickerDismiss -> {
                _state.update { it.copy(showTimePicker = false, timePickerType = null) }
            }
            is PreferencesContract.Event.OnFirstDayOfWeekClick -> {
                 _state.update { 
                     it.copy(firstDayOfWeek = if (it.firstDayOfWeek == "Monday") "Sunday" else "Monday") 
                 }
            }
            is PreferencesContract.Event.OnVacationModeToggle -> {
                _state.update { it.copy(isVacationMode = event.enabled) }
            }
            is PreferencesContract.Event.OnDailyReminderToggle -> {
                _state.update { it.copy(isDailyReminderEnabled = event.enabled) }
            }
            PreferencesContract.Event.OnClearCacheClicked -> {
                viewModelScope.launch { _effect.send(PreferencesContract.Effect.ShowToast("Cache cleared!")) }
            }
            PreferencesContract.Event.OnRestartHabitsClicked -> {
                viewModelScope.launch { _effect.send(PreferencesContract.Effect.ShowToast("All habits restarted!")) }
            }
        }
    }
}
