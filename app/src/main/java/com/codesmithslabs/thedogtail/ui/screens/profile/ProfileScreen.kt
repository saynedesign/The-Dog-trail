package com.codesmithslabs.thedogtail.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.codesmithslabs.thedogtail.ui.theme.BrandBlue
import com.codesmithslabs.thedogtail.ui.theme.BrandSurface
import com.codesmithslabs.thedogtail.ui.theme.TextSecondary
import com.codesmithslabs.thedogtail.ui.theme.WarningOrange
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import coil.compose.AsyncImage
import com.codesmithslabs.thedogtail.R
import java.util.Locale

@Composable
fun ProfileScreen(
    state: ProfileContract.State,
    onEvent: (ProfileContract.Event) -> Unit,
    modifier: Modifier = Modifier,
    showBackButton: Boolean = false // Parameter kept but unused in new design as per image
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            ProfileTopBar()
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BrandBlue)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    ProfileHeaderCard(state = state, onClick = { onEvent(ProfileContract.Event.OnPersonalInfoClicked) })
                }

                item {
                    LevelBanner(
                        level = state.level,
                        onClick = { onEvent(ProfileContract.Event.OnLevelBannerClicked) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        ProfileOptionItem(
                            icon = Icons.Default.Settings,
                            title = stringResource(R.string.profile_preferences),
                            onClick = { onEvent(ProfileContract.Event.OnPreferencesClicked) }
                        )
                        ProfileOptionItem(
                            icon = Icons.Default.Person,
                            title = stringResource(R.string.profile_personal_info),
                            onClick = { onEvent(ProfileContract.Event.OnPersonalInfoClicked) }
                        )
                        ProfileOptionItem(
                            icon = Icons.Default.Security,
                            title = stringResource(R.string.profile_account_security),
                            onClick = { onEvent(ProfileContract.Event.OnAccountSecurityClicked) }
                        )
                        ProfileOptionItem(
                            icon = Icons.Default.SwapHoriz,
                            title = stringResource(R.string.profile_linked_accounts),
                            onClick = { onEvent(ProfileContract.Event.OnLinkedAccountsClicked) }
                        )
                        ProfileOptionItem(
                            icon = Icons.Default.Visibility,
                            title = stringResource(R.string.profile_app_appearance),
                            onClick = { onEvent(ProfileContract.Event.OnAppAppearanceClicked) }
                        )
                        ProfileOptionItem(
                            icon = Icons.Default.Analytics,
                            title = stringResource(R.string.profile_data_analytics),
                            onClick = { onEvent(ProfileContract.Event.OnDataAnalyticsClicked) }
                        )
                        ProfileOptionItem(
                            icon = Icons.Default.Description,
                            title = stringResource(R.string.profile_help_support),
                            onClick = { onEvent(ProfileContract.Event.OnHelpSupportClicked) }
                        )
                        ProfileOptionItem(
                            icon = Icons.AutoMirrored.Filled.Logout,
                            title = stringResource(R.string.profile_logout),
                            onClick = { onEvent(ProfileContract.Event.OnLogoutClicked) },
                            isDestructive = true,
                            showDivider = false
                        )
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTopBar() {
    CenterAlignedTopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.ic_icon_habit_loop),
                    contentDescription = stringResource(R.string.app_name),
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    stringResource(R.string.profile_account),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        actions = {
            IconButton(onClick = { /* Handle menu */ }) {
                Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.common_more))
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Composable
fun ProfileHeaderCard(state: ProfileContract.State, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (state.profileImageUri != null) {
                    AsyncImage(
                        model = state.profileImageUri,
                        contentDescription = stringResource(R.string.common_profile_image),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = state.userName.take(2).uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = BrandBlue
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = state.userName.ifEmpty { stringResource(R.string.profile_user_name_placeholder) },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                if (state.userDob.isNotEmpty() || state.userHeight > 0) {
                     Spacer(modifier = Modifier.height(4.dp))
                     val details = buildList {
                         if (state.userDob.isNotEmpty()) add(stringResource(R.string.profile_born, state.userDob))
                        if (state.userHeight > 0) add(stringResource(R.string.profile_height_cm, formatHeightValue(state.userHeight)))
                     }.joinToString(" • ")
                     
                     Text(
                        text = details,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun LevelBanner(level: Int, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Level Badge
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = WarningOrange,
                modifier = Modifier.size(40.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.level_label, level),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.profile_level_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun ProfileOptionItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false,
    showDivider: Boolean = false // In the image, items are in a list, likely no dividers or subtle ones. But usually lists have dividers. The image looks like clean white background. I'll omit dividers for now to match the clean look.
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(20.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            modifier = Modifier.weight(1f)
        )
        
        if (!isDestructive) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = TextSecondary.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

private fun formatHeightValue(height: Float): String {
    return if (height % 1f == 0f) {
        height.toInt().toString()
    } else {
        String.format(Locale.US, "%.1f", height)
    }
}
