package com.codesmithslabs.thedogtail.ui.screens.home.habits

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.codesmithslabs.thedogtail.R
import com.codesmithslabs.thedogtail.ui.components.HabitCard
import com.codesmithslabs.thedogtail.ui.components.HomeHeader
import com.codesmithslabs.thedogtail.ui.screens.home.CalendarStrip
import com.codesmithslabs.thedogtail.util.LevelSystem
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsScreen(
    state: HabitsContract.State,
    onEvent: (HabitsContract.Event) -> Unit,
    modifier: Modifier = Modifier,
    innerPadding: PaddingValues = PaddingValues(0.dp)
) {
    val haptic = LocalHapticFeedback.current

    // Dialogs
    if (state.showDeleteDialog) {
        val habit = state.habits.find { it.id == state.selectedHabitId }
        AlertDialog(
            onDismissRequest = { onEvent(HabitsContract.Event.OnDismissDialog) },
            title = { Text(stringResource(R.string.home_delete_habit_title)) },
            text = {
                val habitName = habit?.title ?: stringResource(R.string.home_this_habit)
                Text(stringResource(R.string.home_delete_habit_message, habitName))
            },
            confirmButton = {
                TextButton(onClick = { onEvent(HabitsContract.Event.OnConfirmDelete) }) {
                    Text(stringResource(R.string.home_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(HabitsContract.Event.OnDismissDialog) }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    if (state.showEditDialog) {
        val habit = state.habits.find { it.id == state.selectedHabitId }
        AlertDialog(
            onDismissRequest = { onEvent(HabitsContract.Event.OnDismissDialog) },
            title = { Text(stringResource(R.string.home_edit_habit_title)) },
            text = {
                val habitName = habit?.title ?: stringResource(R.string.home_this_habit)
                Text(stringResource(R.string.home_edit_habit_message, habitName))
            },
            confirmButton = {
                TextButton(onClick = { onEvent(HabitsContract.Event.OnConfirmEdit) }) {
                    Text(stringResource(R.string.common_edit))
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(HabitsContract.Event.OnDismissDialog) }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    if (state.showRestDaySheet) {
        AlertDialog(
            onDismissRequest = { onEvent(HabitsContract.Event.OnDismissRestDaySheet) },
            title = { Text(stringResource(R.string.report_day_sun) + " Rest Day?") },
            text = {
                Text("Take a break! You have ${1 - state.restDaysUsedThisWeek} rest day left this week for this habit. It won't break your momentum.")
            },
            confirmButton = {
                TextButton(onClick = { onEvent(HabitsContract.Event.OnConfirmRestDay) }) {
                    Text("Take Rest Day", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(HabitsContract.Event.OnDismissRestDaySheet) }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                val levelInfo = LevelSystem.getLevelInfo(state.currentLevel)
                HomeHeader(
                    totalXp = state.totalXp,
                    levelEmoji = levelInfo.emoji,
                    onNotificationClick = { /* TODO */ },
                    modifier = Modifier.padding(horizontal = 0.dp)
                )
            }

            item {
                CalendarStrip(
                    selectedDate = state.selectedDate,
                    onDateSelected = { onEvent(HabitsContract.Event.OnDateSelected(it)) }
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.home_habits),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    // Simple + button
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                            .clickable { onEvent(HabitsContract.Event.OnAddHabitClicked) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.common_add),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            items(state.habits, key = { it.id }) { habit ->
                val log = state.habitLogs[habit.id]

                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = {
                        when (it) {
                            SwipeToDismissBoxValue.StartToEnd -> {
                                onEvent(HabitsContract.Event.OnEditHabitClicked(habit.id))
                                false
                            }
                            SwipeToDismissBoxValue.EndToStart -> {
                                onEvent(HabitsContract.Event.OnDeleteHabitClicked(habit.id))
                                false
                            }
                            SwipeToDismissBoxValue.Settled -> false
                        }
                    }
                )

                LaunchedEffect(dismissState.targetValue) {
                    if (dismissState.targetValue != SwipeToDismissBoxValue.Settled) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                }

                SwipeToDismissBox(
                    state = dismissState,
                    backgroundContent = {
                        val color = when (dismissState.targetValue) {
                            SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.primary
                            SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.error
                            else -> Color.Transparent
                        }
                        val alignment = when (dismissState.targetValue) {
                            SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                            SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                            else -> Alignment.CenterStart
                        }
                        val icon = when (dismissState.targetValue) {
                            SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Edit
                            SwipeToDismissBoxValue.EndToStart -> Icons.Default.Delete
                            else -> Icons.Default.Edit
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 8.dp)
                                .background(color, RoundedCornerShape(20.dp))
                                .padding(horizontal = 20.dp),
                            contentAlignment = alignment
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    },
                    content = {
                        HabitCard(
                            title = habit.title,
                            subtitle = when (habit.type) {
                                "NUMERIC" -> {
                                    val current = log?.value ?: 0f
                                    val target = if (habit.targetValue % 1.0 == 0.0) {
                                        habit.targetValue.toInt().toString()
                                    } else {
                                        habit.targetValue.toString()
                                    }
                                    val currentStr = if (current % 1.0 == 0.0) current.toInt().toString() else current.toString()
                                    "$currentStr / $target ${habit.unit}"
                                }
                                "TIMER" -> {
                                    val currentMinutes = log?.value?.toInt() ?: 0
                                    if (currentMinutes > 0) {
                                        stringResource(R.string.home_timer_minutes_completed, currentMinutes)
                                    } else {
                                        stringResource(R.string.home_timer_habit)
                                    }
                                }
                                else -> stringResource(R.string.home_simple_habit)
                            },
                            icon = when (habit.type) {
                                "NUMERIC" -> Icons.AutoMirrored.Filled.List
                                "TIMER" -> Icons.Default.Timer
                                else -> Icons.Default.Check
                            },
                            iconTint = Color(habit.color),
                            isResting = state.restingHabitIds.contains(habit.id),
                            onClick = {
                                if (habit.type == "TIMER") {
                                    onEvent(HabitsContract.Event.OnTimerClicked(habit.id))
                                } else {
                                    onEvent(HabitsContract.Event.OnHabitClicked(habit.id))
                                }
                            },
                            onLongClick = {
                                onEvent(HabitsContract.Event.OnRestDayRequested(habit.id))
                            },
                            rightContent = {
                                when (habit.type) {
                                    "NUMERIC" -> {
                                        val current = log?.value ?: 0f
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            IconButton(
                                                onClick = { onEvent(HabitsContract.Event.OnUpdateHabitValue(habit.id, current - 1)) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Remove,
                                                    contentDescription = stringResource(R.string.home_decrease),
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                            Text(
                                                text = if (current % 1.0 == 0.0) current.toInt().toString() else current.toString(),
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onBackground
                                            )

                                            IconButton(
                                                onClick = { onEvent(HabitsContract.Event.OnUpdateHabitValue(habit.id, current + 1)) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Add,
                                                    contentDescription = stringResource(R.string.home_increase),
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                    "TIMER" -> {
                                        IconButton(
                                            onClick = { onEvent(HabitsContract.Event.OnTimerClicked(habit.id)) },
                                            modifier = Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                                        ) {
                                            Icon(
                                                Icons.Default.PlayArrow,
                                                contentDescription = stringResource(R.string.home_start_timer),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                    else -> {
                                        Checkbox(
                                            checked = log != null,
                                            onCheckedChange = { isChecked ->
                                                onEvent(HabitsContract.Event.OnToggleHabit(habit.id, isChecked))
                                            },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = MaterialTheme.colorScheme.primary,
                                                uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                checkmarkColor = MaterialTheme.colorScheme.onPrimary
                                            )
                                        )
                                    }
                                }
                            }
                        )
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(120.dp))
            }
        }

        // Overlay for +XP floating animation
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding() + 64.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            AnimatedVisibility(
                visible = state.xpPopAmount != null,
                enter = scaleIn() + slideInVertically(initialOffsetY = { 100 }) + fadeIn(),
                exit = fadeOut(animationSpec = tween(500))
            ) {
                LaunchedEffect(state.xpPopAmount) {
                    if (state.xpPopAmount != null) {
                        delay(1500)
                        onEvent(HabitsContract.Event.OnXpPopDismissed)
                    }
                }
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text(
                        "+${state.xpPopAmount} XP",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
