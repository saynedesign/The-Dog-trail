package com.saynedesign.habitloop.ui.screens.habitdetail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import com.saynedesign.habitloop.ui.theme.isAppInDarkTheme as isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saynedesign.habitloop.R
import com.saynedesign.habitloop.data.HabitEntity
import com.saynedesign.habitloop.data.HabitLogEntity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailScreen(
    state: HabitDetailContract.State,
    onEvent: (HabitDetailContract.Event) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Habit Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isSystemInDarkTheme()) Color.White else Color(0xFF1D1B20)
                    )
                },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (isSystemInDarkTheme()) Color(0xFF1E2230) else Color(0xFFF5F6FA))
                            .clickable { onEvent(HabitDetailContract.Event.OnBackClicked) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back),
                            tint = if (isSystemInDarkTheme()) Color.White else Color(0xFF1D1B20),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (isSystemInDarkTheme()) Color(0xFF1E2230) else Color(0xFFF5F6FA))
                            .clickable { onEvent(HabitDetailContract.Event.OnEditClicked) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.common_edit),
                            tint = if (isSystemInDarkTheme()) Color.White else Color(0xFF1D1B20),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.habit != null) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    item {
                        HabitMainCard(
                            habit = state.habit,
                            consistency = state.completionRate,
                            currentStreak = state.currentStreak
                        )
                    }

                    item {
                        StatsRowCard(
                            currentStreak = state.currentStreak,
                            habitXp = state.habitXp,
                            completionRate = state.completionRate,
                            totalCompletions = state.totalCompletions
                        )
                    }
                    
                    item {
                        TodayStatusActionCard(
                            habit = state.habit,
                            isCompletedToday = state.isCompletedToday,
                            todayLogValue = state.todayLogValue,
                            onToggleCompletion = { onEvent(HabitDetailContract.Event.OnToggleTodayCompletion(it)) },
                            onLogValueChanged = { onEvent(HabitDetailContract.Event.OnUpdateTodayLogValue(it)) }
                        )
                    }

                    item {
                        Last14DaysCard(
                            logs = state.logs,
                            selectedDaysCsv = state.habit.selectedDays,
                            restDayEpochs = state.restDayEpochs,
                            onEvent = onEvent
                        )
                    }

                    item {
                        YearlyGridCard(
                            habit = state.habit,
                            logs = state.logs,
                            totalCompletions = state.totalCompletions
                        )
                    }

                    item {
                        MilestonesSection(
                            currentStreak = state.currentStreak,
                            totalValue = state.totalValue,
                            unit = state.habit.unit,
                            totalCompletions = state.totalCompletions,
                            completionRate = state.completionRate,
                            habitXp = state.habitXp,
                            logs = state.logs,
                            restDayEpochs = state.restDayEpochs,
                            onEvent = onEvent
                        )
                    }

                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            } else {
                 Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.habit_detail_not_found))
                }
            }
        }
    }

    if (state.showFullHistorySheet && state.habit != null) {
        ModalBottomSheet(
            onDismissRequest = { onEvent(HabitDetailContract.Event.OnToggleHistorySheet(false)) },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "History: ${state.habit.title}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (state.logs.isEmpty()) {
                    Text(
                        text = "No history recorded yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 24.dp),
                        textAlign = TextAlign.Center
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.heightIn(max = 400.dp)
                    ) {
                        items(state.logs.sortedByDescending { it.dateEpochDay }) { log ->
                            val date = LocalDate.ofEpochDay(log.dateEpochDay)
                            val formatter = DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy")
                            val dateStr = date.format(formatter)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = dateStr,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Logged Value: ${formatValue(log.value ?: 0f)} ${state.habit.unit}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                val isGoalMet = if (state.habit.type == "YES_NO") {
                                    true
                                } else {
                                    if (state.habit.isAtLeast) {
                                        (log.value ?: 0f) >= state.habit.targetValue
                                    } else {
                                        (log.value ?: 0f) <= state.habit.targetValue
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isGoalMet) Color(0xFF4CAF50).copy(alpha = 0.15f) else Color(0xFFFF9800).copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (isGoalMet) "Completed ✅" else "In Progress ⏱️",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isGoalMet) Color(0xFF4CAF50) else Color(0xFFFF9800),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HabitMainCard(
    habit: HabitEntity,
    consistency: Int,
    currentStreak: Int
) {
    val isDark = isSystemInDarkTheme()
    val progress = consistency / 100f
    val userColor = Color(habit.color)
    
    val cardBackground = if (isDark) {
        Brush.linearGradient(
            colors = listOf(
                userColor.copy(alpha = 0.22f),
                Color(0xFF13112B)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                userColor.copy(alpha = 0.08f),
                Color.White
            )
        )
    }
    
    Card(
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBackground)
                .then(
                    if (!isDark) {
                        Modifier.border(1.dp, userColor.copy(alpha = 0.15f), RoundedCornerShape(28.dp))
                    } else Modifier
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Circular Progress Box
                    Box(
                        modifier = Modifier.size(130.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val trackColor = if (isDark) Color(0xFF1E2230) else Color(0xFFF5F6FA)
                        val strokeWidth = 10.dp
                        
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Draw track with a 260-degree sweep leaving a bottom gap
                            drawArc(
                                color = trackColor,
                                startAngle = 140f,
                                sweepAngle = 260f,
                                useCenter = false,
                                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                            )
                            
                            // Draw progress with gradient based on user selected color
                            val progressBrush = Brush.horizontalGradient(
                                colors = listOf(
                                    userColor,
                                    userColor.copy(alpha = 0.6f)
                                )
                            )
                            
                            drawArc(
                                brush = progressBrush,
                                startAngle = 140f,
                                sweepAngle = 260f * progress,
                                useCenter = false,
                                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                            )
                        }
                        
                        // Center icon/emoji
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(if (isDark) Color(0xFF25293E).copy(alpha = 0.5f) else userColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (habit.icon.isNotBlank()) {
                                Text(text = habit.icon, fontSize = 28.sp)
                            } else {
                                Text(
                                    text = habit.title.take(1).uppercase(),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color.White else userColor
                                )
                            }
                        }
                        
                        // Bottom streak badge overlay
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .offset(y = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isDark) Color(0xFF141622) else userColor.copy(alpha = 0.12f))
                                .border(
                                    width = 1.dp,
                                    color = if (isDark) Color(0xFF292E3B) else userColor.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🔥", fontSize = 10.sp)
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "$currentStreak",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (isDark) Color.White else userColor
                                    )
                                }
                                Text(
                                    text = "Day Streak",
                                    fontSize = 7.sp,
                                    color = if (isDark) Color(0xFF8B93A6) else userColor
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // Right: Title, Metadata, Callout Card
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = habit.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color.Black
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // Metadata row
                        val timeEmoji = when (habit.timeOfDay.uppercase()) {
                            "MORNING" -> "☀️"
                            "AFTERNOON" -> "🌤️"
                            "EVENING" -> "🌙"
                            else -> "⏱️"
                        }
                        val reminderStr = if (habit.reminderEnabled) habit.reminderTime else "Anytime"
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "$timeEmoji ${formatTimeOfDayLabel(habit.timeOfDay)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isDark) Color(0xFF8B93A6) else Color(0xFF757575)
                            )
                            Text(
                                text = "|",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isDark) Color(0xFF292E3B) else Color(0xFFE8EAF6)
                            )
                            Text(
                                text = "🔁 ${formatFrequencyLabel(habit.frequency)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isDark) Color(0xFF8B93A6) else Color(0xFF757575)
                            )
                            Text(
                                text = "|",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isDark) Color(0xFF292E3B) else Color(0xFFE8EAF6)
                            )
                            Text(
                                text = "⏰ $reminderStr",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isDark) Color(0xFF8B93A6) else Color(0xFF757575)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        // Streak motivator card
                        val annotatedCalloutText = buildAnnotatedString {
                            append("You've checked in ")
                            withStyle(style = SpanStyle(color = userColor, fontWeight = FontWeight.Bold)) {
                                append("$currentStreak days straight.")
                            }
                            append(" Keep the momentum going! 💪")
                        }
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isDark) Color(0xFF1E2230).copy(alpha = 0.5f) else userColor.copy(alpha = 0.12f))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🔥", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = annotatedCalloutText,
                                fontSize = 10.sp,
                                color = if (isDark) Color.White else userColor,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Bottom: Consistency Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Consistency",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isDark) Color(0xFF8B93A6) else Color(0xFF757575)
                        )
                        Text(
                            text = "$consistency%",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else userColor
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape)
                                .background(if (isDark) Color(0xFF1E2230) else Color(0xFFE8EAF6))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progress)
                                    .fillMaxHeight()
                                    .clip(CircleShape)
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(userColor, userColor.copy(alpha = 0.6f))
                                        )
                                    )
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$consistency% of your planned check-ins completed",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isDark) Color(0xFF8B93A6) else Color(0xFF757575)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatsRowCard(
    currentStreak: Int,
    habitXp: Int,
    completionRate: Int,
    totalCompletions: Int
) {
    val isDark = isSystemInDarkTheme()
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF1C202B) else Color.White
        ),
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (!isDark) {
                    Modifier.border(1.dp, Color(0xFFE8EAF6), RoundedCornerShape(24.dp))
                } else Modifier
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatMetricColumn(
                icon = Icons.Default.LocalFireDepartment,
                value = "$currentStreak",
                label = "Day Streak",
                iconColor = Color(0xFFFF7A00),
                modifier = Modifier.weight(1f)
            )
            
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
                    .background(if (isDark) Color(0xFF232836) else Color(0xFFE8EAF6))
            )
            
            StatMetricColumn(
                icon = Icons.Default.Star,
                value = "$habitXp",
                label = "Total XP",
                iconColor = Color(0xFF6C4BFF),
                modifier = Modifier.weight(1f)
            )
            
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
                    .background(if (isDark) Color(0xFF232836) else Color(0xFFE8EAF6))
            )
            
            StatMetricColumn(
                icon = Icons.Default.CheckCircle,
                value = "$completionRate%",
                label = "Success Rate",
                iconColor = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f)
            )
            
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
                    .background(if (isDark) Color(0xFF232836) else Color(0xFFE8EAF6))
            )
            
            StatMetricColumn(
                icon = Icons.Default.WaterDrop,
                value = "$totalCompletions",
                label = "Check-ins",
                iconColor = Color(0xFF2196F3),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatMetricColumn(
    icon: ImageVector,
    value: String,
    label: String,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    Column(
        modifier = modifier.padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (isDark) Color.White else Color(0xFF1D1B20)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isDark) Color(0xFF8B93A6) else Color(0xFF757575),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun TodayStatusActionCard(
    habit: HabitEntity,
    isCompletedToday: Boolean,
    todayLogValue: Float,
    onToggleCompletion: (Boolean) -> Unit,
    onLogValueChanged: (Float) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF1C202B) else Color.White
        ),
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (!isDark) {
                    Modifier.border(1.dp, Color(0xFFE8EAF6), RoundedCornerShape(24.dp))
                } else Modifier
            )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Today",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else Color.Black
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (habit.type == "NUMERIC" || habit.type == "TIMER") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isCompletedToday) Color(0xFF4CAF50) else Color(0xFFFF9800))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isCompletedToday) "Completed" else "In Progress",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = if (isCompletedToday) Color(0xFF4CAF50) else Color(0xFFFF9800)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${formatValue(todayLogValue)} / ${formatValue(habit.targetValue)} ${habit.unit}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color.Black
                        )
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        IconButton(
                            onClick = { onLogValueChanged((todayLogValue - 1f).coerceAtLeast(0f)) },
                            modifier = Modifier
                                .size(36.dp)
                                .background(if (isDark) Color(0xFF1E2230) else Color(0xFFF5F6FA), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Decrease Progress",
                                tint = if (isDark) Color.White else Color.Black
                            )
                        }
                        
                        IconButton(
                            onClick = { onLogValueChanged(todayLogValue + 1f) },
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFF4B68FF).copy(alpha = 0.12f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Increase Progress",
                                tint = Color(0xFF4B68FF)
                            )
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isCompletedToday) Color(0xFF4CAF50) else Color(0xFFFF9800))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isCompletedToday) "Completed" else "Pending",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = if (isCompletedToday) Color(0xFF4CAF50) else Color(0xFFFF9800)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isCompletedToday) "Great job! Keep it up." else "Complete your workout and earn 10 XP",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isDark) Color(0xFF8B93A6) else Color(0xFF757575)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Button(
                        onClick = { onToggleCompletion(!isCompletedToday) },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCompletedToday) Color(0xFF4CAF50) else Color(0xFF4B68FF),
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isCompletedToday) "Completed" else "Check In",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Last14DaysCard(
    logs: List<HabitLogEntity>,
    selectedDaysCsv: String,
    restDayEpochs: Set<Long>,
    onEvent: (HabitDetailContract.Event) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF1C202B) else Color.White
        ),
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (!isDark) {
                    Modifier.border(1.dp, Color(0xFFE8EAF6), RoundedCornerShape(24.dp))
                } else Modifier
            )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Last 14 Days",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color.Black
                )
                Text(
                    text = "View full history >",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF4B68FF),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onEvent(HabitDetailContract.Event.OnToggleHistorySheet(true)) }
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            val today = remember { LocalDate.now() }
            val last14Days = remember { (13 downTo 0).map { today.minusDays(it.toLong()) } }
            val logDates = remember(logs) { logs.map { it.dateEpochDay }.toSet() }
            val scheduledDays = remember(selectedDaysCsv) {
                selectedDaysCsv.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                last14Days.forEach { date ->
                    val dayName = date.dayOfWeek.name.take(1)
                    val epoch = date.toEpochDay()
                    val hasLog = logDates.contains(epoch)
                    val isScheduled = scheduledDays.contains(date.dayOfWeek.value)
                    val isFuture = date.isAfter(today)
                    val isToday = date.isEqual(today)
                    
                    val dotColor = when {
                        hasLog -> Color(0xFF4CAF50)
                        isFuture || isToday -> Color.Transparent
                        isScheduled && !restDayEpochs.contains(epoch) -> Color(0xFFF44336)
                        else -> Color.Transparent
                    }
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = dayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isDark) Color(0xFF8B93A6) else Color(0xFF757575),
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                                .then(
                                    if (dotColor == Color.Transparent) {
                                        Modifier.border(
                                            width = 1.5.dp,
                                            color = if (isDark) Color(0xFF8B93A6).copy(alpha = 0.5f) else Color(0xFFBDBDBD),
                                            shape = CircleShape
                                        )
                                    } else Modifier
                                )
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF4CAF50)))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Done", style = MaterialTheme.typography.labelSmall, color = if (isDark) Color(0xFF8B93A6) else Color(0xFF757575))
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFF44336)))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Missed", style = MaterialTheme.typography.labelSmall, color = if (isDark) Color(0xFF8B93A6) else Color(0xFF757575))
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .border(1.dp, if (isDark) Color(0xFF8B93A6) else Color(0xFFBDBDBD), CircleShape)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Not done", style = MaterialTheme.typography.labelSmall, color = if (isDark) Color(0xFF8B93A6) else Color(0xFF757575))
            }
        }
    }
}

