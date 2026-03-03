package com.codesmithslabs.thedogtail.ui.screens.preferences

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.codesmithslabs.thedogtail.R
import com.codesmithslabs.thedogtail.ui.theme.BrandSurface
import com.codesmithslabs.thedogtail.ui.theme.TextPrimary
import com.codesmithslabs.thedogtail.ui.theme.TextSecondary
import com.codesmithslabs.thedogtail.ui.theme.BrandBlue
import android.app.TimePickerDialog
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferencesScreen(
    state: PreferencesContract.State,
    onEvent: (PreferencesContract.Event) -> Unit
) {
    val context = LocalContext.current

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
                    Text(
                        stringResource(R.string.preferences_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onEvent(PreferencesContract.Event.OnBackClicked) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back),
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BrandSurface
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
                        showDivider = false,
                        enabled = state.isDailyReminderEnabled
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
                }
            }
            
            item { Spacer(modifier = Modifier.height(32.dp)) }
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
                color = if (enabled) TextPrimary else TextSecondary
            )
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (value != null) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    tint = if (enabled) TextSecondary.copy(alpha = 0.5f) else TextSecondary.copy(alpha = 0.2f),
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
                color = TextPrimary
            )
            
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = BrandBlue,
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
