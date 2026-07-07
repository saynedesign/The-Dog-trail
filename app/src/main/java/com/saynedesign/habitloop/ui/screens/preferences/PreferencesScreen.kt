package com.saynedesign.habitloop.ui.screens.preferences

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.saynedesign.habitloop.R
import com.saynedesign.habitloop.ui.components.headerTitleBrush
import android.app.TimePickerDialog
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferencesScreen(
    state: PreferencesContract.State,
    onEvent: (PreferencesContract.Event) -> Unit
) {
    val context = LocalContext.current
    var showSoundDialog by remember { mutableStateOf(false) }

    // When the user comes back from the system overlay-permission screen with
    // the permission granted, finish enabling overlay reminders automatically.
    var pendingOverlayPermission by rememberSaveable { mutableStateOf(false) }
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME && pendingOverlayPermission) {
                if (android.provider.Settings.canDrawOverlays(context)) {
                    onEvent(PreferencesContract.Event.OnOverlayReminderToggle(true))
                }
                pendingOverlayPermission = false
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (showSoundDialog) {
        AlertDialog(
            onDismissRequest = { showSoundDialog = false },
            title = { Text("Select Reminder Sound") },
            text = {
                Column {
                    val options = listOf(
                        "alarm" to "System Alarm 🚨",
                        "notification" to "System Notification 🔔",
                        "ringtone" to "System Ringtone 🎵",
                        "mute" to "Muted 🔇"
                    )
                    options.forEach { (key, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onEvent(PreferencesContract.Event.OnOverlayReminderSoundChange(key))
                                    showSoundDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (state.overlayReminderSound == key),
                                onClick = {
                                    onEvent(PreferencesContract.Event.OnOverlayReminderSoundChange(key))
                                    showSoundDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = label, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSoundDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (state.showTimePicker && state.timePickerType != null) {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        DisposableEffect(state.timePickerType) {
            val dialog = TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    val formattedTime = String.format("%02d:%02d", hourOfDay, minute)
                    onEvent(PreferencesContract.Event.OnTimeSelected(formattedTime))
                },
                currentHour,
                currentMinute,
                true // 24 hour format
            )
            
            dialog.setOnCancelListener { onEvent(PreferencesContract.Event.OnTimePickerDismiss) }
            dialog.show()
            
            onDispose {
                dialog.dismiss()
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(R.drawable.habit_tracker_icon),
                            contentDescription = stringResource(R.string.app_name),
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.preferences_title),
                            style = MaterialTheme.typography.titleLarge.copy(brush = headerTitleBrush()),
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { onEvent(PreferencesContract.Event.OnBackClicked) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Section 1: Time of Day
            item {
                PreferenceSection {
                    PreferenceItem(
                        title = stringResource(R.string.preferences_morning),
                        value = stringResource(R.string.preferences_start_at, state.morningTime),
                        onClick = { onEvent(PreferencesContract.Event.OnTimeClick(PreferencesContract.TimePickerType.MORNING)) }
                    )
                    PreferenceItem(
                        title = stringResource(R.string.preferences_afternoon),
                        value = stringResource(R.string.preferences_start_at, state.afternoonTime),
                        onClick = { onEvent(PreferencesContract.Event.OnTimeClick(PreferencesContract.TimePickerType.AFTERNOON)) }
                    )
                    PreferenceItem(
                        title = stringResource(R.string.preferences_evening),
                        value = stringResource(R.string.preferences_start_at, state.eveningTime),
                        onClick = { onEvent(PreferencesContract.Event.OnTimeClick(PreferencesContract.TimePickerType.EVENING)) },
                        showDivider = false
                    )
                }
            }

            // Section 2: General Settings
            item {
                PreferenceSection {
                    PreferenceItem(
                        title = stringResource(R.string.preferences_first_day_of_week),
                        value = state.firstDayOfWeek,
                        onClick = { onEvent(PreferencesContract.Event.OnFirstDayOfWeekClick) }
                    )
                    PreferenceToggleItem(
                        title = stringResource(R.string.preferences_vacation_mode),
                        checked = state.isVacationMode,
                        onCheckedChange = { onEvent(PreferencesContract.Event.OnVacationModeToggle(it)) },
                        showDivider = false
                    )
                }
            }

            // Section 3: Reminders
            item {
                PreferenceSection {
                    PreferenceToggleItem(
                        title = stringResource(R.string.preferences_daily_reminder),
                        checked = state.isDailyReminderEnabled,
                        onCheckedChange = { onEvent(PreferencesContract.Event.OnDailyReminderToggle(it)) }
                    )
                    PreferenceItem(
                        title = stringResource(R.string.preferences_reminder_time),
                        value = state.reminderTime,
                        onClick = { onEvent(PreferencesContract.Event.OnTimeClick(PreferencesContract.TimePickerType.REMINDER)) },
                        showDivider = true,
                        enabled = state.isDailyReminderEnabled
                    )
                    PreferenceToggleItem(
                        title = "Overlay Reminders",
                        checked = state.isOverlayReminderEnabled,
                        onCheckedChange = { isChecked ->
                            if (isChecked) {
                                if (!android.provider.Settings.canDrawOverlays(context)) {
                                    // Remember the request so we can auto-enable
                                    // when the user returns with the permission.
                                    pendingOverlayPermission = true
                                    val intent = android.content.Intent(
                                        android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        android.net.Uri.parse("package:${context.packageName}")
                                    )
                                    context.startActivity(intent)
                                    onEvent(PreferencesContract.Event.OnOverlayReminderToggle(false))
                                } else {
                                    onEvent(PreferencesContract.Event.OnOverlayReminderToggle(true))
                                }
                            } else {
                                onEvent(PreferencesContract.Event.OnOverlayReminderToggle(false))
                            }
                        },
                        showDivider = true
                    )
                    // Applies to both overlay and notification reminders
                    PreferenceItem(
                        title = "Reminder Sound",
                        value = when (state.overlayReminderSound) {
                            "alarm" -> "System Alarm"
                            "notification" -> "System Notification"
                            "ringtone" -> "System Ringtone"
                            "mute" -> "Silent"
                            else -> state.overlayReminderSound.replaceFirstChar { it.uppercase() }
                        },
                        onClick = { showSoundDialog = true },
                        showDivider = true
                    )
                    // Behavior-driven coach: streak warnings, break check-ins,
                    // celebrations and the Sunday digest (max 1/day, 20:30)
                    PreferenceToggleItem(
                        title = "Smart Coach",
                        checked = state.isCoachEnabled,
                        onCheckedChange = { onEvent(PreferencesContract.Event.OnCoachToggle(it)) },
                        showDivider = false
                    )
                }
            }

            // Section 4: Data Actions
            item {
                PreferenceSection {
                    PreferenceItem(
                        title = stringResource(R.string.preferences_clear_cache),
                        onClick = { onEvent(PreferencesContract.Event.OnClearCacheClicked) }
                    )
                    PreferenceItem(
                        title = stringResource(R.string.preferences_restart_habits),
                        onClick = { onEvent(PreferencesContract.Event.OnRestartHabitsClicked) },
                        showDivider = false
                    )
                    /*
                    // Uncomment this item during development to seed the database with 30 days of mock history
                    PreferenceItem(
                        title = "Seed Sample Data",
                        onClick = { onEvent(PreferencesContract.Event.OnSeedDataClicked) },
                        showDivider = false
                    )
                    */
                }
            }
            
            item { 
                Spacer(modifier = Modifier.navigationBarsPadding())
                Spacer(modifier = Modifier.height(32.dp)) 
            }
        }
    }
}

@Composable
fun PreferenceSection(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 8.dp), // Padding inside the card
        content = content
    )
}

@Composable
fun PreferenceItem(
    title: String,
    value: String? = null,
    onClick: () -> Unit,
    showDivider: Boolean = true,
    enabled: Boolean = true
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (value != null) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    tint = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        if (showDivider) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
        }
    }
}

@Composable
fun PreferenceToggleItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    showDivider: Boolean = true
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp), // Slightly less padding to accommodate switch height
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurface,
                    uncheckedTrackColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
        }
        if (showDivider) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
        }
    }
}
