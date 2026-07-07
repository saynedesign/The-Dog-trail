package com.saynedesign.habitloop.util

import android.content.Context
import com.saynedesign.habitloop.data.HabitDao
import com.saynedesign.habitloop.data.HabitLogDao
import com.saynedesign.habitloop.data.HabitLogEntity
import com.saynedesign.habitloop.data.HabitRestDayDao
import com.saynedesign.habitloop.widget.WidgetUpdateHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The single source of truth for completing / un-completing a habit.
 *
 * Every completion path (home list, habit detail, timer, overlay reminder,
 * home-screen widgets, notification action buttons) MUST go through this class
 * so that logging, XP awarding (base + first-of-day + perfect-day), XP event
 * history and widget refresh stay consistent everywhere.
 */
@Singleton
class CompleteHabitUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val habitDao: HabitDao,
    private val habitLogDao: HabitLogDao,
    private val habitRestDayDao: HabitRestDayDao,
    private val awardXpUseCase: AwardXpUseCase
) {
    /**
     * @param changed whether the log state actually changed (idempotent guard —
     *  completing an already-completed habit is a no-op and awards nothing)
     * @param xpAwarded total XP granted by this call (for the +XP pop UI)
     */
    data class Result(val changed: Boolean, val xpAwarded: Int)

    /** Mark a habit completed for the given day (default: today). */
    suspend fun complete(
        habitId: Long,
        epochDay: Long = LocalDate.now().toEpochDay(),
        value: Float = 1f
    ): Result {
        val existing = habitLogDao.getLogForDay(habitId, epochDay)
        if (existing != null) {
            // Already completed — update the value if it differs (e.g. a second
            // timer session), but never award XP twice for the same day.
            if (existing.value != value) {
                habitLogDao.insertLog(existing.copy(value = value))
                WidgetUpdateHelper.updateAll(context)
            }
            return Result(changed = false, xpAwarded = 0)
        }

        val logsBefore = habitLogDao.getLogsForDayOneShot(epochDay)
        habitLogDao.insertLog(HabitLogEntity(habitId = habitId, dateEpochDay = epochDay, value = value))

        var xpAwarded = LevelSystem.XpRewards.HABIT_COMPLETE
        awardXpUseCase.award(LevelSystem.XpRewards.HABIT_COMPLETE, LevelSystem.XpReasons.HABIT_COMPLETE, habitId)

        if (logsBefore.isEmpty()) {
            awardXpUseCase.award(LevelSystem.XpRewards.FIRST_OF_DAY, LevelSystem.XpReasons.FIRST_OF_DAY, habitId)
            xpAwarded += LevelSystem.XpRewards.FIRST_OF_DAY
        }

        val completedIds = logsBefore.map { it.habitId }.toSet() + habitId
        if (isPerfectDay(epochDay, completedIds)) {
            awardXpUseCase.award(LevelSystem.XpRewards.PERFECT_DAY, LevelSystem.XpReasons.PERFECT_DAY)
            xpAwarded += LevelSystem.XpRewards.PERFECT_DAY
        }

        WidgetUpdateHelper.updateAll(context)
        return Result(changed = true, xpAwarded = xpAwarded)
    }

    /** Remove a habit's completion for the given day, revoking the matching XP. */
    suspend fun uncomplete(
        habitId: Long,
        epochDay: Long = LocalDate.now().toEpochDay()
    ): Result {
        val existing = habitLogDao.getLogForDay(habitId, epochDay)
            ?: return Result(changed = false, xpAwarded = 0)

        val logsBefore = habitLogDao.getLogsForDayOneShot(epochDay)
        val wasPerfect = isPerfectDay(epochDay, logsBefore.map { it.habitId }.toSet())

        habitLogDao.deleteLog(existing)

        awardXpUseCase.award(-LevelSystem.XpRewards.HABIT_COMPLETE, LevelSystem.XpReasons.HABIT_COMPLETE + "_REVOKE", habitId)
        if (logsBefore.size == 1) {
            awardXpUseCase.award(-LevelSystem.XpRewards.FIRST_OF_DAY, LevelSystem.XpReasons.FIRST_OF_DAY + "_REVOKE", habitId)
        }
        if (wasPerfect) {
            awardXpUseCase.award(-LevelSystem.XpRewards.PERFECT_DAY, LevelSystem.XpReasons.PERFECT_DAY + "_REVOKE")
        }

        WidgetUpdateHelper.updateAll(context)
        return Result(changed = true, xpAwarded = 0)
    }

    /** Set a numeric/timer habit's value; <= 0 un-completes, first positive value completes. */
    suspend fun setValue(
        habitId: Long,
        epochDay: Long = LocalDate.now().toEpochDay(),
        newValue: Float
    ): Result {
        if (newValue <= 0f) return uncomplete(habitId, epochDay)

        val existing = habitLogDao.getLogForDay(habitId, epochDay)
        return if (existing != null) {
            habitLogDao.insertLog(existing.copy(value = newValue))
            WidgetUpdateHelper.updateAll(context)
            Result(changed = true, xpAwarded = 0)
        } else {
            complete(habitId, epochDay, newValue)
        }
    }

    /** Toggle helper for checkbox-style callers. */
    suspend fun setCompleted(habitId: Long, epochDay: Long, completed: Boolean): Result {
        return if (completed) complete(habitId, epochDay) else uncomplete(habitId, epochDay)
    }

    /**
     * A day is "perfect" when every habit scheduled for that day (excluding
     * declared rest days) has a log. Mirrors the home-screen logic.
     */
    private suspend fun isPerfectDay(epochDay: Long, completedIds: Set<Long>): Boolean {
        val dayOfWeek = LocalDate.ofEpochDay(epochDay).dayOfWeek.value // 1=Mon..7=Sun
        val scheduled = habitDao.getAllHabitsOneShot().filter { habit ->
            habit.selectedDays.split(",").mapNotNull { it.trim().toIntOrNull() }.contains(dayOfWeek)
        }
        val restingIds = habitRestDayDao.getRestingHabitIdsForDay(epochDay).toSet()
        val nonResting = scheduled.filter { it.id !in restingIds }
        return nonResting.isNotEmpty() && nonResting.all { it.id in completedIds }
    }
}
