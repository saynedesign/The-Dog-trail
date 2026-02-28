package com.codesmithslabs.thedogtail.ui.screens.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codesmithslabs.thedogtail.data.HabitDao
import com.codesmithslabs.thedogtail.data.HabitLogDao
import com.codesmithslabs.thedogtail.data.MoodDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val habitDao: HabitDao,
    private val habitLogDao: HabitLogDao,
    private val moodDao: MoodDao
) : ViewModel() {

    private val _state = MutableStateFlow(ReportContract.State())
    val state: StateFlow<ReportContract.State> = _state.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            combine(
                habitDao.getAllHabits(),
                habitLogDao.getAllLogs(),
                moodDao.getAllMoods()
            ) { habits, logs, moods ->
                Triple(habits, logs, moods)
            }.collect { (habits, logs, moods) ->
                val today = LocalDate.now()
                
                // 1. Calculate Total Habits Completed
                val totalCompleted = logs.size

                // 2. Calculate Daily Stats (Completion Rate, Perfect Days)
                // Group logs by day
                val logsByDay = logs.groupBy { it.dateEpochDay }
                
                var perfectDays = 0
                var currentStreak = 0
                var streakActive = true
                
                // Calculate streak backwards from today
                // Limit to last 365 days for performance if needed, but here we iterate backwards
                var checkDate = today
                while (true) {
                    val epoch = checkDate.toEpochDay()
                    val dayLogs = logsByDay[epoch] ?: emptyList()
                    
                    if (dayLogs.isNotEmpty()) {
                        currentStreak++
                        checkDate = checkDate.minusDays(1)
                    } else {
                        // If it's today and no logs yet, don't break streak, just ignore
                        if (checkDate == today) {
                            checkDate = checkDate.minusDays(1)
                            continue
                        }
                        break
                    }
                    if (currentStreak > 3650) break // Safety break
                }

                // Calculate Perfect Days and Total Scheduled
                // This is complex because habits change over time (creation date, deletion).
                // For MVP, we'll estimate based on currently active habits and their creation date.
                // A more robust solution would require a separate table for daily schedules or checking creation timestamps.
                
                // Simplified "Perfect Day": If (logs count >= active habits count) for that day
                // We will just count days where completion > 80% as "Perfect" for now to be lenient, 
                // or strict 100%. Let's go with strict but only for habits that existed then.
                // Since we don't have historical habit snapshots, we'll use "createdTimestamp".
                
                val minDate = logs.minOfOrNull { it.dateEpochDay } ?: today.toEpochDay()
                val maxDate = today.toEpochDay()
                
                var totalScheduledSum = 0
                var totalCompletedSum = 0

                // Calendar Stats for selected month
                val calendarStats = mutableListOf<ReportContract.CalendarDayStat>()
                val selectedMonthStart = _state.value.selectedMonth.withDayOfMonth(1)
                val selectedMonthEnd = _state.value.selectedMonth.plusMonths(1).withDayOfMonth(1).minusDays(1)

                for (dayEpoch in minDate..maxDate) {
                    val date = LocalDate.ofEpochDay(dayEpoch)
                    val dayOfWeek = date.dayOfWeek.value // 1=Mon, 7=Sun
                    
                    // Filter habits active on this day
                    val activeHabits = habits.filter { 
                        // created before or on this day
                        val createdDate = java.time.Instant.ofEpochMilli(it.createdTimestamp).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                        !createdDate.isAfter(date) &&
                        // scheduled for this day of week
                        it.selectedDays.split(",").mapNotNull { d -> d.trim().toIntOrNull() }.contains(dayOfWeek)
                    }
                    
                    val scheduledCount = activeHabits.size
                    val completedCount = logsByDay[dayEpoch]?.size ?: 0
                    
                    if (scheduledCount > 0) {
                        totalScheduledSum += scheduledCount
                        totalCompletedSum += completedCount
                        
                        if (completedCount >= scheduledCount) {
                            perfectDays++
                        }
                    }

                    // Populate Calendar Stats if in selected month
                    if (!date.isBefore(selectedMonthStart) && !date.isAfter(selectedMonthEnd)) {
                        val rate = if (scheduledCount > 0) completedCount.toFloat() / scheduledCount else 0f
                        calendarStats.add(ReportContract.CalendarDayStat(date, rate.coerceIn(0f, 1f)))
                    }
                }
                
                val completionRate = if (totalScheduledSum > 0) (totalCompletedSum * 100 / totalScheduledSum) else 0

                // 3. Weekly Habit Counts (Bar Chart)
                // Last 7 days including today
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

                // 4. Monthly Completion Rates (Line Chart)
                // Last 6 months
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
                            val createdDate = java.time.Instant.ofEpochMilli(it.createdTimestamp).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                            !createdDate.isAfter(date) &&
                            it.selectedDays.split(",").mapNotNull { d -> d.trim().toIntOrNull() }.contains(dayOfWeek)
                        }
                        mScheduled += activeHabits.size
                        mCompleted += logsByDay[dayEpoch]?.size ?: 0
                    }
                    
                    val rate = if (mScheduled > 0) (mCompleted * 100 / mScheduled) else 0
                    ReportContract.MonthlyRate(
                        monthLabel = monthDate.format(DateTimeFormatter.ofPattern("MMM")),
                        rate = rate
                    )
                }

                // 5. Weekly Moods (Mood Chart)
                val moodMap = moods.associateBy { 
                     java.time.Instant.ofEpochMilli(it.timestamp).atZone(java.time.ZoneId.systemDefault()).toLocalDate().toEpochDay()
                }
                
                val weeklyMoods = (0..6).map { i ->
                    val date = today.minusDays((6 - i).toLong())
                    val epoch = date.toEpochDay()
                    val mood = moodMap[epoch]
                    
                    val moodValue = when(mood?.moodType) {
                        "Great" -> 5
                        "Good" -> 4
                        "Okay" -> 3
                        "Not Good" -> 2
                        "Bad" -> 1
                        else -> 0
                    }
                    
                    ReportContract.DailyMood(
                        dayLabel = date.dayOfMonth.toString(),
                        moodValue = moodValue,
                        moodEmoji = mood?.moodEmoji ?: ""
                    )
                }

                _state.value = _state.value.copy(
                    isLoading = false,
                    currentStreak = currentStreak,
                    completionRate = completionRate,
                    totalHabitsCompleted = totalCompleted,
                    totalPerfectDays = perfectDays,
                    weeklyHabitCounts = weeklyHabitCounts,
                    monthlyCompletionRates = monthlyRates,
                    weeklyMoods = weeklyMoods,
                    calendarStats = calendarStats
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
                loadData() // Reload to update calendar stats
            }
        }
    }
}
