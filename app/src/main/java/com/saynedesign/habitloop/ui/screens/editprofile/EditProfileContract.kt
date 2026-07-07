package com.saynedesign.habitloop.ui.screens.editprofile

import com.saynedesign.habitloop.data.PrimaryGoal
import com.saynedesign.habitloop.data.ProductivityTime
import com.saynedesign.habitloop.data.MotivationStyle
import com.saynedesign.habitloop.data.ExperienceLevel
import com.saynedesign.habitloop.data.WeekStartsOn

interface EditProfileContract {
    enum class Section {
        PERSONAL,
        HEALTH,
        PREFERENCES,
        MOTIVATION
    }

    data class State(
        val name: String = "",
        val dob: String = "",
        val height: String = "",
        val weight: String = "",
        val profileImageUri: String? = null,
        val isMetric: Boolean = true, // true for CM/KG, false for FT/LBS
        val isDatePickerVisible: Boolean = false,
        val isLoading: Boolean = false,
        val isError: Boolean = false,
        val userId: Long = 0,
        
        // New Profile Fields
        val primaryGoal: PrimaryGoal = PrimaryGoal.FITNESS,
        val weeklyGoal: Int = 5,
        val preferredProductivityTime: ProductivityTime = ProductivityTime.MORNING,
        val motivationStyle: MotivationStyle = MotivationStyle.SEEING_PROGRESS,
        val experienceLevel: ExperienceLevel = ExperienceLevel.BEGINNER,
        val weekStartsOn: WeekStartsOn = WeekStartsOn.MONDAY,
        val defaultReminderWindow: String = "08:00-10:00",
        val timezone: String = "UTC",

        // Collapsible states
        val personalExpanded: Boolean = true,
        val healthExpanded: Boolean = false,
        val preferencesExpanded: Boolean = false,
        val motivationExpanded: Boolean = false
    )

    sealed class Event {
        data class OnNameChange(val name: String) : Event()
        data class OnDobChange(val dob: String) : Event()
        data class OnHeightChange(val height: String) : Event()
        data class OnWeightChange(val weight: String) : Event()
        data class OnImageSelected(val uri: String?) : Event()
        data object OnToggleHeightUnit : Event()
        data object OnToggleDatePicker : Event()
        data class OnDateSelected(val dateMillis: Long?) : Event()
        
        // Extended fields events
        data class OnPrimaryGoalChange(val goal: PrimaryGoal) : Event()
        data class OnWeeklyGoalChange(val goal: Int) : Event()
        data class OnProductivityTimeChange(val time: ProductivityTime) : Event()
        data class OnMotivationStyleChange(val style: MotivationStyle) : Event()
        data class OnExperienceLevelChange(val level: ExperienceLevel) : Event()
        data class OnWeekStartsOnChange(val startsOn: WeekStartsOn) : Event()
        data class OnReminderWindowChange(val window: String) : Event()
        data class OnTimezoneChange(val timezone: String) : Event()
        
        // Section toggle
        data class OnToggleSection(val section: Section) : Event()
        
        data object OnSave : Event()
        data object OnBackClicked : Event()
    }

    sealed class Effect {
        data object NavigateBack : Effect()
        data class ShowToast(val message: String) : Effect()
    }
}
