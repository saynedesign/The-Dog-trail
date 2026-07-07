package com.saynedesign.habitloop.ui.screens.profile

interface ProfileContract {
    data class State(
        val userName: String = "",
        val userDob: String = "",
        val userHeight: Float = 0f,
        val profileImageUri: String? = null,
        val isLoading: Boolean = true,
        val level: Int = 1,
        val totalHabitCount: Int = 0,
        // XP
        val totalXp: Int = 0,
        val xpProgress: Float = 0f,
        val levelName: String = "Initiate",
        val levelEmoji: String = "🗡️",
        val nextLevelXp: Int = 200,
        val currentStreak: Int = 0,
        val bestStreak: Int = 0,
        val totalCheckIns: Int = 0,
        val badgesCount: Int = 0
    )

    sealed class Event {
        data object OnBackClicked : Event()
        data object OnEditProfileClicked : Event() // Kept for compatibility if needed, map to Personal Info
        data object OnBackupClicked : Event()      // Kept for compatibility if needed
        data object OnLogoutClicked : Event()
        
        // New Events based on design
        data object OnPreferencesClicked : Event()
        data object OnPersonalInfoClicked : Event()
        data object OnAccountSecurityClicked : Event()
        data object OnLinkedAccountsClicked : Event()
        data object OnAppAppearanceClicked : Event()
        data object OnDataAnalyticsClicked : Event()
        data object OnHelpSupportClicked : Event()
        data object OnLevelBannerClicked : Event()
        data object OnTrackNewHabitClicked : Event()
        data object OnViewStatsClicked : Event()
        data object OnAboutClicked : Event()
    }

    sealed class Effect {
        data object NavigateBack : Effect()
        data object NavigateToEditProfile : Effect()
        data object NavigateToPreferences : Effect()
        data object NavigateToAppearance : Effect()
        data object NavigateToAchievements : Effect()
        data object NavigateToCreateHabit : Effect()
        data object NavigateToStats : Effect()
        data object NavigateToAbout : Effect()
        data class ShowToast(val message: String) : Effect()
    }
}
