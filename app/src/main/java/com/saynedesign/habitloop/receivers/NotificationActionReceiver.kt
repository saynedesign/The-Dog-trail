package com.saynedesign.habitloop.receivers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.saynedesign.habitloop.util.CompleteHabitUseCase
import com.saynedesign.habitloop.util.NotificationScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

    override fun onReceive(context: Context, intent: Intent) {
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
                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(habitId.toInt())
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_COMPLETE = "com.saynedesign.habitloop.action.COMPLETE_HABIT"
        const val ACTION_SNOOZE = "com.saynedesign.habitloop.action.SNOOZE_HABIT"
    }
}
