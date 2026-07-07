package com.saynedesign.habitloop.di

import android.content.Context
import androidx.room.Room
import com.saynedesign.habitloop.data.HabitDao
import com.saynedesign.habitloop.data.HabitDatabase
import com.saynedesign.habitloop.data.UserDao
import com.saynedesign.habitloop.data.HabitLogDao
import com.saynedesign.habitloop.data.HabitRestDayDao
import com.saynedesign.habitloop.data.XpEventDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideHabitDatabase(
        @ApplicationContext context: Context
    ): HabitDatabase {
        return Room.databaseBuilder(
            context,
            HabitDatabase::class.java,
            "habit_database"
        ).addMigrations(*HabitDatabase.ALL_MIGRATIONS)
         .build()
    }

    @Provides
    @Singleton
    fun provideHabitDao(database: HabitDatabase): HabitDao {
        return database.habitDao()
    }

    @Provides
    @Singleton
    fun provideUserDao(database: HabitDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    @Singleton
    fun provideHabitLogDao(database: HabitDatabase): HabitLogDao {
        return database.habitLogDao()
    }

    @Provides
    @Singleton
    fun provideHabitRestDayDao(database: HabitDatabase): HabitRestDayDao {
        return database.habitRestDayDao()
    }

    @Provides
    @Singleton
    fun provideXpEventDao(database: HabitDatabase): XpEventDao {
        return database.xpEventDao()
    }

    @Provides
    @Singleton
    fun provideCoachEventDao(database: HabitDatabase): com.saynedesign.habitloop.data.CoachEventDao {
        return database.coachEventDao()
    }
}
