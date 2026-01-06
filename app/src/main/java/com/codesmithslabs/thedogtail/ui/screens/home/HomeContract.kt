package com.codesmithslabs.thedogtail.ui.screens.home

import com.codesmithslabs.thedogtail.data.HabitEntity
import com.codesmithslabs.thedogtail.data.HabitLogEntity
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
        val habitLogs: Map<Long, HabitLogEntity> = emptyMap(), // Map of HabitID to Log for selected date
        val isLoading: Boolean = false
    )

    sealed class Event {
        data object OnAddHabitClicked : Event()
        data class OnHabitClicked(val habitId: Long) : Event()
        data class OnToggleHabit(val habitId: Long, val isDone: Boolean) : Event()
        data class OnUpdateHabitValue(val habitId: Long, val newValue: Float) : Event()
        data class OnDateSelected(val date: String) : Event()
        data object OnProfileClicked : Event()
        data class OnTimerClicked(val habitId: Long) : Event()
        data class OnEditHabitClicked(val habitId: Long) : Event()
        data class OnDeleteHabitClicked(val habitId: Long) : Event()
    }

    sealed class Effect {
        data object NavigateToAddHabit : Effect()
        data class NavigateToHabitDetails(val habitId: Long) : Effect()
        data class NavigateToEditHabit(val habitId: Long) : Effect()
        data object NavigateToProfile : Effect()
        data class NavigateToTimer(val habitId: Long) : Effect()
    }
}
