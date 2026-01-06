package com.codesmithslabs.thedogtail.ui.screens.habitdetail

import com.codesmithslabs.thedogtail.data.HabitEntity

interface HabitDetailContract {
    data class State(
        val habit: HabitEntity? = null,
        val logs: List<com.codesmithslabs.thedogtail.data.HabitLogEntity> = emptyList(),
        val currentStreak: Int = 0,
        val completionRate: Int = 0,
        val totalCompletions: Int = 0,
        val totalValue: Float = 0f,
        val isLoading: Boolean = false,
        val isError: Boolean = false
    )

    sealed class Event {
        data object OnBackClicked : Event()
        data object OnEditClicked : Event()
        data object OnDeleteClicked : Event()
    }

    sealed class Effect {
        data object NavigateBack : Effect()
        data class NavigateToEdit(val habitId: Long) : Effect()
    }
}
