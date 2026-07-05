package com.saynedesign.habitloop.util

import com.saynedesign.habitloop.R

object LevelSystem {
    // XP-based level system with dog-themed names
    data class LevelInfo(
        val level: Int,
        val name: String,
        val emoji: String,
        val requiredXp: Int
    )

    val levelInfos = listOf(
        LevelInfo(1, "Initiate", "🗡️", 0),
        LevelInfo(2, "Guardian", "🛡️", 100),
        LevelInfo(3, "Vanguard", "⚔️", 500),
        LevelInfo(4, "Gladiator", "🏅", 2000),
        LevelInfo(5, "Centurion", "🎖️", 5000),
        LevelInfo(6, "Shogun", "⛩️", 10000),
        LevelInfo(7, "Warlord", "👑", 50000),
        LevelInfo(8, "Grandmaster", "🏆", 100000),
        LevelInfo(9, "Overlord", "🔱", 500000),
        LevelInfo(10, "Undefeated Legend", "🌟", 1000000)
    )

    fun getLevelDrawableRes(level: Int): Int {
        return when (level) {
            1 -> R.drawable.warrior_level_1
            2 -> R.drawable.warrior_level_2
            3 -> R.drawable.warrior_level_3
            4 -> R.drawable.warrior_level_4
            5 -> R.drawable.warrior_level_5
            6 -> R.drawable.warrior_level_6
            7 -> R.drawable.warrior_level_7
            8 -> R.drawable.warrior_level_8
            9 -> R.drawable.warrior_level_9
            10 -> R.drawable.warrior_level_10
            else -> R.drawable.warrior_level_1
        }
    }

    // Backward-compatible: maps level->requiredXp (replaces habit count)
    val levels = levelInfos.map { it.level to it.requiredXp }

    fun getLevelForXp(xp: Int): Int {
        return levelInfos.lastOrNull { it.requiredXp <= xp }?.level ?: 1
    }

    /** Backward-compat: old callers still pass habit count, now they should pass XP */
    fun getLevelForHabitCount(count: Int): Int = getLevelForXp(count)

    fun getLevelInfo(level: Int): LevelInfo {
        return levelInfos.find { it.level == level } ?: levelInfos.first()
    }

    fun getLevelInfoForXp(xp: Int): LevelInfo {
        return getLevelInfo(getLevelForXp(xp))
    }

    fun getProgressToNextLevel(xp: Int): Float {
        val currentLevel = getLevelForXp(xp)
        val nextLevel = currentLevel + 1

        val currentRequirement = levelInfos.find { it.level == currentLevel }?.requiredXp ?: 0
        val nextRequirement = levelInfos.find { it.level == nextLevel }?.requiredXp ?: return 1.0f

        val needed = nextRequirement - currentRequirement
        val achieved = xp - currentRequirement

        return (achieved.toFloat() / needed.toFloat()).coerceIn(0f, 1f)
    }

    fun getRequirementForLevel(level: Int): Int {
        return levelInfos.find { it.level == level }?.requiredXp ?: Int.MAX_VALUE
    }

    fun getNextLevelRequirement(xp: Int): Int {
        val currentLevel = getLevelForXp(xp)
        return levelInfos.find { it.level == currentLevel + 1 }?.requiredXp ?: Int.MAX_VALUE
    }

    // XP award constants
    object XpRewards {
        const val HABIT_COMPLETE = 10
        const val PERFECT_DAY = 50
        const val FIRST_OF_DAY = 5
        const val STREAK_3 = 25
        const val STREAK_7 = 75
        const val REST_DAY = 5
        const val MOOD_LOG = 5
        const val HABIT_CREATED = 15
        const val MONTHLY_CONSISTENCY = 200
    }

    // XP reason strings (for the XpEventEntity.reason column)
    object XpReasons {
        const val HABIT_COMPLETE = "HABIT_COMPLETE"
        const val PERFECT_DAY = "PERFECT_DAY"
        const val FIRST_OF_DAY = "FIRST_OF_DAY"
        const val STREAK_3 = "STREAK_3"
        const val STREAK_7 = "STREAK_7"
        const val REST_DAY = "REST_DAY"
        const val MOOD_LOG = "MOOD_LOG"
        const val HABIT_CREATED = "HABIT_CREATED"
        const val MONTHLY_CONSISTENCY = "MONTHLY_CONSISTENCY"
    }
}

