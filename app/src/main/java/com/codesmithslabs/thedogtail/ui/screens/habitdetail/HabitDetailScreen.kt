package com.codesmithslabs.thedogtail.ui.screens.habitdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.codesmithslabs.thedogtail.data.HabitEntity
import com.codesmithslabs.thedogtail.data.HabitLogEntity
import com.codesmithslabs.thedogtail.ui.theme.BrandBackground
import com.codesmithslabs.thedogtail.ui.theme.BrandBlue
import com.codesmithslabs.thedogtail.ui.theme.BrandSurface
import com.codesmithslabs.thedogtail.ui.theme.TextPrimary
import com.codesmithslabs.thedogtail.ui.theme.TextSecondary
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
                title = { Text("Progress", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { onEvent(HabitDetailContract.Event.OnBackClicked) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onEvent(HabitDetailContract.Event.OnEditClicked) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = BrandBlue)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = BrandSurface)
            )
        },
        containerColor = BrandBackground
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.habit != null) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentPadding = PaddingValues(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    item {
                        HabitHeader(
                            habit = state.habit,
                            consistency = state.completionRate
                        )
                    }

                    item {
                        ScoringBreakdownCard(
                            logs = state.logs,
                            completionRate = state.completionRate,
                            currentStreak = state.currentStreak,
                            totalCompletions = state.totalCompletions
                        )
                    }

                    item {
                        SavedDetailsCard(habit = state.habit)
                    }

                    item {
                        YearlyGridCard(
                            logs = state.logs,
                            createdDate = state.habit.createdTimestamp
                        )
                    }

                    item {
                        MilestonesSection(
                            currentStreak = state.currentStreak,
                            totalValue = state.totalValue,
                            unit = state.habit.unit,
                            totalCompletions = state.totalCompletions
                        )
                    }

                    item {
                        MyWhyJournalCard(
                            description = state.habit.description,
                            onEditClick = { onEvent(HabitDetailContract.Event.OnEditClicked) }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            } else {
                 Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Habit not found")
                }
            }
        }
    }
}

