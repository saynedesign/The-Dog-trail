package com.saynedesign.habitloop.ui.screens.habitdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saynedesign.habitloop.data.HabitDao
import com.saynedesign.habitloop.data.HabitEntity
import com.saynedesign.habitloop.data.HabitLogEntity
import com.saynedesign.habitloop.data.HabitRestDayDao
import com.saynedesign.habitloop.data.UserDao
import com.saynedesign.habitloop.data.XpEventDao
import com.saynedesign.habitloop.util.AwardXpUseCase
import com.saynedesign.habitloop.util.LevelSystem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.saynedesign.habitloop.data.HabitLogDao
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@HiltViewModel
class HabitDetailViewModel @Inject constructor(
    private val habitDao: HabitDao,
    private val habitLogDao: HabitLogDao,
    private val habitRestDayDao: HabitRestDayDao,
    private val xpEventDao: XpEventDao,
    private val userDao: UserDao,
    private val awardXpUseCase: AwardXpUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val habitId: Long = checkNotNull(savedStateHandle["habitId"]).toString().toLong()

    private val _state = MutableStateFlow(HabitDetailContract.State(isLoading = true))
    val state = _state.asStateFlow()

    private val _effect = Channel<HabitDetailContract.Effect>()
    val effect = _effect.receiveAsFlow()

    init {
        loadData()
        loadXp()
    }

    private fun loadXp() {
        viewModelScope.launch {
            xpEventDao.getXpForHabit(habitId).collect { xp ->
                _state.value = _state.value.copy(habitXp = xp)
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                val habit = habitDao.getHabitById(habitId)

                // Load rest day epochs for this habit
                habitRestDayDao.getRestDayEpochsForHabit(habitId).collect { restDayEpochsList ->
                    val restDayEpochs = restDayEpochsList.toSet()

                    habitLogDao.getLogsForHabit(habitId).collect { logs ->
                        val momentum = calculateMomentum(logs, restDayEpochs)
                        val streak = calculateStreak(logs, restDayEpochs)
                        val completions = logs.size
                        val totalVal = logs.sumOf { it.value?.toDouble() ?: 0.0 }.toFloat()
                        val consistency = calculateConsistency(logs, habit, restDayEpochs)
                        val strong = calculateStrongDays(logs, habit, restDayEpochs)
                        
                        val todayEpoch = LocalDate.now().toEpochDay()
                        val todayLog = logs.find { it.dateEpochDay == todayEpoch }

                        _state.value = _state.value.copy(
                            habit = habit,
                            logs = logs,
                            currentStreak = streak,
                            totalCompletions = completions,
                            totalValue = totalVal,
                            completionRate = consistency,
                            activeMomentum = momentum,
                            weeklyConsistency = calculateWeeklyConsistency(logs, habit, restDayEpochs),
                            strongDays = strong,
                            restDayEpochs = restDayEpochs,
                            isCompletedToday = todayLog != null,
                            todayLogValue = todayLog?.value ?: 0f,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, isError = true)
            }
        }
    }

    /**
     * Active Momentum: streak that skips through rest days.
     * Rest days don't add to count, but don't break it.
     */
    private fun calculateMomentum(logs: List<HabitLogEntity>, restDayEpochs: Set<Long>): Int {
        if (logs.isEmpty()) return 0

        val today = LocalDate.now().toEpochDay()
        val logDates = logs.map { it.dateEpochDay }.toSet()

        var momentum = 0
        var checkDate = today

        // Grace: if not done today, start from yesterday
        if (!logDates.contains(checkDate) && !restDayEpochs.contains(checkDate)) {
            checkDate--
        }

        while (true) {
            when {
                restDayEpochs.contains(checkDate) -> {
                    // Rest day: neutral — skip but don't break
                    checkDate--
                }
                logDates.contains(checkDate) -> {
                    momentum++
                    checkDate--
                }
                else -> break // Real missed day
            }
            if (momentum > 3650) break // Safety
        }
        return momentum
    }

    /**
     * Traditional streak (kept for display alongside momentum).
     * Rest days are neutral here too.
     */
    private fun calculateStreak(logs: List<HabitLogEntity>, restDayEpochs: Set<Long>): Int {
        return calculateMomentum(logs, restDayEpochs)
    }

    /**
     * Consistency: completed / (scheduled - rest days) over last 30 days.
     * Uses actual creation date as start bound.
     */
    private fun calculateConsistency(
        logs: List<HabitLogEntity>,
        habit: HabitEntity?,
        restDayEpochs: Set<Long>
    ): Int {
        if (logs.isEmpty() || habit == null) return 0
        val today = LocalDate.now()
        val createdDate = Instant.ofEpochMilli(habit.createdTimestamp)
            .atZone(ZoneId.systemDefault()).toLocalDate()
        val startDate = maxOf(createdDate, today.minusDays(29))

        var scheduled = 0
        var completed = 0
        val logDates = logs.map { it.dateEpochDay }.toSet()
        val scheduledDays = habit.selectedDays.split(",").mapNotNull { it.trim().toIntOrNull() }

        var d = startDate
        while (!d.isAfter(today)) {
            val epoch = d.toEpochDay()
            val dayOfWeek = d.dayOfWeek.value
            val isScheduled = scheduledDays.contains(dayOfWeek)

            if (isScheduled && !restDayEpochs.contains(epoch)) {
                scheduled++
                if (logDates.contains(epoch)) completed++
            }
            d = d.plusDays(1)
        }
        return if (scheduled > 0) (completed * 100 / scheduled) else 0
    }

    /**
     * Weekly consistency: 7-day rolling window, excluding rest days from denominator.
     */
    private fun calculateWeeklyConsistency(
        logs: List<HabitLogEntity>,
        habit: HabitEntity?,
        restDayEpochs: Set<Long>
    ): Int {
        if (habit == null) return 0
        val today = LocalDate.now()
        val startDate = today.minusDays(6)

        var scheduled = 0
        var completed = 0
        val logDates = logs.map { it.dateEpochDay }.toSet()
        val scheduledDays = habit.selectedDays.split(",").mapNotNull { it.trim().toIntOrNull() }

        var d = startDate
        while (!d.isAfter(today)) {
            val epoch = d.toEpochDay()
            val dayOfWeek = d.dayOfWeek.value
            val isScheduled = scheduledDays.contains(dayOfWeek)

            if (isScheduled && !restDayEpochs.contains(epoch)) {
                scheduled++
                if (logDates.contains(epoch)) completed++
            }
            d = d.plusDays(1)
        }
        return if (scheduled > 0) (completed * 100 / scheduled) else 0
    }

    /**
     * Strong Days: days where per-habit completion exists (for single habit view = log exists).
     * Counted over last 30 days, excluding rest days.
     */
    private fun calculateStrongDays(
        logs: List<HabitLogEntity>,
        habit: HabitEntity?,
        restDayEpochs: Set<Long>
    ): Int {
        if (habit == null) return 0
        val today = LocalDate.now()
        val startDate = today.minusDays(29)

        val logDates = logs.map { it.dateEpochDay }.toSet()
        var strong = 0

        var d = startDate
        while (!d.isAfter(today)) {
            val epoch = d.toEpochDay()
            if (!restDayEpochs.contains(epoch) && logDates.contains(epoch)) {
                strong++
            }
            d = d.plusDays(1)
        }
        return strong
    }

    fun handleEvent(event: HabitDetailContract.Event) {
        when (event) {
            HabitDetailContract.Event.OnBackClicked -> {
                viewModelScope.launch { _effect.send(HabitDetailContract.Effect.NavigateBack) }
            }
            HabitDetailContract.Event.OnDeleteClicked -> {
                viewModelScope.launch {
                    _state.value.habit?.let { habitDao.deleteHabit(it) }
                    _effect.send(HabitDetailContract.Effect.NavigateBack)
                }
            }
            HabitDetailContract.Event.OnEditClicked -> {
                viewModelScope.launch {
                    _effect.send(HabitDetailContract.Effect.NavigateToEdit(habitId))
                }
            }
            is HabitDetailContract.Event.OnToggleTodayCompletion -> {
                toggleTodayCompletion(event.isDone)
            }
            is HabitDetailContract.Event.OnUpdateTodayLogValue -> {
                updateTodayLogValue(event.value)
            }
        }
    }
    
    private fun toggleTodayCompletion(isDone: Boolean) {
        viewModelScope.launch {
            val todayEpoch = LocalDate.now().toEpochDay()
            val existing = habitLogDao.getLogForDay(habitId, todayEpoch)
            val currentState = _state.value
            val habitTarget = currentState.habit?.targetValue ?: 1f
            
            if (isDone && existing == null) {
                habitLogDao.insertLog(HabitLogEntity(habitId = habitId, dateEpochDay = todayEpoch, value = habitTarget))
                awardXpUseCase.award(LevelSystem.XpRewards.HABIT_COMPLETE, LevelSystem.XpReasons.HABIT_COMPLETE, habitId)
                // Reload
                loadData()
                loadXp()
            } else if (!isDone && existing != null) {
                habitLogDao.deleteLog(existing)
                awardXpUseCase.award(-LevelSystem.XpRewards.HABIT_COMPLETE, LevelSystem.XpReasons.HABIT_COMPLETE + "_REVOKE", habitId)
                // Reload
                loadData()
                loadXp()
            }
        }
    }
    
    private fun updateTodayLogValue(value: Float) {
        viewModelScope.launch {
            val todayEpoch = LocalDate.now().toEpochDay()
            val existing = habitLogDao.getLogForDay(habitId, todayEpoch)
            
            if (existing != null) {
                habitLogDao.insertLog(existing.copy(value = value))
            } else {
                habitLogDao.insertLog(HabitLogEntity(habitId = habitId, dateEpochDay = todayEpoch, value = value))
                awardXpUseCase.award(LevelSystem.XpRewards.HABIT_COMPLETE, LevelSystem.XpReasons.HABIT_COMPLETE, habitId)
            }
            // Reload
            loadData()
            loadXp()
        }
    }
}
