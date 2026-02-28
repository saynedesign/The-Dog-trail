package com.codesmithslabs.thedogtail.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.codesmithslabs.thedogtail.ui.theme.BrandBlue
import com.codesmithslabs.thedogtail.ui.theme.BrandSurface
import com.codesmithslabs.thedogtail.ui.theme.TextSecondary

@Composable
fun ProfileScreen(
    state: ProfileContract.State,
    onEvent: (ProfileContract.Event) -> Unit,
    modifier: Modifier = Modifier,
    showBackButton: Boolean = false
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            ProfileTopBar(
                onBack = if (showBackButton) { { onEvent(ProfileContract.Event.OnBackClicked) } } else null
            )
        },
        containerColor = BrandSurface
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
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    ProfileHeader(state)
                }

                item {
                    Text(
                        text = "Account Settings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    ProfileOptionItem(
                        icon = Icons.Default.Edit,
                        title = "Edit Profile",
                        subtitle = "Update your personal details",
                        onClick = { onEvent(ProfileContract.Event.OnEditProfileClicked) }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    ProfileOptionItem(
                        icon = Icons.Default.Backup,
                        title = "Backup Data",
                        subtitle = "Save your progress to cloud",
                        onClick = { onEvent(ProfileContract.Event.OnBackupClicked) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ProfileOptionItem(
                        icon = Icons.AutoMirrored.Filled.Logout,
                        title = "Logout",
                        subtitle = "Sign out from your account",
                        onClick = { onEvent(ProfileContract.Event.OnLogoutClicked) },
                        isDestructive = true
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTopBar(onBack: (() -> Unit)? = null) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                "Profile",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = BrandSurface
        )
    )
}

@Composable
fun ProfileHeader(state: ProfileContract.State) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFE0B2)), // Placeholder color
            contentAlignment = Alignment.Center
        ) {
            // Placeholder Image or Icon
             if (state.profileImageUri != null) {
                 AsyncImage(
                     model = state.profileImageUri,
                     contentDescription = "Profile Image",
                     modifier = Modifier.fillMaxSize(),
                     contentScale = ContentScale.Crop
                 )
             } else {
                 Text(
                     text = state.userName.take(2).uppercase(),
                     style = MaterialTheme.typography.headlineLarge,
                     fontWeight = FontWeight.Bold,
                     color = Color(0xFFE65100)
                 )
             }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = state.userName,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        if (state.userDob.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Born: ${state.userDob}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
        
        if (state.userHeight > 0) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Height: ${state.userHeight} cm",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun ProfileOptionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isDestructive) Color(0xFFFFEBEE) else BrandBlue.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isDestructive) Color.Red else BrandBlue
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isDestructive) Color.Red else Color.Unspecified
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}
