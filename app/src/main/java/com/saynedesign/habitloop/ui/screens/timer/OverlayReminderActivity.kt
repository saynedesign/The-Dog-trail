package com.saynedesign.habitloop.ui.screens.timer

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.saynedesign.habitloop.data.HabitDatabase
import com.saynedesign.habitloop.data.UserPreferencesRepository
import com.saynedesign.habitloop.data.isScheduledOn
import com.saynedesign.habitloop.ui.theme.TheDogTailTheme
import com.saynedesign.habitloop.util.LevelSystem
import com.saynedesign.habitloop.util.NotificationScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.media.Ringtone
import android.media.RingtoneManager
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import javax.inject.Inject

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

        // The shade notification only exists to launch this overlay from the
        // background. Now that the alarm UI is up, dismiss it so the user sees
        // the alarm rather than a duplicate notification.
        if (habitId != -1L) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            nm.cancel(habitId.toInt())
        }

        lifecycleScope.launch {
            val soundPref = preferencesRepository.overlayReminderSound.firstOrNull() ?: "alarm"
            val customUri = if (soundPref == "custom") preferencesRepository.customSoundUriOnce() else ""
            playSound(soundPref, customUri)
        }

        setContent {
            TheDogTailTheme {
                var data by remember { mutableStateOf<AlarmData?>(null) }
                var showXpPop by remember { mutableStateOf(false) }

                LaunchedEffect(habitId) {
                    withContext(Dispatchers.IO) {
                        data = computeAlarmData(habitId, habitName)
                    }
                }

                val onSnooze: () -> Unit = {
                    stopSound()
                    snoozeHabit(habitId, data?.habitTitle ?: habitName)
                    finish()
                }
                val onSkip: () -> Unit = {
                    stopSound()
                    finish()
                }
                val onComplete: () -> Unit = {
                    stopSound()
                    CoroutineScope(Dispatchers.IO).launch {
                        logCompletion(habitId)
                        withContext(Dispatchers.Main) { showXpPop = true }
                        delay(1600)
                        withContext(Dispatchers.Main) { finish() }
                    }
                }

                val accent = data?.accent ?: Color(0xFF7C5CFF)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF07080C))
                ) {
                    // Accent glow wash behind the header, tinted by the habit color.
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(420.dp)
                            .background(
                                Brush.verticalGradient(
                                    listOf(accent.copy(alpha = 0.22f), Color.Transparent)
                                )
                            )
                    )

                    AnimatedVisibility(visible = !showXpPop, enter = fadeIn(), exit = fadeOut()) {
                        val current = data
                        if (current == null) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = Color.White)
                            }
                        } else {
                            AlarmReminderScreen(current, onComplete, onSnooze, onSkip)
                        }
                    }

                    AnimatedVisibility(
                        visible = showXpPop,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut(),
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        XpPopContent(accent = accent)
                    }
                }
            }
        }
    }

    private fun playSound(soundPref: String, customUri: String) {
        if (soundPref == "mute") return
        try {
            val soundUri = when (soundPref) {
                "alarm" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                "notification" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                "ringtone" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                "custom" -> customUri.takeIf { it.isNotBlank() }?.let { android.net.Uri.parse(it) }
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                else -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            } ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            ringtone = RingtoneManager.getRingtone(applicationContext, soundUri)?.apply {
                // Alarm-like tones loop until the user acts.
                if (soundPref == "alarm" || soundPref == "ringtone" || soundPref == "custom") {
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
            ringtone?.let { if (it.isPlaying) it.stop() }
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
        val calendar = Calendar.getInstance().apply { add(Calendar.MINUTE, 15) }
        val snoozeTime = String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
        notificationScheduler.scheduleOneTimeReminder(
            habitId = habitId,
            habitName = name,
            scheduledDate = calendar.timeInMillis,
            time = snoozeTime
        )
    }

    private suspend fun logCompletion(habitId: Long) {
        completeHabitUseCase.complete(habitId)
    }

    /** Builds the alarm screen entirely from real data (habit, logs, user/level). */
    private suspend fun computeAlarmData(habitId: Long, fallbackName: String): AlarmData {
        val today = LocalDate.now()
        val todayEpoch = today.toEpochDay()

        val user = db.userDao().getUserOneShot()
        val habit = if (habitId != -1L) db.habitDao().getHabitById(habitId) else null

        val logs = if (habitId != -1L) db.habitLogDao().getLogsForHabitOneShot(habitId) else emptyList()
        val days = logs.map { it.dateEpochDay }.toSortedSet()

        // Current & best streak.
        var current = 0
        var cursor = if (days.contains(todayEpoch)) todayEpoch else todayEpoch - 1
        while (days.contains(cursor)) { current++; cursor-- }
        var best = 0; var run = 0; var prev: Long? = null
        for (d in days) {
            run = if (prev != null && d == prev + 1) run + 1 else 1
            if (run > best) best = run
            prev = d
        }

        // This-month consistency: completed vs scheduled days so far this month.
        val monthStart = today.withDayOfMonth(1)
        var scheduledSoFar = 0
        var completedSoFar = 0
        var d = monthStart
        while (!d.isAfter(today)) {
            val scheduled = habit?.isScheduledOn(d) ?: false
            if (scheduled) {
                scheduledSoFar++
                if (days.contains(d.toEpochDay())) completedSoFar++
            }
            d = d.plusDays(1)
        }
        val monthPercent = if (scheduledSoFar > 0) (completedSoFar * 100 / scheduledSoFar) else 0

        // "Estimated" tile adapts to the habit type so it's always real data.
        val (tile1Label, tile1Value) = when (habit?.type) {
            "TIMER" -> "Estimated" to "${habit.targetValue.toInt()} min"
            "NUMERIC" -> "Target" to "${habit.targetValue.toInt()} ${habit.unit}"
            else -> "Best Streak" to "$best day${if (best == 1) "" else "s"}"
        }

        // Level progress.
        val totalXp = user?.totalXp ?: 0
        val level = user?.currentLevel ?: 1
        val nextReq = LevelSystem.getNextLevelRequirement(totalXp)
        val xpToNext = if (nextReq == Int.MAX_VALUE) null else (nextReq - totalXp).coerceAtLeast(0)
        val levelProgress = LevelSystem.getProgressToNextLevel(totalXp)

        return AlarmData(
            userName = user?.name?.takeIf { it.isNotBlank() } ?: "You",
            habitTitle = habit?.title ?: fallbackName,
            habitIcon = habit?.icon ?: "",
            accent = habit?.color?.let { Color(it) } ?: Color(0xFF7C5CFF),
            tile1Label = tile1Label,
            tile1Value = tile1Value,
            xpReward = LevelSystem.XpRewards.HABIT_COMPLETE,
            currentStreak = current,
            monthPercent = monthPercent,
            level = level,
            levelName = LevelSystem.getLevelInfo(level).name,
            xpToNext = xpToNext,
            levelProgress = levelProgress,
            timeText = LocalDateTime.now().format(DateTimeFormatter.ofPattern("h:mm a"))
        )
    }
}

data class AlarmData(
    val userName: String,
    val habitTitle: String,
    val habitIcon: String,
    val accent: Color,
    val tile1Label: String,
    val tile1Value: String,
    val xpReward: Int,
    val currentStreak: Int,
    val monthPercent: Int,
    val level: Int,
    val levelName: String,
    val xpToNext: Int?,
    val levelProgress: Float,
    val timeText: String
)

private val CardBg = Color(0xFF14151C)
private val CardBorder = Color(0x14FFFFFF)
private val TextDim = Color(0xFF8B8FA3)

@Composable
fun AlarmReminderScreen(
    data: AlarmData,
    onComplete: () -> Unit,
    onSnooze: () -> Unit,
    onSkip: () -> Unit
) {
    val accent = data.accent
    val accentLight = lerp(accent, Color.White, 0.42f)
    val accentDeep = lerp(accent, Color.Black, 0.55f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(top = 8.dp, bottom = 28.dp)
    ) {
        // Top bar: time pill + close
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.06f))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Schedule, null, tint = accentLight, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(data.timeText, style = MaterialTheme.typography.labelLarge, color = Color.White, fontWeight = FontWeight.SemiBold)
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.06f))
                    .clickable { onSkip() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Close, "Dismiss", tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }

        Spacer(Modifier.height(18.dp))

        // Habit identity
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(84.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.14f))
                .border(2.dp, accent.copy(alpha = 0.55f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (data.habitIcon.isNotBlank()) {
                Text(data.habitIcon, fontSize = 34.sp)
            } else {
                Icon(Icons.Default.Check, null, tint = accentLight, modifier = Modifier.size(38.dp))
            }
        }

        Spacer(Modifier.height(14.dp))
        Text(
            "IT'S HABIT TIME",
            modifier = Modifier.align(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.labelLarge,
            color = accentLight,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
        Spacer(Modifier.height(6.dp))
        Text(
            data.habitTitle.uppercase(),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.displayMedium.copy(
                brush = Brush.verticalGradient(listOf(Color.White, accentLight))
            ),
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            lineHeight = 44.sp
        )
        Spacer(Modifier.height(10.dp))
        Text(
            buildString { append("Future ${data.userName} planned this moment. 💜") },
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.titleMedium,
            color = Color.White.copy(alpha = 0.75f),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(22.dp))

        // Quick stats strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(CardBg)
                .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
                .padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            QuickStat("🕒", data.tile1Label, data.tile1Value, Color.White, Modifier.weight(1f))
            StatDivider()
            QuickStat("⭐", "You'll Earn", "+${data.xpReward} XP", Color(0xFFFFC531), Modifier.weight(1f))
            StatDivider()
            QuickStat("🔥", "Current Streak", "${data.currentStreak} day${if (data.currentStreak == 1) "" else "s"}", Color.White, Modifier.weight(1f))
        }

        Spacer(Modifier.height(14.dp))

        // Month consistency card
        MonthCard(data, accent, accentLight)

        Spacer(Modifier.height(14.dp))

        // Level progress card
        LevelCard(data, accent, accentLight)

        Spacer(Modifier.height(22.dp))

        // Primary CTA
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.horizontalGradient(listOf(accent, accentLight)))
                .clickable { onComplete() },
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    "COMPLETE NOW",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = accentDeep
                )
            }
            Text(
                "Let's get it done!",
                style = MaterialTheme.typography.bodyMedium,
                color = accentDeep.copy(alpha = 0.85f)
            )
        }

        Spacer(Modifier.height(12.dp))

        // Secondary actions
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SecondaryAction("🕒", "Snooze 15 min", "Remind me later", accentLight, Modifier.weight(1f), onSnooze)
            SecondaryAction("»", "Skip Today", "I'll do it tomorrow", accentLight, Modifier.weight(1f), onSkip)
        }

        Spacer(Modifier.height(20.dp))
        Column(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("💜", fontSize = 16.sp)
            Spacer(Modifier.height(4.dp))
            Text("Small steps today.", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
            Text("Stronger you tomorrow.", style = MaterialTheme.typography.bodyMedium, color = accentLight)
        }
    }
}

