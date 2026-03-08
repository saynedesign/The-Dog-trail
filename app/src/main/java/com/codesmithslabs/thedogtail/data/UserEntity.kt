package com.codesmithslabs.thedogtail.data

import androidx.room.Entity
import androidx.room.PrimaryKey

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
    val currentLevel: Int = 1
)
