package com.codesmithslabs.thedogtail.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.codesmithslabs.thedogtail.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

private val SBg = Color(0xFF1C202B)
private val STextPrimary = Color(0xFFFFFFFF)
private val STextDark = Color(0xFF1D1B20)
private val SAccent = Color(0xFF4B68FF)

/**
 * Widget 2: Streak & XP Summary
 */
class StreakSummaryWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val db = WidgetDatabaseProvider.getDatabase(context)
        val today = LocalDate.now()
        val todayEpoch = today.toEpochDay()
        val dayOfWeek = today.dayOfWeek.value

        val data = withContext(Dispatchers.IO) {
            val allHabits = db.habitDao().getAllHabitsOneShot()
            val todayLogs = db.habitLogDao().getLogsForDayOneShot(todayEpoch)
            val todayHabits = allHabits.filter { habit ->
                val days = habit.selectedDays.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
                days.contains(dayOfWeek)
            }
            val user = db.userDao().getUserOneShot()

            var streak = 0
            var checkDay = todayEpoch
            while (true) {
                val logsForDay = db.habitLogDao().getLogsForDayOneShot(checkDay)
                if (logsForDay.isNotEmpty()) { streak++; checkDay-- } else break
            }

            StreakData(
                completedToday = todayLogs.size,
                totalToday = todayHabits.size,
                currentStreak = streak,
                level = user?.currentLevel ?: 1,
                totalXp = user?.totalXp ?: 0
            )
        }

        provideContent {
            GlanceTheme {
                StreakSummaryContent(data)
            }
        }
    }
}

private data class StreakData(
    val completedToday: Int,
    val totalToday: Int,
    val currentStreak: Int,
    val level: Int,
    val totalXp: Int
)

@Composable
private fun StreakSummaryContent(data: StreakData) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(SBg))
            .padding(16.dp)
            .clickable(actionStartActivity<MainActivity>()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "🔥", style = TextStyle(fontSize = 32.sp))

        Spacer(modifier = GlanceModifier.height(4.dp))

        Text(
            text = "${data.currentStreak} Day Streak",
            style = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = ColorProvider(STextPrimary)
            )
        )

        Spacer(modifier = GlanceModifier.height(12.dp))

        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "⭐", style = TextStyle(fontSize = 18.sp))
                Text(
                    text = "Lvl ${data.level}",
                    style = TextStyle(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = ColorProvider(SAccent)
                    )
                )
            }

            Spacer(modifier = GlanceModifier.width(24.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "✨", style = TextStyle(fontSize = 18.sp))
                Text(
                    text = "${data.totalXp} XP",
                    style = TextStyle(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = ColorProvider(SAccent)
                    )
                )
            }

            Spacer(modifier = GlanceModifier.width(24.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "✅", style = TextStyle(fontSize = 18.sp))
                Text(
                    text = "${data.completedToday}/${data.totalToday}",
                    style = TextStyle(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = ColorProvider(SAccent)
                    )
                )
            }
        }
    }
}

class StreakSummaryWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = StreakSummaryWidget()
}
