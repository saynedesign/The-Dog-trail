package com.saynedesign.habitloop.ui.screens.home.habits

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.ui.draw.clip
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Star
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.saynedesign.habitloop.ui.theme.isAppInDarkTheme
import com.saynedesign.habitloop.R
import com.saynedesign.habitloop.ui.components.HabitCard
import com.saynedesign.habitloop.ui.components.ProfileAvatar
import com.saynedesign.habitloop.ui.screens.home.CalendarStrip
import com.saynedesign.habitloop.util.LevelSystem
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

    val isDark = isAppInDarkTheme()

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Welcome Header
            item {
                val levelInfo = LevelSystem.getLevelInfo(state.currentLevel)
                val name = if (state.userName.isNotEmpty()) state.userName.split(" ").firstOrNull() ?: "Aman" else "Aman"
                
                val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                val greetingPrefix = when (hour) {
                    in 5..11 -> "Good Morning"
                    in 12..16 -> "Good Afternoon"
                    else -> "Good Evening"
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Profile picture (or consistent initials placeholder)
                    ProfileAvatar(
                        imageUri = state.profileImageUri,
                        name = state.userName,
                        modifier = Modifier.size(52.dp),
                        backgroundColor = if (isDark) Color(0xFF1E2230) else Color(0xFFF0F2FA),
                        contentColor = Color(0xFF4B68FF),
                        initialsFontSize = 20.sp
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "$greetingPrefix, $name! 👋",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color.Black
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Let's build something amazing today.",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDark) Color(0xFF8B93A6) else Color(0xFF757575)
                        )
                    }

                    // XP Badge
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFF222635) else Color(0xFFEEF1FF))
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🐾", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${state.totalXp} XP",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4B68FF)
                            )
                        }
                    }
                }
            }
            // 2. Motivation custom header based on MotivationStyle
            item {
                val totalHabits = state.habits.size
                val completedHabits = state.habits.count { habit ->
                    val log = state.habitLogs[habit.id]
                    log != null && (habit.type != "NUMERIC" && habit.type != "TIMER" || (log.value ?: 0f) >= habit.targetValue)
                }
                val progress = if (totalHabits > 0) completedHabits.toFloat() / totalHabits else 0f
                val motivationText = when {
                    progress == 0f -> "Start your habits today! 🚀"
                    progress > 0f && progress < 0.5f -> "Off to a good start! Keep it up 👍"
                    progress >= 0.5f && progress < 1f -> "Halfway there! Keep going 💪"
                    else -> "Perfect day! You've crushed all habits! 🎉"
                }
                val levelInfo = LevelSystem.getLevelInfo(state.currentLevel)

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    when (state.motivationStyle) {
                        com.saynedesign.habitloop.data.MotivationStyle.KEEPING_STREAKS -> {
                            // Streak Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF2E1C18) else Color(0xFFFFF3E0)),
                                border = BorderStroke(1.dp, if (isDark) Color(0xFF7E3B20) else Color(0xFFFFB74D))
                            ) {
                                Row(
                                    modifier = Modifier.padding(18.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .background(if (isDark) Color(0xFF4E2A1C) else Color(0xFFFFE0B2), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("🔥", fontSize = 28.sp)
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(
                                            text = "Active Day Streak",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isDark) Color(0xFFFFB74D) else Color(0xFFE65100),
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${state.currentStreak} Days Active",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (isDark) Color.White else Color.Black
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Keep the fire burning! Consistently perform habits daily.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isDark) Color(0xFFD7CCC8) else Color(0xFF5D4037)
                                        )
                                    }
                                }
                            }
                        }
                        com.saynedesign.habitloop.data.MotivationStyle.LEVELING_UP -> {
                            // Large XP / Level Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E2235) else Color(0xFFE8EAF6)),
                                border = BorderStroke(1.dp, if (isDark) Color(0xFF3F51B5) else Color(0xFFC5CAE9))
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .background(if (isDark) Color(0xFF283593) else Color(0xFFC5CAE9), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Star, null, tint = Color(0xFF3F51B5), modifier = Modifier.size(24.dp))
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "LEVEL PROGRESS",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF3F51B5),
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Level ${levelInfo.level} - ${levelInfo.name}",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isDark) Color.White else Color.Black
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(14.dp))
                                    val progressToNext = LevelSystem.getProgressToNextLevel(state.totalXp)
                                    LinearProgressIndicator(
                                        progress = { if (progressToNext.isNaN()) 0f else progressToNext },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(CircleShape),
                                        color = Color(0xFF3F51B5),
                                        trackColor = if (isDark) Color(0xFF222635) else Color(0xFFEEF1FF)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    val req = LevelSystem.getNextLevelRequirement(state.totalXp)
                                    Text(
                                        text = "${state.totalXp} / $req XP",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isDark) Color(0xFF8B93A6) else Color(0xFF757575)
                                    )
                                }
                            }
                        }
                        com.saynedesign.habitloop.data.MotivationStyle.ACHIEVEMENTS -> {
                            // Achievements / Badges Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E2824) else Color(0xFFE8F5E9)),
                                border = BorderStroke(1.dp, if (isDark) Color(0xFF2E7D32) else Color(0xFFC8E6C9))
                            ) {
                                Row(
                                    modifier = Modifier.padding(18.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .background(if (isDark) Color(0xFF1B5E20) else Color(0xFFC8E6C9), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("🏆", fontSize = 28.sp)
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(
                                            text = "UNLOCKED BADGES",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF2E7D32),
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${state.currentLevel} Badges Earned",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDark) Color.White else Color.Black
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Unlock special achievements for consistency. Every milestone matters!",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isDark) Color(0xFFA5D6A7) else Color(0xFF1B5E20)
                                        )
                                    }
                                }
                            }
                        }
                        com.saynedesign.habitloop.data.MotivationStyle.QUOTES -> {
                            // Quote Card
                            val quotes = listOf(
                                "We are what we repeatedly do. Excellence, then, is not an act, but a habit. - Aristotle" to "Aristotle",
                                "It is easier to prevent bad habits than to break them. - Benjamin Franklin" to "Benjamin Franklin",
                                "Your habits will determine your future. - Jack Canfield" to "Jack Canfield",
                                "Small daily improvements over time lead to stunning results. - Robin Sharma" to "Robin Sharma"
                            )
                            val index = 0 // Show first for stable rendering
                            val quotePair = quotes[index]

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF251C2C) else Color(0xFFF3E5F5)),
                                border = BorderStroke(1.dp, if (isDark) Color(0xFF7B1FA2) else Color(0xFFE1BEE7))
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Text(
                                        text = "DAILY INSPIRATION",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF7B1FA2),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "\"${quotePair.first.substringBefore(" -")}\"",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                        color = if (isDark) Color.White else Color.Black
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "— ${quotePair.second}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isDark) Color(0xFFCE93D8) else Color(0xFF7B1FA2),
                                        modifier = Modifier.align(Alignment.End)
                                    )
                                }
                            }
                        }
                        else -> {
                            // Default Progress Card + Level Badge Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Card(
                                    modifier = Modifier.weight(0.65f),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1C202B) else Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier.size(54.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(
                                                progress = { progress },
                                                modifier = Modifier.fillMaxSize(),
                                                color = Color(0xFF4B68FF),
                                                trackColor = if (isDark) Color(0xFF222635) else Color(0xFFEEF1FF),
                                                strokeWidth = 4.dp
                                            )
                                            Text(
                                                text = "$completedHabits/$totalHabits",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                                fontWeight = FontWeight.Bold,
                                                color = if (isDark) Color.White else Color.Black
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "Today's Progress",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                color = if (isDark) Color(0xFF8B93A6) else Color(0xFF757575)
                                            )
                                            Text(
                                                text = "$completedHabits of $totalHabits completed",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isDark) Color.White else Color.Black
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            LinearProgressIndicator(
                                                progress = { progress },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(4.dp)
                                                    .clip(CircleShape),
                                                color = Color(0xFF4B68FF),
                                                trackColor = if (isDark) Color(0xFF222635) else Color(0xFFEEF1FF)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = motivationText,
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                color = if (isDark) Color(0xFF8B93A6) else Color(0xFF757575),
                                                maxLines = 1,
                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }

                                Card(
                                    modifier = Modifier.weight(0.35f),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF222635) else Color(0xFFF3F5FF)),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .padding(12.dp)
                                            .fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF6C4BFF).copy(alpha = 0.12f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = null,
                                                tint = Color(0xFF6C4BFF),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Level ${levelInfo.level}",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF6C4BFF)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = levelInfo.name,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                            color = if (isDark) Color(0xFF8B93A6) else Color(0xFF757575),
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. Weekly Calendar Strip
            item {
                CalendarStrip(
                    selectedDate = state.selectedDate,
                    onDateSelected = { onEvent(HabitsContract.Event.OnDateSelected(it)) }
                )
            }

            // 4. Section Header
            item {
                val totalHabits = state.habits.size
                val completedHabits = state.habits.count { habit ->
                    val log = state.habitLogs[habit.id]
                    log != null && (habit.type != "NUMERIC" && habit.type != "TIMER" || (log.value ?: 0f) >= habit.targetValue)
                }
                val remaining = (totalHabits - completedHabits).coerceAtLeast(0)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Today's Habits",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color.Black
                        )
                        Text(
                            text = "$remaining remaining",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDark) Color(0xFF8B93A6) else Color(0xFF757575)
                        )
                    }

                    // "+ Add Habit" button in capsule layout
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFF222635) else Color(0xFFEEF1FF))
                            .clickable { onEvent(HabitsContract.Event.OnAddHabitClicked) }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Habit",
                                tint = Color(0xFF4B68FF),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Add Habit",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4B68FF)
                            )
                        }
                    }
                }
            }

            // 5. Habits List Items
            if (state.habits.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = stringResource(R.string.home_empty_habits_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = stringResource(R.string.home_empty_habits_message),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(
                                onClick = { onEvent(HabitsContract.Event.OnAddHabitClicked) },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.size(8.dp))
                                Text(
                                    text = stringResource(R.string.home_empty_habits_cta),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            } else {
                items(state.habits, key = { it.id }) { habit ->
                    val log = state.habitLogs[habit.id]
                    
                    val isCompleted = log != null && (habit.type != "NUMERIC" || (log.value ?: 0f) >= habit.targetValue)
                    
                    val subtitle = when (habit.type) {
                        "NUMERIC" -> {
                            val current = log?.value ?: 0f
                            val currentStr = if (current % 1f == 0f) current.toInt().toString() else current.toString()
                            val targetStr = if (habit.targetValue % 1f == 0f) habit.targetValue.toInt().toString() else habit.targetValue.toString()
                            "💧 $currentStr / $targetStr ${habit.unit} • ${habit.timeOfDay}"
                        }
                        "TIMER" -> {
                            val currentMinutes = log?.value?.toInt() ?: 0
                            "⏱️ $currentMinutes min • ${habit.timeOfDay}"
                        }
                        else -> {
                            "⏰ ${habit.timeOfDay}"
                        }
                    }
                    
                    val progressPercentage = if (!isCompleted && (habit.type == "NUMERIC" || habit.type == "TIMER")) {
                        val current = log?.value ?: 0f
                        ((current / habit.targetValue) * 100f).coerceIn(0f, 100f)
                    } else {
                        null
                    }
                    
                    val iconEmoji = habit.icon.ifEmpty { "🎯" }
                    val streak = state.habitStreaks[habit.id] ?: 0

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
                                subtitle = subtitle,
                                iconEmoji = iconEmoji,
                                color = Color(habit.color),
                                streak = streak,
                                isCompleted = isCompleted,
                                xpReward = 10,
                                progressPercentage = progressPercentage,
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
                                onCheckClick = {
                                    if (habit.type == "NUMERIC") {
                                        val current = log?.value ?: 0f
                                        if (current >= habit.targetValue) {
                                            onEvent(HabitsContract.Event.OnUpdateHabitValue(habit.id, 0f))
                                        } else {
                                            onEvent(HabitsContract.Event.OnUpdateHabitValue(habit.id, current + 1f))
                                        }
                                    } else {
                                        onEvent(HabitsContract.Event.OnToggleHabit(habit.id, !isCompleted))
                                    }
                                }
                            )
                        }
                    )
                }
            }

            // 6. BOTTOM CALLOUT / ENCOURAGEMENT CARD
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Color(0xFF1C202B) else Color(0xFFF5F7FF)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4B68FF).copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✨", fontSize = 18.sp)
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "You're doing great!",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Color.Black
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Consistency today, better tomorrow.",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isDark) Color(0xFF8B93A6) else Color(0xFF757575)
                            )
                        }
                        
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = if (isDark) Color(0xFF8B93A6) else Color(0xFF757575),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(innerPadding.calculateBottomPadding() + 80.dp))
            }
        }

        // Overlay for +XP floating animation.
        // Keep the last shown amount so the exit (fade-out) animation still
        // renders a real number instead of "+null XP" once the state clears.
        var lastXpAmount by remember { mutableStateOf(0) }
        LaunchedEffect(state.xpPopAmount) {
            val amount = state.xpPopAmount
            if (amount != null) {
                lastXpAmount = amount
                delay(1500)
                onEvent(HabitsContract.Event.OnXpPopDismissed)
            }
        }
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
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text(
                        "+$lastXpAmount XP",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Level-up celebration overlay
        LevelUpOverlay(
            level = state.levelUpToLevel,
            onDismiss = { onEvent(HabitsContract.Event.OnLevelUpDismissed) }
        )
    }
}

