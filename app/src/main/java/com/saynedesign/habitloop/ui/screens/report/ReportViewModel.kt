package com.saynedesign.habitloop.ui.screens.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saynedesign.habitloop.data.HabitDao
import com.saynedesign.habitloop.data.HabitLogDao
import com.saynedesign.habitloop.data.HabitRestDayDao
import com.saynedesign.habitloop.data.UserDao
import com.saynedesign.habitloop.data.XpEventDao
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
                habitRestDayDao.getAllRestDays(),
                userDao.getUser()
            ) { habits, logs, restDays, user ->
                data class CombinedData(
                    val habits: List<com.saynedesign.habitloop.data.HabitEntity>,
                    val logs: List<com.saynedesign.habitloop.data.HabitLogEntity>,
                    val restDays: List<com.saynedesign.habitloop.data.HabitRestDayEntity>,
                    val user: com.saynedesign.habitloop.data.UserEntity?
                )
                CombinedData(habits, logs, restDays, user)
            }.collect { (habits, logs, restDays, user) ->
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
                val weeklyGoal = user?.weeklyGoal ?: 5
                val activeDaysInWeek = (0..6).count { i ->
                    val date = today.minusDays(i.toLong())
                    val epoch = date.toEpochDay()
                    (logsByDay[epoch]?.size ?: 0) > 0
                }
                val weeklyConsistencyScore = ((activeDaysInWeek * 100) / weeklyGoal).coerceIn(0, 100)

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
                    restDayEpochs = restDayEpochsAll,
                    weekStartsOn = user?.weekStartsOn ?: com.saynedesign.habitloop.data.WeekStartsOn.MONDAY
                )

                // Trigger JNI calculations asynchronously
                viewModelScope.launch(kotlinx.coroutines.Dispatchers.Default) {
                    _state.value = _state.value.copy(isEngineLoading = true)
                    try {
                        val hIds = habits.map { it.id }.toLongArray()
                        val hTitles = habits.map { it.title }.toTypedArray()
                        val hTargetValues = habits.map { it.targetValue }.toFloatArray()
                        val hUnits = habits.map { it.unit }.toTypedArray()
                        val hTypes = habits.map { it.type }.toTypedArray()
                        val hSelectedDays = habits.map { it.selectedDays }.toTypedArray()
                        val hFrequencies = habits.map { it.frequency }.toTypedArray()
                        val hTimesOfDay = habits.map { it.timeOfDay }.toTypedArray()
                        val hCreatedEpochDays = habits.map {
                            java.time.Instant.ofEpochMilli(it.createdTimestamp)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                                .toEpochDay()
                        }.toLongArray()

                        val logHabitIds = logs.map { it.habitId }.toLongArray()
                        val logDates = logs.map { it.dateEpochDay }.toLongArray()
                        val logValues = logs.map { it.value ?: 0f }.toFloatArray()

                        val todayEpochDay = LocalDate.now().toEpochDay()

                        val jsonResult = com.saynedesign.habitloop.data.InsightEngine.generateInsightsJson(
                            userName = user?.name.orEmpty(),
                            userDob = user?.dob.orEmpty(),
                            userHeight = user?.height ?: 0f,
                            userXp = user?.totalXp ?: 0,
                            userLevel = user?.currentLevel ?: 1,
                            userJournal = user?.journal.orEmpty(),
                            habitIds = hIds,
                            habitTitles = hTitles,
                            habitTargetValues = hTargetValues,
                            habitUnits = hUnits,
                            habitTypes = hTypes,
                            habitSelectedDays = hSelectedDays,
                            habitFrequencies = hFrequencies,
                            habitTimesOfDay = hTimesOfDay,
                            habitCreatedEpochDays = hCreatedEpochDays,
                            logHabitIds = logHabitIds,
                            logDates = logDates,
                            logValues = logValues,
                            todayEpochDay = todayEpochDay
                        )

                        val jsonObject = org.json.JSONObject(jsonResult)

                        fun parseStringArray(key: String): List<String> {
                            val arr = jsonObject.optJSONArray(key) ?: return emptyList()
                            return (0 until arr.length()).map { arr.getString(it) }
                        }

                        val highlightsList = parseStringArray("highlights")
                        val insightsList = parseStringArray("insights")
                        val advicesList = parseStringArray("advices")
                        val levelProj = jsonObject.optInt("levelProjectionDays", -1)

                        val enrichedAdvices = advicesList.toMutableList()
                        
                        when (user?.primaryGoal) {
                            com.saynedesign.habitloop.data.PrimaryGoal.FITNESS -> {
                                enrichedAdvices.add("Prioritize recovery: Ensure you allow enough sleep and rest days for muscle regeneration.")
                                enrichedAdvices.add("Stay hydrated: Tracking water intake is as critical as physical training.")
                            }
                            com.saynedesign.habitloop.data.PrimaryGoal.DISCIPLINE -> {
                                enrichedAdvices.add("Consistency over intensity: Keeping streaks alive is more important than massive daily efforts.")
                                enrichedAdvices.add("Tackle hard tasks early: Try scheduling your habits for the morning window.")
                            }
                            com.saynedesign.habitloop.data.PrimaryGoal.PRODUCTIVITY -> {
                                enrichedAdvices.add("Minimize distractions: Block out 90 minutes of focused work daily for maximum productivity.")
                                enrichedAdvices.add("Review weekly: Analyze your most productive times to align future tasks.")
                            }
                            com.saynedesign.habitloop.data.PrimaryGoal.STUDY -> {
                                enrichedAdvices.add("Active recall: Rather than just reading, test your memory to improve learning retention.")
                                enrichedAdvices.add("Use Pomodoro: Take short 5-minute breaks after 25 minutes of intense studying.")
                            }
                            com.saynedesign.habitloop.data.PrimaryGoal.MENTAL_HEALTH -> {
                                enrichedAdvices.add("Practice mindfulness: Even 5 minutes of meditation daily can significantly lower stress levels.")
                                enrichedAdvices.add("Mindful rest: Utilize your rest days guilt-free to recharge your mental battery.")
                            }
                            else -> {}
                        }

                        when (user?.experienceLevel) {
                            com.saynedesign.habitloop.data.ExperienceLevel.BEGINNER -> {
                                enrichedAdvices.add("Start small: Don't overload yourself with too many habits in the first few weeks.")
                            }
                            com.saynedesign.habitloop.data.ExperienceLevel.BUILDING -> {
                                enrichedAdvices.add("Habit stacking: Anchor new habits to existing ones to build strong momentum.")
                            }
                            com.saynedesign.habitloop.data.ExperienceLevel.CONSISTENT -> {
                                enrichedAdvices.add("Optimize routine: Fine-tune the timing of your habits to make them effortless.")
                            }
                            com.saynedesign.habitloop.data.ExperienceLevel.ADVANCED -> {
                                enrichedAdvices.add("Raise the stakes: Challenge yourself with more demanding targets to avoid stagnation.")
                            }
                            else -> {}
                        }

                        val userWeeklyGoal = user?.weeklyGoal ?: 5
                        if (userWeeklyGoal >= 6) {
                            enrichedAdvices.add("High weekly goal: You're aiming for extreme consistency. Make sure to schedule rest days to prevent burnout.")
                        }

                        val habitScoresList = mutableListOf<ReportContract.HabitScore>()
                        val scoresArr = jsonObject.optJSONArray("habitScores")
                        if (scoresArr != null) {
                            for (i in 0 until scoresArr.length()) {
                                val obj = scoresArr.getJSONObject(i)
                                habitScoresList.add(
                                    ReportContract.HabitScore(
                                        habitId = obj.optLong("habitId"),
                                        title = obj.optString("title"),
                                        grade = obj.optString("grade"),
                                        consistency7d = obj.optInt("consistency7d"),
                                        consistency30d = obj.optInt("consistency30d"),
                                        currentStreak = obj.optInt("currentStreak"),
                                        longestStreak = obj.optInt("longestStreak"),
                                        trend = obj.optString("trend"),
                                        ageDays = obj.optInt("ageDays")
                                    )
                                )
                            }
                        }

                        _state.value = _state.value.copy(
                            highlights = highlightsList,
                            insights = insightsList,
                            advices = enrichedAdvices,
                            habitScores = habitScoresList,
                            levelProjectionDays = levelProj,
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                        _state.value = _state.value.copy(isEngineLoading = false)
                    }
                }
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
