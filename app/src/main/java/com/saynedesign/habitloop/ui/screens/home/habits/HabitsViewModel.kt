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
import com.saynedesign.habitloop.util.AwardXpUseCase
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
    private val awardXpUseCase: AwardXpUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HabitsContract.State())
    val state: StateFlow<HabitsContract.State> = _state.asStateFlow()

    private val _effect = Channel<HabitsContract.Effect>()
    val effect = _effect.receiveAsFlow()

    private var logsCache: List<HabitLogEntity> = emptyList()
    private var allHabitsCache: List<HabitEntity> = emptyList()
    private var restDaysCache: List<HabitRestDayEntity> = emptyList()

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
            userDao.getUser().collect { user ->
                _state.value = _state.value.copy(
                    totalXp = user?.totalXp ?: 0,
                    currentLevel = user?.currentLevel ?: 1,
                    userName = user?.name ?: "",
                    profileImageUri = user?.profileImageUri ?: user?.photoUri,
                    motivationStyle = user?.motivationStyle ?: com.saynedesign.habitloop.data.MotivationStyle.SEEING_PROGRESS
                )
            }
        }
    }

    private fun updateHabitsForSelectedDate() {
        val selectedDate = _state.value.selectedDate
        val dayOfWeek = selectedDate.dayOfWeek.value

        val filteredHabits = allHabitsCache.filter { habit ->
            val scheduledDays = habit.selectedDays.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
            scheduledDays.contains(dayOfWeek)
        }
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
        }
    }

    private fun toggleHabit(habitId: Long, isDone: Boolean) {
        viewModelScope.launch {
            val day = _state.value.selectedEpochDay
            val existing = habitLogDao.getLogForDay(habitId, day)
            if (isDone && existing == null) {
                habitLogDao.insertLog(HabitLogEntity(habitId = habitId, dateEpochDay = day, value = 1f))

                val logsToday = logsCache.count { it.dateEpochDay == day }
                var xpAwarded = LevelSystem.XpRewards.HABIT_COMPLETE
                awardXpUseCase.award(LevelSystem.XpRewards.HABIT_COMPLETE, LevelSystem.XpReasons.HABIT_COMPLETE, habitId)

                if (logsToday == 0) {
                    awardXpUseCase.award(LevelSystem.XpRewards.FIRST_OF_DAY, LevelSystem.XpReasons.FIRST_OF_DAY, habitId)
                    xpAwarded += LevelSystem.XpRewards.FIRST_OF_DAY
                }

                val scheduledHabits = _state.value.habits
                val restingIds = _state.value.restingHabitIds
                val nonRestingHabits = scheduledHabits.filter { it.id !in restingIds }
                val logsForDay = logsCache.filter { it.dateEpochDay == day }.map { it.habitId }.toSet() + habitId
                val allDone = nonRestingHabits.all { it.id in logsForDay }
                if (allDone && nonRestingHabits.isNotEmpty()) {
                    awardXpUseCase.award(LevelSystem.XpRewards.PERFECT_DAY, LevelSystem.XpReasons.PERFECT_DAY)
                    xpAwarded += LevelSystem.XpRewards.PERFECT_DAY
                }

                _state.value = _state.value.copy(xpPopAmount = xpAwarded)
            } else if (!isDone && existing != null) {
                habitLogDao.deleteLog(existing)

                val logsToday = logsCache.count { it.dateEpochDay == day }
                awardXpUseCase.award(-LevelSystem.XpRewards.HABIT_COMPLETE, LevelSystem.XpReasons.HABIT_COMPLETE + "_REVOKE", habitId)
                if (logsToday == 1) {
                    awardXpUseCase.award(-LevelSystem.XpRewards.FIRST_OF_DAY, LevelSystem.XpReasons.FIRST_OF_DAY + "_REVOKE", habitId)
                }

                val scheduledHabits = _state.value.habits
                val restingIds = _state.value.restingHabitIds
                val nonRestingHabits = scheduledHabits.filter { it.id !in restingIds }
                val logsForDayBeforeUncheck = logsCache.filter { it.dateEpochDay == day }.map { it.habitId }.toSet()
                val allDoneBefore = nonRestingHabits.all { it.id in logsForDayBeforeUncheck }
                if (allDoneBefore && nonRestingHabits.isNotEmpty()) {
                    awardXpUseCase.award(-LevelSystem.XpRewards.PERFECT_DAY, LevelSystem.XpReasons.PERFECT_DAY + "_REVOKE")
                }
            }
            WidgetUpdateHelper.updateAll(context)
        }
    }

    private fun updateHabitValue(habitId: Long, newValue: Float) {
        viewModelScope.launch {
            val day = _state.value.selectedEpochDay
            val existing = habitLogDao.getLogForDay(habitId, day)
            
            if (newValue <= 0f) {
                if (existing != null) {
                    habitLogDao.deleteLog(existing)
                    
                    // Revoke completion XP
                    val logsToday = logsCache.count { it.dateEpochDay == day }
                    awardXpUseCase.award(-LevelSystem.XpRewards.HABIT_COMPLETE, LevelSystem.XpReasons.HABIT_COMPLETE + "_REVOKE", habitId)
                    if (logsToday == 1) {
                        awardXpUseCase.award(-LevelSystem.XpRewards.FIRST_OF_DAY, LevelSystem.XpReasons.FIRST_OF_DAY + "_REVOKE", habitId)
                    }

                    val scheduledHabits = _state.value.habits
                    val restingIds = _state.value.restingHabitIds
                    val nonRestingHabits = scheduledHabits.filter { it.id !in restingIds }
                    val logsForDayBeforeUncheck = logsCache.filter { it.dateEpochDay == day }.map { it.habitId }.toSet()
                    val allDoneBefore = nonRestingHabits.all { it.id in logsForDayBeforeUncheck }
                    if (allDoneBefore && nonRestingHabits.isNotEmpty()) {
                        awardXpUseCase.award(-LevelSystem.XpRewards.PERFECT_DAY, LevelSystem.XpReasons.PERFECT_DAY + "_REVOKE")
                    }
                }
            } else {
                if (existing != null) {
                    habitLogDao.insertLog(existing.copy(value = newValue))
                } else {
                    habitLogDao.insertLog(HabitLogEntity(habitId = habitId, dateEpochDay = day, value = newValue))
                    
                    val logsToday = logsCache.count { it.dateEpochDay == day }
                    var xpAwarded = LevelSystem.XpRewards.HABIT_COMPLETE
                    awardXpUseCase.award(LevelSystem.XpRewards.HABIT_COMPLETE, LevelSystem.XpReasons.HABIT_COMPLETE, habitId)

                    if (logsToday == 0) {
                        awardXpUseCase.award(LevelSystem.XpRewards.FIRST_OF_DAY, LevelSystem.XpReasons.FIRST_OF_DAY, habitId)
                        xpAwarded += LevelSystem.XpRewards.FIRST_OF_DAY
                    }

                    val scheduledHabits = _state.value.habits
                    val restingIds = _state.value.restingHabitIds
                    val nonRestingHabits = scheduledHabits.filter { it.id !in restingIds }
                    val logsForDay = logsCache.filter { it.dateEpochDay == day }.map { it.habitId }.toSet() + habitId
                    val allDone = nonRestingHabits.all { it.id in logsForDay }
                    if (allDone && nonRestingHabits.isNotEmpty()) {
                        awardXpUseCase.award(LevelSystem.XpRewards.PERFECT_DAY, LevelSystem.XpReasons.PERFECT_DAY)
                        xpAwarded += LevelSystem.XpRewards.PERFECT_DAY
                    }

                    _state.value = _state.value.copy(xpPopAmount = xpAwarded)
                }
            }
            WidgetUpdateHelper.updateAll(context)
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
