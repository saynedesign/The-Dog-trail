package com.codesmithslabs.thedogtail.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "moods")
data class MoodEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val moodType: String, // "Good", "Great", "Okay", "Bad", "Not Good"
    val moodEmoji: String, // "😊", "😎", "😐", "😡", "😢"
    val timestamp: Long, // Date of the mood entry (Start of day in millis)
    val note: String = "",
    val feeling: String = "" // Additional descriptor like "Happy", "Brave", etc.
)
