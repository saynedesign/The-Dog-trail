package com.codesmithslabs.thedogtail.ui.screens.timer

interface TimerContract {
    data class State(
        val habitId: Long = 0,
        val habitTitle: String = "Habit",
        val timeLeftSeconds: Long = 25 * 60, // Default 25 min
        val totalTimeSeconds: Long = 25 * 60,
        val isRunning: Boolean = false,
        val isCompleted: Boolean = false,
        val isLoading: Boolean = false
    )

    sealed class Event {
        data class LoadHabit(val habitId: Long) : Event()
        data object OnStart : Event()
        data object OnPause : Event()
        data object OnReset : Event()
        data object OnFinish : Event() // User manually finishes or timer ends
        data object OnBackClicked : Event()
        data class OnDurationChange(val minutes: Int) : Event()
    }

    sealed class Effect {
        data object NavigateBack : Effect()
    }
}
