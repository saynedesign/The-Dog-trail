package com.codesmithslabs.thedogtail.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "xp_events")
data class XpEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val xpAmount: Int,
    val reason: String,
    val relatedHabitId: Long? = null,
    val timestamp: Long = System.currentTimeMillis()
)
