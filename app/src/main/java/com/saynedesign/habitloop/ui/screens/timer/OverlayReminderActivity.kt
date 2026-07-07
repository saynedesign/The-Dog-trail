package com.saynedesign.habitloop.ui.screens.timer

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saynedesign.habitloop.data.HabitDatabase
import com.saynedesign.habitloop.data.HabitEntity
import com.saynedesign.habitloop.data.HabitLogEntity
import com.saynedesign.habitloop.ui.theme.TheDogTailTheme
import com.saynedesign.habitloop.util.AwardXpUseCase
import com.saynedesign.habitloop.util.LevelSystem
import com.saynedesign.habitloop.util.NotificationScheduler
import com.saynedesign.habitloop.widget.WidgetUpdateHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.Calendar
import javax.inject.Inject
import com.saynedesign.habitloop.data.UserPreferencesRepository
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.firstOrNull
import android.media.Ringtone
import android.media.RingtoneManager

@AndroidEntryPoint
class OverlayReminderActivity : ComponentActivity() {

    @Inject
    lateinit var db: HabitDatabase

    @Inject
    lateinit var completeHabitUseCase: com.saynedesign.habitloop.util.CompleteHabitUseCase

    @Inject
    lateinit var notificationScheduler: NotificationScheduler

    @Inject
    lateinit var preferencesRepository: UserPreferencesRepository

    private var ringtone: Ringtone? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Wake screen and show over lockscreen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        val habitId = intent.getLongExtra("habitId", -1L)
        val habitName = intent.getStringExtra("habitName") ?: "Habit Reminder"

        lifecycleScope.launch {
            val soundPref = preferencesRepository.overlayReminderSound.firstOrNull() ?: "alarm"
            playSound(soundPref)
        }

        setContent {
            TheDogTailTheme {
                var habitState by remember { mutableStateOf<HabitEntity?>(null) }
                var showXpPop by remember { mutableStateOf(false) }

                LaunchedEffect(habitId) {
                    if (habitId != -1L) {
                        withContext(Dispatchers.IO) {
                            habitState = db.habitDao().getHabitById(habitId)
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedVisibility(
                        visible = !showXpPop,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut()
                    ) {
                        OverlayCard(
                            habit = habitState,
                            defaultName = habitName,
                            onSnooze = {
                                stopSound()
                                snoozeHabit(habitId, habitState?.title ?: habitName)
                                finish()
                            },
                            onSkip = {
                                stopSound()
                                finish()
                            },
                            onComplete = {
                                stopSound()
                                CoroutineScope(Dispatchers.IO).launch {
                                    logCompletion(habitId)
                                    withContext(Dispatchers.Main) {
                                        showXpPop = true
                                    }
                                    delay(1600)
                                    withContext(Dispatchers.Main) {
                                        finish()
                                    }
                                }
                            }
                        )
                    }

                    AnimatedVisibility(
                        visible = showXpPop,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut()
                    ) {
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp))
                                .padding(horizontal = 32.dp, vertical = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .background(Color.White.copy(alpha = 0.2f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "+${LevelSystem.XpRewards.HABIT_COMPLETE} XP",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Habit Logged Successfully!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun playSound(soundPref: String) {
        if (soundPref == "mute") return
        try {
            val soundUri = when (soundPref) {
                "alarm" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                "notification" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                "ringtone" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                else -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            } ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            ringtone = RingtoneManager.getRingtone(applicationContext, soundUri)?.apply {
                if (soundPref == "alarm" || soundPref == "ringtone") {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        isLooping = true
                    }
                }
                play()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopSound() {
        try {
            ringtone?.let {
                if (it.isPlaying) {
                    it.stop()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            ringtone = null
        }
    }

    override fun onStop() {
        super.onStop()
        stopSound()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopSound()
    }

    private fun snoozeHabit(habitId: Long, name: String) {
        val calendar = Calendar.getInstance().apply {
            add(Calendar.MINUTE, 15)
        }
        val snoozeTime = String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
        notificationScheduler.scheduleOneTimeReminder(
            habitId = habitId,
            habitName = name,
            scheduledDate = calendar.timeInMillis,
            time = snoozeTime
        )
    }

    private suspend fun logCompletion(habitId: Long) {
        // Unified path: log + full XP (incl. first-of-day / perfect-day) + widgets
        completeHabitUseCase.complete(habitId)
    }
}

@Composable
fun OverlayCard(
    habit: HabitEntity?,
    defaultName: String,
    onSnooze: () -> Unit,
    onSkip: () -> Unit,
    onComplete: () -> Unit
) {
    val accentColor = habit?.color?.let { Color(it) } ?: MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .wrapContentHeight(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    )
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Accent Icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                if (habit?.icon?.isNotBlank() == true) {
                    Text(text = habit.icon, fontSize = 28.sp)
                } else {
                    Icon(
                        imageVector = Icons.Default.Alarm,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Habit Title
            Text(
                text = habit?.title ?: defaultName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            // Habit Description / Motivation Card
            val why = habit?.description?.ifBlank { null }
            if (why != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = accentColor.copy(alpha = 0.08f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "\"$why\"",
                        style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "It's time to build your habit loop!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Actions Layout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Snooze Button
                OutlinedButton(
                    onClick = onSnooze,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                ) {
                    Text(text = "Snooze", fontWeight = FontWeight.SemiBold)
                }

                // Skip Button
                TextButton(
                    onClick = onSkip,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                ) {
                    Text(text = "Dismiss", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Done Button
            Button(
                onClick = onComplete,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor,
                    contentColor = if (accentColor.luminance() > 0.5f) Color.Black else Color.White
                )
            ) {
                Text(
                    text = "Complete (+10 XP)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

fun Color.luminance(): Float {
    val r = red
    val g = green
    val b = blue
    return 0.2126f * r + 0.7152f * g + 0.0722f * b
}
