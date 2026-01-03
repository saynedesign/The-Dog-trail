package com.codesmithslabs.thedogtail.ui.screens.createhabit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.codesmithslabs.thedogtail.ui.components.HabitOutlinedTextField
import com.codesmithslabs.thedogtail.ui.theme.BrandBackground
import com.codesmithslabs.thedogtail.ui.theme.BrandBlue
import com.codesmithslabs.thedogtail.ui.theme.BrandPurple
import com.codesmithslabs.thedogtail.ui.theme.BrandSurface
import com.codesmithslabs.thedogtail.ui.theme.TextPrimary
import com.codesmithslabs.thedogtail.ui.theme.TextSecondary

@Composable
fun CreateHabitScreen(
    state: CreateHabitContract.State,
    onEvent: (CreateHabitContract.Event) -> Unit
) {
    Scaffold(
        topBar = {
            CreateHabitTopBar(
                onBack = { onEvent(CreateHabitContract.Event.OnBackClicked) },
                onSave = { onEvent(CreateHabitContract.Event.OnSaveClicked) }
            )
        },
        containerColor = BrandBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Text(
                    text = "WHAT DO YOU WANT TO DO?",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // Name Input
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HabitOutlinedTextField(
                            value = state.habitName,
                            onValueChange = { onEvent(CreateHabitContract.Event.OnNameChange(it)) },
                            placeholder = "Enter habit name",
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        // Icon Picker Placeholder
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(BrandBlue.copy(alpha = 0.1f))
                                .clickable { /* TODO: Open Icon Picker */ },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Book, // Default/Placeholder
                                contentDescription = "Icon",
                                tint = BrandBlue
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "HOW TO MEASURE IT?",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                TypeSelector(
                    selectedType = state.habitType,
                    onTypeSelect = { onEvent(CreateHabitContract.Event.OnTypeChange(it)) }
                )
            }

            if (state.habitType == CreateHabitContract.HabitType.NUMERIC) {
                item {
                    GoalSettings(
                        target = state.target,
                        unitName = state.unitName,
                        isAtLeast = state.isAtLeast,
                        onTargetChange = { onEvent(CreateHabitContract.Event.OnTargetChange(it)) },
                        onUnitNameChange = { onEvent(CreateHabitContract.Event.OnUnitNameChange(it)) },
                        onGoalTypeChange = { onEvent(CreateHabitContract.Event.OnGoalTypeChange(it)) }
                    )
                }
            }

            item {
                 CommitmentCard(state)
            }
            
             item {
                 AdvancedOptionsCard(
                     isExpanded = state.isAdvancedOptionsExpanded,
                     reminderEnabled = state.reminderEnabled,
                     reminderTime = state.reminderTime,
                     selectedDays = state.selectedDays,
                     onToggleExpand = { onEvent(CreateHabitContract.Event.OnToggleAdvancedOptions) },
                     onReminderToggle = { onEvent(CreateHabitContract.Event.OnReminderToggle(it)) },
                     onReminderTimeChange = { onEvent(CreateHabitContract.Event.OnReminderTimeChange(it)) },
                     onDayToggle = { onEvent(CreateHabitContract.Event.OnDayToggle(it)) }
                 )
             }
             
             item {
                 Spacer(modifier = Modifier.height(32.dp))
             }
        }
    }
}

