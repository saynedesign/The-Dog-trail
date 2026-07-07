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
        XpEventEntity::class,
        CoachEventEntity::class
    ],
    version = 9,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class HabitDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun userDao(): UserDao
    abstract fun habitLogDao(): HabitLogDao
    abstract fun habitRestDayDao(): HabitRestDayDao
    abstract fun xpEventDao(): XpEventDao
    abstract fun coachEventDao(): CoachEventDao

    companion object {
        /** All migrations — every database builder MUST register this list. */
        val ALL_MIGRATIONS: Array<Migration>
            get() = arrayOf(MIGRATION_7_8, MIGRATION_8_9)

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS coach_events (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "type TEXT NOT NULL, " +
                        "timestamp INTEGER NOT NULL)"
                )
            }
        }
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
