package com.saynedesign.habitloop.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.saynedesign.habitloop.data.HabitDao
import com.saynedesign.habitloop.util.NotificationScheduler
import com.saynedesign.habitloop.widget.WidgetUpdateHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * AlarmManager alarms do not survive a reboot (or an app update). This receiver
 * re-schedules every enabled habit reminder from the database after
 * BOOT_COMPLETED / MY_PACKAGE_REPLACED, re-arms the midnight widget refresh,
 * and refreshes widgets so they show current data.
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var habitDao: HabitDao

    @Inject
    lateinit var notificationScheduler: NotificationScheduler

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED && action != Intent.ACTION_MY_PACKAGE_REPLACED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                habitDao.getAllHabitsOneShot()
                    .filter { it.reminderEnabled }
                    .forEach { habit ->
                        if (habit.isOneTime) {
                            habit.scheduledDate?.let { date ->
                                notificationScheduler.scheduleOneTimeReminder(
                                    habitId = habit.id,
                                    habitName = habit.title,
                                    scheduledDate = date,
                                    time = habit.reminderTime
                                )
                            }
                        } else {
                            val days = habit.selectedDays
                                .split(",")
                                .mapNotNull { it.trim().toIntOrNull() }
                                .toSet()
                            if (days.isNotEmpty()) {
                                notificationScheduler.scheduleReminder(
                                    habitId = habit.id,
                                    habitName = habit.title,
                                    time = habit.reminderTime,
                                    days = days
                                )
                            }
                        }
                    }

                MidnightRefreshReceiver.schedule(context)
                CoachCheckReceiver.schedule(context)
                WidgetUpdateHelper.updateAll(context)
            } catch (_: Exception) {
                // Never crash the boot broadcast
            } finally {
                pendingResult.finish()
            }
        }
    }
}
