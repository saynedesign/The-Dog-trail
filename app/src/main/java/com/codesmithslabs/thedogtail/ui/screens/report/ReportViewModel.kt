package com.codesmithslabs.thedogtail.ui.screens.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codesmithslabs.thedogtail.data.HabitDao
import com.codesmithslabs.thedogtail.data.HabitLogDao
import com.codesmithslabs.thedogtail.data.HabitRestDayDao
import com.codesmithslabs.thedogtail.data.UserDao
import com.codesmithslabs.thedogtail.data.XpEventDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val habitDao: HabitDao,
    private val habitLogDao: HabitLogDao,
    private val habitRestDayDao: HabitRestDayDao,
    private val userDao: UserDao,
    private val xpEventDao: XpEventDao
) : ViewModel() {

    private val _state = MutableStateFlow(ReportContract.State())
    val state: StateFlow<ReportContract.State> = _state.asStateFlow()

    init {
        loadData()
        loadXpData()
    }

    private fun loadXpData() {
        viewModelScope.launch {
            userDao.getUser().collect { user ->
                _state.value = _state.value.copy(
                    totalXp = user?.totalXp ?: 0,
                    currentLevel = user?.currentLevel ?: 1
                )
            }
        }
        viewModelScope.launch {
            val today = LocalDate.now()
            val weekStart = today.minusDays(6).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val weekEnd = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            xpEventDao.getXpInRange(weekStart, weekEnd).collect { weeklyXp ->
                _state.value = _state.value.copy(weeklyXp = weeklyXp)
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            combine(
                habitDao.getAllHabits(),
                habitLogDao.getAllLogs(),
                habitRestDayDao.getAllRestDays()
            ) { habits, logs, restDays ->
                data class CombinedData(
                    val habits: List<com.codesmithslabs.thedogtail.data.HabitEntity>,
                    val logs: List<com.codesmithslabs.thedogtail.data.HabitLogEntity>,
                    val restDays: List<com.codesmithslabs.thedogtail.data.HabitRestDayEntity>
                )
                CombinedData(habits, logs, restDays)
            }.collect { (habits, logs, restDays) ->
                val today = LocalDate.now()

                // Build rest day sets
                val restDaysByEpoch = restDays.groupBy { it.dateEpochDay }
                val restDayEpochsAll = restDays.map { it.dateEpochDay }.toSet()
                fun isRestDay(epoch: Long, habitId: Long): Boolean {
                    return restDaysByEpoch[epoch]?.any { it.habitId == habitId } == true
                }

                // Total Effort Points
                val totalEffortPoints = logs.size

                // Group logs by day
                val logsByDay = logs.groupBy { it.dateEpochDay }

                // --- Active Momentum (rest-neutral streak) ---
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

                // --- Calculate per-day stats ---
                val minDate = logs.minOfOrNull { it.dateEpochDay } ?: today.toEpochDay()
                val maxDate = today.toEpochDay()

                var totalScheduledSum = 0
                var totalCompletedSum = 0
                var strongDays = 0
                var perfectDays = 0

                // Weekly consistency (7-day window)
                val weekStart7 = today.minusDays(6).toEpochDay()
                var weekScheduled = 0
                var weekCompleted = 0

                val calendarStats = mutableListOf<ReportContract.CalendarDayStat>()
                val selectedMonthStart = _state.value.selectedMonth.withDayOfMonth(1)
                val selectedMonthEnd = _state.value.selectedMonth.plusMonths(1).withDayOfMonth(1).minusDays(1)

                for (dayEpoch in minDate..maxDate) {
                    val date = LocalDate.ofEpochDay(dayEpoch)
                    val dayOfWeek = date.dayOfWeek.value

                    val activeHabits = habits.filter {
                        val createdDate = java.time.Instant.ofEpochMilli(it.createdTimestamp).atZone(ZoneId.systemDefault()).toLocalDate()
                        !createdDate.isAfter(date) &&
                        it.selectedDays.split(",").mapNotNull { d -> d.trim().toIntOrNull() }.contains(dayOfWeek)
                    }

                    val nonRestingHabits = activeHabits.filter { !isRestDay(dayEpoch, it.id) }
                    val scheduledCount = nonRestingHabits.size
                    val completedCount = logsByDay[dayEpoch]?.count { log ->
                        nonRestingHabits.any { it.id == log.habitId }
                    } ?: 0

                    if (scheduledCount > 0) {
                        totalScheduledSum += scheduledCount
                        totalCompletedSum += completedCount

                        if (completedCount >= scheduledCount) {
                            perfectDays++
                        }
                        if (completedCount.toFloat() / scheduledCount >= 0.75f) {
                            strongDays++
                        }

                        if (dayEpoch >= weekStart7) {
                            weekScheduled += scheduledCount
                            weekCompleted += completedCount
                        }
                    }

                    if (!date.isBefore(selectedMonthStart) && !date.isAfter(selectedMonthEnd)) {
                        val rate = if (scheduledCount > 0) completedCount.toFloat() / scheduledCount else 0f
                        calendarStats.add(ReportContract.CalendarDayStat(date, rate.coerceIn(0f, 1f)))
                    }
                }

                val completionRate = if (totalScheduledSum > 0) (totalCompletedSum * 100 / totalScheduledSum) else 0
                val weeklyConsistencyScore = if (weekScheduled > 0) (weekCompleted * 100 / weekScheduled) else 0

                // Weekly Habit Counts (Bar Chart) — last 7 days
                val weeklyHabitCounts = (0..6).map { i ->
                    val date = today.minusDays((6 - i).toLong())
                    val epoch = date.toEpochDay()
                    val count = logsByDay[epoch]?.size ?: 0
                    ReportContract.DailyHabitCount(
                        dayLabel = date.dayOfMonth.toString(),
                        count = count,
                        isToday = date == today
                    )
                }

                // Monthly Completion Rates (Line Chart) — last 6 months
                val monthlyRates = (0..5).map { i ->
                    val monthDate = today.minusMonths((5 - i).toLong())
                    val startOfMonth = monthDate.withDayOfMonth(1)
                    val endOfMonth = monthDate.plusMonths(1).withDayOfMonth(1).minusDays(1)

                    var mScheduled = 0
                    var mCompleted = 0

                    for (dayEpoch in startOfMonth.toEpochDay()..endOfMonth.toEpochDay()) {
                        val date = LocalDate.ofEpochDay(dayEpoch)
                        val dayOfWeek = date.dayOfWeek.value
                        val activeHabits = habits.filter {
                            val createdDate = java.time.Instant.ofEpochMilli(it.createdTimestamp).atZone(ZoneId.systemDefault()).toLocalDate()
                            !createdDate.isAfter(date) &&
                            it.selectedDays.split(",").mapNotNull { d -> d.trim().toIntOrNull() }.contains(dayOfWeek)
                        }
                        val nonResting = activeHabits.filter { !isRestDay(dayEpoch, it.id) }
                        mScheduled += nonResting.size
                        mCompleted += logsByDay[dayEpoch]?.count { log ->
                            nonResting.any { it.id == log.habitId }
                        } ?: 0
                    }

                    val rate = if (mScheduled > 0) (mCompleted * 100 / mScheduled) else 0
                    ReportContract.MonthlyRate(
                        monthLabel = monthDate.format(DateTimeFormatter.ofPattern("MMM")),
                        rate = rate
                    )
                }

                _state.value = _state.value.copy(
                    isLoading = false,
                    currentStreak = activeMomentum,
                    completionRate = completionRate,
                    totalHabitsCompleted = totalEffortPoints,
                    totalPerfectDays = perfectDays,
                    weeklyHabitCounts = weeklyHabitCounts,
                    monthlyCompletionRates = monthlyRates,
                    calendarStats = calendarStats,
                    weeklyConsistencyScore = weeklyConsistencyScore,
                    activeMomentum = activeMomentum,
                    strongDays = strongDays,
                    totalEffortPoints = totalEffortPoints,
                    restDayEpochs = restDayEpochsAll
                )
            }
        }
    }

    fun handleEvent(event: ReportContract.Event) {
        when (event) {
            is ReportContract.Event.Refresh -> {
                loadData()
            }
            is ReportContract.Event.OnMonthChange -> {
                _state.value = _state.value.copy(selectedMonth = event.newMonth)
                loadData()
            }
        }
    }
}
