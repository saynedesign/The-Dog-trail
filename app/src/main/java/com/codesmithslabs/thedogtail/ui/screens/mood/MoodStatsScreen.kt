package com.codesmithslabs.thedogtail.ui.screens.mood

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codesmithslabs.thedogtail.data.MoodEntity
import com.codesmithslabs.thedogtail.ui.theme.BrandBlue
import com.codesmithslabs.thedogtail.ui.theme.TextPrimary
import com.codesmithslabs.thedogtail.ui.theme.TextSecondary
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodStatsScreen(
    state: MoodContract.State,
    onEvent: (MoodContract.Event) -> Unit
) {
    if (state.showAddMoodDialog) {
        MoodSelectionDialog(
            state = state,
            onEvent = onEvent
        )
    }

    if (state.showHistory) {
        MoodHistoryScreen(state, onEvent)
    } else {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Mood Stat",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    actions = {
                        IconButton(onClick = { onEvent(MoodContract.Event.OnHistoryClicked) }) {
                            Icon(Icons.Default.History, contentDescription = "History")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.White
                    )
                )
            },
            containerColor = Color.White
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
            // Month Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onEvent(MoodContract.Event.OnMonthChanged(state.selectedMonth.minusMonths(1))) }) {
                    Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Previous Month")
                }
                
                Text(
                    text = "${state.selectedMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${state.selectedMonth.year}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = { onEvent(MoodContract.Event.OnMonthChanged(state.selectedMonth.plusMonths(1))) }) {
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Next Month")
                }
            }

            // Calendar Grid
            val daysInMonth = state.selectedMonth.lengthOfMonth()
            val firstDayOfWeek = state.selectedMonth.atDay(1).dayOfWeek.value // 1=Mon, 7=Sun

            // Weekday Headers
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su").forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Empty slots for start of month
                items(firstDayOfWeek - 1) {
                    Box(modifier = Modifier.aspectRatio(0.7f))
                }
                
                items(daysInMonth) { dayIndex ->
                    val day = dayIndex + 1
                    val date = state.selectedMonth.atDay(day)
                    val mood = state.moods[day]
                    val isToday = date == LocalDate.now()
                    val isFuture = date.isAfter(LocalDate.now())
                    
                    MoodDayItem(
                        day = day,
                        mood = mood,
                        isToday = isToday,
                        isFuture = isFuture,
                        onClick = { if (!isFuture) onEvent(MoodContract.Event.OnDayClicked(day)) }
                    )
                }
            }
        }
    }
}
}

@Composable
fun MoodDayItem(
    day: Int,
    mood: MoodEntity?,
    isToday: Boolean,
    isFuture: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .aspectRatio(0.6f)
            .clickable(enabled = !isFuture, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (mood != null) {
            Text(text = mood.moodEmoji, fontSize = 32.sp)
            Text(
                text = mood.feeling.ifEmpty { mood.moodType },
                style = MaterialTheme.typography.bodySmall,
                fontSize = 10.sp,
                maxLines = 1,
                textAlign = TextAlign.Center,
                color = TextSecondary
            )
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        } else if (isToday) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .border(1.dp, BrandBlue.copy(alpha = 0.5f), CircleShape)
                    .clickable { onClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = BrandBlue)
            }
            Text(
                text = "Today",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 10.sp,
                color = BrandBlue
            )
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        } else {
            // Placeholder or Empty
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .border(1.dp, Color(0xFFEEEEEE), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Ghost face placeholder if desired, or just empty circle
                Text("☺\uFE0E", color = Color(0xFFEEEEEE), fontSize = 24.sp)
            }
            Text(
                text = "Mood",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 10.sp,
                color = Color(0xFFEEEEEE)
            )
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (isFuture) Color(0xFFEEEEEE) else TextPrimary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MoodSelectionDialog(
    state: MoodContract.State,
    onEvent: (MoodContract.Event) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = { onEvent(MoodContract.Event.OnDismissDialog) },
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (state.currentStep == 1) {
                MoodStep1(state, onEvent)
            } else {
                MoodStep2(state, onEvent)
            }
        }
    }
}

@Composable
fun MoodStep1(state: MoodContract.State, onEvent: (MoodContract.Event) -> Unit) {
    val moods = listOf(
        "Great" to "🤩",
        "Good" to "😊",
        "Okay" to "😐",
        "Not Good" to "😢",
        "Bad" to "😡"
    )

    Text(
        "How is your mood today?",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(24.dp))
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        moods.forEach { (label, emoji) ->
            val isSelected = state.selectedMoodType == label
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onEvent(MoodContract.Event.OnMoodOptionSelected(label, emoji)) }
                    .background(if (isSelected) BrandBlue.copy(alpha = 0.1f) else Color.Transparent)
                    .padding(8.dp)
            ) {
                Text(
                    text = emoji, 
                    fontSize = if (isSelected) 48.sp else 40.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = label, 
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) BrandBlue else TextPrimary
                )
            }
        }
    }
    
    Spacer(modifier = Modifier.height(32.dp))
    
    Button(
        onClick = { onEvent(MoodContract.Event.OnSubmitMood) },
        enabled = state.selectedMoodType != null,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = BrandBlue,
            disabledContainerColor = Color.LightGray
        ),
        shape = RoundedCornerShape(28.dp)
    ) {
        Text(
            text = if (state.selectedMoodType != null) "I Feel ${state.selectedMoodType}!" else "Select a Mood",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MoodStep2(state: MoodContract.State, onEvent: (MoodContract.Event) -> Unit) {
    val moodFeelings = mapOf(
        "Great" to listOf("Happy", "Brave", "Motivated", "Creative", "Confident", "Calm", "Grateful", "Peaceful", "Excited", "Loved", "Hopeful", "Inspired", "Proud", "Euphoric", "Nostalgic"),
        "Good" to listOf("Happy", "Calm", "Relaxed", "Content", "Grateful", "Optimistic", "Productive"),
        "Okay" to listOf("Neutral", "Tired", "Bored", "Distracted", "Busy", "Normal"),
        "Not Good" to listOf("Anxious", "Stressed", "Lonely", "Sad", "Frustrated", "Disappointed"),
        "Bad" to listOf("Angry", "Depressed", "Hopeless", "Heartbroken", "Exhausted")
    )
    
    val currentFeelings = moodFeelings[state.selectedMoodType] ?: emptyList()

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Header with Back Button
        Box(modifier = Modifier.fillMaxWidth()) {
            IconButton(
                onClick = { onEvent(MoodContract.Event.OnBackStep) },
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Back")
            }
            
            Text(
                text = "${state.selectedMoodType}! How would you describe your feelings?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 48.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            currentFeelings.forEach { feeling ->
                val isSelected = state.selectedFeeling == feeling
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) BrandBlue else Color.Transparent)
                        .border(1.dp, if (isSelected) BrandBlue else Color.LightGray, RoundedCornerShape(20.dp))
                        .clickable { onEvent(MoodContract.Event.OnFeelingOptionSelected(feeling)) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = feeling,
                        color = if (isSelected) Color.White else TextPrimary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = { onEvent(MoodContract.Event.OnSubmitFeeling) },
            enabled = state.selectedFeeling != null,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = BrandBlue,
                disabledContainerColor = Color.LightGray
            ),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text(
                text = if (state.selectedFeeling != null) "I Feel ${state.selectedFeeling}!" else "Select a Feeling",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