@Composable
fun YearlyGridCard(
    habit: HabitEntity,
    logs: List<HabitLogEntity>,
    totalCompletions: Int
) {
    val isDark = isSystemInDarkTheme()
    val currentYear = LocalDate.now().year
    val createdDate = habit.createdTimestamp
    
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF1C202B) else Color.White
        ),
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (!isDark) {
                    Modifier.border(1.dp, Color(0xFFE8EAF6), RoundedCornerShape(24.dp))
                } else Modifier
            )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$currentYear Activity",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color.Black
                )
                Text(
                    text = "$totalCompletions Active Days",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(habit.color),
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            val today = LocalDate.now()
            val logDates = remember(logs) { logs.map { it.dateEpochDay }.toSet() }
            val createdEpochDay = remember(createdDate) {
                Instant.ofEpochMilli(createdDate)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                    .toEpochDay()
            }
            
            val emptyCellColor = if (isDark) Color(0xFF161922) else Color(0xFFE8EAF6)
            val completedColor = Color(habit.color)

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                items(52) { weekIndex ->
                      val weeksAgo = 51 - weekIndex
                      val startOfWeek = today.minusWeeks(weeksAgo.toLong())
                          .with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                      
                      Column(
                          verticalArrangement = Arrangement.spacedBy(4.dp),
                          modifier = Modifier.padding(horizontal = 2.dp)
                      ) {
                          repeat(7) { dayIndex ->
                              val date = startOfWeek.plusDays(dayIndex.toLong())
                              val epochDay = date.toEpochDay()
                              val log = logs.find { it.dateEpochDay == epochDay }
                              val isCompleted = log != null
                              val isFuture = date.isAfter(today)
                              val beforeHabitCreation = epochDay < createdEpochDay
                              
                              val cellColor = when {
                                  beforeHabitCreation || isFuture -> Color.Transparent
                                  isCompleted -> {
                                      if (habit.type == "YES_NO" || habit.targetValue <= 0f) {
                                          completedColor
                                      } else {
                                          val ratio = ((log?.value ?: 0f) / habit.targetValue).coerceIn(0f, 1f)
                                          if (ratio == 0f) emptyCellColor
                                          else completedColor.copy(alpha = (ratio * 0.8f + 0.2f).coerceIn(0.2f, 1f))
                                      }
                                  }
                                  else -> emptyCellColor
                              }
                              
                              Box(
                                  modifier = Modifier
                                      .size(11.dp)
                                      .clip(RoundedCornerShape(2.dp))
                                      .background(cellColor)
                                      .then(
                                          if (cellColor == Color.Transparent && !isFuture && !beforeHabitCreation) {
                                              Modifier.border(0.5.dp, emptyCellColor, RoundedCornerShape(2.dp))
                                          } else if (isFuture || beforeHabitCreation) {
                                              Modifier.border(0.5.dp, emptyCellColor.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
                                          } else Modifier
                                      )
                              )
                          }
                      }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Less",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isDark) Color(0xFF8B93A6) else Color(0xFF757575)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.size(11.dp).clip(RoundedCornerShape(2.dp)).background(emptyCellColor))
                Spacer(modifier = Modifier.width(4.dp))
                Box(modifier = Modifier.size(11.dp).clip(RoundedCornerShape(2.dp)).background(completedColor.copy(alpha = 0.3f)))
                Spacer(modifier = Modifier.width(4.dp))
                Box(modifier = Modifier.size(11.dp).clip(RoundedCornerShape(2.dp)).background(completedColor.copy(alpha = 0.6f)))
                Spacer(modifier = Modifier.width(4.dp))
                Box(modifier = Modifier.size(11.dp).clip(RoundedCornerShape(2.dp)).background(completedColor))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "More",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isDark) Color(0xFF8B93A6) else Color(0xFF757575)
                )
            }
        }
    }
}

