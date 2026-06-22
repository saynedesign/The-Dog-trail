package com.saynedesign.habitloop.ui.screens.achievements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.saynedesign.habitloop.R
import com.saynedesign.habitloop.ui.theme.AccentGold
import com.saynedesign.habitloop.ui.theme.AccentOrangeSoft
import com.saynedesign.habitloop.ui.theme.AccentOrangeStrong

@Composable
fun AchievementsScreen(
    state: AchievementsContract.State,
    onEvent: (AchievementsContract.Event) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.secondary,
                        MaterialTheme.colorScheme.primary
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onEvent(AchievementsContract.Event.OnBackClicked) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.common_back),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
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
                        text = stringResource(R.string.achievements_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(48.dp)) // Balance back button
            }

            // Header Section (Current Level)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Big Badge
                Box(
                    modifier = Modifier.size(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = AccentGold,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Star inside
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = AccentOrangeStrong,
                        modifier = Modifier.size(40.dp).offset(y = (-10).dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Level ${state.currentLevel}: ${state.levelName}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "${state.totalXp} XP Earned",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                )
            }

            // Grid Section
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    items(state.levels) { (level, requiredXp) ->
                        LevelGridItem(
                            level = level,
                            requiredXp = requiredXp,
                            currentLevel = state.currentLevel,
                            isCompleted = level < state.currentLevel,
                            isCurrent = level == state.currentLevel,
                            isLocked = level > state.currentLevel
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LevelGridItem(
    level: Int,
    requiredXp: Int,
    currentLevel: Int,
    isCompleted: Boolean,
    isCurrent: Boolean,
    isLocked: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Badge Icon
        Box(
            modifier = Modifier.size(80.dp),
            contentAlignment = Alignment.Center
        ) {
            val badgeColor = when {
                isCompleted -> AccentOrangeSoft
                isCurrent -> AccentOrangeStrong
                else -> MaterialTheme.colorScheme.outlineVariant
            }
            
            Icon(
                imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.WorkspacePremium,
                contentDescription = null,
                tint = badgeColor,
                modifier = Modifier.fillMaxSize()
            )
            
            if (isCurrent) {
                 // Add some sparkle or indicator
                 Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp).offset(y = (-5).dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = stringResource(R.string.level_label, level),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (isLocked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        val subtitle = when {
            isCompleted -> stringResource(R.string.achievements_level_passed)
            isCurrent -> stringResource(R.string.achievements_current_level)
            else -> "$requiredXp XP"
        }
        
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