/**
 * Full-screen celebration shown when the user crosses into a new level.
 * Renders the level badge with a spring pop-in, the level name, and
 * auto-dismisses after a few seconds (also dismissible by tapping).
 */
@Composable
private fun LevelUpOverlay(
    level: Int?,
    onDismiss: () -> Unit
) {
    val isDark = isAppInDarkTheme()

    // Remember the last celebrated level so the fade-out still shows content.
    var lastLevel by remember { mutableStateOf(1) }
    LaunchedEffect(level) {
        if (level != null) {
            lastLevel = level
            delay(3000)
            onDismiss()
        }
    }

    val levelInfo = LevelSystem.getLevelInfo(lastLevel)
    val badgeScale by animateFloatAsState(
        targetValue = if (level != null) 1f else 0.6f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "levelup_badge_scale"
    )

    AnimatedVisibility(
        visible = level != null,
        enter = fadeIn(animationSpec = tween(200)),
        exit = fadeOut(animationSpec = tween(400))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
                .clickable(indication = null, interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }) { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(32.dp)
            ) {
                Text(
                    text = "LEVEL UP!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(24.dp))
                Image(
                    painter = painterResource(id = LevelSystem.getLevelDrawableRes(lastLevel)),
                    contentDescription = "Level ${levelInfo.level} badge",
                    modifier = Modifier
                        .size(160.dp)
                        .scale(badgeScale)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Level ${levelInfo.level}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.85f)
                )
                Text(
                    text = "${levelInfo.emoji} ${levelInfo.name}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color(0xFF8CA0FF) else Color(0xFFFFD54F)
                )
            }
        }
    }
}
