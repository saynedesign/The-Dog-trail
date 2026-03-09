package com.codesmithslabs.thedogtail.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.codesmithslabs.thedogtail.R
import com.codesmithslabs.thedogtail.ui.components.HabitButton
import com.codesmithslabs.thedogtail.ui.theme.AccentGold
import com.codesmithslabs.thedogtail.ui.theme.AccentPeach
import com.codesmithslabs.thedogtail.ui.theme.AccentPeachLight
import com.codesmithslabs.thedogtail.ui.theme.TheDogTailTheme

@Composable
fun OnboardingScreen(
    state: OnboardingContract.State,
    onEvent: (OnboardingContract.Event) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary
                    )
                )
            )
    ) {
        // Decorative Circles (Approximating the illustration background)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 100.dp)
                .size(300.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f))
        )
        
        // Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            // Placeholder for Illustration
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                 // In a real app, this would be an Image composable
                 // For now, we simulate the bubbles with a simple composition or leave blank
                 // to focus on the UI structure requested.
                 // Using a simple placeholder representation
                 PeopleBubblesPlaceholder()
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                // Tagline
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(50)
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "${stringResource(R.string.onboarding_tagline)} ${stringResource(R.string.onboarding_tagline_emoji)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = AccentGold,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Title
                Text(
                    text = stringResource(R.string.onboarding_title),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Subtitle
                Text(
                    text = stringResource(R.string.onboarding_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // CTA Button
            HabitButton(
                text = stringResource(R.string.onboarding_button_start),
                onClick = { onEvent(OnboardingContract.Event.OnStartClicked) },
                containerColor = MaterialTheme.colorScheme.onPrimary,
                contentColor = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun PeopleBubblesPlaceholder() {
    // A simplified visual representation of the bubbles in the design
    Box(
        modifier = Modifier.size(300.dp)
    ) {
        // Top Left Bubble
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 40.dp, top = 40.dp)
                .size(80.dp)
                .clip(CircleShape)
                .background(AccentPeachLight)
        )
        
        // Bottom Center Bubble
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 20.dp)
                .size(120.dp)
                .clip(CircleShape)
                .background(AccentPeach)
        )
        
         // Top Right Bubble
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 20.dp, top = 60.dp)
                .size(60.dp)
                .clip(CircleShape)
                .background(AccentPeachLight)
        )
    }
}


@Preview
@Composable
fun OnboardingScreenPreview() {
    TheDogTailTheme {
        OnboardingScreen(
            state = OnboardingContract.State(),
            onEvent = {}
        )
    }
}
