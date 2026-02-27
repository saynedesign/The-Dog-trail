package com.codesmithslabs.thedogtail.di

import android.content.Context
import androidx.room.Room
import com.codesmithslabs.thedogtail.data.HabitDao
import com.codesmithslabs.thedogtail.data.HabitDatabase
import com.codesmithslabs.thedogtail.data.UserDao
import com.codesmithslabs.thedogtail.data.HabitLogDao
import com.codesmithslabs.thedogtail.data.MoodDao
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
        ).fallbackToDestructiveMigration()
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
    fun provideMoodDao(database: HabitDatabase): MoodDao {
        return database.moodDao()
    }
}
