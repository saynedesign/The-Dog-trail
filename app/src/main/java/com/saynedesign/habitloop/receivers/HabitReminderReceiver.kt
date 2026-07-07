package com.saynedesign.habitloop.receivers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
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
                    val soundPref = preferencesRepository.overlayReminderSound.firstOrNull() ?: "alarm"
                    showNotification(context, habitId, habitName, soundPref)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun reminderSoundUri(soundPref: String): android.net.Uri? = when (soundPref) {
        "mute" -> null
        "alarm" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        "ringtone" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        else -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
    } ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

    private fun showNotification(context: Context, habitId: Long, habitName: String, soundPref: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Channel settings are immutable after creation, so the sound choice is
        // baked into the channel id — changing the tune switches channels.
        val channelId = "habit_reminders_$soundPref"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Drop the legacy channel that was created without an explicit sound
            notificationManager.deleteNotificationChannel("habit_reminders")

            if (notificationManager.getNotificationChannel(channelId) == null) {
                val channel = NotificationChannel(channelId, "Habit Reminders", NotificationManager.IMPORTANCE_HIGH)
                channel.enableVibration(true)
                if (soundPref == "mute") {
                    channel.setSound(null, null)
                } else {
                    val usage = when (soundPref) {
                        "alarm" -> AudioAttributes.USAGE_ALARM
                        "ringtone" -> AudioAttributes.USAGE_NOTIFICATION_RINGTONE
                        else -> AudioAttributes.USAGE_NOTIFICATION
                    }
                    channel.setSound(
                        reminderSoundUri(soundPref),
                        AudioAttributes.Builder()
                            .setUsage(usage)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                }
                notificationManager.createNotificationChannel(channel)
            }
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

        // Inline actions: complete or snooze without opening the app
        val completeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_COMPLETE
            putExtra("habitId", habitId)
            putExtra("habitName", habitName)
        }
        val completePending = PendingIntent.getBroadcast(
            context,
            (habitId * 10 + 1).toInt(),
            completeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_SNOOZE
            putExtra("habitId", habitId)
            putExtra("habitName", habitName)
        }
        val snoozePending = PendingIntent.getBroadcast(
            context,
            (habitId * 10 + 2).toInt(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Time for your habit!")
            .setContentText(habitName)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .addAction(0, "Done ✓", completePending)
            .addAction(0, "Snooze 15m", snoozePending)
            .setAutoCancel(true)

        // Pre-O devices ignore channel sound settings — set it on the notification
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            if (soundPref != "mute") {
                builder.setSound(reminderSoundUri(soundPref))
            }
            builder.setDefaults(NotificationCompat.DEFAULT_VIBRATE)
        }

        notificationManager.notify(habitId.toInt(), builder.build())
    }
}