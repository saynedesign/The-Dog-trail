package com.saynedesign.habitloop.ui.screens.home.habits

import com.saynedesign.habitloop.data.HabitEntity
import com.saynedesign.habitloop.data.HabitLogEntity
import java.time.LocalDate

import com.saynedesign.habitloop.data.MotivationStyle

interface HabitsContract {

    data class State(
        val selectedDate: LocalDate = LocalDate.now(),
        val selectedEpochDay: Long = LocalDate.now().toEpochDay(),
        val habits: List<HabitEntity> = emptyList(),
        val habitLogs: Map<Long, HabitLogEntity> = emptyMap(),
        val habitStreaks: Map<Long, Int> = emptyMap(),
        val isLoading: Boolean = false,
        
        // Dialogs
        val showDeleteDialog: Boolean = false,
        val showEditDialog: Boolean = false,
        val selectedHabitId: Long? = null,
        
        // Rest Day
        val restingHabitIds: Set<Long> = emptySet(),
        val showRestDaySheet: Boolean = false,
        val restDayTargetHabitId: Long? = null,
        val restDaysUsedThisWeek: Int = 0,
        
        // XP Context
        val totalXp: Int = 0,
        val currentLevel: Int = 1,
        val xpPopAmount: Int? = null,
        val levelUpToLevel: Int? = null,
        val userName: String = "",
        val profileImageUri: String? = null,
        
        // Motivation custom
        val motivationStyle: MotivationStyle = MotivationStyle.SEEING_PROGRESS,
        val currentStreak: Int = 0
    )

    sealed class Event {
        data object OnAddHabitClicked : Event()
        data class OnHabitClicked(val habitId: Long) : Event()
        data class OnToggleHabit(val habitId: Long, val isDone: Boolean) : Event()
        data class OnUpdateHabitValue(val habitId: Long, val newValue: Float) : Event()
        data class OnDateSelected(val date: LocalDate) : Event()
        data class OnTimerClicked(val habitId: Long) : Event()
        
        // Dialog Events
        data class OnEditHabitClicked(val habitId: Long) : Event()
        data class OnDeleteHabitClicked(val habitId: Long) : Event()
        data object OnDismissDialog : Event()
        data object OnConfirmDelete : Event()
        data object OnConfirmEdit : Event()
        
        // Rest Day
        data class OnRestDayRequested(val habitId: Long) : Event()
        data object OnConfirmRestDay : Event()
        data object OnDismissRestDaySheet : Event()
        
        // XP
        data object OnXpPopDismissed : Event()
        data object OnLevelUpDismissed : Event()
    }

    sealed class Effect {
        data object NavigateToAddHabit : Effect()
        data class NavigateToHabitDetails(val habitId: Long) : Effect()
        data class NavigateToEditHabit(val habitId: Long) : Effect()
        data class NavigateToTimer(val habitId: Long) : Effect()
    }
}
