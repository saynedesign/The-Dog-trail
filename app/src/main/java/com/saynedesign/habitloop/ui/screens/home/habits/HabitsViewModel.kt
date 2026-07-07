package com.saynedesign.habitloop.ui.screens.home.habits

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saynedesign.habitloop.data.HabitDao
import com.saynedesign.habitloop.data.HabitEntity
import com.saynedesign.habitloop.data.HabitLogDao
import com.saynedesign.habitloop.data.HabitLogEntity
import com.saynedesign.habitloop.data.HabitRestDayDao
import com.saynedesign.habitloop.data.HabitRestDayEntity
import com.saynedesign.habitloop.data.UserDao
import com.saynedesign.habitloop.data.UserPreferencesRepository
import com.saynedesign.habitloop.data.isScheduledOn
import com.saynedesign.habitloop.util.AwardXpUseCase
import com.saynedesign.habitloop.util.CompleteHabitUseCase
import com.saynedesign.habitloop.util.LevelSystem
import com.saynedesign.habitloop.widget.WidgetUpdateHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class HabitsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val habitDao: HabitDao,
    private val habitLogDao: HabitLogDao,
    private val habitRestDayDao: HabitRestDayDao,
    private val userDao: UserDao,
    private val awardXpUseCase: AwardXpUseCase,
    private val completeHabitUseCase: CompleteHabitUseCase,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HabitsContract.State())
    val state: StateFlow<HabitsContract.State> = _state.asStateFlow()

    private val _effect = Channel<HabitsContract.Effect>()
    val effect = _effect.receiveAsFlow()

    private var logsCache: List<HabitLogEntity> = emptyList()
    private var allHabitsCache: List<HabitEntity> = emptyList()
    private var restDaysCache: List<HabitRestDayEntity> = emptyList()

    // Highest level for which the celebration has already been shown, seeded
    // from persisted state so a level-up earned on any screen (or in the
    // background via widget/notification) fires exactly once. 0 = not yet seeded.
    private var celebratedLevel: Int = 0

    init {
        loadHabits()
        loadLogs()
        loadXp()
        loadRestDays()
    }

    private fun loadHabits() {
        viewModelScope.launch {
            habitDao.getAllHabits().collect { habits ->
                allHabitsCache = habits
                updateHabitsForSelectedDate()
            }
        }
    }

    private fun loadLogs() {
        viewModelScope.launch {
            habitLogDao.getAllLogs().collect { logs ->
                logsCache = logs
                updateHabitLogsForSelectedDate()
                calculateActiveStreak()
            }
        }
    }

    private fun loadRestDays() {
        viewModelScope.launch {
            habitRestDayDao.getAllRestDays().collect { restDays ->
                restDaysCache = restDays
                calculateActiveStreak()
            }
        }
    }

    private fun calculateActiveStreak() {
        val today = LocalDate.now()
        val logsByDay = logsCache.groupBy { it.dateEpochDay }
        val restDaysByEpoch = restDaysCache.groupBy { it.dateEpochDay }
        
        var activeMomentum = 0
        var checkDate = today
        val todayEpoch = today.toEpochDay()
        val todayLogs = logsByDay[todayEpoch] ?: emptyList()
        val todayHasRest = restDaysByEpoch.containsKey(todayEpoch)
        if (todayLogs.isEmpty() && !todayHasRest) {
            checkDate = checkDate.minusDays(1)
        }
        while (true) {
            val epoch = checkDate.toEpochDay()
            val dayLogs = logsByDay[epoch] ?: emptyList()
            val dayHasRest = restDaysByEpoch.containsKey(epoch)
            when {
                dayHasRest && dayLogs.isEmpty() -> {
                    checkDate = checkDate.minusDays(1)
                }
                dayLogs.isNotEmpty() -> {
                    activeMomentum++
                    checkDate = checkDate.minusDays(1)
                }
                else -> break
            }
            if (activeMomentum > 3650) break
        }
        _state.value = _state.value.copy(currentStreak = activeMomentum)
    }

    private fun loadXp() {
        viewModelScope.launch {
            // Seed the celebration baseline from persisted state. For a fresh
            // install (or existing users on first run after this update) we adopt
            // the current level as the baseline so we never fire a spurious pop.
            celebratedLevel = userPreferencesRepository.lastCelebratedLevelOnce()

            userDao.getUser().collect { user ->
                val newLevel = user?.currentLevel ?: 1

                if (celebratedLevel == 0) {
                    celebratedLevel = newLevel
                    userPreferencesRepository.updateLastCelebratedLevel(newLevel)
                }

                val leveledUp = newLevel > celebratedLevel
                if (leveledUp) {
                    celebratedLevel = newLevel
                    userPreferencesRepository.updateLastCelebratedLevel(newLevel)
                }

                _state.value = _state.value.copy(
                    totalXp = user?.totalXp ?: 0,
                    currentLevel = newLevel,
                    userName = user?.name ?: "",
                    profileImageUri = user?.profileImageUri ?: user?.photoUri,
                    motivationStyle = user?.motivationStyle ?: com.saynedesign.habitloop.data.MotivationStyle.SEEING_PROGRESS,
                    levelUpToLevel = if (leveledUp) newLevel else _state.value.levelUpToLevel
                )
            }
        }
    }

    private fun updateHabitsForSelectedDate() {
        val selectedDate = _state.value.selectedDate

        // Shared scheduling rule: day-of-week + one-time date + end date
        val filteredHabits = allHabitsCache.filter { it.isScheduledOn(selectedDate) }
        _state.value = _state.value.copy(habits = filteredHabits)
        loadRestDayStateForSelectedDate()
    }

    private fun updateHabitLogsForSelectedDate() {
        val day = _state.value.selectedEpochDay
        val logsForDay = logsCache.asSequence()
            .filter { it.dateEpochDay == day }
            .associateBy { it.habitId }
        
        val streaks = allHabitsCache.associate { habit ->
            habit.id to calculateStreakForHabit(habit.id, logsCache)
        }
        
        _state.value = _state.value.copy(
            habitLogs = logsForDay,
            habitStreaks = streaks
        )
    }

    private fun loadRestDayStateForSelectedDate() {
        viewModelScope.launch {
            val epoch = _state.value.selectedEpochDay
            val restingIds = habitRestDayDao.getRestingHabitIdsForDay(epoch).toSet()
            _state.value = _state.value.copy(restingHabitIds = restingIds)
        }
    }

    fun handleEvent(event: HabitsContract.Event) {
        when (event) {
            is HabitsContract.Event.OnAddHabitClicked -> {
                sendEffect(HabitsContract.Effect.NavigateToAddHabit)
            }
            is HabitsContract.Event.OnHabitClicked -> {
                sendEffect(HabitsContract.Effect.NavigateToHabitDetails(event.habitId))
            }
            is HabitsContract.Event.OnDateSelected -> {
                val epoch = event.date.toEpochDay()
                _state.value = _state.value.copy(
                    selectedDate = event.date,
                    selectedEpochDay = epoch
                )
                updateHabitsForSelectedDate()
                updateHabitLogsForSelectedDate()
            }
            is HabitsContract.Event.OnToggleHabit -> {
                toggleHabit(event.habitId, event.isDone)
            }
            is HabitsContract.Event.OnUpdateHabitValue -> {
                updateHabitValue(event.habitId, event.newValue)
            }
            is HabitsContract.Event.OnTimerClicked -> {
                sendEffect(HabitsContract.Effect.NavigateToTimer(event.habitId))
            }
            is HabitsContract.Event.OnEditHabitClicked -> {
                _state.value = _state.value.copy(
                    showEditDialog = true,
                    selectedHabitId = event.habitId
                )
            }
            is HabitsContract.Event.OnDeleteHabitClicked -> {
                _state.value = _state.value.copy(
                    showDeleteDialog = true,
                    selectedHabitId = event.habitId
                )
            }
            is HabitsContract.Event.OnDismissDialog -> {
                _state.value = _state.value.copy(
                    showDeleteDialog = false,
                    showEditDialog = false,
                    selectedHabitId = null
                )
            }
            is HabitsContract.Event.OnConfirmDelete -> {
                val habitId = _state.value.selectedHabitId
                if (habitId != null) {
                    viewModelScope.launch {
                        val habit = habitDao.getHabitById(habitId)
                        if (habit != null) {
                            habitDao.deleteHabit(habit)
                        }
                        WidgetUpdateHelper.updateAll(context)
                    }
                }
                _state.value = _state.value.copy(
                    showDeleteDialog = false,
                    selectedHabitId = null
                )
            }
            is HabitsContract.Event.OnConfirmEdit -> {
                val habitId = _state.value.selectedHabitId
                if (habitId != null) {
                    sendEffect(HabitsContract.Effect.NavigateToEditHabit(habitId))
                }
                _state.value = _state.value.copy(
                    showEditDialog = false,
                    selectedHabitId = null
                )
            }
            
            // Rest Day
            is HabitsContract.Event.OnRestDayRequested -> {
                viewModelScope.launch {
                    val today = LocalDate.now()
                    val weekStart = today.with(DayOfWeek.MONDAY).toEpochDay()
                    val weekEnd = today.with(DayOfWeek.SUNDAY).toEpochDay()
                    val usedThisWeek = habitRestDayDao.countRestDaysInWeek(event.habitId, weekStart, weekEnd)
                    _state.value = _state.value.copy(
                        showRestDaySheet = true,
                        restDayTargetHabitId = event.habitId,
                        restDaysUsedThisWeek = usedThisWeek
                    )
                }
            }
            is HabitsContract.Event.OnConfirmRestDay -> {
                val habitId = _state.value.restDayTargetHabitId ?: return
                viewModelScope.launch {
                    val today = LocalDate.now()
                    val weekStart = today.with(DayOfWeek.MONDAY).toEpochDay()
                    val weekEnd = today.with(DayOfWeek.SUNDAY).toEpochDay()
                    val usedThisWeek = habitRestDayDao.countRestDaysInWeek(habitId, weekStart, weekEnd)
                    if (usedThisWeek < 1) {
                        habitRestDayDao.declareRestDay(
                            HabitRestDayEntity(
                                habitId = habitId,
                                dateEpochDay = _state.value.selectedEpochDay
                            )
                        )
                        awardXpUseCase.award(
                            LevelSystem.XpRewards.REST_DAY,
                            LevelSystem.XpReasons.REST_DAY,
                            habitId
                        )
                        _state.value = _state.value.copy(xpPopAmount = LevelSystem.XpRewards.REST_DAY)
                        loadRestDayStateForSelectedDate()
                        WidgetUpdateHelper.updateAll(context)
                    }
                    _state.value = _state.value.copy(
                        showRestDaySheet = false,
                        restDayTargetHabitId = null
                    )
                }
            }
            is HabitsContract.Event.OnDismissRestDaySheet -> {
                _state.value = _state.value.copy(
                    showRestDaySheet = false,
                    restDayTargetHabitId = null
                )
            }
            
            // XP
            is HabitsContract.Event.OnXpPopDismissed -> {
                _state.value = _state.value.copy(xpPopAmount = null)
            }
            is HabitsContract.Event.OnLevelUpDismissed -> {
                _state.value = _state.value.copy(levelUpToLevel = null)
            }
        }
    }

    private fun toggleHabit(habitId: Long, isDone: Boolean) {
        viewModelScope.launch {
            val day = _state.value.selectedEpochDay
            val result = completeHabitUseCase.setCompleted(habitId, day, isDone)
            if (result.changed && result.xpAwarded > 0) {
                _state.value = _state.value.copy(xpPopAmount = result.xpAwarded)
            }
        }
    }

    private fun updateHabitValue(habitId: Long, newValue: Float) {
        viewModelScope.launch {
            val day = _state.value.selectedEpochDay
            val result = completeHabitUseCase.setValue(habitId, day, newValue)
            if (result.changed && result.xpAwarded > 0) {
                _state.value = _state.value.copy(xpPopAmount = result.xpAwarded)
            }
        }
    }

    private fun sendEffect(effect: HabitsContract.Effect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }

    private fun calculateStreakForHabit(habitId: Long, logs: List<HabitLogEntity>): Int {
        val habitLogs = logs.filter { it.habitId == habitId }
        if (habitLogs.isEmpty()) return 0
        val logDates = habitLogs.map { it.dateEpochDay }.toSet()
        
        var streak = 0
        var checkDate = LocalDate.now().toEpochDay()
        
        if (!logDates.contains(checkDate)) {
            checkDate--
        }
        
        while (logDates.contains(checkDate)) {
            streak++
            checkDate--
            if (streak > 365) break
        }
        return streak
    }
}
