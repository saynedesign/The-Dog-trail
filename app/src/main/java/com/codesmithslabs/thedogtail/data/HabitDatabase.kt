package com.codesmithslabs.thedogtail.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [HabitEntity::class, UserEntity::class, HabitLogEntity::class, MoodEntity::class], version = 5, exportSchema = false)
abstract class HabitDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun userDao(): UserDao
    abstract fun habitLogDao(): HabitLogDao
    abstract fun moodDao(): MoodDao
}