@Composable
fun AdvancedOptionsCard(
    isExpanded: Boolean,
    reminderEnabled: Boolean,
    reminderTime: String,
    selectedDays: Set<Int>,
    onToggleExpand: () -> Unit,
    onReminderToggle: (Boolean) -> Unit,
    onReminderTimeChange: (String) -> Unit,
    onDayToggle: (Int) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Advanced Options",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = TextSecondary
                )
            }
            
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 24.dp)) {
                    Divider(color = BrandBackground)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Reminder Section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = BrandBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reminders", fontWeight = FontWeight.SemiBold)
                        }
                        Switch(
                            checked = reminderEnabled,
                            onCheckedChange = onReminderToggle,
                            colors = SwitchDefaults.colors(checkedThumbColor = BrandBlue, checkedTrackColor = BrandBlue.copy(alpha = 0.2f))
                        )
                    }
                    
                    if (reminderEnabled) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("REMINDER TIME", style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        HabitOutlinedTextField(
                            value = reminderTime,
                            onValueChange = onReminderTimeChange,
                            placeholder = "08:00",
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("DAYS", style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val days = listOf("M", "T", "W", "T", "F", "S", "S")
                            days.forEachIndexed { index, dayLabel ->
                                val dayValue = index + 1
                                val isSelected = selectedDays.contains(dayValue)
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) BrandBlue else BrandBackground)
                                        .clickable { onDayToggle(dayValue) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = dayLabel,
                                        color = if (isSelected) Color.White else TextSecondary,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreateHabitTopBar(onBack: () -> Unit, onSave: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
        }
        
        Text(
            text = "New Habit",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        
        Button(
            onClick = onSave,
            colors = ButtonDefaults.buttonColors(
                containerColor = BrandBlue,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(20.dp),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Text("Save", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun TypeSelector(
    selectedType: CreateHabitContract.HabitType,
    onTypeSelect: (CreateHabitContract.HabitType) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TypeCard(
            title = "Numeric",
            icon = Icons.Default.List, // Or similar
            isSelected = selectedType == CreateHabitContract.HabitType.NUMERIC,
            onClick = { onTypeSelect(CreateHabitContract.HabitType.NUMERIC) },
            modifier = Modifier.weight(1f)
        )
        TypeCard(
            title = "Yes / No",
            icon = Icons.Default.Check,
            isSelected = selectedType == CreateHabitContract.HabitType.YES_NO,
            onClick = { onTypeSelect(CreateHabitContract.HabitType.YES_NO) },
            modifier = Modifier.weight(1f)
        )
        TypeCard(
            title = "Timer",
            icon = Icons.Default.Timer,
            isSelected = selectedType == CreateHabitContract.HabitType.TIMER,
            onClick = { onTypeSelect(CreateHabitContract.HabitType.TIMER) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun TypeCard(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) BrandBlue else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 8.dp else 0.dp),
        modifier = modifier
            .height(100.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else TextSecondary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else TextPrimary
            )
        }
    }
}

@Composable
fun GoalSettings(
    target: String,
    unitName: String,
    isAtLeast: Boolean,
    onTargetChange: (String) -> Unit,
    onUnitNameChange: (String) -> Unit,
    onGoalTypeChange: (Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.List, // Placeholder for "Settings" icon
                    contentDescription = null,
                    tint = BrandBlue
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Goal Settings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("TARGET", style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    HabitOutlinedTextField(
                        value = target,
                        onValueChange = onTargetChange,
                        placeholder = "10",
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("UNIT NAME", style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    HabitOutlinedTextField(
                        value = unitName,
                        onValueChange = onUnitNameChange,
                        placeholder = "Pages",
                        singleLine = true
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // At Least / At Most Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(BrandBackground, RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isAtLeast) Color.White else Color.Transparent)
                        .clickable { onGoalTypeChange(true) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "At Least",
                        color = if (isAtLeast) BrandBlue else TextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (!isAtLeast) Color.White else Color.Transparent)
                        .clickable { onGoalTypeChange(false) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "At Most",
                        color = if (!isAtLeast) BrandBlue else TextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Preview Section (Mock for now, could be interactive)
            Text("PREVIEW", style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(BrandBackground, RoundedCornerShape(12.dp))
            ) {
                 Box(
                     modifier = Modifier
                         .fillMaxHeight()
                         .fillMaxWidth(0.4f)
                         .background(BrandBlue, RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)),
                     contentAlignment = Alignment.Center
                 ) {
                     Text("4 / ${target.ifEmpty { "10" }}", color = Color.White, fontWeight = FontWeight.Bold)
                 }
                 Box(
                     modifier = Modifier
                         .fillMaxHeight()
                         .weight(1f),
                     contentAlignment = Alignment.Center
                 ) {
                     Text("Swipe to log", color = TextSecondary)
                 }
            }
        }
    }
}

@Composable
fun CommitmentCard(state: CreateHabitContract.State) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF2E3244), Color(0xFF1E1E2E))
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = "YOUR COMMITMENT",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = buildString {
                        append("I will ")
                        append(state.habitName.ifEmpty { "..." })
                        append(" for ")
                        if (state.habitType == CreateHabitContract.HabitType.NUMERIC) {
                            append("${state.target} ${state.unitName}")
                        } else {
                            append("completion")
                        }
                        // Placeholder for Time and Place as they are not yet in UI inputs
                        append(" every day.")
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Normal
                )
                
                // TODO: Add visual blocks like in the design if needed, for now text is fine
            }
        }
    }
}
