package com.codesmithslabs.thedogtail.ui.screens.report

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codesmithslabs.thedogtail.ui.theme.BrandBlue
import com.codesmithslabs.thedogtail.ui.theme.BrandSurface
import com.codesmithslabs.thedogtail.ui.theme.TextPrimary
import com.codesmithslabs.thedogtail.ui.theme.TextSecondary
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun ReportScreen(
    state: ReportContract.State,
    onEvent: (ReportContract.Event) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = BrandSurface,
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Placeholder for logo/icon
                Icon(
                    imageVector = Icons.Default.MoreVert, // Replace with app icon if available
                    contentDescription = null,
                    tint = BrandBlue,
                    modifier = Modifier.size(24.dp)
                )
                
                Text(
                    text = "Report",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Menu",
                    tint = TextPrimary
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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
                    state.selectedMonth,
                    state.calendarStats,
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
                value = "${state.currentStreak} days",
                label = "Current streak"
            )
            StatCard(
                modifier = Modifier.weight(1f),
                value = "${state.completionRate}%",
                label = "Completion rate"
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatCard(
                modifier = Modifier.weight(1f),
                value = "${state.totalHabitsCompleted}",
                label = "Habits completed"
            )
            StatCard(
                modifier = Modifier.weight(1f),
                value = "${state.totalPerfectDays}",
                label = "Total perfect days"
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                color = TextPrimary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun HabitsCompletedCard(data: List<ReportContract.DailyHabitCount>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                    text = "Habits Completed",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "This Week",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
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
                            Text(text = it.toString(), style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        }
                    } else {
                        (steps downTo 1).forEach { step ->
                            val value = (maxCount * step / steps)
                            Text(text = value.toString(), style = MaterialTheme.typography.labelSmall, color = TextSecondary)
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
                                    color = if (item.isToday) BrandBlue else BrandBlue.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                                )
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = item.dayLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                    text = "Habit Completion Rate",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Last 6 Months",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // Line Chart
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
                        color = BrandBlue,
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
                                color = android.graphics.Color.GRAY
                                textSize = 30f
                                textAlign = android.graphics.Paint.Align.CENTER
                            }
                        )
                    }
                }
                
                drawPath(
                    path = path,
                    color = BrandBlue,
                    style = Stroke(width = 4f, cap = StrokeCap.Round)
                )
                
                // Fill gradient
                path.lineTo(width, height)
                path.lineTo(0f, height)
                path.close()
                
                drawPath(
                    path = path,
                    brush = Brush.verticalGradient(
                        colors = listOf(BrandBlue.copy(alpha = 0.2f), Color.Transparent),
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
    selectedMonth: LocalDate,
    calendarStats: List<ReportContract.CalendarDayStat>,
    onMonthChange: (LocalDate) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                    text = "Calendar Stats",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
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
                listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { 
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
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
                                
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(2.dp)
                                        .background(
                                            color = if (rate > 0) BrandBlue.copy(alpha = rate.coerceAtLeast(0.1f)) else Color.Transparent,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = dayOfMonth.toString(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (rate > 0.5f) Color.White else TextPrimary
                                    )
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                    text = "Mood Chart",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                // Dropdown/Chip style for "This Week"
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .clickable { /* TODO: Filter */ }
                ) {
                    Text(
                        text = "This Week",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ChevronRight, // Or ArrowDown
                        contentDescription = null,
                        modifier = Modifier.size(16.dp).rotate(90f),
                        tint = TextSecondary
                    )
                }
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
                        color = Color(0xFFE3F2FD).copy(alpha = 0.5f),
                        topLeft = Offset(0f, 0f),
                        size = Size(width, rowHeight * 2)
                    )
                    // Middle (Okay) - Light Grey/Yellow
                    drawRect(
                        color = Color(0xFFFFFDE7).copy(alpha = 0.5f),
                        topLeft = Offset(0f, rowHeight * 2),
                        size = Size(width, rowHeight)
                    )
                    // Bottom (Bad/Terrible) - Light Red
                    drawRect(
                        color = Color(0xFFFFEBEE).copy(alpha = 0.5f),
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
                            colors = listOf(BrandBlue.copy(alpha = 0.3f), BrandBlue.copy(alpha = 0.05f)),
                            startY = 0f,
                            endY = height
                        )
                    )
                    
                    // Draw Line
                    drawPath(
                        path = path,
                        color = BrandBlue,
                        style = Stroke(width = 6f, cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round)
                    )
                    
                    // Draw Points
                    points.forEach { point ->
                        // White circle with colored border
                        drawCircle(
                            color = Color.White,
                            radius = 10f,
                            center = point
                        )
                        drawCircle(
                            color = BrandBlue,
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
                        color = TextSecondary,
                        modifier = Modifier.width(20.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}
