package com.saynedesign.habitloop.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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
        val IS_OVERLAY_REMINDER_ENABLED = booleanPreferencesKey("is_overlay_reminder_enabled")
        val OVERLAY_REMINDER_SOUND = stringPreferencesKey("overlay_reminder_sound")
        val REMINDER_TIME = stringPreferencesKey("reminder_time")
        val APP_THEME = stringPreferencesKey("app_theme")
        val IS_COACH_ENABLED = booleanPreferencesKey("is_coach_enabled")
        // The highest level for which we've already shown the level-up
        // celebration. 0 = never set (baseline adopted on first read).
        val LAST_CELEBRATED_LEVEL = intPreferencesKey("last_celebrated_level")
        // How habit reminders are delivered: "overlay" (full-screen alarm),
        // "notification" (standard heads-up), or "off".
        val REMINDER_STYLE = stringPreferencesKey("reminder_style")
        // A user-picked custom sound (system ringtone picker) used when the
        // reminder sound is set to "custom".
        val CUSTOM_SOUND_URI = stringPreferencesKey("custom_sound_uri")
        val CUSTOM_SOUND_LABEL = stringPreferencesKey("custom_sound_label")
        // Whether the one-time "switch to full-screen alarm" prompt has been
        // shown. Set true after onboarding (new users) or after the prompt is
        // seen (existing users who updated) so it never appears twice.
        val HAS_SEEN_OVERLAY_PROMO = booleanPreferencesKey("has_seen_overlay_promo")
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

    val isOverlayReminderEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_OVERLAY_REMINDER_ENABLED] ?: false
    }

    val overlayReminderSound: Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.OVERLAY_REMINDER_SOUND] ?: "alarm"
    }

    val reminderTime: Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.REMINDER_TIME] ?: "20:00"
    }

    val appTheme: Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.APP_THEME] ?: "system"
    }

    val isCoachEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_COACH_ENABLED] ?: true
    }

    // Reminder style. Falls back to the legacy overlay toggle so existing users
    // who had overlay reminders on keep the full-screen alarm after this update.
    val reminderStyle: Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.REMINDER_STYLE]
            ?: if (preferences[PreferencesKeys.IS_OVERLAY_REMINDER_ENABLED] == true) "overlay" else "notification"
    }

    val customSoundUri: Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.CUSTOM_SOUND_URI] ?: ""
    }

    val customSoundLabel: Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.CUSTOM_SOUND_LABEL] ?: "Custom"
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

    suspend fun updateOverlayReminderEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_OVERLAY_REMINDER_ENABLED] = enabled
        }
    }

    suspend fun updateOverlayReminderSound(sound: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.OVERLAY_REMINDER_SOUND] = sound
        }
    }

    suspend fun updateAppTheme(theme: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.APP_THEME] = theme
        }
    }

    suspend fun updateCoachEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_COACH_ENABLED] = enabled
        }
    }

    /** One-shot read of the last level we celebrated (0 if never set). */
    suspend fun lastCelebratedLevelOnce(): Int =
        dataStore.data.map { it[PreferencesKeys.LAST_CELEBRATED_LEVEL] ?: 0 }.first()

    suspend fun updateLastCelebratedLevel(level: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_CELEBRATED_LEVEL] = level
        }
    }

    suspend fun reminderStyleOnce(): String = reminderStyle.first()

    suspend fun customSoundUriOnce(): String = customSoundUri.first()

    suspend fun hasSeenOverlayPromoOnce(): Boolean =
        dataStore.data.map { it[PreferencesKeys.HAS_SEEN_OVERLAY_PROMO] ?: false }.first()

    suspend fun setHasSeenOverlayPromo() {
        dataStore.edit { it[PreferencesKeys.HAS_SEEN_OVERLAY_PROMO] = true }
    }

    suspend fun updateReminderStyle(style: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.REMINDER_STYLE] = style
            // Keep the legacy flag in sync for any older readers.
            preferences[PreferencesKeys.IS_OVERLAY_REMINDER_ENABLED] = (style == "overlay")
        }
    }

    suspend fun updateCustomSound(uri: String, label: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.CUSTOM_SOUND_URI] = uri
            preferences[PreferencesKeys.CUSTOM_SOUND_LABEL] = label
        }
    }
}
