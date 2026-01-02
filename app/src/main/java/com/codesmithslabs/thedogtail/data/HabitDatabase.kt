package com.codesmithslabs.thedogtail.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [HabitEntity::class, UserEntity::class], version = 2, exportSchema = false)
abstract class HabitDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun userDao(): UserDao
}
