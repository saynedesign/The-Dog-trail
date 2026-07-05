package com.saynedesign.habitloop.ui.screens.preferences

class PreferencesContract {
    data class State(
        val morningTime: String = "05:00",
        val afternoonTime: String = "12:00",
        val eveningTime: String = "18:00",
        val firstDayOfWeek: String = "Monday",
        val isVacationMode: Boolean = false,
        val isDailyReminderEnabled: Boolean = true,
        val isOverlayReminderEnabled: Boolean = false,
        val overlayReminderSound: String = "alarm",
        val reminderTime: String = "07:00",
        
        // Dialog states
        val showTimePicker: Boolean = false,
        val timePickerType: TimePickerType? = null
    )

    enum class TimePickerType {
        MORNING, AFTERNOON, EVENING, REMINDER
    }

    sealed class Event {
        data object OnBackClicked : Event()
        
        // Time selection events
        data class OnTimeClick(val type: TimePickerType) : Event()
        data class OnTimeSelected(val time: String) : Event()
        data object OnTimePickerDismiss : Event()
        
        // Toggles and other clicks
        data object OnFirstDayOfWeekClick : Event()
        data class OnVacationModeToggle(val enabled: Boolean) : Event()
        data class OnDailyReminderToggle(val enabled: Boolean) : Event()
        data class OnOverlayReminderToggle(val enabled: Boolean) : Event()
        data class OnOverlayReminderSoundChange(val sound: String) : Event()
        
        data object OnClearCacheClicked : Event()
        data object OnRestartHabitsClicked : Event()
        data object OnSeedDataClicked : Event()
    }

    sealed class Effect {
        data object NavigateBack : Effect()
        data class ShowToast(val message: String) : Effect()
    }
}