@Composable
private fun QuickStat(emoji: String, label: String, value: String, valueColor: Color, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(emoji, fontSize = 18.sp)
        Spacer(Modifier.width(8.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextDim)
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = valueColor)
        }
    }
}

@Composable
private fun StatDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(34.dp)
            .background(Color.White.copy(alpha = 0.08f))
    )
}

@Composable
private fun MonthCard(data: AlarmData, accent: Color, accentLight: Color) {
    val (headline, sub) = when {
        data.monthPercent >= 75 -> "You're on fire! 🔥" to "Keep the momentum!"
        data.monthPercent >= 50 -> "Great momentum! 💪" to "Keep showing up!"
        data.monthPercent >= 25 -> "Building the habit! 🌱" to "One rep at a time!"
        else -> "Every rep counts! 👊" to "Let's turn it around!"
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(CardBg)
            .border(1.dp, CardBorder, RoundedCornerShape(22.dp))
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(headline, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(Modifier.height(6.dp))
            Text(
                "You've completed ${data.monthPercent}% of ${data.habitTitle} this month.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.75f)
            )
            Spacer(Modifier.height(8.dp))
            Text(sub, style = MaterialTheme.typography.bodyMedium, color = accentLight, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.width(12.dp))
        Box(modifier = Modifier.size(96.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val stroke = 10.dp.toPx()
                val dm = size.minDimension - stroke
                val tl = Offset((size.width - dm) / 2f, (size.height - dm) / 2f)
                val arc = Size(dm, dm)
                drawArc(Color.White.copy(alpha = 0.10f), -90f, 360f, false, tl, arc, style = Stroke(stroke, cap = StrokeCap.Round))
                drawArc(accent, -90f, 360f * (data.monthPercent / 100f), false, tl, arc, style = Stroke(stroke, cap = StrokeCap.Round))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        "${data.monthPercent}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        "%",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        modifier = Modifier.padding(top = 3.dp)
                    )
                }
                Text("This Month", style = MaterialTheme.typography.labelSmall, color = TextDim)
            }
        }
    }
}