@Composable
fun MilestonesSection(
    currentStreak: Int,
    totalValue: Float,
    unit: String,
    totalCompletions: Int,
    completionRate: Int,
    habitXp: Int,
    logs: List<HabitLogEntity>,
    restDayEpochs: Set<Long>,
    onEvent: (HabitDetailContract.Event) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val bestStreakVal = calculateBestStreak(logs, restDayEpochs)
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Milestones",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else Color.Black
            )
            Text(
                text = "View all >",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF4B68FF),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onEvent(HabitDetailContract.Event.OnViewAllMilestonesClicked) }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                MilestoneStatCard(
                    icon = Icons.Default.LocalFireDepartment,
                    value = "$bestStreakVal Days",
                    label = "Best Streak",
                    iconColor = Color(0xFFFF7A00)
                )
            }
            item {
                MilestoneStatCard(
                    icon = Icons.Default.Star,
                    value = "$habitXp XP",
                    label = "Total Earned",
                    iconColor = Color(0xFF6C4BFF)
                )
            }
            item {
                MilestoneStatCard(
                    icon = Icons.Default.CheckCircle,
                    value = "$totalCompletions",
                    label = "Check-ins",
                    iconColor = Color(0xFF4CAF50)
                )
            }
            item {
                MilestoneStatCard(
                    icon = Icons.Default.WaterDrop,
                    value = "$completionRate%",
                    label = "Success Rate",
                    iconColor = Color(0xFF2196F3)
                )
            }
        }
    }
}

