package com.saynedesign.habitloop.ui.screens.createhabit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.saynedesign.habitloop.R
import com.saynedesign.habitloop.ui.components.HabitOutlinedTextField
import com.saynedesign.habitloop.ui.components.headerTitleBrush
import java.text.SimpleDateFormat
import java.util.*

import com.saynedesign.habitloop.data.PrimaryGoal
import androidx.compose.ui.unit.sp

/**
 * Redesigned around progressive disclosure: a habit takes TWO decisions
 * (name + save). Days and reminder are pre-defaulted essentials; everything
 * else lives in "More options". The one-time toggle and the orphaned
 * frequency selector are gone.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateHabitScreen(
    state: CreateHabitContract.State,
    onEvent: (CreateHabitContract.Event) -> Unit
) {
    val context = LocalContext.current

    // Launcher for Android 13+ Notification Permission
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onEvent(CreateHabitContract.Event.OnReminderToggle(true))
        } else {
            onEvent(CreateHabitContract.Event.OnReminderToggle(false))
        }
    }

    // Function to check and request exact alarm permission (Android 12+)
    fun checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                context.startActivity(intent)
            }
        }
    }

    var showMoreOptions by rememberSaveable { mutableStateOf(false) }
    var showCustomDays by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CreateHabitTopBar(
                onBack = { onEvent(CreateHabitContract.Event.OnBackClicked) },
                onSave = { onEvent(CreateHabitContract.Event.OnSaveClicked) }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Suggested Habits Section based on Primary Goal
            if (state.habitId == null) {
                val suggestions = when (state.userPrimaryGoal) {
                    PrimaryGoal.FITNESS -> listOf(
                        Triple("Go for a Run", "🏃", 0xFFFFAB91),
                        Triple("Daily Yoga", "🧘", 0xFFA5D6A7),
                        Triple("Drink 3L Water", "💧", 0xFF90CAF9)
                    )
                    PrimaryGoal.DISCIPLINE -> listOf(
                        Triple("Wake up early", "⏰", 0xFFFFCC80),
                        Triple("Clean my room", "🧹", 0xFFCE93D8),
                        Triple("Plan my tomorrow", "📅", 0xFFB39DDB)
                    )
                    PrimaryGoal.PRODUCTIVITY -> listOf(
                        Triple("Deep Work Session", "⚡", 0xFF90CAF9),
                        Triple("Inbox Zero", "💻", 0xFF80DEEA),
                        Triple("Read 10 pages", "📚", 0xFFA5D6A7)
                    )
                    PrimaryGoal.STUDY -> listOf(
                        Triple("Review notes", "📝", 0xFFFFF9C4),
                        Triple("Study 1 hour", "🎓", 0xFFB39DDB),
                        Triple("Learn new skill", "🧠", 0xFF80CBC4)
                    )
                    PrimaryGoal.MENTAL_HEALTH -> listOf(
                        Triple("10m Meditation", "🧘", 0xFFF48FB1),
                        Triple("Gratitude Journal", "📓", 0xFFF8BBD0),
                        Triple("Walk in Nature", "🕊️", 0xFFA5D6A7)
                    )
                    else -> listOf(
                        Triple("Track my mood", "⭐️", 0xFFCE93D8),
                        Triple("Do something creative", "🎨", 0xFFFFAB91),
                        Triple("Save $5", "🏆", 0xFFFFF9C4)
                    )
                }

                item {
                    CreateHabitSection {
                        Text(
                            text = "Suggested Habits",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(suggestions) { (name, icon, color) ->
                                Card(
                                    modifier = Modifier.clickable {
                                        onEvent(CreateHabitContract.Event.OnNameChange(name))
                                        onEvent(CreateHabitContract.Event.OnIconChange(icon))
                                        onEvent(CreateHabitContract.Event.OnColorChange(color))
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    ),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = icon, fontSize = 18.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Essential 1: Name — with live icon/color preview that opens the icon sheet
            item {
                CreateHabitSection {
                    Text(
                        text = stringResource(R.string.create_habit_habit_name),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(state.habitColor).copy(alpha = 0.35f))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                                .clickable { onEvent(CreateHabitContract.Event.OnToggleIconPicker(true)) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = state.habitIcon, style = MaterialTheme.typography.headlineSmall)
                        }
                        HabitOutlinedTextField(
                            value = state.habitName,
                            onValueChange = { onEvent(CreateHabitContract.Event.OnNameChange(it)) },
                            placeholder = stringResource(R.string.create_habit_habit_name),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    Text(
                        text = "Icon & color are picked for you — tap the icon to change them.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Essential 2: Days — presets first, custom on demand
            item {
                CreateHabitSection {
                    Text(
                        text = stringResource(R.string.create_habit_on_these_days),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    val everyDay = setOf(1, 2, 3, 4, 5, 6, 7)
                    val weekdays = setOf(1, 2, 3, 4, 5)
                    val isEveryDay = state.selectedDays == everyDay && !showCustomDays
                    val isWeekdays = state.selectedDays == weekdays && !showCustomDays
                    val isCustom = showCustomDays || (!isEveryDay && !isWeekdays)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DayPresetChip("Every day", isEveryDay, Modifier.weight(1f)) {
                            showCustomDays = false
                            onEvent(CreateHabitContract.Event.OnDaysPreset(everyDay))
                        }
                        DayPresetChip("Weekdays", isWeekdays, Modifier.weight(1f)) {
                            showCustomDays = false
                            onEvent(CreateHabitContract.Event.OnDaysPreset(weekdays))
                        }
                        DayPresetChip("Custom", isCustom, Modifier.weight(1f)) {
                            showCustomDays = true
                        }
                    }

                    AnimatedVisibility(
                        visible = isCustom,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            DaySelector(
                                selectedDays = state.selectedDays,
                                onDayToggle = { onEvent(CreateHabitContract.Event.OnDayToggle(it)) }
                            )
                        }
                    }
                }
            }

            // Essential 3: Reminder
            item {
                CreateHabitSection {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.create_habit_set_reminder),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Switch(
                            checked = state.reminderEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        onEvent(CreateHabitContract.Event.OnReminderToggle(true))
                                    }
                                    checkExactAlarmPermission()
                                } else {
                                    onEvent(CreateHabitContract.Event.OnReminderToggle(false))
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                    if (state.reminderEnabled) {
                        ReminderTimePicker(
                            time = state.reminderTime,
                            onTimeChange = { onEvent(CreateHabitContract.Event.OnReminderTimeChange(it)) }
                        )
                    }
                }
            }

            // ---- More options (advanced, collapsed by default) ----
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(16.dp),
                    tonalElevation = 1.dp
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showMoreOptions = !showMoreOptions }
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "More options",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Track an amount, motivation note, colors, end date",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = if (showMoreOptions) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        AnimatedVisibility(
                            visible = showMoreOptions,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                // Tracking type + target
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text(
                                        text = "How to track",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    HabitTypeSelector(
                                        selectedType = state.habitType,
                                        onTypeSelect = { onEvent(CreateHabitContract.Event.OnTypeChange(it)) }
                                    )
                                    AnimatedVisibility(
                                        visible = state.habitType != CreateHabitContract.HabitType.YES_NO,
                                        enter = expandVertically() + fadeIn(),
                                        exit = shrinkVertically() + fadeOut()
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // At Least / At Most Toggle
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(56.dp)
                                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                                                    .clickable { onEvent(CreateHabitContract.Event.OnTargetRuleToggle(!state.isAtLeast)) }
                                                    .padding(horizontal = 16.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = if (state.isAtLeast) "At least" else "At most",
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }

                                            OutlinedTextField(
                                                value = state.target,
                                                onValueChange = { onEvent(CreateHabitContract.Event.OnTargetChange(it)) },
                                                placeholder = { Text("e.g. 10") },
                                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                                    keyboardType = KeyboardType.Number
                                                ),
                                                singleLine = true,
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                                )
                                            )

                                            OutlinedTextField(
                                                value = state.unitName,
                                                onValueChange = { onEvent(CreateHabitContract.Event.OnUnitChange(it)) },
                                                placeholder = { Text(if (state.habitType == CreateHabitContract.HabitType.TIMER) "mins" else "times") },
                                                singleLine = true,
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                                )
                                            )
                                        }
                                    }
                                }

                                // Motivation note
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "Motivation / Why (Optional)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    HabitOutlinedTextField(
                                        value = state.description,
                                        onValueChange = { onEvent(CreateHabitContract.Event.OnDescriptionChange(it)) },
                                        placeholder = "e.g. To feel energized and healthy",
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = false
                                    )
                                }

                                // Icon override
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = stringResource(R.string.create_habit_icon),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        TextButton(onClick = { onEvent(CreateHabitContract.Event.OnToggleIconPicker(true)) }) {
                                            Text(stringResource(R.string.create_habit_view_all), color = MaterialTheme.colorScheme.primary)
                                            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    IconPicker(
                                        selectedIcon = state.habitIcon,
                                        onIconSelect = { onEvent(CreateHabitContract.Event.OnIconChange(it)) }
                                    )
                                }

                                // Color override
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = stringResource(R.string.create_habit_color),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    ColorPicker(
                                        selectedColor = state.habitColor,
                                        onColorSelect = { onEvent(CreateHabitContract.Event.OnColorChange(it)) }
                                    )
                                }

                                // Time of day (display/sort category only)
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = stringResource(R.string.create_habit_do_it_at),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    TimeOfDaySelector(
                                        selectedTime = state.timeOfDay,
                                        onTimeSelect = { onEvent(CreateHabitContract.Event.OnTimeOfDayChange(it)) }
                                    )
                                }

                                // End date
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = stringResource(R.string.create_habit_end_habit_on),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Switch(
                                            checked = state.endDateEnabled,
                                            onCheckedChange = { onEvent(CreateHabitContract.Event.OnEndDateToggle(it)) },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                                checkedTrackColor = MaterialTheme.colorScheme.primary
                                            )
                                        )
                                    }
                                    if (state.endDateEnabled && state.endDate != null) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        DatePickerRow(
                                            date = state.endDate,
                                            onDateChange = { onEvent(CreateHabitContract.Event.OnEndDateChange(it)) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = { onEvent(CreateHabitContract.Event.OnSaveClicked) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = stringResource(R.string.common_save),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.navigationBarsPadding())
                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        if (state.showIconPicker) {
            AllIconsSheet(
                selectedIcon = state.habitIcon,
                onIconSelect = {
                    onEvent(CreateHabitContract.Event.OnIconChange(it))
                    onEvent(CreateHabitContract.Event.OnToggleIconPicker(false))
                },
                onDismiss = { onEvent(CreateHabitContract.Event.OnToggleIconPicker(false)) }
            )
        }
    }
}

@Composable
private fun DayPresetChip(
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1
        )
    }
}

@Composable
fun CreateHabitSection(
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllIconsSheet(
    selectedIcon: String,
    onIconSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val allIcons = listOf(
        "📝", "💧", "🏃", "🧘", "📚", "💊", "🥦", "🍎", "🥑", "🥕",
        "🏋️", "🤸", "🚴", "🏊", "🧗", "⛹️", "🏌️", "🏇", "🏄", "🚣",
        "🛌", "🚿", "🧹", "🧺", "🧼", "🧽", "🧴", "🪥", "🦷", "💆",
        "🐶", "🐱", "🐭", "🐹", "🐰", "🦊", "🐻", "🐼", "🐨", "🐯",
        "🦁", "🐮", "🐷", "🐸", "🐵", "🐔", "🐧", "🐦", "🐤", "🐣",
        "🚗", "🚕", "🚙", "🚌", "🚎", "🏎️", "🚓", "🚑", "🚒", "🚐",
        "💻", "🖥️", "🖨️", "🖱️", "🖲️", "🕹️", "🗜️", "💽", "💾", "💿",
        "📕", "📖", "📗", "📘", "📙", "📚", "📓", "📒", "📃", "📜",
        "🎨", "🎬", "🎤", "🎧", "🎼", "🎹", "🥁", "🎷", "🎺", "🎸",
        "⚽", "🏀", "🏈", "⚾", "🥎", "🎾", "🏐", "🏉", "🥏", "🎱",
        "💰", "💴", "💵", "💶", "💷", "💸", "💳", "🧾", "💹", "💱"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = stringResource(R.string.create_habit_select_icon),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.heightIn(max = 400.dp)
            ) {
                val chunkedIcons = allIcons.chunked(5)
                items(chunkedIcons) { rowIcons ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        rowIcons.forEach { icon ->
                            val isSelected = icon == selectedIcon
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface)
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable { onIconSelect(icon) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = icon, style = MaterialTheme.typography.headlineSmall)
                            }
                        }
                        // Fill remaining space
                        repeat(5 - rowIcons.size) {
                            Spacer(modifier = Modifier.size(56.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HabitTypeSelector(
    selectedType: CreateHabitContract.HabitType,
    onTypeSelect: (CreateHabitContract.HabitType) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val options = listOf(
            CreateHabitContract.HabitType.YES_NO to "Yes/No",
            CreateHabitContract.HabitType.NUMERIC to "Numeric",
            CreateHabitContract.HabitType.TIMER to "Timer"
        )
        options.forEach { (type, label) ->
            val isSelected = type == selectedType
            Button(
                onClick = { onTypeSelect(type) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                ),
                border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp) // Avoid text cutoff on small screens
            ) {
                Text(
                    text = label,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
fun IconPicker(
    selectedIcon: String,
    onIconSelect: (String) -> Unit
) {
    val icons = listOf("🏈", "🏆", "🎖️", "🏀", "⛸️", "📝", "💧", "🏃", "🧘", "📚", "💊", "🥦")

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(icons) { icon ->
            val isSelected = icon == selectedIcon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onIconSelect(icon) }
                    .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, style = MaterialTheme.typography.headlineSmall)
            }
        }
    }
}

@Composable
fun ColorPicker(
    selectedColor: Long,
    onColorSelect: (Long) -> Unit
) {
    val colors = listOf(
        0xFFFFF9C4, 0xFFFFCC80, 0xFFBCAAA4, 0xFFA1887F, 0xFFFF8A80,
        0xFFFFAB91, 0xFFF48FB1, 0xFFF8BBD0, 0xFFCE93D8, 0xFFB39DDB,
        0xFF90CAF9, 0xFF80CBC4, 0xFF80DEEA, 0xFFA5D6A7, 0xFFC0CA33
    )

    // Simple FlowRow replacement using nested Rows since FlowRow is experimental/newer
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        val rows = colors.chunked(5)
        for (rowColors in rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (color in rowColors) {
                    val isSelected = selectedColor == color
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(color))
                            .clickable { onColorSelect(color) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
                // Fill remaining space if row is not full
                repeat(5 - rowColors.size) {
                    Spacer(modifier = Modifier.size(48.dp))
                }
            }
        }
    }
}

@Composable
fun DaySelector(
    selectedDays: Set<Int>,
    onDayToggle: (Int) -> Unit
) {
    val days = listOf(
        stringResource(R.string.create_habit_day_s),
        stringResource(R.string.create_habit_day_m),
        stringResource(R.string.create_habit_day_t),
        stringResource(R.string.create_habit_day_w),
        stringResource(R.string.create_habit_day_t),
        stringResource(R.string.create_habit_day_f),
        stringResource(R.string.create_habit_day_s)
    )
    // Order in UI: S, M, T, W, T, F, S -> Sun, Mon, Tue, Wed, Thu, Fri, Sat
    // Values: 7, 1, 2, 3, 4, 5, 6 (1=Mon..7=Sun)
    val dayValues = listOf(7, 1, 2, 3, 4, 5, 6)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        days.forEachIndexed { index, label ->
            val dayValue = dayValues[index]
            val isSelected = selectedDays.contains(dayValue)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onDayToggle(dayValue) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun DatePickerRow(
    date: Long,
    onDateChange: (Long) -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = date

    val datePickerDialog = android.app.DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            onDateChange(calendar.timeInMillis)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val locale = androidx.compose.ui.platform.LocalConfiguration.current.locales[0]
    val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", locale)
    val dateString = dateFormat.format(Date(date))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
            .clickable { datePickerDialog.show() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = dateString, color = MaterialTheme.colorScheme.onBackground)
        }
        Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
    }
}

@Composable
fun TimeOfDaySelector(
    selectedTime: CreateHabitContract.TimeOfDay,
    onTimeSelect: (CreateHabitContract.TimeOfDay) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val options = listOf(CreateHabitContract.TimeOfDay.MORNING, CreateHabitContract.TimeOfDay.AFTERNOON, CreateHabitContract.TimeOfDay.EVENING)
        options.forEach { time ->
            val isSelected = time == selectedTime
            Button(
                onClick = { onTimeSelect(time) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                ),
                border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = timeOfDayLabel(time),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun ReminderTimePicker(
    time: String,
    onTimeChange: (String) -> Unit
) {
    val context = LocalContext.current
    val timeParts = time.split(":")
    val hour = timeParts.getOrNull(0)?.toIntOrNull() ?: 8
    val minute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0

    val timePickerDialog = android.app.TimePickerDialog(
        context,
        { _, hourOfDay, m ->
            onTimeChange(String.format("%02d:%02d", hourOfDay, m))
        },
        hour,
        minute,
        true
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
            .clickable { timePickerDialog.show() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Alarm,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Reminder Time",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = time,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun CreateHabitTopBar(
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onBack) {
            Icon(
                Icons.Default.Close,
                contentDescription = stringResource(R.string.common_close),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.habit_tracker_icon),
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.create_habit_title),
                style = MaterialTheme.typography.titleLarge.copy(brush = headerTitleBrush()),
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.size(48.dp))
    }
}

@Composable
private fun timeOfDayLabel(time: CreateHabitContract.TimeOfDay): String {
    return when (time) {
        CreateHabitContract.TimeOfDay.MORNING -> stringResource(R.string.create_habit_time_morning)
        CreateHabitContract.TimeOfDay.AFTERNOON -> stringResource(R.string.create_habit_time_afternoon)
        CreateHabitContract.TimeOfDay.EVENING -> stringResource(R.string.create_habit_time_evening)
        CreateHabitContract.TimeOfDay.ANYTIME -> stringResource(R.string.create_habit_all_day)
    }
}
