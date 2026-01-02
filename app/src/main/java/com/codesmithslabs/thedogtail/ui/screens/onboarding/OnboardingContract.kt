package com.codesmithslabs.thedogtail.ui.screens.onboarding

// MVI Contract for Onboarding Screen
class OnboardingContract {

    // UI State
    data class State(
        val isLoading: Boolean = false
    )

    // User Intents/Events
    sealed class Event {
        data object OnStartClicked : Event()
        data object OnLoginClicked : Event()
    }

    // Side Effects (One-time events like Navigation)
    sealed class Effect {
        data object NavigateToUserInfo : Effect()
        data object NavigateToHome : Effect()
        data object NavigateToLogin : Effect()
    }
}
