package com.saynedesign.habitloop.receivers

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.saynedesign.habitloop.MainActivity
import com.saynedesign.habitloop.R
import com.saynedesign.habitloop.data.UserPreferencesRepository
import com.saynedesign.habitloop.util.CoachEngine
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

/**
 * Fires the Coach Engine's daily evaluation (default 20:30). Posts at most one
 * coach notification per day, on a per-category channel so users can mute
 * warnings, check-ins, motivation or the digest independently.
 */
@AndroidEntryPoint
class CoachCheckReceiver : BroadcastReceiver() {

    @Inject
    lateinit var coachEngine: CoachEngine

    @Inject
    lateinit var preferencesRepository: UserPreferencesRepository

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val enabled = preferencesRepository.isCoachEnabled.firstOrNull() ?: true
                if (!enabled) return@launch

                val candidate = coachEngine.evaluate() ?: return@launch
                postNotification(context, candidate)
                coachEngine.recordShown(candidate.type)
            } catch (_: Exception) {
                // The coach must never crash a broadcast
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun postNotification(context: Context, candidate: CoachEngine.Candidate) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = when (candidate.category) {
                CoachEngine.Category.WARNING -> NotificationManager.IMPORTANCE_HIGH
                else -> NotificationManager.IMPORTANCE_DEFAULT
            }
            val channel = NotificationChannel(
                candidate.category.channelId,
                candidate.category.channelName,
                importance
            )
            notificationManager.createNotificationChannel(channel)
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val contentPending = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, candidate.category.channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(candidate.title)
            .setContentText(candidate.message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(candidate.message))
            .setContentIntent(contentPending)
            .setAutoCancel(true)

        if (candidate.includeRestAction) {
            val restIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                action = NotificationActionReceiver.ACTION_REST_TODAY
                putExtra("notificationId", NOTIFICATION_ID)
            }
            val restPending = PendingIntent.getBroadcast(
                context,
                NOTIFICATION_ID + 1,
                restIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(0, "Mark today as rest 🌿", restPending)
            builder.addAction(0, "I'm back — open app", contentPending)
        }

        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    companion object {
        private const val NOTIFICATION_ID = 991_000
        private const val REQUEST_CODE = 990_002

        /** Daily inexact repeating alarm at the next coach check time (20:30). */
        fun schedule(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                Intent(context, CoachCheckReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val firstFire = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 20)
                set(Calendar.MINUTE, 30)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                firstFire.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        }
    }
}
