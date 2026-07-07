package com.saynedesign.habitloop.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class PrimaryGoal {
    FITNESS,
    DISCIPLINE,
    PRODUCTIVITY,
    STUDY,
    MENTAL_HEALTH,
    CUSTOM
}

enum class ProductivityTime {
    MORNING,
    AFTERNOON,
    EVENING,
    NIGHT
}

enum class MotivationStyle {
    SEEING_PROGRESS,
    KEEPING_STREAKS,
    LEVELING_UP,
    ACHIEVEMENTS,
    QUOTES
}

enum class ExperienceLevel {
    BEGINNER,
    BUILDING,
    CONSISTENT,
    ADVANCED
}

enum class WeekStartsOn {
    MONDAY,
    SUNDAY
}

enum class UnitSystem {
    METRIC,
    IMPERIAL
}

@Entity(tableName = "user_info")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val dob: String, // Storing as String for simplicity "DD/MM/YYYY"
    val height: Float, // In cm
    val profileImageUri: String? = null,
    val createdTimestamp: Long = System.currentTimeMillis(),
    val journal: String? = null,
    val journalLastUpdated: Long? = null,
    val totalXp: Int = 0,
    val currentLevel: Int = 1,
    
    // Extended fields
    val photoUri: String? = null,
    val dateOfBirth: String? = null,
    val weight: Float? = null,
    val unitSystem: UnitSystem = UnitSystem.METRIC,
    val primaryGoal: PrimaryGoal = PrimaryGoal.FITNESS,
    val weeklyGoal: Int = 5,
    val preferredProductivityTime: ProductivityTime = ProductivityTime.MORNING,
    val motivationStyle: MotivationStyle = MotivationStyle.SEEING_PROGRESS,
    val experienceLevel: ExperienceLevel = ExperienceLevel.BEGINNER,
    val weekStartsOn: WeekStartsOn = WeekStartsOn.MONDAY,
    val defaultReminderWindow: String = "08:00-10:00",
    val timezone: String = "UTC"
)
