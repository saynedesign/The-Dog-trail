package com.saynedesign.habitloop.ui.screens.report

import java.time.LocalDate

interface ReportContract {
    data class State(
        val isLoading: Boolean = false,
        val currentStreak: Int = 0,
        val completionRate: Int = 0,
        val totalHabitsCompleted: Int = 0,
        val totalPerfectDays: Int = 0,
        val weeklyHabitCounts: List<DailyHabitCount> = emptyList(),
        val monthlyCompletionRates: List<MonthlyRate> = emptyList(),
        val calendarStats: List<CalendarDayStat> = emptyList(),
        val selectedMonth: LocalDate = LocalDate.now(),
        // Optimistic Metrics
        val weeklyConsistencyScore: Int = 0,
        val activeMomentum: Int = 0,
        val strongDays: Int = 0,
        val totalEffortPoints: Int = 0,
        val restDayEpochs: Set<Long> = emptySet(),
                // XP
        val totalXp: Int = 0,
        val currentLevel: Int = 1,
        val weeklyXp: Int = 0,
        // Engine V2 Insights
        val highlights: List<String> = emptyList(),
        val insights: List<String> = emptyList(),
        val advices: List<String> = emptyList(),
        val habitScores: List<HabitScore> = emptyList(),
        val levelProjectionDays: Int = -1,
        val isEngineLoading: Boolean = false
    )

    data class HabitScore(
        val habitId: Long,
        val title: String,
        val grade: String,
        val consistency7d: Int,
        val consistency30d: Int,
        val currentStreak: Int,
        val longestStreak: Int,
        val trend: String,
        val ageDays: Int
    )

    sealed class Event {
        data object Refresh : Event()
        data class OnMonthChange(val newMonth: LocalDate) : Event()
    }

    data class DailyHabitCount(
        val dayLabel: String, // e.g., "16", "Mon"
        val count: Int,
        val isToday: Boolean = false
    )

    data class MonthlyRate(
        val monthLabel: String, // e.g., "Jul", "Aug"
        val rate: Int
    )

    data class CalendarDayStat(
        val date: LocalDate,
        val completionRate: Float, // 0.0 to 1.0
        val isSelected: Boolean = false
    )
}
