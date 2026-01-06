package com.codesmithslabs.thedogtail.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.codesmithslabs.thedogtail.receivers.HabitReminderReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject

class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun scheduleReminder(habitId: Long, habitName: String, time: String, days: Set<Int>) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val (hour, minute) = time.split(":").map { it.toInt() }

        days.forEach { dayOfWeek -> // 1=Mon, 7=Sun in UI
            // Convert UI day (1=Mon..7=Sun) to Calendar day (2=Mon..1=Sun)
            // UI: 1(Mon), 2(Tue), 3(Wed), 4(Thu), 5(Fri), 6(Sat), 7(Sun)
            // Calendar: 2(Mon), 3(Tue), 4(Wed), 5(Thu), 6(Fri), 7(Sat), 1(Sun)
            val calendarDay = if (dayOfWeek == 7) Calendar.SUNDAY else dayOfWeek + 1

            val calendar = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, calendarDay)
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            // If time has passed for this week, schedule for next week
            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                calendar.add(Calendar.WEEK_OF_YEAR, 1)
            }

            val intent = Intent(context, HabitReminderReceiver::class.java).apply {
                putExtra("habitName", habitName)
                putExtra("habitId", habitId)
            }
            
            // Unique RequestCode: habitId * 100 + dayOfWeek (to avoid collision)
            val requestCode = (habitId * 100 + dayOfWeek).toInt() 

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } catch (e: SecurityException) {
                // Handle missing exact alarm permission
                e.printStackTrace()
            }
        }
    }
    
    fun cancelReminder(habitId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // Cancel for all possible days
        for (dayOfWeek in 1..7) {
             val intent = Intent(context, HabitReminderReceiver::class.java)
             val requestCode = (habitId * 100 + dayOfWeek).toInt()
             val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
    }
}