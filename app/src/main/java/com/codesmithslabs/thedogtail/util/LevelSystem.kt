package com.codesmithslabs.thedogtail.util

object LevelSystem {
    // Defines levels and their required total habit completions.
    // List of Pair(Level, RequiredHabits)
    // Level 1 starts at 0.
    val levels = listOf(
        1 to 0,
        2 to 50,
        3 to 150,
        4 to 300,
        5 to 500,
        6 to 800,
        7 to 1200,
        8 to 1700,
        9 to 2300,
        10 to 3000,
        11 to 3500,
        12 to 4000,
        13 to 4500,
        14 to 5000,
        15 to 5500,
        16 to 6000,
        17 to 6500,
        18 to 7000,
        19 to 7500,
        20 to 8000
    )

    fun getLevelForHabitCount(count: Int): Int {
        // Find the highest level where requiredHabits <= count
        return levels.lastOrNull { it.second <= count }?.first ?: 1
    }

    fun getProgressToNextLevel(count: Int): Float {
        val currentLevel = getLevelForHabitCount(count)
        val nextLevel = currentLevel + 1
        
        val currentRequirement = levels.find { it.first == currentLevel }?.second ?: 0
        val nextRequirement = levels.find { it.first == nextLevel }?.second ?: return 1.0f // Max level reached

        val needed = nextRequirement - currentRequirement
        val achieved = count - currentRequirement
        
        return (achieved.toFloat() / needed.toFloat()).coerceIn(0f, 1f)
    }

    fun getRequirementForLevel(level: Int): Int {
        return levels.find { it.first == level }?.second ?: Int.MAX_VALUE
    }
}
