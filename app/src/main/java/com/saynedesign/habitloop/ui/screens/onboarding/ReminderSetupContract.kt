package com.saynedesign.habitloop.ui.screens.onboarding

class ReminderSetupContract {

    data class State(
        val selectedStyle: String = "overlay" // "overlay" | "notification"
    )

    sealed class Event {
        data class OnSelectStyle(val style: String) : Event()
        data object OnContinue : Event()
    }

    sealed class Effect {
        data object NavigateHome : Effect()
    }
}
