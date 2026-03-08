package com.codesmithslabs.thedogtail.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codesmithslabs.thedogtail.data.HabitDao
import com.codesmithslabs.thedogtail.data.HabitLogDao
import com.codesmithslabs.thedogtail.data.HabitLogEntity
import com.codesmithslabs.thedogtail.data.HabitEntity
import com.codesmithslabs.thedogtail.data.HabitRestDayDao
import com.codesmithslabs.thedogtail.data.HabitRestDayEntity
import com.codesmithslabs.thedogtail.data.UserDao
import com.codesmithslabs.thedogtail.util.AwardXpUseCase
import com.codesmithslabs.thedogtail.util.LevelSystem
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val habitDao: HabitDao,
    private val habitLogDao: HabitLogDao,
    private val habitRestDayDao: HabitRestDayDao,
    private val userDao: UserDao,
    private val awardXpUseCase: AwardXpUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HomeContract.State())
    val state: StateFlow<HomeContract.State> = _state.asStateFlow()

    private val _effect = Channel<HomeContract.Effect>()
    val effect = _effect.receiveAsFlow()

    private var logsCache: List<HabitLogEntity> = emptyList()
    private var allHabitsCache: List<HabitEntity> = emptyList()

    init {
        loadHabits()
        loadLogs()
        loadXp()
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
            }
        }
    }

    private fun loadXp() {
        viewModelScope.launch {
            userDao.getUser().collect { user ->
                _state.value = _state.value.copy(
                    totalXp = user?.totalXp ?: 0,
                    currentLevel = user?.currentLevel ?: 1
                )
            }
        }
    }

    private fun updateHabitsForSelectedDate() {
        val selectedDate = LocalDate.ofEpochDay(_state.value.selectedEpochDay)
        val dayOfWeek = selectedDate.dayOfWeek.value

        val filteredHabits = allHabitsCache.filter { habit ->
            val scheduledDays = habit.selectedDays.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
            scheduledDays.contains(dayOfWeek)
        }
        _state.value = _state.value.copy(habits = filteredHabits)
        // Also load rest day state for the selected date
        loadRestDayStateForSelectedDate()
    }

    private fun updateHabitLogsForSelectedDate() {
        val day = _state.value.selectedEpochDay
        val logsForDay = logsCache.asSequence()
            .filter { it.dateEpochDay == day }
            .associateBy { it.habitId }
        _state.value = _state.value.copy(habitLogs = logsForDay)
    }

    private fun loadRestDayStateForSelectedDate() {
        viewModelScope.launch {
            val epoch = _state.value.selectedEpochDay
            val restingIds = habitRestDayDao.getRestingHabitIdsForDay(epoch).toSet()
            _state.value = _state.value.copy(restingHabitIds = restingIds)
        }
    }

    private fun epochDayFor(fullDateString: String): Long {
        val formatter = DateTimeFormatter.ofPattern("EEE d", Locale.getDefault())
        val today = LocalDate.now()
        for (offset in -30..0) {
            val date = today.plusDays(offset.toLong())
            if (date.format(formatter) == fullDateString) {
                return date.toEpochDay()
            }
        }
        return today.toEpochDay()
    }

    fun handleEvent(event: HomeContract.Event) {
        when (event) {
            is HomeContract.Event.OnAddHabitClicked -> {
                sendEffect(HomeContract.Effect.NavigateToAddHabit)
            }
            is HomeContract.Event.OnHabitClicked -> {
                sendEffect(HomeContract.Effect.NavigateToHabitDetails(event.habitId))
            }
            is HomeContract.Event.OnDateSelected -> {
                val epoch = epochDayFor(event.date)
                _state.value = _state.value.copy(selectedDate = event.date, selectedEpochDay = epoch)
                updateHabitsForSelectedDate()
                updateHabitLogsForSelectedDate()
            }
            is HomeContract.Event.OnToggleHabit -> {
                toggleHabit(event.habitId, event.isDone)
            }
            is HomeContract.Event.OnUpdateHabitValue -> {
                updateHabitValue(event.habitId, event.newValue)
            }
            is HomeContract.Event.OnTimerClicked -> {
                sendEffect(HomeContract.Effect.NavigateToTimer(event.habitId))
            }
            is HomeContract.Event.OnProfileClicked -> {
                _state.value = _state.value.copy(currentTab = HomeContract.HomeTab.PROFILE)
            }
            is HomeContract.Event.OnMoodClicked -> {
                _state.value = _state.value.copy(currentTab = HomeContract.HomeTab.MOOD)
            }
            is HomeContract.Event.OnReportClicked -> {
                _state.value = _state.value.copy(currentTab = HomeContract.HomeTab.REPORT)
            }
            is HomeContract.Event.OnHomeClicked -> {
                _state.value = _state.value.copy(currentTab = HomeContract.HomeTab.HABITS)
            }
            is HomeContract.Event.OnEditHabitClicked -> {
                _state.value = _state.value.copy(
                    showEditDialog = true,
                    selectedHabitId = event.habitId
                )
            }
            is HomeContract.Event.OnDeleteHabitClicked -> {
                _state.value = _state.value.copy(
                    showDeleteDialog = true,
                    selectedHabitId = event.habitId
                )
            }
            is HomeContract.Event.OnDismissDialog -> {
                _state.value = _state.value.copy(
                    showDeleteDialog = false,
                    showEditDialog = false,
                    selectedHabitId = null
                )
            }
            is HomeContract.Event.OnConfirmDelete -> {
                val habitId = _state.value.selectedHabitId
                if (habitId != null) {
                    viewModelScope.launch {
                        val habit = habitDao.getHabitById(habitId)
                        if (habit != null) {
                            habitDao.deleteHabit(habit)
                        }
                    }
                }
                _state.value = _state.value.copy(
                    showDeleteDialog = false,
                    selectedHabitId = null
                )
            }
            is HomeContract.Event.OnConfirmEdit -> {
                val habitId = _state.value.selectedHabitId
                if (habitId != null) {
                    sendEffect(HomeContract.Effect.NavigateToEditHabit(habitId))
                }
                _state.value = _state.value.copy(
                    showEditDialog = false,
                    selectedHabitId = null
                )
            }
            is HomeContract.Event.OnEditProfileRequested -> {
                sendEffect(HomeContract.Effect.NavigateToEditProfile)
            }
            is HomeContract.Event.OnPreferencesRequested -> {
                sendEffect(HomeContract.Effect.NavigateToPreferences)
            }
            is HomeContract.Event.OnAchievementsRequested -> {
                sendEffect(HomeContract.Effect.NavigateToAchievements)
            }
            // Rest Day
            is HomeContract.Event.OnRestDayRequested -> {
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
            is HomeContract.Event.OnConfirmRestDay -> {
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
                        // Award XP for taking a rest day
                        awardXpUseCase.award(
                            LevelSystem.XpRewards.REST_DAY,
                            LevelSystem.XpReasons.REST_DAY,
                            habitId
                        )
                        _state.value = _state.value.copy(xpPopAmount = LevelSystem.XpRewards.REST_DAY)
                        loadRestDayStateForSelectedDate()
                    }
                    _state.value = _state.value.copy(
                        showRestDaySheet = false,
                        restDayTargetHabitId = null
                    )
                }
            }
            is HomeContract.Event.OnDismissRestDaySheet -> {
                _state.value = _state.value.copy(
                    showRestDaySheet = false,
                    restDayTargetHabitId = null
                )
            }
            // XP
            is HomeContract.Event.OnXpPopDismissed -> {
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

                // Check if this is the first completion of the day
                val logsToday = logsCache.count { it.dateEpochDay == day }
                var xpAwarded = LevelSystem.XpRewards.HABIT_COMPLETE
                awardXpUseCase.award(LevelSystem.XpRewards.HABIT_COMPLETE, LevelSystem.XpReasons.HABIT_COMPLETE, habitId)

                if (logsToday == 0) {
                    // First of the day bonus
                    awardXpUseCase.award(LevelSystem.XpRewards.FIRST_OF_DAY, LevelSystem.XpReasons.FIRST_OF_DAY, habitId)
                    xpAwarded += LevelSystem.XpRewards.FIRST_OF_DAY
                }

                // Check for perfect day (all habits completed)
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
        }
    }

    private fun updateHabitValue(habitId: Long, newValue: Float) {
        viewModelScope.launch {
            val day = _state.value.selectedEpochDay
            val existing = habitLogDao.getLogForDay(habitId, day)
            if (existing != null) {
                habitLogDao.insertLog(existing.copy(value = newValue))
            } else {
                habitLogDao.insertLog(HabitLogEntity(habitId = habitId, dateEpochDay = day, value = newValue))
                
                // Track XP similarly to simple habits
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
    }

    private fun sendEffect(effect: HomeContract.Effect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}
