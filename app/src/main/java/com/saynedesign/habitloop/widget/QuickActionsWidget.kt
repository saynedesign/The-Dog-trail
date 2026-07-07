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
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
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
import com.saynedesign.habitloop.data.isScheduledOn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

/**
 * Widget 3: Quick Actions — Top 3 habits as one-tap buttons.
 */
class QuickActionsWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val db = WidgetDatabaseProvider.getDatabase(context)
        val today = LocalDate.now()
        val todayEpoch = today.toEpochDay()

        val data = withContext(Dispatchers.IO) {
            val allHabits = db.habitDao().getAllHabitsOneShot()
            val todayHabits = allHabits.filter { it.isScheduledOn(today) }.take(3)
            val todayLogs = db.habitLogDao().getLogsForDayOneShot(todayEpoch)
            val loggedIds = todayLogs.map { it.habitId }.toSet()
            Pair(todayHabits, loggedIds)
        }

        provideContent {
            GlanceTheme {
                QuickActionsContent(data.first, data.second)
            }
        }
    }
}

@Composable
private fun QuickActionsContent(
    habits: List<HabitEntity>,
    loggedHabitIds: Set<Long>
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(WidgetTheme.bg)
            .padding(16.dp)
    ) {
        Text(
            text = "⚡ Quick Actions",
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = WidgetTheme.textPrimary
            )
        )

        Spacer(modifier = GlanceModifier.height(12.dp))

        if (habits.isEmpty()) {
            Box(
                modifier = GlanceModifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No habits for today",
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = WidgetTheme.textSecondary
                    )
                )
            }
        } else {
            habits.forEach { habit ->
                val isDone = habit.id in loggedHabitIds
                val bgColor = if (isDone) WidgetTheme.accentDone else WidgetTheme.surface
                val textColor = if (isDone) WidgetTheme.onAccent else WidgetTheme.textPrimary

                Box(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .height(48.dp)
                        .background(bgColor)
                        .clickable(
                            actionRunCallback<QuickToggleAction>(
                                actionParametersOf(
                                    HabitIdKey to habit.id,
                                    IsDoneKey to !isDone
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isDone) "✅" else "⬜",
                            style = TextStyle(fontSize = 16.sp)
                        )
                        Spacer(modifier = GlanceModifier.width(8.dp))
                        Text(
                            text = habit.title,
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = textColor
                            ),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

class QuickToggleAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val habitId = parameters[HabitIdKey] ?: return
        val isDone = parameters[IsDoneKey] ?: return
        val todayEpoch = LocalDate.now().toEpochDay()

        // Unified completion path — same XP/streak behavior as in-app toggles.
        withContext(Dispatchers.IO) {
            context.widgetEntryPoint().completeHabitUseCase()
                .setCompleted(habitId, todayEpoch, isDone)
        }
    }
}

class QuickActionsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QuickActionsWidget()
}
