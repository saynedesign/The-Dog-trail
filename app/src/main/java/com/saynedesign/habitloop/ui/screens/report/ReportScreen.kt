package com.saynedesign.habitloop.ui.screens.report

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saynedesign.habitloop.R
import com.saynedesign.habitloop.util.LevelSystem
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    state: ReportContract.State,
    onEvent: (ReportContract.Event) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = Color(0xFF070B19), // Midnight dark background matching mockup
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Growth 🌱",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Your journey to a better you",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF8B93A6)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        // Open current month datepicker or show options
                    }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Calendar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF070B19)
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. XP Level Progress Card
            item {
                XpLevelProgressCard(state)
            }

            // 2. This Week Metrics
            item {
                WeeklyMetricsCard(state)
            }

            // 3. JNI Insight Card
            item {
                JniSunsetInsightCard(state)
            }

            // 4. Habit Performance List
            item {
                HabitPerformanceCard(state)
            }

            // 5. Milestones Timeline
            item {
                MilestonesTimelineCard(state)
            }

            // 6. Consistency Heatmap
            item {
                ConsistencyHeatmapCard(
                    state = state,
                    onMonthChange = { onEvent(ReportContract.Event.OnMonthChange(it)) }
                )
            }

            // Spacer for navigation bar padding
            item {
                Spacer(modifier = Modifier.navigationBarsPadding())
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun XpLevelProgressCard(state: ReportContract.State) {
    val currentLevelInfo = LevelSystem.getLevelInfo(state.currentLevel)
    val nextLevelInfo = LevelSystem.getLevelInfo(state.currentLevel + 1)
    
    val currentLevelXp = currentLevelInfo.requiredXp
    val nextLevelXp = nextLevelInfo.requiredXp
    val xpInLevel = state.totalXp - currentLevelXp
    val xpRequiredForLevel = nextLevelXp - currentLevelXp
    
    val progress = if (xpRequiredForLevel > 0) {
        (xpInLevel.toFloat() / xpRequiredForLevel.toFloat()).coerceIn(0f, 1f)
    } else {
        1.0f
    }
    
    val xpToLevelUp = (nextLevelXp - state.totalXp).coerceAtLeast(0)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141A30)), // Deep blue card surface
        border = BorderStroke(1.dp, Color(0xFF29314F))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Warrior avatar on the left
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color(0xFF232A45), CircleShape)
                    .border(2.dp, Color(0xFF6C4BFF), CircleShape)
                    .clip(CircleShape)
            ) {
                Image(
                    painter = painterResource(LevelSystem.getLevelDrawableRes(state.currentLevel)),
                    contentDescription = "Warrior Level Avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Level Stats on the right
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "LEVEL ${state.currentLevel}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6C4BFF)
                        )
                        Text(
                            text = "${currentLevelInfo.name} ${currentLevelInfo.emoji}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    
                    // View Rewards link
                    Text(
                        text = "View Rewards >",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF8B93A6),
                        modifier = Modifier.clickable { /* Rewards action */ }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Next Level Pill
                Box(
                    modifier = Modifier
                        .background(Color(0xFF232A45), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Next: ${nextLevelInfo.name} ${nextLevelInfo.emoji}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFE8EAF6)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Linear Progress bar
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color(0xFF6C4BFF),
                    trackColor = Color(0xFF232A45)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "⭐ ${state.totalXp} / ${nextLevelXp} XP",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF8B93A6)
                    )
                    Text(
                        text = "$xpToLevelUp XP to Level ${state.currentLevel + 1}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF8B93A6)
                    )
                }
            }
        }
    }
}

