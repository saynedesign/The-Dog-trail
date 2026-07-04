package com.saynedesign.habitloop.ui.screens.achievements

import com.saynedesign.habitloop.util.LevelSystem

interface AchievementsContract {
    data class State(
        val totalXp: Int = 0,
        val currentLevel: Int = 1,
        val levelName: String = "",
        val levelEmoji: String = "",
        val nextLevel: Int = 2,
        val nextLevelName: String = "",
        val nextLevelEmoji: String = "",
        val nextLevelXp: Int = 100,
        val progressToNextLevel: Float = 0f,
        val levels: List<LevelSystem.LevelInfo> = LevelSystem.levelInfos,
        val currentStreak: Int = 0,
        val totalCheckIns: Int = 0,
        val badgesCount: Int = 0,
        val profileImageUri: String? = null,
        val isLoading: Boolean = false
    )

    sealed class Event {
        data object OnBackClicked : Event()
    }

    sealed class Effect {
        data object NavigateBack : Effect()
    }
}
