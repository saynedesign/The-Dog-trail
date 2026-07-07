package com.saynedesign.habitloop.ui.screens.userinfo

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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saynedesign.habitloop.R
import com.saynedesign.habitloop.data.PrimaryGoal
import com.saynedesign.habitloop.data.ProductivityTime
import com.saynedesign.habitloop.data.MotivationStyle
import com.saynedesign.habitloop.data.ExperienceLevel
import com.saynedesign.habitloop.data.WeekStartsOn

// Semantic accent colors (intentionally constant across themes)
private val SuccessCheckGreen = Color(0xFF4CAF50)
private val TipStarGold = Color(0xFFFBC02D)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserInfoScreen(
    state: UserInfoContract.State,
    onEvent: (UserInfoContract.Event) -> Unit
) {
    val totalSteps = 7
    var currentStep by rememberSaveable { mutableIntStateOf(0) }
    val scrollState = rememberScrollState()

    val isLastStep = currentStep == totalSteps - 1
    val isStepValid = when (currentStep) {
        0 -> state.name.isNotBlank()
        6 -> state.height.isNotBlank() && state.height.toFloatOrNull()?.let { it > 0 } == true
        else -> true
    }

    val primaryButtonText = if (isLastStep) {
        if (state.isLoading) stringResource(R.string.common_saving) else "Let's Go! 🚀"
    } else {
        "Continue"
    }

    fun moveNext() {
        if (isLastStep) {
            onEvent(UserInfoContract.Event.OnSubmit)
            return
        }
        currentStep += 1
    }

    // Theme-driven palette (adapts to light/dark)
    val cardBg = MaterialTheme.colorScheme.surface
    val buttonBlue = MaterialTheme.colorScheme.primary

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (currentStep > 0) {
                            currentStep -= 1
                        }
                    },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f), CircleShape)
                        .size(40.dp),
                    enabled = currentStep > 0
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.common_back),
                        tint = if (currentStep > 0) {
                            MaterialTheme.colorScheme.onBackground
                        } else {
                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                        }
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Thin progress steps bar (Matches screenshot top progress indicator)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until totalSteps) {
                        Box(
                            modifier = Modifier
                                .height(4.dp)
                                .width(if (i == currentStep) 24.dp else 8.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    if (i <= currentStep) {
                                        buttonBlue
                                    } else {
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                                    }
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                TextButton(
                    onClick = {
                        if (isLastStep) {
                            onEvent(UserInfoContract.Event.OnSubmit)
                        } else {
                            currentStep += 1
                        }
                    },
                    enabled = !state.isLoading
                ) {
                    Text(
                        text = stringResource(R.string.common_skip),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Button(
                    onClick = { moveNext() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !state.isLoading && isStepValid,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonBlue,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = buttonBlue.copy(alpha = 0.4f),
                        disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                    )
                ) {
                    Text(
                        text = primaryButtonText,
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
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            val stepTitle = when (currentStep) {
                0 -> "What's your name?"
                1 -> "What's your primary goal?"
                2 -> "When are you most productive?"
                3 -> "How many days per week is your goal?"
                4 -> "What motivates you the most?"
                5 -> "What's your current experience level?"
                else -> "Almost done!"
            }

            val stepSubtitle = when (currentStep) {
                0 -> "This is how we'll address you."
                1 -> "Choose what matters most to you."
                2 -> "We'll schedule smart reminders around it."
                3 -> "Set a target for consistency."
                4 -> "We'll keep you motivated in the way that works best."
                5 -> "This helps us guide you better."
                else -> "A few quick details to personalize your habit journey."
            }

            Text(
                text = stepTitle,
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 26.sp),
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stepSubtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Step Content switcher
            Crossfade(targetState = currentStep, label = "step_crossfade") { step ->
                when (step) {
                    0 -> NameStep(state, onEvent, cardBg, buttonBlue)
                    1 -> GoalStep(state, onEvent, cardBg, buttonBlue)
                    2 -> ProductivityStep(state, onEvent, cardBg, buttonBlue)
                    3 -> WeeklyGoalStep(state, onEvent, cardBg, buttonBlue)
                    4 -> MotivationStep(state, onEvent, cardBg, buttonBlue)
                    5 -> ExperienceStep(state, onEvent, cardBg, buttonBlue)
                    else -> FinalDetailsStep(state, onEvent, cardBg, buttonBlue)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun NameStep(
    state: UserInfoContract.State,
    onEvent: (UserInfoContract.Event) -> Unit,
    cardBg: Color,
    buttonBlue: Color
) {
    val initials = remember(state.name) {
        if (state.name.isBlank()) "?"
        else state.name.trim().split("\\s+".toRegex()).take(2).map { it.first().uppercase() }.joinToString("")
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Initials circle (Matches screenshot Screen 2 initials avatar)
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(cardBg)
                .border(2.dp, buttonBlue, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(36.dp))

        // Full Name Text Field
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBg, RoundedCornerShape(16.dp))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                if (state.name.isEmpty()) {
                    Text("Your name", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 16.sp)
                }
                BasicTextField(
                    value = state.name,
                    onValueChange = { onEvent(UserInfoContract.Event.OnNameChange(it)) },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp),
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            if (state.name.isNotEmpty()) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = SuccessCheckGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun GoalStep(
    state: UserInfoContract.State,
    onEvent: (UserInfoContract.Event) -> Unit,
    cardBg: Color,
    buttonBlue: Color
) {
    val goals = listOf(
        PrimaryGoal.FITNESS to ("Build Fitness" to "🏋️"),
        PrimaryGoal.STUDY to ("Study Better" to "📚"),
        PrimaryGoal.DISCIPLINE to ("Be More Disciplined" to "🛡️"),
        PrimaryGoal.MENTAL_HEALTH to ("Improve Mental Wellbeing" to "🌸"),
        PrimaryGoal.PRODUCTIVITY to ("Boost Productivity" to "🚀"),
        PrimaryGoal.CUSTOM to ("Custom Goal" to "📝")
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        goals.forEach { (goal, info) ->
            val isSelected = state.primaryGoal == goal
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onEvent(UserInfoContract.Event.OnPrimaryGoalChange(goal)) },
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    1.5.dp,
                    if (isSelected) buttonBlue else MaterialTheme.colorScheme.outlineVariant
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) buttonBlue.copy(alpha = 0.08f) else cardBg
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = info.second, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = info.first,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Selected",
                            tint = buttonBlue
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductivityStep(
    state: UserInfoContract.State,
    onEvent: (UserInfoContract.Event) -> Unit,
    cardBg: Color,
    buttonBlue: Color
) {
    val times = listOf(
        ProductivityTime.MORNING to ("Morning" to "🌅 (5 AM - 12 PM)"),
        ProductivityTime.AFTERNOON to ("Afternoon" to "☀️ (12 PM - 5 PM)"),
        ProductivityTime.EVENING to ("Evening" to "🌇 (5 PM - 9 PM)"),
        ProductivityTime.NIGHT to ("Night Owl" to "🌙 (9 PM - 5 AM)")
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        times.forEach { (time, info) ->
            val isSelected = state.preferredProductivityTime == time
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onEvent(UserInfoContract.Event.OnProductivityTimeChange(time)) },
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    1.5.dp,
                    if (isSelected) buttonBlue else MaterialTheme.colorScheme.outlineVariant
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) buttonBlue.copy(alpha = 0.08f) else cardBg
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = info.first,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = info.second,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Selected",
                            tint = buttonBlue
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeeklyGoalStep(
    state: UserInfoContract.State,
    onEvent: (UserInfoContract.Event) -> Unit,
    cardBg: Color,
    buttonBlue: Color
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(
                onClick = { if (state.weeklyGoal > 3) onEvent(UserInfoContract.Event.OnWeeklyGoalChange(state.weeklyGoal - 1)) },
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f), CircleShape)
            ) {
                Icon(Icons.Default.Remove, null, tint = MaterialTheme.colorScheme.onSurface)
            }

            Spacer(modifier = Modifier.width(32.dp))

            Text(
                text = "${state.weeklyGoal}",
                fontSize = 64.sp,
                fontWeight = FontWeight.ExtraBold,
                color = buttonBlue
            )

            Spacer(modifier = Modifier.width(32.dp))

            IconButton(
                onClick = { if (state.weeklyGoal < 7) onEvent(UserInfoContract.Event.OnWeeklyGoalChange(state.weeklyGoal + 1)) },
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f), CircleShape)
            ) {
                Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.onSurface)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Days per week", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(48.dp))

        // Great choice notification box
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = buttonBlue.copy(alpha = 0.10f)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, buttonBlue.copy(alpha = 0.35f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Star, null, tint = TipStarGold, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Great choice!",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Most successful users aim for 4-6 days per week.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun MotivationStep(
    state: UserInfoContract.State,
    onEvent: (UserInfoContract.Event) -> Unit,
    cardBg: Color,
    buttonBlue: Color
) {
    val options = listOf(
        MotivationStyle.SEEING_PROGRESS to ("Seeing Progress" to "📈 (Charts, completion metrics first)"),
        MotivationStyle.KEEPING_STREAKS to ("Maintaining Streaks" to "🔥 (Streak badges, active count first)"),
        MotivationStyle.LEVELING_UP to ("Earning XP & Rewards" to "🐾 (Level progression, total XP first)"),
        MotivationStyle.QUOTES to ("Inspirational Quotes" to "✍️ (Inspirational thoughts, affirmations first)"),
        MotivationStyle.ACHIEVEMENTS to ("Unlocking Achievements" to "🏆 (Badges, special rewards first)")
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        options.forEach { (style, info) ->
            val isSelected = state.motivationStyle == style
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onEvent(UserInfoContract.Event.OnMotivationStyleChange(style)) },
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    1.5.dp,
                    if (isSelected) buttonBlue else MaterialTheme.colorScheme.outlineVariant
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) buttonBlue.copy(alpha = 0.08f) else cardBg
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = info.first,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = info.second,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Selected",
                            tint = buttonBlue
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExperienceStep(
    state: UserInfoContract.State,
    onEvent: (UserInfoContract.Event) -> Unit,
    cardBg: Color,
    buttonBlue: Color
) {
    val levels = listOf(
        ExperienceLevel.BEGINNER to ("Just Getting Started" to "🌱 I'm new to habits"),
        ExperienceLevel.BUILDING to ("Building Momentum" to "🚀 I'm trying to be consistent"),
        ExperienceLevel.CONSISTENT to ("Getting Consistent" to "🎯 I have good streaks"),
        ExperienceLevel.ADVANCED to ("Habit Master" to "👑 I rarely break habits")
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        levels.forEach { (lvl, info) ->
            val isSelected = state.experienceLevel == lvl
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onEvent(UserInfoContract.Event.OnExperienceLevelChange(lvl)) },
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    1.5.dp,
                    if (isSelected) buttonBlue else MaterialTheme.colorScheme.outlineVariant
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) buttonBlue.copy(alpha = 0.08f) else cardBg
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = info.first,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = info.second,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Selected",
                            tint = buttonBlue
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FinalDetailsStep(
    state: UserInfoContract.State,
    onEvent: (UserInfoContract.Event) -> Unit,
    cardBg: Color,
    buttonBlue: Color
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Height input field with integrated units switcher in the row (Matches Screen 8)
        Column {
            Text(
                "Height",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cardBg, RoundedCornerShape(16.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (state.height.isEmpty()) {
                        Text("Height", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), fontSize = 16.sp)
                    }
                    BasicTextField(
                        value = state.height,
                        onValueChange = { onEvent(UserInfoContract.Event.OnHeightChange(it)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp),
                        cursorBrush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                // CM/FT Inline Unit Selector
                Row(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp))
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (state.isMetric) buttonBlue else Color.Transparent)
                            .clickable { if (!state.isMetric) onEvent(UserInfoContract.Event.OnToggleHeightUnit) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            "CM",
                            color = if (state.isMetric) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (!state.isMetric) buttonBlue else Color.Transparent)
                            .clickable { if (state.isMetric) onEvent(UserInfoContract.Event.OnToggleHeightUnit) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            "FT",
                            color = if (!state.isMetric) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Weight input field with integrated unit label on the right (Matches Screen 8)
        Column {
            Text(
                "Weight (Optional)",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cardBg, RoundedCornerShape(16.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (state.weight.isEmpty()) {
                        Text("Weight (Optional)", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), fontSize = 16.sp)
                    }
                    BasicTextField(
                        value = state.weight,
                        onValueChange = { onEvent(UserInfoContract.Event.OnWeightChange(it)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp),
                        cursorBrush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                Text(
                    text = if (state.isMetric) "Kg" else "Lbs",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        // Timezone Dropdown Selector
        var showTzDialog by remember { mutableStateOf(false) }
        Column {
            Text(
                "Timezone",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cardBg, RoundedCornerShape(16.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                    .clickable { showTzDialog = true }
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (state.timezone == "Asia/Kolkata") "Asia/Kolkata (GMT +5:30)" else state.timezone,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp
                    )
                    Icon(Icons.Default.ArrowDropDown, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Week starts on Dropdown
        var showWeekStartDialog by remember { mutableStateOf(false) }
        Column {
            Text(
                "Week starts on",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cardBg, RoundedCornerShape(16.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                    .clickable { showWeekStartDialog = true }
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (state.weekStartsOn == WeekStartsOn.MONDAY) "Monday" else "Sunday",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp
                    )
                    Icon(Icons.Default.ArrowDropDown, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Timezone selection dialog
        if (showTzDialog) {
            val tzIds = remember {
                listOf(
                    java.util.TimeZone.getDefault().id,
                    "Asia/Kolkata", "UTC", "America/New_York", "America/Los_Angeles", "Europe/London",
                    "Europe/Paris", "Asia/Tokyo", "Asia/Singapore", "Australia/Sydney"
                ).distinct()
            }
            AlertDialog(
                onDismissRequest = { showTzDialog = false },
                title = { Text("Select Timezone") },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        tzIds.forEach { tz ->
                            Text(
                                text = if (tz == "Asia/Kolkata") "Asia/Kolkata (GMT +5:30)" else tz,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onEvent(UserInfoContract.Event.OnTimezoneChange(tz))
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

        // Week starts on selection dialog
        if (showWeekStartDialog) {
            AlertDialog(
                onDismissRequest = { showWeekStartDialog = false },
                title = { Text("Select week start day") },
                text = {
                    Column {
                        Text(
                            text = "Monday",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onEvent(UserInfoContract.Event.OnWeekStartsOnChange(WeekStartsOn.MONDAY))
                                    showWeekStartDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Sunday",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onEvent(UserInfoContract.Event.OnWeekStartsOnChange(WeekStartsOn.SUNDAY))
                                    showWeekStartDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showWeekStartDialog = false }) { Text("Close") }
                }
            )
        }
    }
}
