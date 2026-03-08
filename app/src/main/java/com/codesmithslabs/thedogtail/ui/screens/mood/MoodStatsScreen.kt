package com.codesmithslabs.thedogtail.ui.screens.mood

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.codesmithslabs.thedogtail.R
import com.codesmithslabs.thedogtail.data.MoodEntity
import com.codesmithslabs.thedogtail.ui.components.HabitButton
import com.codesmithslabs.thedogtail.ui.components.HabitIconButton
import com.codesmithslabs.thedogtail.ui.theme.BrandBackground
import com.codesmithslabs.thedogtail.ui.theme.BrandBlue
import com.codesmithslabs.thedogtail.ui.theme.TheDogTailTheme
import com.codesmithslabs.thedogtail.ui.theme.TextPrimary
import com.codesmithslabs.thedogtail.ui.theme.TextSecondary
import java.time.LocalDate
import java.time.YearMonth
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
                            stringResource(R.string.mood_stats_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    },
                    actions = {
                        HabitIconButton(
                            icon = Icons.Default.History,
                            onClick = { onEvent(MoodContract.Event.OnHistoryClicked) },
                            contentDescription = stringResource(R.string.mood_stats_history),
                            tint = TextPrimary
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = BrandBackground
                    )
                )
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                MoodCalendarCard(state = state, onEvent = onEvent)
            }
        }
    }
}

@Composable
private fun MoodCalendarCard(
    state: MoodContract.State,
    onEvent: (MoodContract.Event) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            // Month Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onEvent(MoodContract.Event.OnMonthChanged(state.selectedMonth.minusMonths(1))) }) {
                    Icon(
                        Icons.Default.KeyboardArrowLeft,
                        contentDescription = stringResource(R.string.mood_stats_previous_month),
                        tint = TextPrimary
                    )
                }

                Text(
                    text = "${state.selectedMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${state.selectedMonth.year}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                IconButton(onClick = { onEvent(MoodContract.Event.OnMonthChanged(state.selectedMonth.plusMonths(1))) }) {
                    Icon(
                        Icons.Default.KeyboardArrowRight,
                        contentDescription = stringResource(R.string.mood_stats_next_month),
                        tint = TextPrimary
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(bottom = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // Weekday Headers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
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
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val daysInMonth = state.selectedMonth.lengthOfMonth()
            val firstDayOfWeek = state.selectedMonth.atDay(1).dayOfWeek.value // 1=Mon, 7=Sun

            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // Empty slots for start of month
                items(firstDayOfWeek - 1) {
                    Box(modifier = Modifier.aspectRatio(0.65f))
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
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        if (mood != null) {
            Text(
                text = mood.moodEmoji,
                style = MaterialTheme.typography.headlineLarge
            )
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
                    tint = BrandBlue,
                    modifier = Modifier.size(20.dp)
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
    androidx.compose.material3.ModalBottomSheet(
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
        fontWeight = FontWeight.Bold,
        color = TextPrimary
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

    HabitButton(
        text = if (state.selectedMoodType != null) {
            stringResource(R.string.mood_stats_i_feel, state.selectedMoodType.orEmpty())
        } else {
            stringResource(R.string.mood_stats_select_mood)
        },
        onClick = { onEvent(MoodContract.Event.OnSubmitMood) },
        enabled = state.selectedMoodType != null,
        modifier = Modifier.fillMaxWidth()
    )
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
                Icon(
                    Icons.Default.KeyboardArrowLeft,
                    contentDescription = stringResource(R.string.common_back),
                    tint = TextPrimary
                )
            }

            Text(
                text = stringResource(
                    R.string.mood_stats_describe_feelings,
                    state.selectedMoodType.orEmpty()
                ),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = TextPrimary,
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
                        .border(
                            1.dp,
                            if (isSelected) BrandBlue else MaterialTheme.colorScheme.outlineVariant,
                            RoundedCornerShape(20.dp)
                        )
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

        HabitButton(
            text = if (state.selectedFeeling != null) {
                stringResource(R.string.mood_stats_i_feel, state.selectedFeeling.orEmpty())
            } else {
                stringResource(R.string.mood_stats_select_feeling)
            },
            onClick = { onEvent(MoodContract.Event.OnSubmitFeeling) },
            enabled = state.selectedFeeling != null,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ─── Previews ────────────────────────────────────────────────────────────────

@Preview(name = "Mood Stats – Light", showBackground = true, backgroundColor = 0xFFE8EAF6)
@Composable
private fun MoodStatsScreenPreview() {
    TheDogTailTheme(darkTheme = false) {
        MoodStatsScreen(
            state = MoodContract.State(
                selectedMonth = YearMonth.of(2024, 12),
                moods = mapOf(
                    1 to MoodEntity(moodType = "Great", moodEmoji = "🤩", timestamp = 0L, feeling = "Happy"),
                    2 to MoodEntity(moodType = "Good", moodEmoji = "😊", timestamp = 0L, feeling = "Calm"),
                    3 to MoodEntity(moodType = "Okay", moodEmoji = "😐", timestamp = 0L, feeling = "Neutral"),
                    4 to MoodEntity(moodType = "Bad", moodEmoji = "😡", timestamp = 0L, feeling = "Angry"),
                )
            ),
            onEvent = {}
        )
    }
}

@Preview(name = "Mood Stats – Dark", showBackground = true, backgroundColor = 0xFF01040E)
@Composable
private fun MoodStatsScreenDarkPreview() {
    TheDogTailTheme(darkTheme = true) {
        MoodStatsScreen(
            state = MoodContract.State(selectedMonth = YearMonth.of(2024, 12)),
            onEvent = {}
        )
    }
}

@Preview(name = "Mood Step 1 – Selected", showBackground = true)
@Composable
private fun MoodStep1Preview() {
    TheDogTailTheme(darkTheme = false) {
        Column(modifier = Modifier.padding(24.dp)) {
            MoodStep1(state = MoodContract.State(selectedMoodType = "Great"), onEvent = {})
        }
    }
}
