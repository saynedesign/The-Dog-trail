package com.saynedesign.habitloop.widget

import android.content.Context
import androidx.room.Room
import com.saynedesign.habitloop.data.HabitDatabase

/**
 * Provides a singleton Room database instance for widgets.
 * Widgets can't use Hilt injection, so they access the DB through this helper.
 */
object WidgetDatabaseProvider {

    @Volatile
    private var INSTANCE: HabitDatabase? = null

    fun getDatabase(context: Context): HabitDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                HabitDatabase::class.java,
                "habit_database"
            )
                // Must carry the same migrations as the app's Hilt instance.
                // The previous fallbackToDestructiveMigration() would have
                // WIPED the entire database if a widget rendered before the
                // app process after a schema version bump.
                .addMigrations(*HabitDatabase.ALL_MIGRATIONS)
                .build()
            INSTANCE = instance
            instance
        }
    }
}
