package com.saynedesign.habitloop.ui.screens.editprofile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.saynedesign.habitloop.R
import com.saynedesign.habitloop.data.PrimaryGoal
import com.saynedesign.habitloop.data.ProductivityTime
import com.saynedesign.habitloop.data.MotivationStyle
import com.saynedesign.habitloop.data.ExperienceLevel
import com.saynedesign.habitloop.data.WeekStartsOn
import com.saynedesign.habitloop.ui.components.HabitOutlinedTextField
import com.saynedesign.habitloop.ui.components.headerTitleBrush

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    state: EditProfileContract.State,
    onEvent: (EditProfileContract.Event) -> Unit
) {
    val imageCropLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            onEvent(EditProfileContract.Event.OnImageSelected(it.toString()))
        }
    }

    if (state.isDatePickerVisible) {
        val datePickerState = rememberDatePickerState()
        
        DatePickerDialog(
            onDismissRequest = { onEvent(EditProfileContract.Event.OnToggleDatePicker) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onEvent(EditProfileContract.Event.OnDateSelected(datePickerState.selectedDateMillis))
                    }
                ) {
                    Text(stringResource(R.string.common_ok), color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { onEvent(EditProfileContract.Event.OnToggleDatePicker) }
                ) {
                    Text(stringResource(R.string.common_cancel), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
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
                            "Personal Profile",
                            style = MaterialTheme.typography.titleLarge.copy(brush = headerTitleBrush()),
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { onEvent(EditProfileContract.Event.OnBackClicked) }) {
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
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(24.dp)
            ) {
                Button(
                    onClick = { onEvent(EditProfileContract.Event.OnSave) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = state.name.isNotBlank() && !state.isLoading,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        text = if (state.isLoading) {
                            stringResource(R.string.common_saving)
                        } else {
                            stringResource(R.string.edit_profile_save_changes)
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Profile photo selection inside header
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clickable { 
                        imageCropLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (state.profileImageUri != null) {
                        AsyncImage(
                            model = state.profileImageUri,
                            contentDescription = "Profile Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
                
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.background, CircleShape)
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Edit photo",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Collapsible Section: Personal
            CollapsibleSection(
                title = "Personal Information",
                icon = Icons.Default.Person,
                expanded = state.personalExpanded,
                onToggle = { onEvent(EditProfileContract.Event.OnToggleSection(EditProfileContract.Section.PERSONAL)) }
            ) {
                // Name
                Text("Full Name", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                HabitOutlinedTextField(
                    value = state.name,
                    onValueChange = { onEvent(EditProfileContract.Event.OnNameChange(it)) },
                    placeholder = "Enter full name",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // DOB
                Text("Date of Birth", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Box(modifier = Modifier.clickable { onEvent(EditProfileContract.Event.OnToggleDatePicker) }) {
                    HabitOutlinedTextField(
                        value = state.dob,
                        onValueChange = {},
                        placeholder = "Select DOB (DD/MM/YYYY)",
                        trailingIcon = {
                            Icon(Icons.Default.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        enabled = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Collapsible Section: Health
            CollapsibleSection(
                title = "Health metrics",
                icon = Icons.Default.Favorite,
                expanded = state.healthExpanded,
                onToggle = { onEvent(EditProfileContract.Event.OnToggleSection(EditProfileContract.Section.HEALTH)) }
            ) {
                // Unit Switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (state.isMetric) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { if (!state.isMetric) onEvent(EditProfileContract.Event.OnToggleHeightUnit) }
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Metric (cm, kg)", 
                            fontWeight = FontWeight.Bold,
                            color = if (state.isMetric) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (!state.isMetric) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { if (state.isMetric) onEvent(EditProfileContract.Event.OnToggleHeightUnit) }
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Imperial (ft, lb)", 
                            fontWeight = FontWeight.Bold,
                            color = if (!state.isMetric) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Height
                Text("Height", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                HabitOutlinedTextField(
                    value = state.height,
                    onValueChange = { onEvent(EditProfileContract.Event.OnHeightChange(it)) },
                    placeholder = if (state.isMetric) "cm" else "ft",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Weight
                Text("Weight (Optional)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                HabitOutlinedTextField(
                    value = state.weight,
                    onValueChange = { onEvent(EditProfileContract.Event.OnWeightChange(it)) },
                    placeholder = if (state.isMetric) "kg" else "lb",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Collapsible Section: Habit Preferences
            CollapsibleSection(
                title = "Habit Preferences",
                icon = Icons.Default.Settings,
                expanded = state.preferencesExpanded,
                onToggle = { onEvent(EditProfileContract.Event.OnToggleSection(EditProfileContract.Section.PREFERENCES)) }
            ) {
                // Productive Time Select
                PreferenceSelectField(
                    label = "Most Productive Time",
                    value = state.preferredProductivityTime.name.lowercase().replaceFirstChar { it.titlecase() },
                    options = listOf(
                        ProductivityTime.MORNING to "Morning (5 AM - 12 PM)",
                        ProductivityTime.AFTERNOON to "Afternoon (12 PM - 5 PM)",
                        ProductivityTime.EVENING to "Evening (5 PM - 9 PM)",
                        ProductivityTime.NIGHT to "Night Owl (9 PM - 5 AM)"
                    ),
                    onSelect = { onEvent(EditProfileContract.Event.OnProductivityTimeChange(it)) }
                )

                // Weekly Goal (Slider)
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Text(
                        text = "Weekly Goal: ${state.weeklyGoal} days", 
                        style = MaterialTheme.typography.bodyMedium, 
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Slider(
                        value = state.weeklyGoal.toFloat(),
                        onValueChange = { onEvent(EditProfileContract.Event.OnWeeklyGoalChange(it.toInt())) },
                        valueRange = 3f..7f,
                        steps = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Reminder Window
                Text("Reminder Window (HH:MM-HH:MM)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                HabitOutlinedTextField(
                    value = state.defaultReminderWindow,
                    onValueChange = { onEvent(EditProfileContract.Event.OnReminderWindowChange(it)) },
                    placeholder = "e.g. 08:00-10:00",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Week Starts
                PreferenceSelectField(
                    label = "Week Starts On",
                    value = state.weekStartsOn.name.lowercase().replaceFirstChar { it.titlecase() },
                    options = listOf(
                        WeekStartsOn.MONDAY to "Monday",
                        WeekStartsOn.SUNDAY to "Sunday"
                    ),
                    onSelect = { onEvent(EditProfileContract.Event.OnWeekStartsOnChange(it)) }
                )

                // Timezone Dialog Selection
                var showTzDialog by remember { mutableStateOf(false) }
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Text("Timezone", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                            .clickable { showTzDialog = true }
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(state.timezone, style = MaterialTheme.typography.bodyLarge)
                            Icon(Icons.Default.ArrowDropDown, null)
                        }
                    }
                }

                if (showTzDialog) {
                    val tzIds = remember { 
                        listOf(
                            java.util.TimeZone.getDefault().id,
                            "UTC", "America/New_York", "America/Los_Angeles", "Europe/London", 
                            "Europe/Paris", "Asia/Kolkata", "Asia/Tokyo", "Asia/Singapore", "Australia/Sydney"
                        ).distinct()
                    }
                    AlertDialog(
                        onDismissRequest = { showTzDialog = false },
                        title = { Text("Select Timezone") },
                        text = {
                            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                tzIds.forEach { tz ->
                                    Text(
                                        text = tz,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                onEvent(EditProfileContract.Event.OnTimezoneChange(tz))
                                                showTzDialog = false
                                            }
                                            .padding(vertical = 12.dp, horizontal = 8.dp),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showTzDialog = false }) { Text("Close") }
                        }
                    )
                }
            }

            // Collapsible Section: Motivation
            CollapsibleSection(
                title = "Motivation & Focus",
                icon = Icons.Default.Star,
                expanded = state.motivationExpanded,
                onToggle = { onEvent(EditProfileContract.Event.OnToggleSection(EditProfileContract.Section.MOTIVATION)) }
            ) {
                // Primary Goal Select
                PreferenceSelectField(
                    label = "Primary Goal",
                    value = state.primaryGoal.name.replace("_", " ").lowercase().replaceFirstChar { it.titlecase() },
                    options = listOf(
                        PrimaryGoal.FITNESS to "Build Fitness",
                        PrimaryGoal.DISCIPLINE to "Be More Disciplined",
                        PrimaryGoal.PRODUCTIVITY to "Boost Productivity",
                        PrimaryGoal.STUDY to "Study Better",
                        PrimaryGoal.MENTAL_HEALTH to "Improve Mental Wellbeing",
                        PrimaryGoal.CUSTOM to "Custom Goal"
                    ),
                    onSelect = { onEvent(EditProfileContract.Event.OnPrimaryGoalChange(it)) }
                )

                // Motivation Style Select
                PreferenceSelectField(
                    label = "Motivation Style",
                    value = state.motivationStyle.name.replace("_", " ").lowercase().replaceFirstChar { it.titlecase() },
                    options = listOf(
                        MotivationStyle.SEEING_PROGRESS to "Seeing Progress",
                        MotivationStyle.KEEPING_STREAKS to "Maintaining Streaks",
                        MotivationStyle.LEVELING_UP to "Earning XP & Rewards",
                        MotivationStyle.ACHIEVEMENTS to "Unlocking Achievements",
                        MotivationStyle.QUOTES to "Inspirational Quotes"
                    ),
                    onSelect = { onEvent(EditProfileContract.Event.OnMotivationStyleChange(it)) }
                )

                // Experience Level Select
                PreferenceSelectField(
                    label = "Experience Level",
                    value = state.experienceLevel.name.replace("_", " ").lowercase().replaceFirstChar { it.titlecase() },
                    options = listOf(
                        ExperienceLevel.BEGINNER to "Just Getting Started",
                        ExperienceLevel.BUILDING to "Building Momentum",
                        ExperienceLevel.CONSISTENT to "Getting Consistent",
                        ExperienceLevel.ADVANCED to "Habit Master"
                    ),
                    onSelect = { onEvent(EditProfileContract.Event.OnExperienceLevelChange(it)) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun <T> PreferenceSelectField(
    label: String,
    value: String,
    options: List<Pair<T, String>>,
    onSelect: (T) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                .clickable { showDialog = true }
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(value, style = MaterialTheme.typography.bodyLarge)
                Icon(Icons.Default.ArrowDropDown, null)
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Choose $label") },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        options.forEach { (option, labelText) ->
                            Text(
                                text = labelText,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelect(option)
                                        showDialog = false
                                    }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showDialog = false }) { Text("Close") }
                }
            )
        }
    }
}

@Composable
fun CollapsibleSection(
    title: String,
    icon: ImageVector,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
            }
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    content()
                }
            }
        }
    }
}
