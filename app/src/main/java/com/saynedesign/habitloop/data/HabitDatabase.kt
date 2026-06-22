package com.saynedesign.habitloop.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        HabitEntity::class,
        UserEntity::class,
        HabitLogEntity::class,
        HabitRestDayEntity::class,
        XpEventEntity::class
    ],
    version = 7,
    exportSchema = false
)
abstract class HabitDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun userDao(): UserDao
    abstract fun habitLogDao(): HabitLogDao
    abstract fun habitRestDayDao(): HabitRestDayDao
    abstract fun xpEventDao(): XpEventDao
}
