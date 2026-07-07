package com.saynedesign.habitloop.ui.screens.createhabit

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saynedesign.habitloop.data.HabitDao
import com.saynedesign.habitloop.data.HabitEntity
import com.saynedesign.habitloop.data.UserDao
import com.saynedesign.habitloop.data.ProductivityTime
import com.saynedesign.habitloop.util.AwardXpUseCase
import com.saynedesign.habitloop.util.LevelSystem
import com.saynedesign.habitloop.util.NotificationScheduler
import com.saynedesign.habitloop.widget.WidgetUpdateHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.saynedesign.habitloop.data.UserPreferencesRepository

@HiltViewModel
class CreateHabitViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val habitDao: HabitDao,
    private val userDao: UserDao,
    private val notificationScheduler: NotificationScheduler,
    private val awardXpUseCase: AwardXpUseCase,
    private val preferencesRepository: UserPreferencesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(CreateHabitContract.State())
    val state: StateFlow<CreateHabitContract.State> = _state.asStateFlow()

    private val _effect = Channel<CreateHabitContract.Effect>()
    val effect = _effect.receiveAsFlow()

    // Store latest global times
    private var morningTimePref = "08:00"
    private var afternoonTimePref = "13:00"
    private var eveningTimePref = "18:00"

    // Once the user picks an icon/color themselves, stop auto-suggesting
    private var iconManuallySet = false
    private var colorManuallySet = false

    init {
        // Collect global time preferences
        viewModelScope.launch {
            preferencesRepository.morningTime.collect { time -> morningTimePref = time }
        }
        viewModelScope.launch {
            preferencesRepository.afternoonTime.collect { time -> afternoonTimePref = time }
        }
        viewModelScope.launch {
            preferencesRepository.eveningTime.collect { time -> eveningTimePref = time }
        }

        val habitId = savedStateHandle.get<Long>("habitId")

        viewModelScope.launch {
            userDao.getUser().collect { user ->
                if (user != null) {
                    val defaultTime = when (user.preferredProductivityTime) {
                        ProductivityTime.MORNING -> user.defaultReminderWindow.split("-").firstOrNull() ?: morningTimePref
                        ProductivityTime.AFTERNOON -> user.defaultReminderWindow.split("-").firstOrNull() ?: afternoonTimePref
                        ProductivityTime.EVENING -> user.defaultReminderWindow.split("-").firstOrNull() ?: eveningTimePref
                        ProductivityTime.NIGHT -> user.defaultReminderWindow.split("-").firstOrNull() ?: "22:00"
                    }
                    _state.update {
                        it.copy(
                            userPrimaryGoal = user.primaryGoal,
                            reminderTime = if (habitId == null || habitId == -1L) defaultTime else it.reminderTime
                        )
                    }
                }
            }
        }

        if (habitId != null && habitId != -1L) {
            loadHabit(habitId)
        } else {
            // Apply default reminder time if it's a new habit (e.g., Morning)
            _state.update { it.copy(reminderTime = morningTimePref) }
        }
    }

    private fun loadHabit(habitId: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val habit = habitDao.getHabitById(habitId)
                if (habit != null) {
                    // Editing an existing habit: never overwrite what the user chose
                    iconManuallySet = true
                    colorManuallySet = true
                    _state.value = _state.value.copy(
                        habitId = habit.id,
                        habitName = habit.title,
                        description = habit.description,
                        habitIcon = habit.icon.ifEmpty { "📝" },
                        habitType = try {
                            CreateHabitContract.HabitType.valueOf(habit.type)
                        } catch (e: Exception) {
                            CreateHabitContract.HabitType.YES_NO
                        },
                        habitColor = habit.color,
                        timeOfDay = try {
                            CreateHabitContract.TimeOfDay.valueOf(habit.timeOfDay.uppercase())
                        } catch (e: Exception) {
                            CreateHabitContract.TimeOfDay.ANYTIME
                        },
                        endDate = habit.endDate,
                        endDateEnabled = habit.endDate != null,

                        target = if (habit.type == "TIMER") habit.targetValue.toLong().toString() else habit.targetValue.toInt().toString(),
                        unitName = habit.unit,
                        isAtLeast = habit.isAtLeast,
                        reminderEnabled = habit.reminderEnabled,
                        reminderTime = habit.reminderTime,
                        selectedDays = habit.selectedDays.split(",").mapNotNull { it.toIntOrNull() }.toSet(),
                        isLoading = false
                    )
                } else {
                    _state.value = _state.value.copy(isLoading = false, isError = true)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, isError = true)
            }
        }
    }

    fun handleEvent(event: CreateHabitContract.Event) {
        when (event) {
            is CreateHabitContract.Event.OnNameChange -> {
                _state.value = _state.value.copy(habitName = event.name)
                applyVisualSuggestion(event.name)
            }
            is CreateHabitContract.Event.OnDescriptionChange -> {
                _state.value = _state.value.copy(description = event.description)
            }
            is CreateHabitContract.Event.OnTypeChange -> {
                _state.value = _state.value.copy(habitType = event.type)
            }
            is CreateHabitContract.Event.OnTargetChange -> {
                _state.value = _state.value.copy(target = event.target)
            }
            is CreateHabitContract.Event.OnUnitChange -> {
                _state.value = _state.value.copy(unitName = event.unit)
            }
            is CreateHabitContract.Event.OnTargetRuleToggle -> {
                _state.value = _state.value.copy(isAtLeast = event.isAtLeast)
            }

            is CreateHabitContract.Event.OnColorChange -> {
                colorManuallySet = true
                _state.value = _state.value.copy(habitColor = event.color)
            }
            is CreateHabitContract.Event.OnIconChange -> {
                iconManuallySet = true
                _state.value = _state.value.copy(habitIcon = event.icon)
            }
            is CreateHabitContract.Event.OnTimeOfDayChange -> {
                // Purely a display/sort category — never silently rewrites the
                // reminder time anymore.
                _state.value = _state.value.copy(timeOfDay = event.timeOfDay)
            }
            is CreateHabitContract.Event.OnEndDateToggle -> {
                _state.value = _state.value.copy(endDateEnabled = event.enabled)
                if (!event.enabled) {
                    _state.value = _state.value.copy(endDate = null)
                } else if (_state.value.endDate == null) {
                    _state.value = _state.value.copy(endDate = System.currentTimeMillis())
                }
            }
            is CreateHabitContract.Event.OnEndDateChange -> {
                _state.value = _state.value.copy(endDate = event.date)
            }

            is CreateHabitContract.Event.OnReminderToggle -> {
                _state.value = _state.value.copy(reminderEnabled = event.enabled)
            }
            is CreateHabitContract.Event.OnReminderTimeChange -> {
                _state.value = _state.value.copy(reminderTime = event.time)
            }
            is CreateHabitContract.Event.OnDayToggle -> {
                val currentDays = _state.value.selectedDays.toMutableSet()
                if (currentDays.contains(event.day)) {
                    if (currentDays.size > 1) { // Prevent empty days
                        currentDays.remove(event.day)
                    }
                } else {
                    currentDays.add(event.day)
                }
                _state.value = _state.value.copy(selectedDays = currentDays)
            }
            is CreateHabitContract.Event.OnDaysPreset -> {
                _state.value = _state.value.copy(selectedDays = event.days)
            }
            is CreateHabitContract.Event.OnToggleIconPicker -> {
                _state.value = _state.value.copy(showIconPicker = event.show)
            }
            is CreateHabitContract.Event.OnSaveClicked -> {
                saveHabit()
            }
            is CreateHabitContract.Event.OnBackClicked -> {
                viewModelScope.launch {
                    _effect.send(CreateHabitContract.Effect.NavigateBack)
                }
            }
        }
    }

    /**
     * Live icon + color suggestion from the habit title, using the same stem
     * matching idea as the native insight engine's category classifier. Only
     * applies while the user hasn't picked their own icon/color.
     */
    private fun applyVisualSuggestion(title: String) {
        if (_state.value.habitId != null) return
        if (iconManuallySet && colorManuallySet) return
        val t = title.lowercase()
        val suggestion = when {
            listOf("gym", "workout", "run", "jog", "exercise", "walk", "sport", "swim", "cycle", "pushup", "push-up").any { it in t } -> "🏃" to 0xFFFFAB91
            listOf("water", "drink", "hydrat").any { it in t } -> "💧" to 0xFF90CAF9
            listOf("meditat", "breath", "mindful", "yoga", "calm").any { it in t } -> "🧘" to 0xFFA5D6A7
            listOf("read", "book", "study", "learn", "course", "revise").any { it in t } -> "📚" to 0xFFB39DDB
            listOf("code", "program", "develop").any { it in t } -> "💻" to 0xFF80DEEA
            listOf("sleep", "bed", "wake").any { it in t } -> "🛌" to 0xFFCE93D8
            listOf("eat", "meal", "diet", "fruit", "veggie", "vegetable", "protein", "cook").any { it in t } -> "🥦" to 0xFFA5D6A7
            listOf("journal", "write", "gratitude", "diary").any { it in t } -> "📓" to 0xFFF8BBD0
            listOf("clean", "tidy", "chore", "laundry").any { it in t } -> "🧹" to 0xFFFFCC80
            listOf("money", "save", "budget", "invest").any { it in t } -> "💰" to 0xFFFFF9C4
            else -> null
        } ?: return

        _state.update {
            it.copy(
                habitIcon = if (iconManuallySet) it.habitIcon else suggestion.first,
                habitColor = if (colorManuallySet) it.habitColor else suggestion.second
            )
        }
    }

    private fun saveHabit() {
        val currentState = _state.value
        if (currentState.habitName.isBlank()) {
            // Show error via Effect
            viewModelScope.launch {
                _effect.send(CreateHabitContract.Effect.ShowToast("Habit name cannot be blank"))
            }
            return
        }

        viewModelScope.launch {
            _state.value = currentState.copy(isLoading = true)
            try {
                val existingHabit = currentState.habitId?.let { habitDao.getHabitById(it) }

                val habit = HabitEntity(
                    id = currentState.habitId ?: 0L,
                    title = currentState.habitName,
                    description = currentState.description,
                    type = currentState.habitType.name,
                    targetValue = currentState.target.toFloatOrNull() ?: 1f,
                    unit = currentState.unitName,
                    isAtLeast = currentState.isAtLeast,

                    icon = currentState.habitIcon,
                    color = currentState.habitColor,

                    // One-time tasks and the frequency selector were removed from
                    // the UI; legacy values are preserved when editing old habits.
                    isOneTime = existingHabit?.isOneTime ?: false,
                    frequency = existingHabit?.frequency ?: "Daily",
                    scheduledDate = existingHabit?.scheduledDate,
                    timeOfDay = currentState.timeOfDay.name,
                    endDate = if (currentState.endDateEnabled) currentState.endDate else null,

                    reminderEnabled = currentState.reminderEnabled,
                    reminderTime = currentState.reminderTime,
                    selectedDays = currentState.selectedDays.sorted().joinToString(","),
                    createdTimestamp = System.currentTimeMillis()
                )

                if (currentState.habitId != null) {
                    // Update existing
                    if (existingHabit != null) {
                        val updatedHabit = habit.copy(
                            createdTimestamp = existingHabit.createdTimestamp,
                            isCompletedToday = existingHabit.isCompletedToday
                        )
                        habitDao.updateHabit(updatedHabit)
                        scheduleNotification(currentState, currentState.habitId, updatedHabit)
                        WidgetUpdateHelper.updateAll(context)
                    }
                } else {
                    // Insert new
                    val habitId = habitDao.insertHabit(habit)
                    scheduleNotification(currentState, habitId, habit)
                    awardXpUseCase.award(LevelSystem.XpRewards.HABIT_CREATED, LevelSystem.XpReasons.HABIT_CREATED, habitId)
                    WidgetUpdateHelper.updateAll(context)
                }

                _effect.send(CreateHabitContract.Effect.NavigateBack)
            } catch (e: Exception) {
                _state.value = currentState.copy(isLoading = false, isError = true)
            }
        }
    }

    private fun scheduleNotification(state: CreateHabitContract.State, habitId: Long, habit: HabitEntity) {
        if (state.reminderEnabled) {
            if (habit.isOneTime && habit.scheduledDate != null) {
                // Legacy one-time habit being edited — keep its one-shot reminder
                notificationScheduler.scheduleOneTimeReminder(
                    habitId = habitId,
                    habitName = state.habitName,
                    scheduledDate = habit.scheduledDate,
                    time = state.reminderTime
                )
            } else {
                notificationScheduler.scheduleReminder(
                    habitId = habitId,
                    habitName = state.habitName,
                    time = state.reminderTime,
                    days = state.selectedDays
                )
            }
        } else {
            notificationScheduler.cancelReminder(habitId)
        }
    }
}
