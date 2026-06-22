package com.saynedesign.habitloop.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habit_rest_days")
data class HabitRestDayEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitId: Long,
    val dateEpochDay: Long,
    val declaredTimestamp: Long = System.currentTimeMillis()
)
