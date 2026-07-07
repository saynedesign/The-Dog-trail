package com.saynedesign.habitloop.ui.screens.userinfo

import com.saynedesign.habitloop.data.PrimaryGoal
import com.saynedesign.habitloop.data.ProductivityTime
import com.saynedesign.habitloop.data.MotivationStyle
import com.saynedesign.habitloop.data.ExperienceLevel
import com.saynedesign.habitloop.data.WeekStartsOn

interface UserInfoContract {
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
        
        // Extended onboarding fields
        val primaryGoal: PrimaryGoal = PrimaryGoal.FITNESS,
        val preferredProductivityTime: ProductivityTime = ProductivityTime.MORNING,
        val weeklyGoal: Int = 5,
        val motivationStyle: MotivationStyle = MotivationStyle.SEEING_PROGRESS,
        val experienceLevel: ExperienceLevel = ExperienceLevel.BEGINNER,
        val weekStartsOn: WeekStartsOn = WeekStartsOn.MONDAY,
        val defaultReminderWindow: String = "08:00-10:00",
        val timezone: String = java.util.TimeZone.getDefault().id
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
        
        // Extended onboarding events
        data class OnPrimaryGoalChange(val goal: PrimaryGoal) : Event()
        data class OnProductivityTimeChange(val time: ProductivityTime) : Event()
        data class OnWeeklyGoalChange(val goal: Int) : Event()
        data class OnMotivationStyleChange(val style: MotivationStyle) : Event()
        data class OnExperienceLevelChange(val level: ExperienceLevel) : Event()
        data class OnWeekStartsOnChange(val startsOn: WeekStartsOn) : Event()
        data class OnReminderWindowChange(val window: String) : Event()
        data class OnTimezoneChange(val timezone: String) : Event()
        
        data object OnSubmit : Event()
    }

    sealed class Effect {
        data object NavigateToHome : Effect()
    }
}
