package com.saynedesign.habitloop.receivers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.saynedesign.habitloop.MainActivity
import com.saynedesign.habitloop.R
import com.saynedesign.habitloop.data.UserPreferencesRepository
import com.saynedesign.habitloop.ui.screens.timer.OverlayReminderActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HabitReminderReceiver : BroadcastReceiver() {

    @Inject
    lateinit var preferencesRepository: UserPreferencesRepository

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val habitName = intent.getStringExtra("habitName") ?: "Habit Reminder"
                val habitId = intent.getLongExtra("habitId", -1)

                val isOverlayEnabled = preferencesRepository.isOverlayReminderEnabled.firstOrNull() ?: false
                val canDrawOverlay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Settings.canDrawOverlays(context)
                } else {
                    true
                }

                if (isOverlayEnabled && canDrawOverlay) {
                    val overlayIntent = Intent(context, OverlayReminderActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        putExtra("habitId", habitId)
                        putExtra("habitName", habitName)
                    }
                    context.startActivity(overlayIntent)
                } else {
                    showNotification(context, habitId, habitName)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showNotification(context: Context, habitId: Long, habitName: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "habit_reminders"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Habit Reminders", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("habitId", habitId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            habitId.toInt(),
            openIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Time for your habit!")
            .setContentText(habitName)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(habitId.toInt(), notification)
    }
}