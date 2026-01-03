package com.codesmithslabs.thedogtail.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codesmithslabs.thedogtail.data.HabitLogDao
import com.codesmithslabs.thedogtail.data.UserDao
import com.codesmithslabs.thedogtail.data.UserEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userDao: UserDao,
    private val habitLogDao: HabitLogDao
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileContract.State())
    val state = _state.asStateFlow()

    private val _effect = Channel<ProfileContract.Effect>()
    val effect = _effect.receiveAsFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                userDao.getUser(),
                habitLogDao.getAllLogs()
            ) { user, logs ->
                val today = LocalDate.now()
                val last30Start = today.minusDays(29)
                val last60Start = today.minusDays(59)
                val last30Days = logs.filter { LocalDate.ofEpochDay(it.dateEpochDay).isAfter(last30Start.minusDays(1)) }
                val prev30Days = logs.filter {
                    val d = LocalDate.ofEpochDay(it.dateEpochDay)
                    d.isAfter(last60Start.minusDays(1)) && d.isBefore(last30Start)
                }
                val daysWithCompletions = last30Days.map { LocalDate.ofEpochDay(it.dateEpochDay) }.distinct().size
                val consistency = ((daysWithCompletions / 30.0) * 100).toInt()
                val totalCompletions = logs.size
                val userLevel = 1 + (totalCompletions / 20)
                val yearlyGridData = computeWeeklyIntensity(logs, weeks = 48)
                val bestStreak = computeBestStreak(logs)
                val milestones = buildMilestones(bestStreak, totalCompletions)
                val insights = buildInsights(last30Days.size, prev30Days.size, bestStreak)
                val journalQuote = user?.journal ?: ""
                val journalUpdated = user?.journalLastUpdated?.let { millisToRelative(it) } ?: ""
                val userName = user?.name ?: ""
                val stateUpdate = _state.value.copy(
                    userName = userName,
                    userLevel = userLevel,
                    consistency = consistency,
                    yearlyGridData = yearlyGridData,
                    milestones = milestones,
                    insights = insights,
                    journalQuote = journalQuote,
                    journalLastUpdated = journalUpdated,
                    isLoading = false
                )
                stateUpdate
            }.collectLatest { updated ->
                _state.value = updated
            }
        }
    }

    private fun computeWeeklyIntensity(logs: List<com.codesmithslabs.thedogtail.data.HabitLogEntity>, weeks: Int): List<Int> {
        val today = LocalDate.now()
        return (0 until weeks).map { w ->
            val start = today.minusWeeks((weeks - 1 - w).toLong()).with(java.time.DayOfWeek.MONDAY)
            val end = start.plusDays(6)
            val count = logs.count {
                val d = LocalDate.ofEpochDay(it.dateEpochDay)
                !d.isBefore(start) && !d.isAfter(end)
            }
            when {
                count == 0 -> 0
                count <= 2 -> 1
                count <= 5 -> 2
                count <= 9 -> 3
                else -> 4
            }
        }
    }

    private fun computeBestStreak(logs: List<com.codesmithslabs.thedogtail.data.HabitLogEntity>): Int {
        if (logs.isEmpty()) return 0
        val days = logs.map { LocalDate.ofEpochDay(it.dateEpochDay) }.distinct().sorted()
        var best = 1
        var current = 1
        for (i in 1 until days.size) {
            val prev = days[i - 1]
            val curr = days[i]
            if (ChronoUnit.DAYS.between(prev, curr) == 1L) {
                current += 1
                if (current > best) best = current
            } else {
                current = 1
            }
        }
        return best
    }

    private fun buildMilestones(bestStreak: Int, totalCompletions: Int): List<ProfileContract.Milestone> {
        val ms = mutableListOf<ProfileContract.Milestone>()
        if (bestStreak >= 30) {
            ms += ProfileContract.Milestone("30 Day", "Streak Master", ProfileContract.IconType.FIRE, 0xFFFFAB91)
        } else if (bestStreak >= 7) {
            ms += ProfileContract.Milestone("7 Day", "Streak Starter", ProfileContract.IconType.FIRE, 0xFFFFAB91)
        }
        if (totalCompletions >= 100) {
            ms += ProfileContract.Milestone("100 Completions", "Consistency Hero", ProfileContract.IconType.BOOK, 0xFF9C27B0)
        }
        return ms
    }

    private fun buildInsights(currCount: Int, prevCount: Int, bestStreak: Int): List<ProfileContract.Insight> {
        val delta = currCount - prevCount
        val changePct = if (prevCount == 0) 100 else ((delta / prevCount.toDouble()) * 100).toInt()
        val isPositive = changePct >= 0
        return listOf(
            ProfileContract.Insight(
                title = "Daily Completions",
                value = String.format("%.2f", currCount / 30.0),
                unit = "/day",
                change = if (isPositive) "+$changePct%" else "$changePct%",
                isPositiveChange = isPositive,
                iconType = ProfileContract.IconType.SUN,
                color = 0xFF4F6DFE
            ),
            ProfileContract.Insight(
                title = "Active Days",
                value = currCount.toString(),
                unit = " in 30d",
                change = if (isPositive) "+$delta days" else "$delta days",
                isPositiveChange = isPositive,
                iconType = ProfileContract.IconType.FIRE,
                color = 0xFFFFAB91
            ),
            ProfileContract.Insight(
                title = "Best Streak",
                value = "Best streak: $bestStreak days",
                unit = "",
                change = "",
                isPositiveChange = true,
                iconType = ProfileContract.IconType.BOOK,
                color = 0xFF9C27B0,
                isWide = true
            )
        )
    }

    private fun millisToRelative(millis: Long): String {
        val days = ChronoUnit.DAYS.between(LocalDate.ofEpochDay(millis / 86_400_000), LocalDate.now())
        return when {
            days <= 0 -> "Updated today"
            days == 1L -> "Updated 1 day ago"
            else -> "Updated $days days ago"
        }
    }

    fun handleEvent(event: ProfileContract.Event) {
        when (event) {
            ProfileContract.Event.OnBackClicked -> {
                viewModelScope.launch { _effect.send(ProfileContract.Effect.NavigateBack) }
            }
            ProfileContract.Event.OnShareClicked -> {
                // TODO: Share logic
            }
            ProfileContract.Event.OnEditJournalClicked -> {
                viewModelScope.launch {
                    val user = userDao.getUserOneShot()
                    if (user != null) {
                        val updatedText = _state.value.journalQuote
                        userDao.updateJournal(user.id, updatedText, System.currentTimeMillis())
                    }
                }
            }
            ProfileContract.Event.OnViewAllMilestonesClicked -> {
                // TODO: View all
            }
        }
    }
}
