package com.codesmithslabs.thedogtail.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(OnboardingContract.State())
    val state: StateFlow<OnboardingContract.State> = _state.asStateFlow()

    private val _effect = Channel<OnboardingContract.Effect>()
    val effect = _effect.receiveAsFlow()

    fun handleEvent(event: OnboardingContract.Event) {
        when (event) {
            is OnboardingContract.Event.OnStartClicked -> {
                sendEffect(OnboardingContract.Effect.NavigateToUserInfo)
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
