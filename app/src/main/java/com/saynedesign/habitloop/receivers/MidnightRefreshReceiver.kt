package com.saynedesign.habitloop.receivers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.saynedesign.habitloop.widget.WidgetUpdateHelper
import java.util.Calendar

/**
 * Refreshes all widgets shortly after midnight so they roll over to the new
 * day (checklist resets, streak/date counters update) without requiring the
 * user to open the app first.
 *
 * Uses an inexact repeating RTC alarm (non-wakeup): battery-friendly — if the
 * device is asleep at 00:05, the refresh fires as soon as it next wakes.
 */
class MidnightRefreshReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        WidgetUpdateHelper.updateAll(context)
    }

    companion object {
        private const val REQUEST_CODE = 990_001

        fun schedule(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                Intent(context, MidnightRefreshReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val firstFire = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 5)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            alarmManager.setInexactRepeating(
                AlarmManager.RTC,
                firstFire.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        }
    }
}
