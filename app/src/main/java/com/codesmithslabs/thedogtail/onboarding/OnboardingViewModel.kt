package com.codesmithslabs.thedogtail.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class OnboardingViewModel : ViewModel() {

    private val _state = MutableStateFlow(OnboardingContract.State())
    val state: StateFlow<OnboardingContract.State> = _state.asStateFlow()

    private val _effect = Channel<OnboardingContract.Effect>()
    val effect = _effect.receiveAsFlow()

    fun handleEvent(event: OnboardingContract.Event) {
        when (event) {
            is OnboardingContract.Event.OnStartClicked -> {
                sendEffect(OnboardingContract.Effect.NavigateToHome)
            }
            is OnboardingContract.Event.OnLoginClicked -> {
                sendEffect(OnboardingContract.Effect.NavigateToLogin)
            }
        }
    }

    private fun sendEffect(effect: OnboardingContract.Effect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}
