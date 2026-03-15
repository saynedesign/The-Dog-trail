package com.codesmithslabs.thedogtail.widget

import android.content.Context
import androidx.room.Room
import com.codesmithslabs.thedogtail.data.HabitDatabase

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
            ).fallbackToDestructiveMigration()
                .build()
            INSTANCE = instance
            instance
        }
    }
}
