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
    val createdTimestamp: Long = System.currentTimeMillis()
)