@Composable
private fun LevelCard(data: AlarmData, accent: Color, accentLight: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(CardBg)
            .border(1.dp, CardBorder, RoundedCornerShape(22.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Shield-style level badge
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 26.dp, bottomEnd = 26.dp))
                .background(Brush.verticalGradient(listOf(accentLight, accent))),
            contentAlignment = Alignment.Center
        ) {
            Text("${data.level}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = Color.White)
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("LEVEL ${data.level}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = accentLight)
                Text("${(data.levelProgress * 100).toInt()}%", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Text(
                if (data.xpToNext != null) "${data.xpToNext} XP to Level ${data.level + 1}" else "Max level reached 🏆",
                style = MaterialTheme.typography.bodySmall,
                color = TextDim
            )
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.10f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(data.levelProgress.coerceIn(0f, 1f))
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(Brush.horizontalGradient(listOf(accent, accentLight)))
                )
            }
        }
    }
}

@Composable
private fun SecondaryAction(
    glyph: String,
    title: String,
    subtitle: String,
    accentLight: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(CardBg)
            .border(1.dp, CardBorder, RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(accentLight.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Text(glyph, fontSize = 15.sp, color = accentLight)
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = TextDim)
        }
    }
}

@Composable
private fun XpPopContent(accent: Color) {
    Box(
        modifier = Modifier
            .background(accent, RoundedCornerShape(24.dp))
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
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
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
