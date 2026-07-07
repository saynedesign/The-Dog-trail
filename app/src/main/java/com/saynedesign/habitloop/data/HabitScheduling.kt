package com.saynedesign.habitloop.data

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Single source of truth for "is this habit scheduled on this date?".
 *
 * Fixes two long-standing bugs where every call site filtered by day-of-week
 * only: one-time tasks showed up every day (and never expired), and habits
 * with an end date kept appearing after it passed.
 */
fun HabitEntity.isScheduledOn(date: LocalDate): Boolean {
    if (isOneTime) {
        val scheduled = scheduledDate ?: return false
        return Instant.ofEpochMilli(scheduled).atZone(ZoneId.systemDefault()).toLocalDate() == date
    }
    endDate?.let { end ->
        val endDay = Instant.ofEpochMilli(end).atZone(ZoneId.systemDefault()).toLocalDate()
        if (date.isAfter(endDay)) return false
    }
    return selectedDays.split(",").mapNotNull { it.trim().toIntOrNull() }.contains(date.dayOfWeek.value)
}
