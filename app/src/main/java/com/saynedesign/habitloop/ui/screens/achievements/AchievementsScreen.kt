package com.saynedesign.habitloop.ui.screens.achievements

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.saynedesign.habitloop.R
import com.saynedesign.habitloop.util.LevelSystem

@Composable
fun AchievementsScreen(
    state: AchievementsContract.State,
    onEvent: (AchievementsContract.Event) -> Unit
) {
    var showInfoDialog by remember { mutableStateOf(false) }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF6C4BFF)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Scoring & Ranks Guide",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Earn XP by completing habits and building consistency. Each milestone unlocks a new dog rank!",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    HorizontalDivider(color = Color(0xFFE8EAF6))
                    XpRuleRow(label = "Habit Completed", xp = "+10 XP")
                    XpRuleRow(label = "First Habit of the Day", xp = "+5 XP")
                    XpRuleRow(label = "Perfect Day (All Habits)", xp = "+50 XP")
                    XpRuleRow(label = "Habit Created", xp = "+15 XP")
                    XpRuleRow(label = "Rest Day Taken", xp = "+5 XP")
                    XpRuleRow(label = "Mood Logged", xp = "+5 XP")
                }
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text("Got it", color = Color(0xFF6C4BFF))
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button
                IconButton(
                    onClick = { onEvent(AchievementsContract.Event.OnBackClicked) },
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White, shape = CircleShape)
                        .border(BorderStroke(1.dp, Color(0xFFE8EAF6)), shape = CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.common_back),
                        tint = Color(0xFF1D1B20),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Title & App Icon - Achievements
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                        text = stringResource(R.string.achievements_title),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF1D1B20)
                    )
                }

                // Info Button
                IconButton(
                    onClick = { showInfoDialog = true },
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White, shape = CircleShape)
                        .border(BorderStroke(1.dp, Color(0xFFE8EAF6)), shape = CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        tint = Color(0xFF1D1B20),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        containerColor = Color(0xFFF8F9FE) // BrandBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. Current Rank Header Card
            CurrentRankCard(state)

            // 2. Ranks Map Grid (Tree View)
            RanksMap(state)

            // 3. Bottom Statistics Card
            BottomStatsCard(state)
        }
    }
}