@Composable
fun HabitHeader(habit: HabitEntity, consistency: Int) {
    val accent = Color(habit.color)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                if (habit.icon.isNotBlank()) {
                    Text(text = habit.icon, style = MaterialTheme.typography.titleLarge)
                } else {
                    Text(
                        text = habit.title.take(1).uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        color = accent,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = habit.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Keep going! You're on fire \uD83D\uDD25",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
        
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "$consistency%",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = accent
            )
            Text(
                text = "Consistency",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun ScoringBreakdownCard(
    logs: List<HabitLogEntity>,
    completionRate: Int,
    currentStreak: Int,
    totalCompletions: Int
) {
    val today = LocalDate.now().toEpochDay()
    val thirtyDaysAgo = today - 29
    val logsInLast30Days = remember(logs, today) {
        logs.count { it.dateEpochDay in thirtyDaysAgo..today }
    }
    val uniqueDaysInLast30Days = remember(logs, today) {
        logs.map { it.dateEpochDay }.toSet().count { it in thirtyDaysAgo..today }
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Scoring & Progress Logic",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Consistency = logs in last 30 days ÷ 30 × 100",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricChip(
                    title = "Consistency",
                    value = "$completionRate%",
                    modifier = Modifier.weight(1f)
                )
                MetricChip(
                    title = "30D Logs",
                    value = "$logsInLast30Days / 30",
                    modifier = Modifier.weight(1f)
                )
                MetricChip(
                    title = "Unique Days",
                    value = uniqueDaysInLast30Days.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricChip(
                    title = "Current Streak",
                    value = "$currentStreak days",
                    modifier = Modifier.weight(1f)
                )
                MetricChip(
                    title = "Total Check-ins",
                    value = totalCompletions.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun MetricChip(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(BrandBlue.copy(alpha = 0.08f))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(text = title, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        Text(text = value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
    }
}

@Composable
fun SavedDetailsCard(habit: HabitEntity) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Saved Habit Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            DetailRow(label = "Type", value = formatTypeLabel(habit.type))
            DetailRow(label = "Goal Rule", value = formatGoalRule(habit))
            DetailRow(label = "Frequency", value = habit.frequency.lowercase().replaceFirstChar { it.uppercase() })
            DetailRow(label = "Selected Days", value = formatSelectedDays(habit.selectedDays))
            DetailRow(label = "Time of Day", value = habit.timeOfDay.lowercase().replaceFirstChar { it.uppercase() })
            DetailRow(label = "Reminder", value = if (habit.reminderEnabled) "Enabled at ${habit.reminderTime}" else "Disabled")
            DetailRow(label = "Schedule", value = formatSchedule(habit))
            DetailRow(label = "Created", value = formatTimestampDate(habit.createdTimestamp))
            if (habit.description.isBlank()) {
                Text(
                    text = "Description is empty. Add your \"why\" to keep motivation visible.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontStyle = FontStyle.Italic
                )
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            modifier = Modifier.weight(0.38f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.62f)
        )
    }
}

@Composable
fun YearlyGridCard(
    logs: List<HabitLogEntity>,
    createdDate: Long
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Yearly Grid",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = LocalDate.now().year.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = BrandBlue,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                val today = LocalDate.now()
                val logDates = remember(logs) { logs.map { it.dateEpochDay }.toSet() }
                val createdEpochDay = remember(createdDate) {
                    Instant.ofEpochMilli(createdDate)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                        .toEpochDay()
                }

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
                                val isCompleted = logDates.contains(date.toEpochDay())
                                val isFuture = date.isAfter(today)
                                val beforeHabitCreation = epochDay < createdEpochDay
                                
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(
                                            when {
                                                beforeHabitCreation -> Color.Transparent
                                                isFuture -> Color.Transparent
                                                isCompleted -> BrandBlue
                                                else -> BrandBlue.copy(alpha = 0.1f)
                                            }
                                        )
                                        .border(
                                            width = if (beforeHabitCreation) 0.8.dp else 0.dp,
                                            color = if (beforeHabitCreation) BrandBlue.copy(alpha = 0.2f) else Color.Transparent,
                                            shape = RoundedCornerShape(2.dp)
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
                    Text("Less", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.size(12.dp).background(BrandBlue.copy(alpha = 0.1f), RoundedCornerShape(2.dp)))
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(modifier = Modifier.size(12.dp).background(BrandBlue.copy(alpha = 0.4f), RoundedCornerShape(2.dp)))
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(modifier = Modifier.size(12.dp).background(BrandBlue, RoundedCornerShape(2.dp)))
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(modifier = Modifier.size(12.dp).border(0.8.dp, BrandBlue.copy(alpha = 0.2f), RoundedCornerShape(2.dp)))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("More", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
            }
        }
    }
}

@Composable
fun MilestonesSection(
    currentStreak: Int,
    totalValue: Float,
    unit: String,
    totalCompletions: Int
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Milestones",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "VIEW ALL",
                style = MaterialTheme.typography.labelSmall,
                color = BrandBlue,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            StatCard(
                icon = Icons.Default.LocalFireDepartment,
                value = "$currentStreak Day${if (currentStreak == 1) "" else "s"}",
                label = "Streak Master",
                color = Color(0xFFFF9800),
                modifier = Modifier.weight(1f)
            )

            StatCard(
                icon = Icons.Default.Star,
                value = formatValue(totalValue),
                label = "Total $unit",
                color = BrandBlue,
                modifier = Modifier.weight(1f)
            )

            StatCard(
                icon = Icons.Default.WaterDrop,
                value = "$totalCompletions",
                label = "Check-ins",
                color = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatCard(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier.height(140.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun MyWhyJournalCard(
    description: String,
    onEditClick: () -> Unit
) {
    Column {
        Text(
            text = "My \"Why\" Journal",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = BrandBlue),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(modifier = Modifier.padding(24.dp)) {
                Column {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (description.isNotBlank()) "\"$description\"" else "\"I want to build this habit to prove to myself that I am capable of change.\"",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        fontStyle = FontStyle.Italic
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Updated recently",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        IconButton(
                            onClick = onEditClick,
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

fun formatTypeLabel(type: String): String {
    return when (type) {
        "NUMERIC" -> "Numeric"
        "TIMER" -> "Timer"
        else -> "Yes / No"
    }
}

fun formatValue(value: Float): String {
    return if (value % 1.0f == 0f) value.toInt().toString() else value.toString()
}

fun formatGoalRule(habit: HabitEntity): String {
    return when (habit.type) {
        "NUMERIC", "TIMER" -> {
            val comparator = if (habit.isAtLeast) "At least" else "At most"
            "$comparator ${formatValue(habit.targetValue)} ${habit.unit}"
        }
        else -> "Complete once on selected days"
    }
}

fun formatSelectedDays(selectedDaysCsv: String): String {
    val dayNameByIso = mapOf(
        1 to "Mon",
        2 to "Tue",
        3 to "Wed",
        4 to "Thu",
        5 to "Fri",
        6 to "Sat",
        7 to "Sun"
    )
    val days = selectedDaysCsv
        .split(",")
        .mapNotNull { it.trim().toIntOrNull() }
        .distinct()
        .sorted()
    if (days.isEmpty()) return "Not set"
    if (days.size == 7) return "Every day"
    return days.joinToString(", ") { dayNameByIso[it] ?: it.toString() }
}

fun formatSchedule(habit: HabitEntity): String {
    return if (habit.isOneTime) {
        "One-time on ${habit.scheduledDate?.let { formatTimestampDate(it) } ?: "Not set"}"
    } else {
        val endDate = habit.endDate?.let { formatTimestampDate(it) } ?: "No end date"
        "Regular habit • End: $endDate"
    }
}

fun formatTimestampDate(timestamp: Long): String {
    return Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
}