@Composable
fun MilestoneStatCard(
    icon: ImageVector,
    value: String,
    label: String,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF1E2230) else Color.White
        ),
        modifier = modifier
            .width(160.dp)
            .then(
                if (!isDark) {
                    Modifier.border(1.dp, Color(0xFFE8EAF6), RoundedCornerShape(16.dp))
                } else Modifier
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color.Black
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isDark) Color(0xFF8B93A6) else Color(0xFF757575)
                )
            }
        }
    }
}

fun calculateBestStreak(logs: List<com.saynedesign.habitloop.data.HabitLogEntity>, restDayEpochs: Set<Long>): Int {
    if (logs.isEmpty()) return 0
    val logDates = logs.map { it.dateEpochDay }.sorted()
    if (logDates.isEmpty()) return 0
    
    var maxStreak = 0
    var currentStreak = 0
    
    val minDate = logDates.first()
    val maxDate = logDates.last()
    
    var checkDate = minDate
    while (checkDate <= maxDate) {
        when {
            restDayEpochs.contains(checkDate) -> {
                checkDate++
            }
            logDates.contains(checkDate) -> {
                currentStreak++
                maxStreak = maxOf(maxStreak, currentStreak)
                checkDate++
            }
            else -> {
                currentStreak = 0
                checkDate++
            }
        }
    }
    return maxOf(maxStreak, currentStreak)
}

