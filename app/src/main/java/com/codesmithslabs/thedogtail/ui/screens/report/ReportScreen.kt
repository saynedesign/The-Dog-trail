package com.codesmithslabs.thedogtail.ui.screens.report

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codesmithslabs.thedogtail.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    state: ReportContract.State,
    onEvent: (ReportContract.Event) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    Image(
                        painter = painterResource(R.drawable.ic_icon_habit_loop),
                        contentDescription = stringResource(R.string.app_name),
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(32.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                },
                title = {
                    Text(
                        text = stringResource(R.string.report_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
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
            // 0. Weekly XP Chip
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "earned ${state.weeklyXp} XP this week",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // 1. Top Stats Cards
            item {
                StatsGrid(state)
            }

            // 2. Habits Completed Bar Chart
            item {
                HabitsCompletedCard(state.weeklyHabitCounts)
            }

            // 3. Habit Completion Rate Line Chart
            item {
                CompletionRateCard(state.monthlyCompletionRates)
            }

            // 4. Calendar Stats
            item {
                CalendarStatsCard(
                    state = state,
                    onMonthChange = { onEvent(ReportContract.Event.OnMonthChange(it)) }
                )
            }

            // 5. Mood Chart
            item {
                MoodChartCard(state.weeklyMoods)
            }
            
            // Spacer for bottom nav
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun StatsGrid(state: ReportContract.State) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatCard(
                modifier = Modifier.weight(1f),
                value = stringResource(R.string.habit_detail_days_plural, state.activeMomentum),
                label = "Active Momentum"
            )
            StatCard(
                modifier = Modifier.weight(1f),
                value = "${state.weeklyConsistencyScore}%",
                label = "Weekly Consistency"
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatCard(
                modifier = Modifier.weight(1f),
                value = "${state.totalEffortPoints}",
                label = "Effort Points"
            )
            StatCard(
                modifier = Modifier.weight(1f),
                value = "${state.strongDays}",
                label = "Strong Days"
            )
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun HabitsCompletedCard(data: List<ReportContract.DailyHabitCount>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.report_habits_completed_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.report_this_week),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Bar Chart
            val maxCount = data.maxOfOrNull { it.count }?.coerceAtLeast(1) ?: 1
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Y-Axis labels (simplified)
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    val steps = 5
                    if (maxCount <= steps) {
                        (maxCount downTo 1).forEach { 
                            Text(text = it.toString(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        (steps downTo 1).forEach { step ->
                            val value = (maxCount * step / steps)
                            Text(text = value.toString(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                data.forEach { item ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Bar
                        val heightFraction = (item.count.toFloat() / maxCount.toFloat()).coerceIn(0f, 1f)
                        
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .fillMaxHeight(heightFraction)
                                .background(
                                    color = if (item.isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                                )
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = item.dayLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CompletionRateCard(data: List<ReportContract.MonthlyRate>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val axisLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.report_habit_completion_rate_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.report_last_six_months),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // Line Chart
            val primaryColor = MaterialTheme.colorScheme.primary
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                val width = size.width
                val height = size.height
                val spacePerPoint = width / (data.size - 1).coerceAtLeast(1)
                
                val path = Path()
                
                data.forEachIndexed { index, item ->
                    val x = index * spacePerPoint
                    val y = height - (item.rate / 100f * height)
                    
                    if (index == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                    
                    // Draw point
                    drawCircle(
                        color = primaryColor,
                        radius = 8f,
                        center = Offset(x, y)
                    )
                    
                    // Draw label
                    drawContext.canvas.nativeCanvas.apply {
                        drawText(
                            item.monthLabel,
                            x,
                            height + 40f,
                            android.graphics.Paint().apply {
                                color = axisLabelColor
                                textSize = 30f
                                textAlign = android.graphics.Paint.Align.CENTER
                            }
                        )
                    }
                }
                
                drawPath(
                    path = path,
                    color = primaryColor,
                    style = Stroke(width = 4f, cap = StrokeCap.Round)
                )
                
                // Fill gradient
                path.lineTo(width, height)
                path.lineTo(0f, height)
                path.close()
                
                drawPath(
                    path = path,
                    brush = Brush.verticalGradient(
                        colors = listOf(primaryColor.copy(alpha = 0.2f), Color.Transparent),
                        startY = 0f,
                        endY = height
                    )
                )
            }
        }
    }
}

@Composable
fun CalendarStatsCard(
    state: ReportContract.State,
    onMonthChange: (LocalDate) -> Unit
) {
    val selectedMonth = state.selectedMonth
    val calendarStats = state.calendarStats
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.report_calendar_stats),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onMonthChange(selectedMonth.minusMonths(1)) }) {
                        Icon(Icons.Default.ChevronLeft, null)
                    }
                    Text(
                        text = selectedMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { onMonthChange(selectedMonth.plusMonths(1)) }) {
                        Icon(Icons.Default.ChevronRight, null)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // Calendar Grid
            // Days of week header
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf(
                    stringResource(R.string.report_day_mon),
                    stringResource(R.string.report_day_tue),
                    stringResource(R.string.report_day_wed),
                    stringResource(R.string.report_day_thu),
                    stringResource(R.string.report_day_fri),
                    stringResource(R.string.report_day_sat),
                    stringResource(R.string.report_day_sun)
                ).forEach {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Days
            // We need to pad the start of the month
            val firstDayOfWeek = selectedMonth.withDayOfMonth(1).dayOfWeek.value // 1=Mon
            val daysInMonth = selectedMonth.lengthOfMonth()
            
            val totalCells = firstDayOfWeek - 1 + daysInMonth
            val rows = (totalCells + 6) / 7
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (row in 0 until rows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (col in 0 until 7) {
                            val dayIndex = row * 7 + col
                            val dayOfMonth = dayIndex - (firstDayOfWeek - 1) + 1
                            
                            if (dayOfMonth in 1..daysInMonth) {
                                val date = selectedMonth.withDayOfMonth(dayOfMonth)
                                val stat = calendarStats.find { it.date == date }
                                val rate = stat?.completionRate ?: 0f
                                val isRestDay = state.restDayEpochs.contains(date.toEpochDay())
                                
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(2.dp)
                                        .background(
                                            color = when {
                                                rate >= 0.75f -> com.codesmithslabs.thedogtail.ui.theme.SuccessGreen // Strong day
                                                rate > 0f -> com.codesmithslabs.thedogtail.ui.theme.SuccessGreen.copy(alpha = rate.coerceAtLeast(0.3f))
                                                isRestDay -> com.codesmithslabs.thedogtail.ui.theme.BrandBackground
                                                else -> Color.Transparent
                                            },
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isRestDay && rate == 0f) {
                                        Text("🌿", style = MaterialTheme.typography.bodySmall)
                                    } else {
                                        Text(
                                            text = dayOfMonth.toString(),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (rate >= 0.75f) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
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
        }
    }
}

@Composable
fun MoodChartCard(data: List<ReportContract.DailyMood>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.report_mood_chart),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Mood Chart Content
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp) // Increased height for better spacing
            ) {
                // Y-Axis Emojis
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("😎", "😊", "😐", "😡", "😢").forEach { 
                        Box(
                            modifier = Modifier.height(24.dp), // Fixed height for alignment
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = it, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                val primaryBandColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                val secondaryBandColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                val errorBandColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                val pointFillColor = MaterialTheme.colorScheme.surface
                val primaryColor = MaterialTheme.colorScheme.primary
                
                Canvas(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    val width = size.width
                    val height = size.height
                    // We have 5 rows of emojis. The chart area corresponds to these 5 rows.
                    // The emojis are centered in their "rows".
                    // Let's divide height into 5 segments.
                    val rowHeight = height / 5f
                    
                    // Draw Background Bands
                    // Top (Great/Good) - Light Blue/Green
                    drawRect(
                        color = primaryBandColor,
                        topLeft = Offset(0f, 0f),
                        size = Size(width, rowHeight * 2)
                    )
                    // Middle (Okay) - Light Grey/Yellow
                    drawRect(
                        color = secondaryBandColor,
                        topLeft = Offset(0f, rowHeight * 2),
                        size = Size(width, rowHeight)
                    )
                    // Bottom (Bad/Terrible) - Light Red
                    drawRect(
                        color = errorBandColor,
                        topLeft = Offset(0f, rowHeight * 3),
                        size = Size(width, rowHeight * 2)
                    )

                    if (data.isEmpty()) return@Canvas

                    val spacePerPoint = width / (data.size - 1).coerceAtLeast(1)
                    val path = Path()
                    val points = mutableListOf<Offset>()
                    
                    data.forEachIndexed { index, item ->
                        val x = index * spacePerPoint
                        // Map mood value (1-5) to Y.
                        // 5 (Great) -> Top of first row -> rowHeight * 0.5
                        // 4 (Good) -> Top of second row -> rowHeight * 1.5
                        // ...
                        // 1 (Bad) -> Top of fifth row -> rowHeight * 4.5
                        
                        // Formula: rowIndex = (5 - moodValue)
                        // y = rowIndex * rowHeight + (rowHeight / 2)
                        
                        val y = if (item.moodValue > 0) {
                            val rowFromTop = 5 - item.moodValue
                            rowFromTop * rowHeight + (rowHeight / 2)
                        } else {
                            // If no mood, maybe skip or put at bottom?
                            height
                        }
                        
                        points.add(Offset(x, y))
                        
                        if (index == 0) {
                            path.moveTo(x, y)
                        } else {
                            path.lineTo(x, y)
                        }
                    }
                    
                    // Draw Gradient Fill
                    val fillPath = Path()
                    fillPath.addPath(path)
                    fillPath.lineTo(width, height)
                    fillPath.lineTo(0f, height)
                    fillPath.close()
                    
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(primaryColor.copy(alpha = 0.3f), primaryColor.copy(alpha = 0.05f)),
                            startY = 0f,
                            endY = height
                        )
                    )
                    
                    // Draw Line
                    drawPath(
                        path = path,
                        color = primaryColor,
                        style = Stroke(width = 6f, cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round)
                    )
                    
                    // Draw Points
                    points.forEach { point ->
                        // White circle with colored border
                        drawCircle(
                            color = pointFillColor,
                            radius = 10f,
                            center = point
                        )
                        drawCircle(
                            color = primaryColor,
                            radius = 10f,
                            center = point,
                            style = Stroke(width = 4f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))

            // X-Axis Labels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 40.dp), // Offset for emojis
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                data.forEach { 
                    Text(
                        text = it.dayLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(20.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}
