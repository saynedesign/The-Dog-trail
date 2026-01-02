package com.codesmithslabs.thedogtail.ui.screens.userinfo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.codesmithslabs.thedogtail.ui.components.HabitButton
import com.codesmithslabs.thedogtail.ui.components.ScreenHeader
import com.codesmithslabs.thedogtail.ui.components.HabitNumberInput
import com.codesmithslabs.thedogtail.ui.components.HabitTextField
import com.codesmithslabs.thedogtail.ui.theme.BrandBackground
import com.codesmithslabs.thedogtail.ui.theme.BrandBlue
import com.codesmithslabs.thedogtail.ui.theme.BrandPurple
import com.codesmithslabs.thedogtail.ui.theme.TextPrimary
import com.codesmithslabs.thedogtail.ui.theme.TextSecondary

@Composable
fun UserInfoScreen(
    state: UserInfoContract.State,
    onEvent: (UserInfoContract.Event) -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BrandBackground,
        topBar = {
            ScreenHeader(
                title = "About You",
                onBackClick = { /* Handle back if needed, or disable */ }
            )
        },
        bottomBar = {
             Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                HabitButton(
                    text = "CONTINUE",
                    onClick = { onEvent(UserInfoContract.Event.OnSubmit) },
                    enabled = state.name.isNotBlank() && state.dob.isNotBlank() && state.height.isNotBlank(),
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
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Let's get to know you!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "This helps us personalize your experience.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(32.dp))

            HabitTextField(
                value = state.name,
                onValueChange = { onEvent(UserInfoContract.Event.OnNameChange(it)) },
                label = "Your Name",
                placeholder = "Enter your name"
            )

            Spacer(modifier = Modifier.height(16.dp))

            HabitTextField(
                value = state.dob,
                onValueChange = { onEvent(UserInfoContract.Event.OnDobChange(it)) },
                label = "Date of Birth",
                placeholder = "DD/MM/YYYY"
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Height",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            HabitNumberInput(
                value = state.height,
                onValueChange = { onEvent(UserInfoContract.Event.OnHeightChange(it)) },
                unit = "CM"
            )
        }
    }
}
