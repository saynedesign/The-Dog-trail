package com.saynedesign.habitloop.ui.screens.onboarding

// MVI Contract for Onboarding Screen
class OnboardingContract {

    // UI State
    data class State(
        val isLoading: Boolean = false
    )

    // User Intents/Events
    sealed class Event {
        data object OnStartClicked : Event()
    }

    // Side Effects (One-time events like Navigation)
    sealed class Effect {
        data object NavigateToUserInfo : Effect()
        data object NavigateToHome : Effect()
    }
}
