package com.saynedesign.habitloop.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        HabitEntity::class,
        UserEntity::class,
        HabitLogEntity::class,
        HabitRestDayEntity::class,
        XpEventEntity::class
    ],
    version = 8,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class HabitDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun userDao(): UserDao
    abstract fun habitLogDao(): HabitLogDao
    abstract fun habitRestDayDao(): HabitRestDayDao
    abstract fun xpEventDao(): XpEventDao

    companion object {
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_info ADD COLUMN photoUri TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE user_info ADD COLUMN dateOfBirth TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE user_info ADD COLUMN weight REAL DEFAULT NULL")
                db.execSQL("ALTER TABLE user_info ADD COLUMN unitSystem TEXT NOT NULL DEFAULT 'METRIC'")
                db.execSQL("ALTER TABLE user_info ADD COLUMN primaryGoal TEXT NOT NULL DEFAULT 'FITNESS'")
                db.execSQL("ALTER TABLE user_info ADD COLUMN weeklyGoal INTEGER NOT NULL DEFAULT 5")
                db.execSQL("ALTER TABLE user_info ADD COLUMN preferredProductivityTime TEXT NOT NULL DEFAULT 'MORNING'")
                db.execSQL("ALTER TABLE user_info ADD COLUMN motivationStyle TEXT NOT NULL DEFAULT 'SEEING_PROGRESS'")
                db.execSQL("ALTER TABLE user_info ADD COLUMN experienceLevel TEXT NOT NULL DEFAULT 'BEGINNER'")
                db.execSQL("ALTER TABLE user_info ADD COLUMN weekStartsOn TEXT NOT NULL DEFAULT 'MONDAY'")
                db.execSQL("ALTER TABLE user_info ADD COLUMN defaultReminderWindow TEXT NOT NULL DEFAULT '08:00-10:00'")
                db.execSQL("ALTER TABLE user_info ADD COLUMN timezone TEXT NOT NULL DEFAULT 'UTC'")
            }
        }
    }
}
