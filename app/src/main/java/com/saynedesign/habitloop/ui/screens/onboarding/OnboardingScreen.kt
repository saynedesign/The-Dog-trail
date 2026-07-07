package com.saynedesign.habitloop.ui.screens.onboarding

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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import com.saynedesign.habitloop.R
import com.saynedesign.habitloop.ui.components.HabitButton
import com.saynedesign.habitloop.ui.theme.AccentGold
import com.saynedesign.habitloop.ui.theme.AccentPeach
import com.saynedesign.habitloop.ui.theme.AccentPeachLight
import com.saynedesign.habitloop.ui.theme.TheDogTailTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource

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
                .navigationBarsPadding()
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

    val scaleAnim by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
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
        // Background glow
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .scale(scaleAnim)
                .size(220.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    )
                )
        )
        // Puppy Mascot Illustration
        Image(
            painter = painterResource(R.drawable.puppy_level_avatar),
            contentDescription = "Dog Mascot",
            modifier = Modifier
                .size(180.dp)
                .offset(y = floatAnim1.dp)
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
