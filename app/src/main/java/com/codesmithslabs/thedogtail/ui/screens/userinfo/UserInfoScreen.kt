package com.codesmithslabs.thedogtail.ui.screens.userinfo

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import android.app.Activity
import androidx.activity.result.PickVisualMediaRequest
import coil.compose.AsyncImage
import com.codesmithslabs.thedogtail.ui.components.HabitOutlinedTextField
import com.codesmithslabs.thedogtail.ui.theme.BrandBackground
import com.codesmithslabs.thedogtail.ui.theme.BrandBlue
import com.codesmithslabs.thedogtail.ui.theme.BrandLightBlue
import com.codesmithslabs.thedogtail.ui.theme.BrandSurface
import com.codesmithslabs.thedogtail.ui.theme.TextPrimary
import com.codesmithslabs.thedogtail.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserInfoScreen(
    state: UserInfoContract.State,
    onEvent: (UserInfoContract.Event) -> Unit
) {
    val context = LocalContext.current
    
    // Image Picker Launcher
    val imageCropLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            onEvent(UserInfoContract.Event.OnImageSelected(it.toString()))
        }
    }

    // Date Picker State
    if (state.isDatePickerVisible) {
        val datePickerState = rememberDatePickerState()
        
        // Force light theme for the Date Picker
        MaterialTheme(
            colorScheme = lightColorScheme(
                primary = BrandBlue,
                onPrimary = Color.White,
                surface = Color.White,
                onSurface = TextPrimary,
                surfaceContainerHigh = Color.White, // Dialog background
                onSurfaceVariant = TextSecondary,
                primaryContainer = BrandLightBlue,
                onPrimaryContainer = BrandBlue
            )
        ) {
            DatePickerDialog(
                onDismissRequest = { onEvent(UserInfoContract.Event.OnToggleDatePicker) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onEvent(UserInfoContract.Event.OnDateSelected(datePickerState.selectedDateMillis))
                        }
                    ) {
                        Text("OK", color = BrandBlue)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { onEvent(UserInfoContract.Event.OnToggleDatePicker) }
                    ) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BrandSurface, // White background
        topBar = {
            // Custom Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { /* Handle back if needed */ },
                    modifier = Modifier
                        .background(BrandBackground, CircleShape)
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Button(
                    onClick = { onEvent(UserInfoContract.Event.OnSubmit) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = state.name.isNotBlank() && state.dob.isNotBlank() && state.height.isNotBlank(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandBlue,
                        contentColor = Color.White,
                        disabledContainerColor = BrandBlue.copy(alpha = 0.5f),
                        disabledContentColor = Color.White
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Continue",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "By continuing, you agree to our Terms of Service",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Image Section
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
                        .background(BrandLightBlue),
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
                            tint = BrandBlue,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
                
                // Edit Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(32.dp)
                        .background(BrandSurface, CircleShape)
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(BrandSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = BrandBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Tell us about you",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Let's personalize your experience. This helps\nus tailor habits just for you.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Form Fields
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Full Name",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                HabitOutlinedTextField(
                    value = state.name,
                    onValueChange = { onEvent(UserInfoContract.Event.OnNameChange(it)) },
                    placeholder = "Mert Kahveci",
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = TextSecondary
                        )
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Date of Birth",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // Read-only text field for Date that opens DatePicker
                Box(modifier = Modifier.clickable { onEvent(UserInfoContract.Event.OnToggleDatePicker) }) {
                     HabitOutlinedTextField(
                        value = state.dob,
                        onValueChange = { }, // Read only
                        placeholder = "mm/dd/yyyy",
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                tint = TextSecondary
                            )
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Select Date",
                                tint = TextPrimary,
                                modifier = Modifier.clickable { onEvent(UserInfoContract.Event.OnToggleDatePicker) }
                            )
                        }
                    )
                    // Overlay box to intercept clicks
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { onEvent(UserInfoContract.Event.OnToggleDatePicker) }
                    )
                }
               
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Height",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                HabitOutlinedTextField(
                    value = state.height,
                    onValueChange = { onEvent(UserInfoContract.Event.OnHeightChange(it)) },
                    placeholder = if (state.isMetric) "180" else "5.9",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    leadingIcon = {
                         Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        // CM / FT Toggle
                        Row(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .background(BrandBackground, RoundedCornerShape(8.dp))
                                .padding(4.dp)
                                .clickable { onEvent(UserInfoContract.Event.OnToggleHeightUnit) },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CM",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (state.isMetric) BrandBlue else TextSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            Text(
                                text = "FT",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (!state.isMetric) BrandBlue else TextSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
