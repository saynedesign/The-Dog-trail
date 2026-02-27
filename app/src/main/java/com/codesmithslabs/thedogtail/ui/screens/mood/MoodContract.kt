package com.codesmithslabs.thedogtail.ui.screens.mood

import com.codesmithslabs.thedogtail.data.MoodEntity
import java.time.YearMonth

interface MoodContract {
    data class State(
        val selectedMonth: YearMonth = YearMonth.now(),
        val moods: Map<Int, MoodEntity> = emptyMap(), // Day of month -> Mood
        val isLoading: Boolean = false,
        val showAddMoodDialog: Boolean = false,
        val selectedDateForMood: Int? = null, // Day of month selected for adding mood
        val showHistory: Boolean = false, // Toggle between Calendar and History view
        val moodHistory: List<MoodEntity> = emptyList(), // Full history list

        // Dialog State
        val currentStep: Int = 1, // 1: Mood Selection, 2: Feeling Selection
        val selectedMoodType: String? = null,
        val selectedMoodEmoji: String? = null,
        val selectedFeeling: String? = null
    )

    sealed class Event {
        data class OnMonthChanged(val month: YearMonth) : Event()
        data class OnDayClicked(val day: Int) : Event()
        data object OnAddTodayMoodClicked : Event()
        
        // Dialog Events
        data class OnMoodOptionSelected(val moodType: String, val emoji: String) : Event()
        data object OnSubmitMood : Event()
        data class OnFeelingOptionSelected(val feeling: String) : Event()
        data object OnSubmitFeeling : Event()
        data object OnBackStep : Event()
        
        data object OnDismissDialog : Event()
        data object OnBackClicked : Event()
        
        data object OnHistoryClicked : Event()
        data object OnCloseHistory : Event()
    }

    sealed class Effect {
        data class ShowToast(val message: String) : Effect()
        data object NavigateBack : Effect()
    }
}
