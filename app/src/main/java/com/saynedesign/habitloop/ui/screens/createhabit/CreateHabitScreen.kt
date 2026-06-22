package com.saynedesign.habitloop.ui.screens.createhabit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import java.text.SimpleDateFormat // Optional: If you need any formatting
import java.util.*

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
            // Permission denied, handle accordingly (e.g. show a snackbar)
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
            item {
                CreateHabitSection {
                    HabitTypeToggle(
                        isOneTime = state.isOneTime,
                        onToggle = { onEvent(CreateHabitContract.Event.OnToggleOneTime(it)) }
                    )
                }
            }

            item {
                CreateHabitSection {
                    Text(
                        text = if (state.isOneTime) {
                            stringResource(R.string.create_habit_task_name)
                        } else {
                            stringResource(R.string.create_habit_habit_name)
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    HabitOutlinedTextField(
                        value = state.habitName,
                        onValueChange = { onEvent(CreateHabitContract.Event.OnNameChange(it)) },
                        placeholder = if (state.isOneTime) {
                            stringResource(R.string.create_habit_task_name)
                        } else {
                            stringResource(R.string.create_habit_habit_name)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            if (!state.isOneTime) {
                item {
                    CreateHabitSection {
                        Text(
                            text = "Habit Type",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        HabitTypeSelector(
                            selectedType = state.habitType,
                            onTypeSelect = { onEvent(CreateHabitContract.Event.OnTypeChange(it)) }
                        )
                        
                        // Show additional inputs if not Yes/No
                        AnimatedVisibility(
                            visible = state.habitType != CreateHabitContract.HabitType.YES_NO,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(modifier = Modifier.padding(top = 16.dp)) {
                                Text(
                                    text = "Target",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
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
                                    
                                    // Target Value Input
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

                                    // Unit Input
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
                    }
                }
            }

            item {
                CreateHabitSection {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.create_habit_icon),
                            style = MaterialTheme.typography.titleMedium,
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
            }

            item {
                CreateHabitSection {
                    Text(
                        text = stringResource(R.string.create_habit_color),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    ColorPicker(
                        selectedColor = state.habitColor,
                        onColorSelect = { onEvent(CreateHabitContract.Event.OnColorChange(it)) }
                    )
                }
            }

            if (!state.isOneTime) {
                item {
                    CreateHabitSection {
                        Text(
                            text = stringResource(R.string.create_habit_repeat),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        FrequencySelector(
                            selectedFrequency = state.frequency,
                            onFrequencySelect = { onEvent(CreateHabitContract.Event.OnFrequencyChange(it)) }
                        )
                    }
                }

                item {
                    CreateHabitSection {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.create_habit_on_these_days),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    stringResource(R.string.create_habit_all_day),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Checkbox(
                                    checked = state.selectedDays.size == 7,
                                    onCheckedChange = { isChecked ->
                                        if (isChecked) {
                                            (1..7).forEach { onEvent(CreateHabitContract.Event.OnDayToggle(it)) }
                                        }
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = MaterialTheme.colorScheme.primary,
                                        uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                        DaySelector(
                            selectedDays = state.selectedDays,
                            onDayToggle = { onEvent(CreateHabitContract.Event.OnDayToggle(it)) }
                        )
                    }
                }
            } else {
                item {
                    CreateHabitSection {
                        Text(
                            text = stringResource(R.string.create_habit_when),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        DatePickerRow(
                            date = state.scheduledDate,
                            onDateChange = { onEvent(CreateHabitContract.Event.OnDateChange(it)) }
                        )
                    }
                }
            }

            item {
                CreateHabitSection {
                    Text(
                        text = stringResource(R.string.create_habit_do_it_at),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TimeOfDaySelector(
                        selectedTime = state.timeOfDay,
                        onTimeSelect = { onEvent(CreateHabitContract.Event.OnTimeOfDayChange(it)) }
                    )
                }
            }

            if (!state.isOneTime) {
                item {
                    CreateHabitSection {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.create_habit_end_habit_on),
                                style = MaterialTheme.typography.titleMedium,
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
                            DatePickerRow(
                                date = state.endDate,
                                onDateChange = { onEvent(CreateHabitContract.Event.OnEndDateChange(it)) }
                            )
                        }
                    }
                }
            }

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
fun HabitTypeToggle(
    isOneTime: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(8.dp))
                .background(if (!isOneTime) MaterialTheme.colorScheme.primary else Color.Transparent)
                .clickable { onToggle(false) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.create_habit_regular),
                color = if (!isOneTime) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(8.dp))
                .background(if (isOneTime) MaterialTheme.colorScheme.primary else Color.Transparent)
                .clickable { onToggle(true) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.create_habit_one_time),
                color = if (isOneTime) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
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
fun FrequencySelector(
    selectedFrequency: CreateHabitContract.Frequency,
    onFrequencySelect: (CreateHabitContract.Frequency) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CreateHabitContract.Frequency.entries.forEach { frequency ->
            val isSelected = frequency == selectedFrequency
            Button(
                onClick = { onFrequencySelect(frequency) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                ),
                border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = frequencyLabel(frequency),
                    fontWeight = FontWeight.SemiBold
                )
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
    // HabitEntity uses 1=Mon, 7=Sun generally, but Calendar uses 1=Sun.
    // Let's assume standard UI S M T W T F S usually means Sun -> Sat.
    // So S=7 (Sun) or 1? Let's map indices 0..6 to days.
    // If we assume 1=Mon, then S(Sun)=7, S(Sat)=6.
    // Order in UI: S, M, T, W, T, F, S -> Sun, Mon, Tue, Wed, Thu, Fri, Sat
    // Values: 7, 1, 2, 3, 4, 5, 6
    
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

    val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
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
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp, 
                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = timeOfDayLabel(time),
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
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
            .clickable { timePickerDialog.show() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            stringResource(R.string.create_habit_time_label),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        Text(time, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
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
private fun frequencyLabel(frequency: CreateHabitContract.Frequency): String {
    return when (frequency) {
        CreateHabitContract.Frequency.DAILY -> stringResource(R.string.create_habit_frequency_daily)
        CreateHabitContract.Frequency.WEEKLY -> stringResource(R.string.create_habit_frequency_weekly)
        CreateHabitContract.Frequency.MONTHLY -> stringResource(R.string.create_habit_frequency_monthly)
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
