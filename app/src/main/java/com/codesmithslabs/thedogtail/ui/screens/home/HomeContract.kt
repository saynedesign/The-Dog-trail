package com.codesmithslabs.thedogtail.ui.screens.home

import com.codesmithslabs.thedogtail.data.HabitEntity

interface HomeContract {
    data class State(
        val userName: String = "",
        val userImageUri: String? = null,
        val selectedDate: String = "Sat 3", // Placeholder
        val habits: List<HabitEntity> = emptyList(),
        val isLoading: Boolean = false
    )

    sealed class Event {
        data object OnAddHabitClicked : Event()
        data class OnHabitClicked(val habitId: Long) : Event()
        data class OnDateSelected(val date: String) : Event()
    }

    sealed class Effect {
        data object NavigateToAddHabit : Effect()
        data class NavigateToHabitDetails(val habitId: Long) : Effect()
    }
}
