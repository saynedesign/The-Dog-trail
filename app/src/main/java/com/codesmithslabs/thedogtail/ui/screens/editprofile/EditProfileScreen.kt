package com.codesmithslabs.thedogtail.ui.screens.editprofile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
fun EditProfileScreen(
    state: EditProfileContract.State,
    onEvent: (EditProfileContract.Event) -> Unit
) {
    // Image Picker Launcher
    val imageCropLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            onEvent(EditProfileContract.Event.OnImageSelected(it.toString()))
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
                onDismissRequest = { onEvent(EditProfileContract.Event.OnToggleDatePicker) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onEvent(EditProfileContract.Event.OnDateSelected(datePickerState.selectedDateMillis))
                        }
                    ) {
                        Text("OK", color = BrandBlue)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { onEvent(EditProfileContract.Event.OnToggleDatePicker) }
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
        containerColor = BrandSurface,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Edit Profile",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onEvent(EditProfileContract.Event.OnBackClicked) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BrandSurface
                )
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Button(
                    onClick = { onEvent(EditProfileContract.Event.OnSave) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = state.name.isNotBlank() && state.dob.isNotBlank() && state.height.isNotBlank() && !state.isLoading,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandBlue,
                        contentColor = Color.White,
                        disabledContainerColor = BrandBlue.copy(alpha = 0.5f),
                        disabledContentColor = Color.White
                    )
                ) {
                    Text(
                        text = if (state.isLoading) "Saving..." else "Save Changes",
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
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

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
                    onValueChange = { onEvent(EditProfileContract.Event.OnNameChange(it)) },
                    placeholder = "Enter your name",
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Date of Birth",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Box(modifier = Modifier.clickable { onEvent(EditProfileContract.Event.OnToggleDatePicker) }) {
                    HabitOutlinedTextField(
                        value = state.dob,
                        onValueChange = {}, // Read only, set by date picker
                        placeholder = "DD/MM/YYYY",
                        trailingIcon = {
                            Icon(Icons.Default.DateRange, contentDescription = null, tint = TextSecondary)
                        },
                        enabled = false, // Disable typing
                        modifier = Modifier.fillMaxWidth()
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
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    HabitOutlinedTextField(
                        value = state.height,
                        onValueChange = { onEvent(EditProfileContract.Event.OnHeightChange(it)) },
                        placeholder = if (state.isMetric) "175" else "5.9",
                        modifier = Modifier.weight(1f)
                    )
                    
                    Spacer(modifier = Modifier.size(16.dp))
                    
                    Row(
                        modifier = Modifier
                            .background(BrandBackground, RoundedCornerShape(8.dp))
                            .clickable { onEvent(EditProfileContract.Event.OnToggleHeightUnit) }
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    if (!state.isMetric) BrandBlue else Color.Transparent,
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                "FT",
                                color = if (!state.isMetric) Color.White else TextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(
                                    if (state.isMetric) BrandBlue else Color.Transparent,
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                "CM",
                                color = if (state.isMetric) Color.White else TextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
