package com.saynedesign.habitloop.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saynedesign.habitloop.data.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReminderSetupViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ReminderSetupContract.State())
    val state: StateFlow<ReminderSetupContract.State> = _state.asStateFlow()

    private val _effect = Channel<ReminderSetupContract.Effect>()
    val effect = _effect.receiveAsFlow()

    fun handleEvent(event: ReminderSetupContract.Event) {
        when (event) {
            is ReminderSetupContract.Event.OnSelectStyle -> {
                _state.update { it.copy(selectedStyle = event.style) }
            }
            is ReminderSetupContract.Event.OnContinue -> {
                viewModelScope.launch {
                    preferencesRepository.updateReminderStyle(_state.value.selectedStyle)
                    _effect.send(ReminderSetupContract.Effect.NavigateHome)
                }
            }
        }
    }
}