@Composable
fun WeeklyMetricsCard(state: ReportContract.State) {
    val today = LocalDate.now()
    val startOfWeek = today.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
    val endOfWeek = startOfWeek.plusDays(6)
    val formatter = DateTimeFormatter.ofPattern("MMM d")
    val dateRangeStr = "${startOfWeek.format(formatter)} – ${endOfWeek.format(formatter)}"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141A30)),
        border = BorderStroke(1.dp, Color(0xFF29314F))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "This Week",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = dateRangeStr,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF8B93A6)
                    )
                }
                
                // Static positive indicator comparing to last week
                Box(
                    modifier = Modifier
                        .background(Color(0xFF2E7D32).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "▲ 14% vs last week",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF81C784)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Metrics row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricItem(
                    modifier = Modifier.weight(1f),
                    value = "${state.weeklyXp} XP",
                    label = "Earned",
                    emoji = "⚡",
                    circleColor = Color(0xFF6C4BFF)
                )
                MetricItem(
                    modifier = Modifier.weight(1f),
                    value = "${state.totalHabitsCompleted}",
                    label = "Habits Done",
                    emoji = "✅",
                    circleColor = Color(0xFF2E7D32)
                )
                MetricItem(
                    modifier = Modifier.weight(1f),
                    value = "${state.totalPerfectDays}",
                    label = "Perfect Days",
                    emoji = "🔥",
                    circleColor = Color(0xFFE65100)
                )
                MetricItem(
                    modifier = Modifier.weight(1f),
                    value = "${state.completionRate}%",
                    label = "Completion",
                    emoji = "🎯",
                    circleColor = Color(0xFF1565C0)
                )
            }
        }
    }
}

@Composable
fun MetricItem(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    emoji: String,
    circleColor: Color
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(circleColor.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF8B93A6),
            maxLines = 1
        )
    }
}

@Composable
fun JniSunsetInsightCard(state: ReportContract.State) {
    // Select the best insight or advice to feature in the sunset card
    val featuredInsight = state.insights.firstOrNull() 
        ?: state.advices.firstOrNull() 
        ?: "Keep logging your habits to generate custom C++ insights!"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF1A1230), // Dark indigo
                            Color(0xFF3B1E43), // Deep purple
                            Color(0xFF8D4F38)  // Warm sunset orange
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "✨", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Insight",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = featuredInsight,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
fun HabitPerformanceCard(state: ReportContract.State) {
    val expanded = remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141A30)),
        border = BorderStroke(1.dp, Color(0xFF29314F))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Habit Performance",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Text(
                    text = if (expanded.value) "Show Less" else "View All Habits >",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF6C4BFF),
                    modifier = Modifier.clickable { expanded.value = !expanded.value }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val displayScores = if (expanded.value) state.habitScores else state.habitScores.take(5)
            
            if (displayScores.isEmpty()) {
                Text(
                    text = "No active habits tracked yet. Start completing habits on the Home screen!",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF8B93A6)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    displayScores.forEach { score ->
                        val (catColor, emoji) = when {
                            score.title.contains("gym", ignoreCase = true) || score.title.contains("run", ignoreCase = true) || score.title.contains("workout", ignoreCase = true) -> Color(0xFFE65100) to "🏃"
                            score.title.contains("read", ignoreCase = true) || score.title.contains("book", ignoreCase = true) -> Color(0xFF6C4BFF) to "📖"
                            score.title.contains("water", ignoreCase = true) || score.title.contains("drink", ignoreCase = true) -> Color(0xFF1565C0) to "💧"
                            score.title.contains("meditat", ignoreCase = true) || score.title.contains("yoga", ignoreCase = true) -> Color(0xFF2E7D32) to "🧘"
                            score.title.contains("sleep", ignoreCase = true) -> Color(0xFF6A1B9A) to "😴"
                            else -> Color(0xFF00ACC1) to "⚡"
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Circular Category Icon
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(catColor.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = emoji, fontSize = 16.sp)
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            // Habit Title
                            Text(
                                text = score.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                modifier = Modifier.width(100.dp),
                                maxLines = 1
                            )
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            // Progress bar
                            val progressFactor = (score.consistency30d / 100f).coerceIn(0f, 1f)
                            LinearProgressIndicator(
                                progress = { progressFactor },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = catColor,
                                trackColor = Color(0xFF232A45)
                            )
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            // Percentage text
                            Text(
                                text = "${score.consistency30d}%",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.width(42.dp)
                            )
                            
                            // Trend
                            val (trendText, trendColor) = when (score.trend) {
                                "improving" -> "▲ 8%" to Color(0xFF81C784)
                                "declining" -> "▼ 4%" to Color(0xFFE57373)
                                else -> "—" to Color(0xFF8B93A6)
                            }
                            
                            Text(
                                text = trendText,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = trendColor,
                                modifier = Modifier.width(36.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MilestonesTimelineCard(state: ReportContract.State) {
    val milestones = listOf(
        MilestoneItem("First Habit", "🟢", state.totalHabitsCompleted >= 1),
        MilestoneItem("7 Day Streak", "🔥", state.currentStreak >= 7),
        MilestoneItem("100 Check-ins", "⭐", state.totalHabitsCompleted >= 100),
        MilestoneItem("500 XP", "🏆", state.totalXp >= 500),
        MilestoneItem("30 Day Streak", "🔒", state.currentStreak >= 30),
        MilestoneItem("Legend", "👑", state.currentLevel >= 9)
    )
    val achievedCount = milestones.count { it.isAchieved }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141A30)),
        border = BorderStroke(1.dp, Color(0xFF29314F))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Milestones",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "$achievedCount / ${milestones.size} Achieved",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF8B93A6)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Milestone horizontal list
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                milestones.forEachIndexed { index, milestone ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(80.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(
                                    color = if (milestone.isAchieved) Color(0xFF6C4BFF) else Color(0xFF232A45),
                                    shape = CircleShape
                                )
                                .border(
                                    width = 2.dp,
                                    color = if (milestone.isAchieved) Color(0xFF9E86FF) else Color(0xFF29314F),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = milestone.icon, fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = milestone.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (milestone.isAchieved) Color.White else Color(0xFF8B93A6),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            maxLines = 2
                        )
                    }
                    
                    // Simple divider between nodes except last
                    if (index < milestones.size - 1) {
                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .height(2.dp)
                                .background(if (milestone.isAchieved && milestones[index+1].isAchieved) Color(0xFF6C4BFF) else Color(0xFF29314F))
                        )
                    }
                }
            }
        }
    }
}

