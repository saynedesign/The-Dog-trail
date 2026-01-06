package com.codesmithslabs.thedogtail.ui.screens.habitdetail

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codesmithslabs.thedogtail.data.HabitEntity
import com.codesmithslabs.thedogtail.ui.theme.BrandBackground
import com.codesmithslabs.thedogtail.ui.theme.BrandBlue
import com.codesmithslabs.thedogtail.ui.theme.BrandSurface
import com.codesmithslabs.thedogtail.ui.theme.TextPrimary
import com.codesmithslabs.thedogtail.ui.theme.TextSecondary
import java.time.LocalDate
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
                    // 1. Header Section
                    item {
                        HabitHeader(
                            habit = state.habit,
                            consistency = state.completionRate
                        )
                    }

                    // 2. Yearly Grid (Heatmap)
                    item {
                        YearlyGridCard(
                            logs = state.logs,
                            createdDate = state.habit.createdTimestamp
                        )
                    }

                    // 3. Milestones / Stats
                    item {
                        MilestonesSection(
                            currentStreak = state.currentStreak,
                            totalValue = state.totalValue,
                            unit = state.habit.unit,
                            totalCompletions = state.totalCompletions
                        )
                    }

                    // 4. My "Why" Journal
                    item {
                        MyWhyJournalCard(
                            description = state.habit.description,
                            onEditClick = { onEvent(HabitDetailContract.Event.OnEditClicked) }
                        )
                    }
                    
                    // Bottom spacing
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
                    .background(BrandBlue.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = habit.title.take(1).uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    color = BrandBlue,
                    fontWeight = FontWeight.Bold
                )
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
                color = BrandBlue
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
fun YearlyGridCard(
    logs: List<com.codesmithslabs.thedogtail.data.HabitLogEntity>,
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
                // Heatmap Grid
                // We'll show the last ~20 weeks (approx 5 months) to fit on screen or use horizontal scroll
                // For "Yearly", let's use a horizontal scroll lazy row of columns
                
                val today = LocalDate.now()
                val logDates = remember(logs) { logs.map { it.dateEpochDay }.toSet() }
                
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End // Start from right (today)
                ) {
                    // Show last 52 weeks
                    items(52) { weekIndex ->
                        // weekIndex 0 is 51 weeks ago, weekIndex 51 is this week
                        // Let's iterate backwards: index 0 is THIS week
                        val weeksAgo = 51 - weekIndex
                        val startOfWeek = today.minusWeeks(weeksAgo.toLong())
                            .with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                        
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 2.dp)
                        ) {
                            repeat(7) { dayIndex ->
                                val date = startOfWeek.plusDays(dayIndex.toLong())
                                val isCompleted = logDates.contains(date.toEpochDay())
                                val isFuture = date.isAfter(today)
                                
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(
                                            when {
                                                isFuture -> Color.Transparent
                                                isCompleted -> BrandBlue
                                                else -> BrandBlue.copy(alpha = 0.1f)
                                            }
                                        )
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Legend
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
            // Card 1: Streak
            StatCard(
                icon = Icons.Default.LocalFireDepartment,
                value = "$currentStreak Day",
                label = "Streak Master",
                color = Color(0xFFFF9800), // Orange
                modifier = Modifier.weight(1f)
            )
            
            // Card 2: Total Volume (if numeric) or Completions
            StatCard(
                icon = Icons.Default.Star,
                value = "${totalValue.toInt()}",
                label = "Total $unit",
                color = BrandBlue,
                modifier = Modifier.weight(1f)
            )
            
            // Card 3: Completions
            StatCard(
                icon = Icons.Default.WaterDrop, // Generic icon
                value = "$totalCompletions",
                label = "Check-ins",
                color = Color(0xFF4CAF50), // Green
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
                        imageVector = Icons.Default.Edit, // Quote icon placeholder if needed, using Edit for now or specific quote icon
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (description.isNotBlank()) "\"$description\"" else "\"I want to build this habit to prove to myself that I am capable of change.\"",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
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
