package com.codesmithslabs.thedogtail.ui.screens.home

import com.codesmithslabs.thedogtail.data.HabitEntity
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

interface HomeContract {
    data class State(
        val userName: String = "",
        val userImageUri: String? = null,
        val selectedDate: String = LocalDate.now().format(DateTimeFormatter.ofPattern("EEE d", Locale.getDefault())),
        val selectedEpochDay: Long = LocalDate.now().toEpochDay(),
        val habits: List<HabitEntity> = emptyList(),
        val completedForSelectedDate: Set<Long> = emptySet(),
        val isLoading: Boolean = false
    )

    sealed class Event {
        data object OnAddHabitClicked : Event()
        data class OnHabitClicked(val habitId: Long) : Event()
        data class OnToggleHabit(val habitId: Long, val isDone: Boolean) : Event()
        data class OnDateSelected(val date: String) : Event()
        data object OnProfileClicked : Event()
    }

    sealed class Effect {
        data object NavigateToAddHabit : Effect()
        data class NavigateToHabitDetails(val habitId: Long) : Effect()
        data object NavigateToProfile : Effect()
    }
}
