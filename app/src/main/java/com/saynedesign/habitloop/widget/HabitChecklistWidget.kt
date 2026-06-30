package com.saynedesign.habitloop.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.CheckBox
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
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
import com.saynedesign.habitloop.MainActivity
import com.saynedesign.habitloop.data.HabitEntity
import com.saynedesign.habitloop.data.HabitLogEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

private val WidgetBg = Color(0xFF1C202B)
private val WidgetTextPrimary = Color(0xFFFFFFFF)
private val WidgetTextSecondary = Color(0xFF8B93A6)
private val WidgetAccent = Color(0xFF4B68FF)
private val WidgetAccentSurface = Color(0xFF292E3B)
private val WidgetAccentMuted = Color(0xFF3D4566)

/**
 * Widget 1: Habit Checklist — Shows today's habits with checkboxes.
 * Users can mark habits as done directly from the home screen.
 */
class HabitChecklistWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val db = WidgetDatabaseProvider.getDatabase(context)
        val today = LocalDate.now()
        val dayOfWeek = today.dayOfWeek.value
        val todayEpoch = today.toEpochDay()

        val allHabits = withContext(Dispatchers.IO) { db.habitDao().getAllHabitsOneShot() }
        val todayHabits = allHabits.filter { habit ->
            val days = habit.selectedDays.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
            days.contains(dayOfWeek)
        }
        val todayLogs = withContext(Dispatchers.IO) { db.habitLogDao().getLogsForDayOneShot(todayEpoch) }
        val loggedHabitIds = todayLogs.map { it.habitId }.toSet()

        provideContent {
            GlanceTheme {
                HabitChecklistContent(todayHabits, loggedHabitIds)
            }
        }
    }
}

@Composable
private fun HabitChecklistContent(
    habits: List<HabitEntity>,
    loggedHabitIds: Set<Long>
) {
    val completed = habits.count { it.id in loggedHabitIds }
    val total = habits.size

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(WidgetBg))
            .padding(16.dp)
            .clickable(actionStartActivity<MainActivity>())
    ) {
        // Header
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Today's Habits",
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(WidgetTextPrimary)
                )
            )
            Spacer(modifier = GlanceModifier.defaultWeight())
            Text(
                text = "$completed/$total",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = ColorProvider(WidgetAccent)
                )
            )
        }

        Spacer(modifier = GlanceModifier.height(8.dp))

        // Progress bar background
        val progress = if (total > 0) completed.toFloat() / total else 0f
        LinearProgressIndicator(
            progress = progress,
            modifier = GlanceModifier.fillMaxWidth().height(6.dp),
            color = ColorProvider(WidgetAccent),
            backgroundColor = ColorProvider(WidgetAccentSurface)
        )

        Spacer(modifier = GlanceModifier.height(12.dp))

        val displayHabits = habits.take(5)
        if (displayHabits.isEmpty()) {
            Box(
                modifier = GlanceModifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No habits for today ✨",
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = ColorProvider(WidgetTextSecondary)
                    )
                )
            }
        } else {
            displayHabits.forEach { habit ->
                val isDone = habit.id in loggedHabitIds
                Row(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CheckBox(
                        checked = isDone,
                        onCheckedChange = actionRunCallback<ToggleHabitAction>(
                            actionParametersOf(
                                HabitIdKey to habit.id,
                                IsDoneKey to !isDone
                            )
                        ),
                        text = habit.title
                    )
                }
            }
            if (habits.size > 5) {
                Spacer(modifier = GlanceModifier.height(2.dp))
                Text(
                    text = "+${habits.size - 5} more",
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = ColorProvider(WidgetTextSecondary)
                    )
                )
            }
        }
    }
}

val HabitIdKey = ActionParameters.Key<Long>("habitId")
val IsDoneKey = ActionParameters.Key<Boolean>("isDone")

class ToggleHabitAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val habitId = parameters[HabitIdKey] ?: return
        val isDone = parameters[IsDoneKey] ?: return
        val todayEpoch = LocalDate.now().toEpochDay()

        val db = WidgetDatabaseProvider.getDatabase(context)
        withContext(Dispatchers.IO) {
            if (isDone) {
                val existing = db.habitLogDao().getLogForDay(habitId, todayEpoch)
                if (existing == null) {
                    db.habitLogDao().insertLog(
                        HabitLogEntity(habitId = habitId, dateEpochDay = todayEpoch, value = 1f)
                    )
                }
            } else {
                val existing = db.habitLogDao().getLogForDay(habitId, todayEpoch)
                if (existing != null) {
                    db.habitLogDao().deleteLog(existing)
                }
            }
        }

        HabitChecklistWidget().updateAll(context)
        StreakSummaryWidget().updateAll(context)
        QuickActionsWidget().updateAll(context)
    }
}

class HabitChecklistWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = HabitChecklistWidget()
}
