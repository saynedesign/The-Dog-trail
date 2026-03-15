package com.codesmithslabs.thedogtail.ui.screens.onboarding

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
        // Subtle large background glow circle
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp)
                .size(340.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.06f))
        )

        // Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 32.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Spacer(modifier = Modifier.height(16.dp))

            // Illustration Area — centered
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                PeopleBubblesPlaceholder()
            }

            // Text Content — centered
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Tagline pill
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(50)
                        )
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "${stringResource(R.string.onboarding_tagline)} ${stringResource(R.string.onboarding_tagline_emoji)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = AccentGold,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Title
                Text(
                    text = stringResource(R.string.onboarding_title),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Subtitle
                Text(
                    text = stringResource(R.string.onboarding_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
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
    val infiniteTransition = rememberInfiniteTransition(label = "bubble")

    val floatAnim1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float1"
    )

    val floatAnim2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float2"
    )

    val scaleAnim by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier.size(300.dp),
        contentAlignment = Alignment.Center
    ) {
        // Large center bubble with gentle scale
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = floatAnim1.dp)
                .scale(scaleAnim)
                .size(130.dp)
                .clip(CircleShape)
                .background(AccentPeach.copy(alpha = 0.7f))
        )

        // Top-left bubble
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 30.dp, top = 30.dp)
                .offset(y = floatAnim2.dp)
                .size(85.dp)
                .clip(CircleShape)
                .background(AccentPeachLight.copy(alpha = 0.6f))
        )

        // Top-right bubble
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 15.dp, top = 55.dp)
                .offset(y = floatAnim1.dp)
                .size(65.dp)
                .clip(CircleShape)
                .background(AccentPeachLight.copy(alpha = 0.5f))
        )

        // Bottom-left small bubble
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 55.dp, bottom = 40.dp)
                .offset(y = floatAnim2.dp)
                .size(45.dp)
                .clip(CircleShape)
                .background(AccentPeach.copy(alpha = 0.4f))
        )

        // Bottom-right accent bubble
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 30.dp, bottom = 20.dp)
                .offset(y = floatAnim1.dp)
                .size(55.dp)
                .clip(CircleShape)
                .background(AccentPeachLight.copy(alpha = 0.45f))
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
