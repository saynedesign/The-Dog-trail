package com.codesmithslabs.thedogtail.ui.screens.createhabit

interface CreateHabitContract {
    data class State(
        val habitId: Long? = null, // If not null, we are in edit mode
        val habitName: String = "",
        val description: String = "",
        val habitIcon: String? = null,
        val habitType: HabitType = HabitType.NUMERIC,
        val target: String = "10",
        val unitName: String = "Pages",
        val isAtLeast: Boolean = true, // At least vs At most
        val currentPreviewValue: Float = 4f, // For the slider preview
        val isLoading: Boolean = false,
        val isError: Boolean = false,
        // Advanced Options
        val isAdvancedOptionsExpanded: Boolean = false,
        val reminderEnabled: Boolean = false,
        val reminderTime: String = "08:00",
        val selectedDays: Set<Int> = setOf(1, 2, 3, 4, 5, 6, 7)
    )

    enum class HabitType {
        NUMERIC, YES_NO, TIMER
    }

    sealed class Event {
        data class OnNameChange(val name: String) : Event()
        data class OnDescriptionChange(val description: String) : Event()
        data class OnTypeChange(val type: HabitType) : Event()
        data class OnTargetChange(val target: String) : Event()
        data class OnUnitNameChange(val unitName: String) : Event()
        data class OnGoalTypeChange(val isAtLeast: Boolean) : Event() // At least / At most
        
        // Advanced Options
        data object OnToggleAdvancedOptions : Event()
        data class OnReminderToggle(val enabled: Boolean) : Event()
        data class OnReminderTimeChange(val time: String) : Event()
        data class OnDayToggle(val day: Int) : Event() // 1=Mon, 7=Sun

        data object OnSaveClicked : Event()
        data object OnBackClicked : Event()
    }

    sealed class Effect {
        data object NavigateBack : Effect()
        data class ShowToast(val message: String) : Effect()
    }
}
