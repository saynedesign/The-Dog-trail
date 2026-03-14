package com.codesmithslabs.thedogtail.ui.screens.home

interface HomeContract {
    enum class HomeTab {
        HABITS, REPORT, PROFILE
    }

    data class State(
        val currentTab: HomeTab = HomeTab.HABITS
    )

    sealed class Event {
        data object OnProfileClicked : Event()
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
    }
}
