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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.codesmithslabs.thedogtail.R
import com.codesmithslabs.thedogtail.data.HabitEntity
import com.codesmithslabs.thedogtail.data.HabitLogEntity
import com.codesmithslabs.thedogtail.ui.theme.BrandBlue
import com.codesmithslabs.thedogtail.ui.theme.SuccessGreen
import com.codesmithslabs.thedogtail.ui.theme.TextPrimary
import com.codesmithslabs.thedogtail.ui.theme.TextSecondary
import com.codesmithslabs.thedogtail.ui.theme.WarningOrange
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
                title = { Text(stringResource(R.string.habit_detail_progress), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { onEvent(HabitDetailContract.Event.OnBackClicked) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
                actions = {
                    IconButton(onClick = { onEvent(HabitDetailContract.Event.OnEditClicked) }) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.common_edit), tint = BrandBlue)
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
                    Text(stringResource(R.string.habit_detail_not_found))
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
                    text = stringResource(R.string.habit_detail_keep_going),
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
                text = stringResource(R.string.habit_detail_consistency),
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                text = stringResource(R.string.habit_detail_scoring_logic),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.habit_detail_scoring_formula),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricChip(
                    title = stringResource(R.string.habit_detail_consistency),
                    value = "$completionRate%",
                    modifier = Modifier.weight(1f)
                )
                MetricChip(
                    title = stringResource(R.string.habit_detail_30d_logs),
                    value = "$logsInLast30Days / 30",
                    modifier = Modifier.weight(1f)
                )
                MetricChip(
                    title = stringResource(R.string.habit_detail_unique_days),
                    value = uniqueDaysInLast30Days.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricChip(
                    title = stringResource(R.string.habit_detail_current_streak),
                    value = stringResource(R.string.habit_detail_days_plural, currentStreak),
                    modifier = Modifier.weight(1f)
                )
                MetricChip(
                    title = stringResource(R.string.habit_detail_total_checkins),
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                text = stringResource(R.string.habit_detail_saved_details),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            DetailRow(label = stringResource(R.string.habit_detail_type), value = formatTypeLabel(habit.type))
            DetailRow(label = stringResource(R.string.habit_detail_goal_rule), value = formatGoalRule(habit))
            DetailRow(label = stringResource(R.string.habit_detail_frequency), value = formatFrequencyLabel(habit.frequency))
            DetailRow(label = stringResource(R.string.habit_detail_selected_days), value = formatSelectedDays(habit.selectedDays))
            DetailRow(label = stringResource(R.string.habit_detail_time_of_day), value = formatTimeOfDayLabel(habit.timeOfDay))
            DetailRow(
                label = stringResource(R.string.habit_detail_reminder),
                value = if (habit.reminderEnabled) {
                    stringResource(R.string.habit_detail_reminder_enabled_at, habit.reminderTime)
                } else {
                    stringResource(R.string.habit_detail_disabled)
                }
            )
            DetailRow(label = stringResource(R.string.habit_detail_schedule), value = formatSchedule(habit))
            DetailRow(label = stringResource(R.string.habit_detail_created), value = formatTimestampDate(habit.createdTimestamp))
            if (habit.description.isBlank()) {
                Text(
                    text = stringResource(R.string.habit_detail_description_empty),
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
                text = stringResource(R.string.habit_detail_yearly_grid),
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
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                    Text(
                        stringResource(R.string.habit_detail_less),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.size(12.dp).background(BrandBlue.copy(alpha = 0.1f), RoundedCornerShape(2.dp)))
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(modifier = Modifier.size(12.dp).background(BrandBlue.copy(alpha = 0.4f), RoundedCornerShape(2.dp)))
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(modifier = Modifier.size(12.dp).background(BrandBlue, RoundedCornerShape(2.dp)))
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(modifier = Modifier.size(12.dp).border(0.8.dp, BrandBlue.copy(alpha = 0.2f), RoundedCornerShape(2.dp)))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.habit_detail_more),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
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
        val streakValue = if (currentStreak == 1) {
            stringResource(R.string.habit_detail_day_single, currentStreak)
        } else {
            stringResource(R.string.habit_detail_days_plural, currentStreak)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.habit_detail_milestones),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.common_view_all),
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
                value = streakValue,
                label = stringResource(R.string.habit_detail_streak_master),
                color = WarningOrange,
                modifier = Modifier.weight(1f)
            )

            StatCard(
                icon = Icons.Default.Star,
                value = formatValue(totalValue),
                label = stringResource(R.string.habit_detail_total_unit, unit),
                color = BrandBlue,
                modifier = Modifier.weight(1f)
            )

            StatCard(
                icon = Icons.Default.WaterDrop,
                value = "$totalCompletions",
                label = stringResource(R.string.habit_detail_check_ins),
                color = SuccessGreen,
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
            text = stringResource(R.string.habit_detail_my_why_journal),
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
                        tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (description.isNotBlank()) "\"$description\"" else stringResource(R.string.habit_detail_default_why),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontStyle = FontStyle.Italic
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.habit_detail_updated_recently),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        )
                        IconButton(
                            onClick = onEditClick,
                            modifier = Modifier
                                .size(32.dp)
                                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = stringResource(R.string.common_edit),
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
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