@Composable
fun formatTypeLabel(type: String): String {
    return when (type) {
        "NUMERIC" -> stringResource(R.string.habit_detail_numeric)
        "TIMER" -> stringResource(R.string.habit_detail_timer)
        else -> stringResource(R.string.habit_detail_yes_no)
    }
}

fun formatValue(value: Float): String {
    return if (value % 1.0f == 0f) value.toInt().toString() else value.toString()
}

@Composable
fun formatGoalRule(habit: HabitEntity): String {
    return when (habit.type) {
        "NUMERIC", "TIMER" -> {
            val comparator = if (habit.isAtLeast) {
                stringResource(R.string.habit_detail_at_least)
            } else {
                stringResource(R.string.habit_detail_at_most)
            }
            "$comparator ${formatValue(habit.targetValue)} ${habit.unit}"
        }
        else -> stringResource(R.string.habit_detail_complete_once_days)
    }
}

@Composable
fun formatSelectedDays(selectedDaysCsv: String): String {
    val dayNameByIso = mapOf(
        1 to stringResource(R.string.report_day_mon),
        2 to stringResource(R.string.report_day_tue),
        3 to stringResource(R.string.report_day_wed),
        4 to stringResource(R.string.report_day_thu),
        5 to stringResource(R.string.report_day_fri),
        6 to stringResource(R.string.report_day_sat),
        7 to stringResource(R.string.report_day_sun)
    )
    val days = selectedDaysCsv
        .split(",")
        .mapNotNull { it.trim().toIntOrNull() }
        .distinct()
        .sorted()
    if (days.isEmpty()) return stringResource(R.string.habit_detail_not_set)
    if (days.size == 7) return stringResource(R.string.habit_detail_every_day)
    return days.joinToString(", ") { dayNameByIso[it] ?: it.toString() }
}

