package com.codesmithslabs.thedogtail.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val targetValue: Float = 1f,
    val unit: String = "times",
    // Type can be "NUMERIC", "YES_NO", "TIMER"
    val type: String = "YES_NO", 
    val isAtLeast: Boolean = true, // true = At Least, false = At Most
    val color: Long = 0xFF5D3FD3, // Default BrandBlue
    val icon: String = "",
    val createdTimestamp: Long = System.currentTimeMillis(),
    // New fields
    val reminderEnabled: Boolean = false,
    val reminderTime: String = "08:00",
    val selectedDays: String = "1,2,3,4,5,6,7", // Mon=1, Sun=7
    val isCompletedToday: Boolean = false, // Quick way to track daily status for MVP
    
    // New Fields for Design Update
    val isOneTime: Boolean = false,
    val scheduledDate: Long? = null, // For One-Time Task
    val endDate: Long? = null, // For Regular Habit
    val timeOfDay: String = "Anytime", // Morning, Afternoon, Evening
    val frequency: String = "Daily" // Daily, Weekly, Monthly
)
