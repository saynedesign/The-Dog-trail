package com.codesmithslabs.thedogtail.ui.screens.habitdetail

import com.codesmithslabs.thedogtail.data.HabitEntity

interface HabitDetailContract {
    data class State(
        val habit: HabitEntity? = null,
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
        data object NavigateToEdit : Effect()
    }
}
