package com.saynedesign.habitloop.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    // Keys
    private object PreferencesKeys {
        val MORNING_TIME = stringPreferencesKey("morning_time")
        val AFTERNOON_TIME = stringPreferencesKey("afternoon_time")
        val EVENING_TIME = stringPreferencesKey("evening_time")
        val FIRST_DAY_OF_WEEK = stringPreferencesKey("first_day_of_week")
        val IS_VACATION_MODE = booleanPreferencesKey("is_vacation_mode")
        val IS_DAILY_REMINDER_ENABLED = booleanPreferencesKey("is_daily_reminder_enabled")
        val REMINDER_TIME = stringPreferencesKey("reminder_time")
    }

    // Flows
    val morningTime: Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.MORNING_TIME] ?: "08:00"
    }

    val afternoonTime: Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.AFTERNOON_TIME] ?: "13:00"
    }

    val eveningTime: Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.EVENING_TIME] ?: "18:00"
    }

    val firstDayOfWeek: Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.FIRST_DAY_OF_WEEK] ?: "Monday"
    }

    val isVacationMode: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_VACATION_MODE] ?: false
    }

    val isDailyReminderEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_DAILY_REMINDER_ENABLED] ?: false
    }

    val reminderTime: Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.REMINDER_TIME] ?: "20:00"
    }

    // Suspend functions to update settings
    suspend fun updateMorningTime(time: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.MORNING_TIME] = time
        }
    }

    suspend fun updateAfternoonTime(time: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.AFTERNOON_TIME] = time
        }
    }

    suspend fun updateEveningTime(time: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.EVENING_TIME] = time
        }
    }

    suspend fun updateFirstDayOfWeek(day: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.FIRST_DAY_OF_WEEK] = day
        }
    }

    suspend fun updateVacationMode(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_VACATION_MODE] = enabled
        }
    }

    suspend fun updateDailyReminderEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_DAILY_REMINDER_ENABLED] = enabled
        }
    }

    suspend fun updateReminderTime(time: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.REMINDER_TIME] = time
        }
    }
}