@Composable
fun CurrentRankCard(state: AchievementsContract.State) {
    val gradient = Brush.linearGradient(
        colors = listOf(Color(0xFF6C4BFF), Color(0xFF9E8BFF))
    )

    Card(
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .background(brush = gradient)
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Dog Avatar / Profile Image
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(Color.White.copy(alpha = 0.2f), shape = CircleShape)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (state.profileImageUri != null) {
                            AsyncImage(
                                model = state.profileImageUri,
                                contentDescription = "Profile Image",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        // YOUR CURRENT RANK capsule
                        Box(
                            modifier = Modifier
                                .background(Color.White, shape = RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFF6C4BFF),
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "YOUR CURRENT RANK",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF6C4BFF)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Level ${state.currentLevel}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Text(
                            text = "${state.levelName} ${state.levelEmoji}",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        // Next level capsule
                        Box(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.25f), shape = RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Next: ${state.nextLevelName} ${state.nextLevelEmoji}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                // Progress Label Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "XP Progress",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "${state.totalXp} / ${state.nextLevelXp} XP",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                // Progress Bar
                LinearProgressIndicator(
                    progress = { state.progressToNextLevel },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Remaining XP to reach next level
                val xpNeeded = maxOf(0, state.nextLevelXp - state.totalXp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (state.currentLevel >= 10) "Max Level reached!" else "$xpNeeded XP to reach Level ${state.nextLevel}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

@Composable
fun RanksMap(state: AchievementsContract.State) {
    val level1 = state.levels.getOrNull(0)
    val level2 = state.levels.getOrNull(1)
    val level3 = state.levels.getOrNull(2)
    val level4 = state.levels.getOrNull(3)
    val level5 = state.levels.getOrNull(4)
    val level6 = state.levels.getOrNull(5)
    val level7 = state.levels.getOrNull(6)
    val level8 = state.levels.getOrNull(7)
    val level9 = state.levels.getOrNull(8)
    val level10 = state.levels.getOrNull(9)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Row 1: Levels 1, 2, 3
        Box(modifier = Modifier.fillMaxWidth()) {
            // Draw background horizontal lines connecting Card 1 -> Card 2 -> Card 3
            Canvas(modifier = Modifier.fillMaxWidth().height(64.dp).align(Alignment.TopCenter).offset(y = 12.dp)) {
                val yCenter = 32.dp.toPx()
                val xStart = size.width * 1 / 6f
                val xMid = size.width * 1 / 2f
                val xEnd = size.width * 5 / 6f

                // Segment L1 -> L2 (highlighted if achieved level 2)
                val isSegment1High = state.currentLevel >= 2
                val color1 = if (isSegment1High) Color(0xFF6C4BFF) else Color(0xFFE8EAF6)
                val pathEffect1 = if (isSegment1High) null else PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                drawLine(
                    color = color1,
                    start = Offset(xStart, yCenter),
                    end = Offset(xMid, yCenter),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = pathEffect1
                )

                // Segment L2 -> L3 (highlighted if achieved level 3)
                val isSegment2High = state.currentLevel >= 3
                val color2 = if (isSegment2High) Color(0xFF6C4BFF) else Color(0xFFE8EAF6)
                val pathEffect2 = if (isSegment2High) null else PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                drawLine(
                    color = color2,
                    start = Offset(xMid, yCenter),
                    end = Offset(xEnd, yCenter),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = pathEffect2
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(modifier = Modifier.weight(1f).padding(horizontal = 6.dp), contentAlignment = Alignment.Center) {
                    level1?.let { LevelCard(it, state.currentLevel) }
                }
                Box(modifier = Modifier.weight(1f).padding(horizontal = 6.dp), contentAlignment = Alignment.TopCenter) {
                    level2?.let { LevelCard(it, state.currentLevel) }
                }
                Box(modifier = Modifier.weight(1f).padding(horizontal = 6.dp), contentAlignment = Alignment.TopCenter) {
                    level3?.let { LevelCard(it, state.currentLevel) }
                }
            }
        }

        // Transition line from Level 3 (Row 1 Col 3) to Level 4 (Row 2 Col 1)
        RowTransitionLine(
            fromFraction = 5 / 6f,
            toFraction = 1 / 6f,
            isHighlighted = state.currentLevel >= 4
        )

        // Row 2: Levels 4, 5, 6
        Box(modifier = Modifier.fillMaxWidth()) {
            // Draw background horizontal lines connecting Card 4 -> Card 5 -> Card 6
            Canvas(modifier = Modifier.fillMaxWidth().height(64.dp).align(Alignment.TopCenter).offset(y = 12.dp)) {
                val yCenter = 32.dp.toPx()
                val xStart = size.width * 1 / 6f
                val xMid = size.width * 1 / 2f
                val xEnd = size.width * 5 / 6f

                // Segment L4 -> L5 (highlighted if achieved level 5)
                val isSegment3High = state.currentLevel >= 5
                val color3 = if (isSegment3High) Color(0xFF6C4BFF) else Color(0xFFE8EAF6)
                val pathEffect3 = if (isSegment3High) null else PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                drawLine(
                    color = color3,
                    start = Offset(xStart, yCenter),
                    end = Offset(xMid, yCenter),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = pathEffect3
                )

                // Segment L5 -> L6 (highlighted if achieved level 6)
                val isSegment4High = state.currentLevel >= 6
                val color4 = if (isSegment4High) Color(0xFF6C4BFF) else Color(0xFFE8EAF6)
                val pathEffect4 = if (isSegment4High) null else PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                drawLine(
                    color = color4,
                    start = Offset(xMid, yCenter),
                    end = Offset(xEnd, yCenter),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = pathEffect4
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(modifier = Modifier.weight(1f).padding(horizontal = 6.dp), contentAlignment = Alignment.TopCenter) {
                    level4?.let { LevelCard(it, state.currentLevel) }
                }
                Box(modifier = Modifier.weight(1f).padding(horizontal = 6.dp), contentAlignment = Alignment.TopCenter) {
                    level5?.let { LevelCard(it, state.currentLevel) }
                }
                Box(modifier = Modifier.weight(1f).padding(horizontal = 6.dp), contentAlignment = Alignment.TopCenter) {
                    level6?.let { LevelCard(it, state.currentLevel) }
                }
            }
        }

        // Transition line from Level 6 (Row 2 Col 3) to Level 7 (Row 3 Col 1)
        RowTransitionLine(
            fromFraction = 5 / 6f,
            toFraction = 1 / 6f,
            isHighlighted = state.currentLevel >= 7
        )

        // Row 3: Levels 7, 8, 9
        Box(modifier = Modifier.fillMaxWidth()) {
            // Draw background horizontal lines connecting Card 7 -> Card 8 -> Card 9
            Canvas(modifier = Modifier.fillMaxWidth().height(64.dp).align(Alignment.TopCenter).offset(y = 12.dp)) {
                val yCenter = 32.dp.toPx()
                val xStart = size.width * 1 / 6f
                val xMid = size.width * 1 / 2f
                val xEnd = size.width * 5 / 6f

                // Segment L7 -> L8 (highlighted if achieved level 8)
                val isSegment5High = state.currentLevel >= 8
                val color5 = if (isSegment5High) Color(0xFF6C4BFF) else Color(0xFFE8EAF6)
                val pathEffect5 = if (isSegment5High) null else PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                drawLine(
                    color = color5,
                    start = Offset(xStart, yCenter),
                    end = Offset(xMid, yCenter),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = pathEffect5
                )

                // Segment L8 -> L9 (highlighted if achieved level 9)
                val isSegment6High = state.currentLevel >= 9
                val color6 = if (isSegment6High) Color(0xFF6C4BFF) else Color(0xFFE8EAF6)
                val pathEffect6 = if (isSegment6High) null else PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                drawLine(
                    color = color6,
                    start = Offset(xMid, yCenter),
                    end = Offset(xEnd, yCenter),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = pathEffect6
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(modifier = Modifier.weight(1f).padding(horizontal = 6.dp), contentAlignment = Alignment.TopCenter) {
                    level7?.let { LevelCard(it, state.currentLevel) }
                }
                Box(modifier = Modifier.weight(1f).padding(horizontal = 6.dp), contentAlignment = Alignment.TopCenter) {
                    level8?.let { LevelCard(it, state.currentLevel) }
                }
                Box(modifier = Modifier.weight(1f).padding(horizontal = 6.dp), contentAlignment = Alignment.TopCenter) {
                    level9?.let { LevelCard(it, state.currentLevel) }
                }
            }
        }

        // Transition line from Level 9 (Row 3 Col 3) to Level 10 (Row 4 Col 2 - centered)
        RowTransitionLine(
            fromFraction = 5 / 6f,
            toFraction = 1 / 2f,
            isHighlighted = state.currentLevel >= 10
        )

        // Row 4: Level 10 (centered)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Box(modifier = Modifier.weight(1f).padding(horizontal = 6.dp), contentAlignment = Alignment.TopCenter) {
                level10?.let { LevelCard(it, state.currentLevel) }
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun LevelCard(levelInfo: LevelSystem.LevelInfo, currentLevel: Int) {
    val level = levelInfo.level
    val requiredXp = levelInfo.requiredXp
    val name = levelInfo.name
    val emoji = levelInfo.emoji

    val isCompleted = level < currentLevel
    val isCurrent = level == currentLevel
    val isLocked = level > currentLevel

    val border = if (isCurrent) BorderStroke(2.dp, Color(0xFF6C4BFF)) else null
    val cardBg = if (isCurrent) Color(0xFFF7F5FF) else Color.White
    val cardElevation = if (isCurrent) 8.dp else if (isLocked) 1.dp else 2.dp

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = border,
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation),
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (isLocked) 0.65f else 1.0f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Badge Circle containing Emoji (if unlocked) or Lock (if locked)
            Box(
                modifier = Modifier
                    .size(if (isCurrent) 44.dp else 40.dp)
                    .background(
                        color = when {
                            isCompleted -> Color(0xFFF0ECFF)
                            isCurrent -> Color(0xFFF0ECFF)
                            else -> Color(0xFFE8EAF6)
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isLocked) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color(0xFF9A89FF),
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Text(
                        text = emoji,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            // Level label
            Text(
                text = "Level $level",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(2.dp))
            // Rank Name (no emoji)
            Text(
                text = name,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Bold
                ),
                color = if (isCurrent) Color(0xFF6C4BFF) else Color(0xFF1D1B20),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            // XP milestone
            Text(
                text = "$requiredXp XP",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = if (isCurrent) Color(0xFF6C4BFF) else Color.Gray
            )
        }
    }
}

@Composable
fun StatusTag(text: String, containerColor: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .background(containerColor, shape = RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = textColor
        )
    }
}

@Composable
fun RowTransitionLine(
    fromFraction: Float,
    toFraction: Float,
    isHighlighted: Boolean,
    modifier: Modifier = Modifier
) {
    val lineColor = if (isHighlighted) Color(0xFF6C4BFF) else Color(0xFFE8EAF6)
    val strokeWidth = 2.dp
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(32.dp)
    ) {
        val width = size.width
        val height = size.height
        val pathEffect = if (isHighlighted) null else PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

        val xStart = width * fromFraction
        val xEnd = width * toFraction
        val yMid = height / 2

        // Draw step line: vertical down -> horizontal -> vertical down
        drawLine(
            color = lineColor,
            start = Offset(xStart, 0f),
            end = Offset(xStart, yMid),
            strokeWidth = strokeWidth.toPx(),
            pathEffect = pathEffect
        )
        drawLine(
            color = lineColor,
            start = Offset(xStart, yMid),
            end = Offset(xEnd, yMid),
            strokeWidth = strokeWidth.toPx(),
            pathEffect = pathEffect
        )
        drawLine(
            color = lineColor,
            start = Offset(xEnd, yMid),
            end = Offset(xEnd, height),
            strokeWidth = strokeWidth.toPx(),
            pathEffect = pathEffect
        )
    }
}

@Composable
fun BottomStatsCard(state: AchievementsContract.State) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Total XP
            StatColumn(
                icon = Icons.Default.Bolt,
                iconColor = Color(0xFF6C4BFF),
                bgColor = Color(0xFFF0ECFF),
                value = state.totalXp.toString(),
                label = "Total XP",
                modifier = Modifier.weight(1f)
            )

            // Divider
            VerticalDivider(modifier = Modifier.height(32.dp), color = Color(0xFFE8EAF6))

            // 2. Day Streak
            StatColumn(
                icon = Icons.Default.Whatshot,
                iconColor = Color(0xFFFF5722),
                bgColor = Color(0xFFFFECEB),
                value = state.currentStreak.toString(),
                label = "Day Streak",
                modifier = Modifier.weight(1f)
            )

            // Divider
            VerticalDivider(modifier = Modifier.height(32.dp), color = Color(0xFFE8EAF6))

            // 3. Check-ins
            StatColumn(
                icon = Icons.Default.CheckCircle,
                iconColor = Color(0xFF2E7D32),
                bgColor = Color(0xFFE8F5E9),
                value = state.totalCheckIns.toString(),
                label = "Check-ins",
                modifier = Modifier.weight(1f)
            )

            // Divider
            VerticalDivider(modifier = Modifier.height(32.dp), color = Color(0xFFE8EAF6))

            // 4. Badges Earned
            StatColumn(
                icon = Icons.Default.Shield,
                iconColor = Color(0xFF1E88E5),
                bgColor = Color(0xFFE3F2FD),
                value = state.badgesCount.toString(),
                label = "Badges",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatColumn(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    bgColor: Color,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color = bgColor, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF1D1B20)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            color = Color.Gray
        )
    }
}

@Composable
fun XpRuleRow(label: String, xp: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF1D1B20)
        )
        Text(
            text = xp,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF2E7D32)
        )
    }
}
