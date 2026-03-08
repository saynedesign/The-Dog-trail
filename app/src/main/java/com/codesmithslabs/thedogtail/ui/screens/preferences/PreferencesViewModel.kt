package com.codesmithslabs.thedogtail.ui.screens.preferences

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codesmithslabs.thedogtail.data.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PreferencesViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PreferencesContract.State())
    val state = _state.asStateFlow()

    private val _effect = Channel<PreferencesContract.Effect>()
    val effect = _effect.receiveAsFlow()

    init {
        // Collect flows from DataStore
        viewModelScope.launch {
            preferencesRepository.morningTime.collectLatest { time ->
                _state.update { it.copy(morningTime = time) }
            }
        }
        viewModelScope.launch {
            preferencesRepository.afternoonTime.collectLatest { time ->
                _state.update { it.copy(afternoonTime = time) }
            }
        }
        viewModelScope.launch {
            preferencesRepository.eveningTime.collectLatest { time ->
                _state.update { it.copy(eveningTime = time) }
            }
        }
        viewModelScope.launch {
            preferencesRepository.firstDayOfWeek.collectLatest { day ->
                _state.update { it.copy(firstDayOfWeek = day) }
            }
        }
        viewModelScope.launch {
            preferencesRepository.isVacationMode.collectLatest { mode ->
                _state.update { it.copy(isVacationMode = mode) }
            }
        }
        viewModelScope.launch {
            preferencesRepository.isDailyReminderEnabled.collectLatest { enabled ->
                _state.update { it.copy(isDailyReminderEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            preferencesRepository.reminderTime.collectLatest { time ->
                _state.update { it.copy(reminderTime = time) }
            }
        }
    }

    fun handleEvent(event: PreferencesContract.Event) {
        when (event) {
            PreferencesContract.Event.OnBackClicked -> {
                viewModelScope.launch { _effect.send(PreferencesContract.Effect.NavigateBack) }
            }
            is PreferencesContract.Event.OnTimeClick -> {
                _state.update { it.copy(showTimePicker = true, timePickerType = event.type) }
            }
            is PreferencesContract.Event.OnTimeSelected -> {
                _state.update { it.copy(showTimePicker = false, timePickerType = null) }
                
                viewModelScope.launch {
                    when (_state.value.timePickerType) {
                        PreferencesContract.TimePickerType.MORNING -> preferencesRepository.updateMorningTime(event.time)
                        PreferencesContract.TimePickerType.AFTERNOON -> preferencesRepository.updateAfternoonTime(event.time)
                        PreferencesContract.TimePickerType.EVENING -> preferencesRepository.updateEveningTime(event.time)
                        PreferencesContract.TimePickerType.REMINDER -> preferencesRepository.updateReminderTime(event.time)
                        null -> {} // Do nothing
                    }
                }
            }
            PreferencesContract.Event.OnTimePickerDismiss -> {
                _state.update { it.copy(showTimePicker = false, timePickerType = null) }
            }
            is PreferencesContract.Event.OnFirstDayOfWeekClick -> {
                viewModelScope.launch {
                    val current = _state.value.firstDayOfWeek
                    preferencesRepository.updateFirstDayOfWeek(if (current == "Monday") "Sunday" else "Monday")
                }
            }
            is PreferencesContract.Event.OnVacationModeToggle -> {
                viewModelScope.launch { preferencesRepository.updateVacationMode(event.enabled) }
            }
            is PreferencesContract.Event.OnDailyReminderToggle -> {
                viewModelScope.launch { preferencesRepository.updateDailyReminderEnabled(event.enabled) }
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
