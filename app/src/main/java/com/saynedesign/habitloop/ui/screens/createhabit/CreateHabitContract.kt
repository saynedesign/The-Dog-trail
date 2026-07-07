package com.saynedesign.habitloop.ui.screens.createhabit

import com.saynedesign.habitloop.data.PrimaryGoal

interface CreateHabitContract {
    data class State(
        val habitId: Long? = null, // If not null, we are in edit mode
        val userPrimaryGoal: PrimaryGoal? = null,

        val habitName: String = "",
        val description: String = "",
        val habitIcon: String = "📝", // Default emoji
        val habitType: HabitType = HabitType.YES_NO,
        val target: String = "1",
        val unitName: String = "Times",
        val isAtLeast: Boolean = true,

        val habitColor: Long = 0xFF5D3FD3, // Default BrandBlue
        val timeOfDay: TimeOfDay = TimeOfDay.ANYTIME,
        val endDateEnabled: Boolean = false,
        val endDate: Long? = null,

        val isLoading: Boolean = false,
        val isError: Boolean = false,

        // Reminder & Days
        val reminderEnabled: Boolean = false,
        val reminderTime: String = "08:00",
        val selectedDays: Set<Int> = setOf(1, 2, 3, 4, 5, 6, 7), // 1=Mon, 7=Sun

        // Icon Picker
        val showIconPicker: Boolean = false
    )

    enum class HabitType {
        NUMERIC, YES_NO, TIMER
    }

    enum class TimeOfDay {
        MORNING, AFTERNOON, EVENING, ANYTIME
    }

    sealed class Event {
        data class OnNameChange(val name: String) : Event()
        data class OnDescriptionChange(val description: String) : Event()
        data class OnTypeChange(val type: HabitType) : Event()
        data class OnTargetChange(val target: String) : Event()
        data class OnUnitChange(val unit: String) : Event()
        data class OnTargetRuleToggle(val isAtLeast: Boolean) : Event()

        data class OnColorChange(val color: Long) : Event()
        data class OnIconChange(val icon: String) : Event()
        data class OnTimeOfDayChange(val timeOfDay: TimeOfDay) : Event()
        data class OnEndDateToggle(val enabled: Boolean) : Event()
        data class OnEndDateChange(val date: Long) : Event()

        data class OnReminderToggle(val enabled: Boolean) : Event()
        data class OnReminderTimeChange(val time: String) : Event()
        data class OnDayToggle(val day: Int) : Event() // 1=Mon, 7=Sun
        /** Day chips: Every day / Weekdays presets */
        data class OnDaysPreset(val days: Set<Int>) : Event()
        data class OnToggleIconPicker(val show: Boolean) : Event()

        data object OnSaveClicked : Event()
        data object OnBackClicked : Event()
    }

    sealed class Effect {
        data object NavigateBack : Effect()
        data class ShowToast(val message: String) : Effect()
    }
}