data class MilestoneItem(
    val name: String,
    val icon: String,
    val isAchieved: Boolean
)

@Composable
fun ConsistencyHeatmapCard(
    state: ReportContract.State,
    onMonthChange: (LocalDate) -> Unit
) {
    val selectedMonth = state.selectedMonth
    val calendarStats = state.calendarStats

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141A30)),
        border = BorderStroke(1.dp, Color(0xFF29314F))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Consistency Heatmap",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "Info",
                        tint = Color(0xFF8B93A6),
                        modifier = Modifier.size(16.dp)
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onMonthChange(selectedMonth.minusMonths(1)) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.ChevronLeft, null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = selectedMonth.format(DateTimeFormatter.ofPattern("MMM yyyy")),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = { onMonthChange(selectedMonth.plusMonths(1)) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.ChevronRight, null, tint = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Calendar Heatmap Grid
            val daysOfWeekLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            
            // Pad start of month
            val firstDayOfWeek = selectedMonth.withDayOfMonth(1).dayOfWeek.value // 1=Mon
            val daysInMonth = selectedMonth.lengthOfMonth()
            
            val totalCells = firstDayOfWeek - 1 + daysInMonth
            val rows = (totalCells + 6) / 7
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Header of week days
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    daysOfWeekLabels.forEach { label ->
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF8B93A6),
                            modifier = Modifier.weight(1f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))

                for (row in 0 until rows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (col in 0 until 7) {
                            val cellIndex = row * 7 + col
                            val dayNumber = cellIndex - (firstDayOfWeek - 1) + 1
                            
                            if (dayNumber in 1..daysInMonth) {
                                val date = selectedMonth.withDayOfMonth(dayNumber)
                                val stat = calendarStats.find { it.date == date }
                                val rate = stat?.completionRate ?: 0f
                                val isRestDay = state.restDayEpochs.contains(date.toEpochDay())
                                
                                val itemBgColor = when {
                                    rate >= 0.75f -> Color(0xFF6C4BFF) // Fully complete (Primary theme color)
                                    rate > 0f -> Color(0xFF6C4BFF).copy(alpha = rate.coerceAtLeast(0.2f))
                                    isRestDay -> Color(0xFF232A45) // Rest day indicator background
                                    else -> Color(0xFF1C223C) // Empty cell
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(3.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(itemBgColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isRestDay && rate == 0f) {
                                        Text("🌿", fontSize = 10.sp)
                                    } else {
                                        Text(
                                            text = dayNumber.toString(),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (rate >= 0.75f) Color.White else Color(0xFF8B93A6)
                                        )
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Legend / Key
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = "Less", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B93A6))
                    Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(Color(0xFF1C223C)))
                    Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(Color(0xFF6C4BFF).copy(alpha = 0.3f)))
                    Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(Color(0xFF6C4BFF).copy(alpha = 0.6f)))
                    Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(Color(0xFF6C4BFF)))
                    Text(text = "More", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B93A6))
                }
                
                Text(text = "Tap a day to see details", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B93A6))
            }
        }
    }
}
