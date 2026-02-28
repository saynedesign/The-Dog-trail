package com.codesmithslabs.thedogtail.ui.screens.report

import com.codesmithslabs.thedogtail.data.MoodEntity
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
        val weeklyMoods: List<DailyMood> = emptyList(),
        val calendarStats: List<CalendarDayStat> = emptyList(),
        val selectedMonth: LocalDate = LocalDate.now()
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

    data class DailyMood(
        val dayLabel: String,
        val moodValue: Int, // 1-5 scale
        val moodEmoji: String
    )
    
    data class CalendarDayStat(
        val date: LocalDate,
        val completionRate: Float, // 0.0 to 1.0
        val isSelected: Boolean = false
    )
}
