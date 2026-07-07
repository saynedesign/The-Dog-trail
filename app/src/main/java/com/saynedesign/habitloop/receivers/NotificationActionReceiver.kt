package com.saynedesign.habitloop.receivers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.saynedesign.habitloop.data.HabitDao
import com.saynedesign.habitloop.data.HabitLogDao
import com.saynedesign.habitloop.data.HabitRestDayDao
import com.saynedesign.habitloop.data.HabitRestDayEntity
import com.saynedesign.habitloop.util.AwardXpUseCase
import com.saynedesign.habitloop.util.CompleteHabitUseCase
import com.saynedesign.habitloop.util.LevelSystem
import com.saynedesign.habitloop.util.NotificationScheduler
import com.saynedesign.habitloop.widget.WidgetUpdateHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

/**
 * Handles the inline action buttons on habit reminder notifications:
 * "Done" completes the habit through the unified path (full XP, widgets,
 * history), "Snooze" re-fires the reminder in 15 minutes. Both dismiss the
 * notification without opening the app.
 */
@AndroidEntryPoint
class NotificationActionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var completeHabitUseCase: CompleteHabitUseCase

    @Inject
    lateinit var notificationScheduler: NotificationScheduler

    @Inject
    lateinit var habitDao: HabitDao

    @Inject
    lateinit var habitLogDao: HabitLogDao

    @Inject
    lateinit var habitRestDayDao: HabitRestDayDao

    @Inject
    lateinit var awardXpUseCase: AwardXpUseCase

    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // App-wide coach action — no habitId attached
        if (intent.action == ACTION_REST_TODAY) {
            val notificationId = intent.getIntExtra("notificationId", -1)
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    declareRestToday(context)
                    if (notificationId != -1) notificationManager.cancel(notificationId)
                } finally {
                    pendingResult.finish()
                }
            }
            return
        }

        val habitId = intent.getLongExtra("habitId", -1L)
        if (habitId == -1L) return
        val habitName = intent.getStringExtra("habitName") ?: "Habit"

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (intent.action) {
                    ACTION_COMPLETE -> {
                        completeHabitUseCase.complete(habitId)
                    }
                    ACTION_SNOOZE -> {
                        val fireAt = Calendar.getInstance().apply { add(Calendar.MINUTE, 15) }
                        val time = String.format(
                            Locale.US, "%02d:%02d",
                            fireAt.get(Calendar.HOUR_OF_DAY),
                            fireAt.get(Calendar.MINUTE)
                        )
                        notificationScheduler.scheduleOneTimeReminder(
                            habitId = habitId,
                            habitName = habitName,
                            scheduledDate = fireAt.timeInMillis,
                            time = time
                        )
                    }
                }
                notificationManager.cancel(habitId.toInt())
            } finally {
                pendingResult.finish()
            }
        }
    }

    /**
     * Coach "Mark today as rest 🌿": declares a rest day for every habit that
     * is scheduled today and not yet completed, so streak math treats today
     * as protected. Awards the rest-day XP once.
     */
    private suspend fun declareRestToday(context: Context) {
        val today = LocalDate.now()
        val todayEpoch = today.toEpochDay()
        val dayOfWeek = today.dayOfWeek.value

        val loggedIds = habitLogDao.getLogsForDayOneShot(todayEpoch).map { it.habitId }.toSet()
        val restingIds = habitRestDayDao.getRestingHabitIdsForDay(todayEpoch).toSet()

        val toRest = habitDao.getAllHabitsOneShot().filter { habit ->
            habit.selectedDays.split(",").mapNotNull { it.trim().toIntOrNull() }.contains(dayOfWeek) &&
                habit.id !in loggedIds && habit.id !in restingIds
        }
        if (toRest.isEmpty()) return

        toRest.forEach { habit ->
            habitRestDayDao.declareRestDay(
                HabitRestDayEntity(habitId = habit.id, dateEpochDay = todayEpoch)
            )
        }
        awardXpUseCase.award(LevelSystem.XpRewards.REST_DAY, LevelSystem.XpReasons.REST_DAY)
        WidgetUpdateHelper.updateAll(context)
    }

    companion object {
        const val ACTION_COMPLETE = "com.saynedesign.habitloop.action.COMPLETE_HABIT"
        const val ACTION_SNOOZE = "com.saynedesign.habitloop.action.SNOOZE_HABIT"
        const val ACTION_REST_TODAY = "com.saynedesign.habitloop.action.REST_TODAY"
    }
}
