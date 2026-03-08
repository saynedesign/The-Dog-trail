package com.codesmithslabs.thedogtail.ui.screens.achievements

import com.codesmithslabs.thedogtail.util.LevelSystem

interface AchievementsContract {
    data class State(
        val totalXp: Int = 0,
        val currentLevel: Int = 1,
        val levelName: String = "",
        val levelEmoji: String = "",
        val nextLevel: Int = 2,
        val progressToNextLevel: Float = 0f,
        val levels: List<Pair<Int, Int>> = LevelSystem.levels, // -> List of Pair(level, requiredXp)
        val isLoading: Boolean = false
    )

    sealed class Event {
        data object OnBackClicked : Event()
    }

    sealed class Effect {
        data object NavigateBack : Effect()
    }
}