@Composable
fun formatFrequencyLabel(frequency: String): String {
    return when (frequency.uppercase()) {
        "DAILY" -> stringResource(R.string.create_habit_frequency_daily)
        "WEEKLY" -> stringResource(R.string.create_habit_frequency_weekly)
        "MONTHLY" -> stringResource(R.string.create_habit_frequency_monthly)
        else -> frequency.lowercase().replaceFirstChar { it.uppercase() }
    }
}

@Composable
fun formatTimeOfDayLabel(timeOfDay: String): String {
    return when (timeOfDay.uppercase()) {
        "MORNING" -> stringResource(R.string.create_habit_time_morning)
        "AFTERNOON" -> stringResource(R.string.create_habit_time_afternoon)
        "EVENING" -> stringResource(R.string.create_habit_time_evening)
        else -> timeOfDay.lowercase().replaceFirstChar { it.uppercase() }
    }
}

@Composable
fun formatSchedule(habit: HabitEntity): String {
    return if (habit.isOneTime) {
        stringResource(
            R.string.habit_detail_one_time_on,
            habit.scheduledDate?.let { formatTimestampDate(it) } ?: stringResource(R.string.habit_detail_not_set)
        )
    } else {
        val endDate = habit.endDate?.let { formatTimestampDate(it) } ?: stringResource(R.string.habit_detail_no_end_date)
        stringResource(R.string.habit_detail_regular_end, endDate)
    }
}

fun formatTimestampDate(timestamp: Long): String {
    return Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
}
