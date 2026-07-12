package com.saynedesign.habitloop.ui.screens.preferences

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saynedesign.habitloop.data.UserPreferencesRepository
import com.saynedesign.habitloop.data.HabitDatabase
import com.saynedesign.habitloop.data.HabitEntity
import com.saynedesign.habitloop.data.HabitLogEntity
import com.saynedesign.habitloop.data.UserEntity
import com.saynedesign.habitloop.data.XpEventEntity
import com.saynedesign.habitloop.widget.WidgetUpdateHelper
import java.time.LocalDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class PreferencesViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val db: HabitDatabase,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(PreferencesContract.State())
    val state = _state.asStateFlow()

    private val _effect = Channel<PreferencesContract.Effect>()
    val effect = _effect.receiveAsFlow()

    init {
        // Collect flows from DataStore
        viewModelScope.launch {
            preferencesRepository.morningTime.collectLatest { time ->
                _state.update { it.copy(morningTime = time) }
            }
        }
        viewModelScope.launch {
            preferencesRepository.afternoonTime.collectLatest { time ->
                _state.update { it.copy(afternoonTime = time) }
            }
        }
        viewModelScope.launch {
            preferencesRepository.eveningTime.collectLatest { time ->
                _state.update { it.copy(eveningTime = time) }
            }
        }
        viewModelScope.launch {
            preferencesRepository.firstDayOfWeek.collectLatest { day ->
                _state.update { it.copy(firstDayOfWeek = day) }
            }
        }
        viewModelScope.launch {
            preferencesRepository.isVacationMode.collectLatest { mode ->
                _state.update { it.copy(isVacationMode = mode) }
            }
        }
        viewModelScope.launch {
            preferencesRepository.isDailyReminderEnabled.collectLatest { enabled ->
                _state.update { it.copy(isDailyReminderEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            preferencesRepository.reminderTime.collectLatest { time ->
                _state.update { it.copy(reminderTime = time) }
            }
        }
        viewModelScope.launch {
            preferencesRepository.isOverlayReminderEnabled.collectLatest { enabled ->
                _state.update { it.copy(isOverlayReminderEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            preferencesRepository.overlayReminderSound.collectLatest { sound ->
                _state.update { it.copy(overlayReminderSound = sound) }
            }
        }
        viewModelScope.launch {
            preferencesRepository.reminderStyle.collectLatest { style ->
                _state.update { it.copy(reminderStyle = style) }
            }
        }
        viewModelScope.launch {
            preferencesRepository.customSoundLabel.collectLatest { label ->
                _state.update { it.copy(customSoundLabel = label) }
            }
        }
        viewModelScope.launch {
            preferencesRepository.isCoachEnabled.collectLatest { enabled ->
                _state.update { it.copy(isCoachEnabled = enabled) }
            }
        }
    }

    fun handleEvent(event: PreferencesContract.Event) {
        when (event) {
            PreferencesContract.Event.OnBackClicked -> {
                viewModelScope.launch { _effect.send(PreferencesContract.Effect.NavigateBack) }
            }
            is PreferencesContract.Event.OnTimeClick -> {
                _state.update { it.copy(showTimePicker = true, timePickerType = event.type) }
            }
            is PreferencesContract.Event.OnTimeSelected -> {
                _state.update { it.copy(showTimePicker = false, timePickerType = null) }
                
                viewModelScope.launch {
                    when (_state.value.timePickerType) {
                        PreferencesContract.TimePickerType.MORNING -> preferencesRepository.updateMorningTime(event.time)
                        PreferencesContract.TimePickerType.AFTERNOON -> preferencesRepository.updateAfternoonTime(event.time)
                        PreferencesContract.TimePickerType.EVENING -> preferencesRepository.updateEveningTime(event.time)
                        PreferencesContract.TimePickerType.REMINDER -> preferencesRepository.updateReminderTime(event.time)
                        null -> {} // Do nothing
                    }
                }
            }
            PreferencesContract.Event.OnTimePickerDismiss -> {
                _state.update { it.copy(showTimePicker = false, timePickerType = null) }
            }
            is PreferencesContract.Event.OnFirstDayOfWeekClick -> {
                viewModelScope.launch {
                    val current = _state.value.firstDayOfWeek
                    preferencesRepository.updateFirstDayOfWeek(if (current == "Monday") "Sunday" else "Monday")
                }
            }
            is PreferencesContract.Event.OnVacationModeToggle -> {
                viewModelScope.launch { preferencesRepository.updateVacationMode(event.enabled) }
            }
            is PreferencesContract.Event.OnDailyReminderToggle -> {
                viewModelScope.launch { preferencesRepository.updateDailyReminderEnabled(event.enabled) }
            }
            is PreferencesContract.Event.OnOverlayReminderToggle -> {
                viewModelScope.launch { preferencesRepository.updateOverlayReminderEnabled(event.enabled) }
            }
            is PreferencesContract.Event.OnOverlayReminderSoundChange -> {
                viewModelScope.launch { preferencesRepository.updateOverlayReminderSound(event.sound) }
            }
            is PreferencesContract.Event.OnReminderStyleChange -> {
                viewModelScope.launch { preferencesRepository.updateReminderStyle(event.style) }
            }
            is PreferencesContract.Event.OnCustomSoundSelected -> {
                viewModelScope.launch {
                    preferencesRepository.updateCustomSound(event.uri, event.label)
                    preferencesRepository.updateOverlayReminderSound("custom")
                }
            }
            is PreferencesContract.Event.OnCoachToggle -> {
                viewModelScope.launch { preferencesRepository.updateCoachEnabled(event.enabled) }
            }
            PreferencesContract.Event.OnClearCacheClicked -> {
                viewModelScope.launch { _effect.send(PreferencesContract.Effect.ShowToast("Cache cleared!")) }
            }
            PreferencesContract.Event.OnRestartHabitsClicked -> {
                viewModelScope.launch { _effect.send(PreferencesContract.Effect.ShowToast("All habits restarted!")) }
            }
            PreferencesContract.Event.OnSeedDataClicked -> {
                viewModelScope.launch {
                    withContext(Dispatchers.IO) {
                        db.clearAllTables()
                        
                        val today = LocalDate.now()
                        val userDao = db.userDao()
                        val habitDao = db.habitDao()
                        val habitLogDao = db.habitLogDao()
                        val xpEventDao = db.xpEventDao()
                        
                        userDao.insertUser(
                            UserEntity(
                                name = "Aman Kumar",
                                dob = "12/04/1998",
                                height = 180f,
                                profileImageUri = null,
                                totalXp = 850,
                                currentLevel = 3
                            )
                        )
                        
                        xpEventDao.insertEvent(
                            XpEventEntity(
                                xpAmount = 850,
                                reason = "Seeded Initial Progress",
                                timestamp = System.currentTimeMillis() - 86400000
                            )
                        )
                        
                        val hydrationId = habitDao.insertHabit(
                            HabitEntity(
                                title = "Hydration Loop",
                                description = "Drink at least 8 cups of water to stay energetic and healthy.",
                                targetValue = 8f,
                                unit = "cups",
                                type = "NUMERIC",
                                color = 0xFF3366FFL,
                                icon = "💧",
                                selectedDays = "1,2,3,4,5,6,7",
                                frequency = "Daily",
                                timeOfDay = "Anytime"
                            )
                        )
                        
                        val morningRunId = habitDao.insertHabit(
                            HabitEntity(
                                title = "Morning Run",
                                description = "Cardio training to boost metabolism and daily physical stamina.",
                                targetValue = 5f,
                                unit = "km",
                                type = "NUMERIC",
                                color = 0xFFFF9800L,
                                icon = "🏃",
                                selectedDays = "1,2,3,4,5,6,7",
                                frequency = "Daily",
                                timeOfDay = "Morning"
                            )
                        )
                        
                        val focusWorkId = habitDao.insertHabit(
                            HabitEntity(
                                title = "Deep Focus Work",
                                description = "Uninterrupted sessions for coding, design, or professional reading.",
                                targetValue = 45f,
                                unit = "min",
                                type = "TIMER",
                                color = 0xFF6C4BFFL,
                                icon = "💻",
                                selectedDays = "1,2,3,4,5,6,7",
                                frequency = "Daily",
                                timeOfDay = "Afternoon"
                            )
                        )
                        
                        for (i in 0..30) {
                            val epochDay = today.minusDays(i.toLong()).toEpochDay()
                            
                            val hydrationVal = if (i % 5 == 0) 5f else if (i % 7 == 0) 4f else 8f
                            habitLogDao.insertLog(
                                HabitLogEntity(
                                    habitId = hydrationId,
                                    dateEpochDay = epochDay,
                                    value = hydrationVal
                                )
                            )
                            
                            if (i % 3 != 0) {
                                val runVal = if (i % 4 == 0) 4f else 5f
                                habitLogDao.insertLog(
                                    HabitLogEntity(
                                        habitId = morningRunId,
                                        dateEpochDay = epochDay,
                                        value = runVal
                                    )
                                )
                            }
                            
                            if (i % 4 != 0) {
                                val focusVal = if (i % 5 == 0) 30f else 45f
                                habitLogDao.insertLog(
                                    HabitLogEntity(
                                        habitId = focusWorkId,
                                        dateEpochDay = epochDay,
                                        value = focusVal
                                    )
                                )
                            }
                        }
                    }
                    
                    WidgetUpdateHelper.updateAll(context)
                    _effect.send(PreferencesContract.Effect.ShowToast("Sample data generated successfully!"))
                }
            }
        }
    }
}
