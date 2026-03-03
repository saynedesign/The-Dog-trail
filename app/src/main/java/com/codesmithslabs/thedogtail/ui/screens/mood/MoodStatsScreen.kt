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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.codesmithslabs.thedogtail.R
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
    onEvent: (MoodContract.Event) -> Unit,
    modifier: Modifier = Modifier
) {
    if (state.showAddMoodDialog) {
        MoodSelectionDialog(
            state = state,
            onEvent = onEvent
        )
    }

    if (state.showHistory) {
        MoodHistoryScreen(state, onEvent, modifier)
    } else {
        Scaffold(
            modifier = modifier,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            stringResource(R.string.mood_stats_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    actions = {
                        IconButton(onClick = { onEvent(MoodContract.Event.OnHistoryClicked) }) {
                            Icon(Icons.Default.History, contentDescription = stringResource(R.string.mood_stats_history))
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.background
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
                    Icon(Icons.Default.KeyboardArrowLeft, contentDescription = stringResource(R.string.mood_stats_previous_month))
                }
                
                Text(
                    text = "${state.selectedMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${state.selectedMonth.year}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = { onEvent(MoodContract.Event.OnMonthChanged(state.selectedMonth.plusMonths(1))) }) {
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = stringResource(R.string.mood_stats_next_month))
                }
            }

            // Calendar Grid
            val daysInMonth = state.selectedMonth.lengthOfMonth()
            val firstDayOfWeek = state.selectedMonth.atDay(1).dayOfWeek.value // 1=Mon, 7=Sun

            // Weekday Headers
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf(
                    stringResource(R.string.mood_stats_day_mo),
                    stringResource(R.string.mood_stats_day_tu),
                    stringResource(R.string.mood_stats_day_we),
                    stringResource(R.string.mood_stats_day_th),
                    stringResource(R.string.mood_stats_day_fr),
                    stringResource(R.string.mood_stats_day_sa),
                    stringResource(R.string.mood_stats_day_su)
                ).forEach { day ->
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
            Text(text = mood.moodEmoji, style = MaterialTheme.typography.headlineLarge)
            Text(
                text = mood.feeling.ifEmpty { mood.moodType },
                style = MaterialTheme.typography.labelSmall,
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
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(R.string.common_add),
                    tint = BrandBlue
                )
            }
            Text(
                text = stringResource(R.string.mood_stats_today),
                style = MaterialTheme.typography.labelSmall,
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
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Ghost face placeholder if desired, or just empty circle
                Text(
                    stringResource(R.string.mood_stats_placeholder_face),
                    color = MaterialTheme.colorScheme.outlineVariant,
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            Text(
                text = stringResource(R.string.mood_stats_mood),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outlineVariant
            )
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (isFuture) MaterialTheme.colorScheme.outlineVariant else TextPrimary
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
        containerColor = MaterialTheme.colorScheme.surface
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
        stringResource(R.string.mood_label_great) to "🤩",
        stringResource(R.string.mood_label_good) to "😊",
        stringResource(R.string.mood_label_okay) to "😐",
        stringResource(R.string.mood_label_not_good) to "😢",
        stringResource(R.string.mood_label_bad) to "😡"
    )

    Text(
        stringResource(R.string.mood_stats_how_today),
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
                    style = if (isSelected) {
                        MaterialTheme.typography.displayMedium
                    } else {
                        MaterialTheme.typography.displaySmall
                    }
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
            disabledContainerColor = MaterialTheme.colorScheme.outlineVariant
        ),
        shape = RoundedCornerShape(28.dp)
    ) {
        Text(
            text = if (state.selectedMoodType != null) {
                stringResource(R.string.mood_stats_i_feel, state.selectedMoodType.orEmpty())
            } else {
                stringResource(R.string.mood_stats_select_mood)
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MoodStep2(state: MoodContract.State, onEvent: (MoodContract.Event) -> Unit) {
    val moodFeelings = mapOf(
        stringResource(R.string.mood_label_great) to listOf(
            stringResource(R.string.mood_feeling_happy),
            stringResource(R.string.mood_feeling_brave),
            stringResource(R.string.mood_feeling_motivated),
            stringResource(R.string.mood_feeling_creative),
            stringResource(R.string.mood_feeling_confident),
            stringResource(R.string.mood_feeling_calm),
            stringResource(R.string.mood_feeling_grateful),
            stringResource(R.string.mood_feeling_peaceful),
            stringResource(R.string.mood_feeling_excited),
            stringResource(R.string.mood_feeling_loved),
            stringResource(R.string.mood_feeling_hopeful),
            stringResource(R.string.mood_feeling_inspired),
            stringResource(R.string.mood_feeling_proud),
            stringResource(R.string.mood_feeling_euphoric),
            stringResource(R.string.mood_feeling_nostalgic)
        ),
        stringResource(R.string.mood_label_good) to listOf(
            stringResource(R.string.mood_feeling_happy),
            stringResource(R.string.mood_feeling_calm),
            stringResource(R.string.mood_feeling_relaxed),
            stringResource(R.string.mood_feeling_content),
            stringResource(R.string.mood_feeling_grateful),
            stringResource(R.string.mood_feeling_optimistic),
            stringResource(R.string.mood_feeling_productive)
        ),
        stringResource(R.string.mood_label_okay) to listOf(
            stringResource(R.string.mood_feeling_neutral),
            stringResource(R.string.mood_feeling_tired),
            stringResource(R.string.mood_feeling_bored),
            stringResource(R.string.mood_feeling_distracted),
            stringResource(R.string.mood_feeling_busy),
            stringResource(R.string.mood_feeling_normal)
        ),
        stringResource(R.string.mood_label_not_good) to listOf(
            stringResource(R.string.mood_feeling_anxious),
            stringResource(R.string.mood_feeling_stressed),
            stringResource(R.string.mood_feeling_lonely),
            stringResource(R.string.mood_feeling_sad),
            stringResource(R.string.mood_feeling_frustrated),
            stringResource(R.string.mood_feeling_disappointed)
        ),
        stringResource(R.string.mood_label_bad) to listOf(
            stringResource(R.string.mood_feeling_angry),
            stringResource(R.string.mood_feeling_depressed),
            stringResource(R.string.mood_feeling_hopeless),
            stringResource(R.string.mood_feeling_heartbroken),
            stringResource(R.string.mood_feeling_exhausted)
        )
    )
    
    val currentFeelings = moodFeelings[state.selectedMoodType] ?: emptyList()

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Header with Back Button
        Box(modifier = Modifier.fillMaxWidth()) {
            IconButton(
                onClick = { onEvent(MoodContract.Event.OnBackStep) },
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = stringResource(R.string.common_back))
            }
            
            Text(
                text = stringResource(
                    R.string.mood_stats_describe_feelings,
                    state.selectedMoodType.orEmpty()
                ),
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
                        .border(1.dp, if (isSelected) BrandBlue else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
                        .clickable { onEvent(MoodContract.Event.OnFeelingOptionSelected(feeling)) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = feeling,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else TextPrimary,
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
                disabledContainerColor = MaterialTheme.colorScheme.outlineVariant
            ),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text(
                text = if (state.selectedFeeling != null) {
                    stringResource(R.string.mood_stats_i_feel, state.selectedFeeling.orEmpty())
                } else {
                    stringResource(R.string.mood_stats_select_feeling)
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
