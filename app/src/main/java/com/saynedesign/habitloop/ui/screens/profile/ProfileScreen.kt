package com.saynedesign.habitloop.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import com.saynedesign.habitloop.ui.theme.isAppInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.saynedesign.habitloop.R

@Composable
fun ProfileScreen(
    state: ProfileContract.State,
    onEvent: (ProfileContract.Event) -> Unit,
    modifier: Modifier = Modifier,
    showBackButton: Boolean = false
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            ProfileTopBar(onEvent = onEvent)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    ProfileHeader(
                        state = state,
                        onEditProfile = { onEvent(ProfileContract.Event.OnPersonalInfoClicked) }
                    )
                }

                item {
                    CurrentLevelCard(
                        state = state,
                        onClick = { onEvent(ProfileContract.Event.OnLevelBannerClicked) }
                    )
                }

                item {
                    StatsGrid(state = state)
                }

                item {
                    QuickActionsRow(
                        onNewHabit = { onEvent(ProfileContract.Event.OnTrackNewHabitClicked) },
                        onViewStats = { onEvent(ProfileContract.Event.OnViewStatsClicked) },
                        onAchievements = { onEvent(ProfileContract.Event.OnLevelBannerClicked) }
                    )
                }

                item {
                    AccountPreferencesList(
                        onPersonalInfo = { onEvent(ProfileContract.Event.OnPersonalInfoClicked) },
                        onNotifications = { onEvent(ProfileContract.Event.OnPreferencesClicked) },
                        onAppearance = { onEvent(ProfileContract.Event.OnAppAppearanceClicked) },
                        onPrivacy = { onEvent(ProfileContract.Event.OnPreferencesClicked) },
                        onAbout = { onEvent(ProfileContract.Event.OnAboutClicked) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.navigationBarsPadding())
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTopBar(onEvent: (ProfileContract.Event) -> Unit) {
    CenterAlignedTopAppBar(
        navigationIcon = {
            Image(
                painter = painterResource(R.drawable.habit_tracker_icon),
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(32.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        },
        title = {},
        actions = {
            IconButton(onClick = { onEvent(ProfileContract.Event.OnPreferencesClicked) }) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Composable
fun ProfileHeader(state: ProfileContract.State, onEditProfile: () -> Unit) {
    val hour = java.time.LocalTime.now().hour
    val greeting = when (hour) {
        in 0..11 -> "Good Morning, 👋"
        in 12..16 -> "Good Afternoon, 👋"
        else -> "Good Evening, 👋"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = greeting,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = state.userName.ifEmpty { "Aman" },
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 32.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Keep building better habits every single day.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Box(
            modifier = Modifier.size(110.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Pets,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.TopStart)
                    .offset(x = 10.dp, y = 5.dp)
            )

            Text(
                text = "✨",
                fontSize = 16.sp,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = 5.dp, y = (-20).dp)
            )

            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
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
                        Text(
                            text = state.userName.take(1).ifEmpty { "A" }.uppercase(),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            IconButton(
                onClick = onEditProfile,
                modifier = Modifier
                    .size(28.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = (-4).dp, y = (-4).dp)
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Profile",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun CurrentLevelCard(state: ProfileContract.State, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🦴",
                        fontSize = 28.sp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "CURRENT LEVEL",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Level ${state.level}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = state.levelName.ifEmpty { "Bone Collector" },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                val tagGreen = if (isAppInDarkTheme()) Color(0xFF81C784) else Color(0xFF2E7D32)
                Box(
                    modifier = Modifier
                        .background(tagGreen.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "↗ Top 12%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = tagGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            val safeProgress = if (state.xpProgress.isNaN()) 0f else state.xpProgress
            LinearProgressIndicator(
                progress = { safeProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val remainingXp = (state.nextLevelXp - state.totalXp).coerceAtLeast(0)
                Text(
                    text = "$remainingXp XP to Level ${state.level + 1}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${state.totalXp} / ${state.nextLevelXp} XP",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun StatsGrid(state: ProfileContract.State) {
    val isDark = isAppInDarkTheme()

    val streakColor = if (isDark) Color(0xFFFF8A65) else Color(0xFFE64A19)
    val xpColor = MaterialTheme.colorScheme.primary
    val checkInColor = if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32)
    val badgeColor = MaterialTheme.colorScheme.secondary

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatGridItem(
            icon = Icons.Default.Whatshot,
            iconTint = streakColor,
            value = state.currentStreak.toString(),
            label = "Day Streak",
            sublabel = "Best: ${state.bestStreak}",
            sublabelColor = streakColor,
            modifier = Modifier.weight(1f)
        )

        StatGridItem(
            icon = Icons.Default.Star,
            iconTint = xpColor,
            value = state.totalXp.toString(),
            label = "Total XP",
            sublabel = "All time",
            sublabelColor = xpColor,
            modifier = Modifier.weight(1f)
        )

        StatGridItem(
            icon = Icons.Default.CheckCircle,
            iconTint = checkInColor,
            value = state.totalCheckIns.toString(),
            label = "Check-ins",
            sublabel = "All time",
            sublabelColor = checkInColor,
            modifier = Modifier.weight(1f)
        )

        StatGridItem(
            icon = Icons.Default.MilitaryTech,
            iconTint = badgeColor,
            value = state.badgesCount.toString(),
            label = "Badges",
            sublabel = "Earned",
            sublabelColor = badgeColor,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatGridItem(
    icon: ImageVector,
    iconTint: Color,
    value: String,
    label: String,
    sublabel: String,
    sublabelColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(iconTint.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = sublabel,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                color = sublabelColor.copy(alpha = 0.85f),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}


@Composable
fun QuickActionsRow(
    onNewHabit: () -> Unit,
    onViewStats: () -> Unit,
    onAchievements: () -> Unit
) {
    val isDark = isAppInDarkTheme()
    val successColor = if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickActionItem(
                icon = Icons.Default.Add,
                iconBgColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                iconTint = MaterialTheme.colorScheme.primary,
                title = "New Habit",
                subtitle = "Start tracking",
                onClick = onNewHabit,
                modifier = Modifier.weight(1f)
            )

            QuickActionItem(
                icon = Icons.Default.BarChart,
                iconBgColor = successColor.copy(alpha = 0.15f),
                iconTint = successColor,
                title = "View Stats",
                subtitle = "See progress",
                onClick = onViewStats,
                modifier = Modifier.weight(1f)
            )

            QuickActionItem(
                icon = Icons.Default.Star,
                iconBgColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                iconTint = MaterialTheme.colorScheme.secondary,
                title = "Achievements",
                subtitle = "View badges",
                onClick = onAchievements,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun QuickActionItem(
    icon: ImageVector,
    iconBgColor: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(iconBgColor, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun AccountPreferencesList(
    onPersonalInfo: () -> Unit,
    onNotifications: () -> Unit,
    onAppearance: () -> Unit,
    onPrivacy: () -> Unit,
    onAbout: () -> Unit
) {
    val isDark = isAppInDarkTheme()

    val blueBg = if (isDark) Color(0xFF1E3A8A) else Color(0xFFE3F2FD)
    val blueTint = if (isDark) Color(0xFF93C5FD) else Color(0xFF1E88E5)

    val orangeBg = if (isDark) Color(0xFF78350F) else Color(0xFFFFE0B2)
    val orangeTint = if (isDark) Color(0xFFFCD34D) else Color(0xFFF57C00)

    val purpleBg = if (isDark) Color(0xFF5B21B6) else Color(0xFFEDE7F6)
    val purpleTint = if (isDark) Color(0xFFC084FC) else Color(0xFF7B1FA2)

    val greenBg = if (isDark) Color(0xFF065F46) else Color(0xFFE8F5E9)
    val greenTint = if (isDark) Color(0xFF34D399) else Color(0xFF388E3C)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Account & Preferences",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                PreferenceListItem(
                    icon = Icons.Default.Person,
                    iconBgColor = blueBg,
                    iconTint = blueTint,
                    title = "Account Details",
                    subtitle = "Personal information & profile",
                    onClick = onPersonalInfo,
                    showDivider = true
                )

                PreferenceListItem(
                    icon = Icons.Default.Notifications,
                    iconBgColor = orangeBg,
                    iconTint = orangeTint,
                    title = "Notifications",
                    subtitle = "Manage reminders & alerts",
                    onClick = onNotifications,
                    showDivider = true
                )

                PreferenceListItem(
                    icon = Icons.Default.Palette,
                    iconBgColor = purpleBg,
                    iconTint = purpleTint,
                    title = "Appearance",
                    subtitle = "Theme, colors & display",
                    onClick = onAppearance,
                    showDivider = true
                )

                PreferenceListItem(
                    icon = Icons.Default.Shield,
                    iconBgColor = greenBg,
                    iconTint = greenTint,
                    title = "Privacy & Security",
                    subtitle = "Manage data & privacy settings",
                    onClick = onPrivacy,
                    showDivider = true
                )

                PreferenceListItem(
                    icon = Icons.Default.Info,
                    iconBgColor = blueBg,
                    iconTint = blueTint,
                    title = "About Habit Loop",
                    subtitle = "Version, help & feedback",
                    onClick = onAbout,
                    showDivider = false
                )
            }
        }
    }
}

@Composable
fun PreferenceListItem(
    icon: ImageVector,
    iconBgColor: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    showDivider: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconBgColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }

        if (showDivider) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                thickness = 1.dp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}
