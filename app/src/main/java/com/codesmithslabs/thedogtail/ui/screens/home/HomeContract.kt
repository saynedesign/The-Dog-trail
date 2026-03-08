package com.codesmithslabs.thedogtail.ui.screens.home

import com.codesmithslabs.thedogtail.data.HabitEntity
import com.codesmithslabs.thedogtail.data.HabitLogEntity
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

interface HomeContract {
    enum class HomeTab {
        HABITS, MOOD, REPORT, PROFILE
    }

    data class State(
        val currentTab: HomeTab = HomeTab.HABITS
    )

    sealed class Event {
        data object OnProfileClicked : Event()
        data object OnMoodClicked : Event()
        data object OnReportClicked : Event()
        data object OnHomeClicked : Event()
        data object OnEditProfileRequested : Event()
        data object OnPreferencesRequested : Event()
        data object OnAchievementsRequested : Event()
    }

    sealed class Effect {
        data object NavigateToProfile : Effect()
        data object NavigateToEditProfile : Effect()
        data object NavigateToPreferences : Effect()
        data object NavigateToAchievements : Effect()
        data object NavigateToMoodStats : Effect()
    }
}
