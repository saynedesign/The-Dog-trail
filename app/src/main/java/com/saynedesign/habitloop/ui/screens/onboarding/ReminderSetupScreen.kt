package com.saynedesign.habitloop.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.saynedesign.habitloop.ui.components.HabitButton
import com.saynedesign.habitloop.ui.theme.AccentGold

@Composable
fun ReminderSetupScreen(
    state: ReminderSetupContract.State,
    onEvent: (ReminderSetupContract.Event) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val onPrimary = MaterialTheme.colorScheme.onPrimary

    // Overlay-permission flow: if the user chose the full-screen alarm but hasn't
    // granted "Display over other apps", send them to the system screen and
    // continue automatically when they return.
    var pendingPermission by rememberSaveable { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && pendingPermission) {
                pendingPermission = false
                onEvent(ReminderSetupContract.Event.OnContinue)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    fun onContinue() {
        if (state.selectedStyle == "overlay" && !android.provider.Settings.canDrawOverlays(context)) {
            pendingPermission = true
            context.startActivity(
                android.content.Intent(
                    android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    android.net.Uri.parse("package:${context.packageName}")
                )
            )
        } else {
            onEvent(ReminderSetupContract.Event.OnContinue)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Never miss a habit",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = onPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "How should we nudge you when it's time?",
                style = MaterialTheme.typography.bodyLarge,
                color = onPrimary.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            // Side-by-side choice
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ChoiceCard(
                    emoji = "🔔",
                    title = "Full-Screen Alarm",
                    tagline = "Takes over your screen with sound",
                    recommended = true,
                    selected = state.selectedStyle == "overlay",
                    modifier = Modifier.weight(1f),
                    onClick = { onEvent(ReminderSetupContract.Event.OnSelectStyle("overlay")) }
                )
                ChoiceCard(
                    emoji = "📩",
                    title = "Notification",
                    tagline = "A quiet heads-up in the tray",
                    recommended = false,
                    selected = state.selectedStyle == "notification",
                    modifier = Modifier.weight(1f),
                    onClick = { onEvent(ReminderSetupContract.Event.OnSelectStyle("notification")) }
                )
            }

            Spacer(Modifier.height(24.dp))

            // Why full-screen works — behavioral-science backed
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(onPrimary.copy(alpha = 0.10f))
                    .padding(18.dp)
            ) {
                Text(
                    "WHY FULL-SCREEN WORKS BETTER",
                    style = MaterialTheme.typography.labelMedium,
                    color = AccentGold,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(14.dp))
                SciencePoint(
                    "👁️",
                    "You'll actually see it",
                    "We ignore dozens of notifications a day on autopilot — \"notification blindness\" is real. A takeover can't be swiped away unseen."
                )
                SciencePoint(
                    "✅",
                    "It forces a decision",
                    "Do it, snooze, or skip — an active choice beats a reflex swipe. Deciding exactly when you'll act roughly doubles follow-through (implementation intentions)."
                )
                SciencePoint(
                    "🎯",
                    "A strong cue builds the habit",
                    "Habits form on a cue → routine → reward loop. Behavioral science says an unmissable cue at the right moment is the single biggest lever."
                )
                SciencePoint(
                    "⏰",
                    "It breaks autopilot",
                    "Like an alarm clock, it interrupts whatever you're doing and creates a real \"now\" moment — where silent reminders quietly fail.",
                    isLast = true
                )
            }

            Spacer(Modifier.height(24.dp))

            HabitButton(
                text = if (state.selectedStyle == "overlay") "Enable Full-Screen Reminders" else "Continue",
                onClick = { onContinue() },
                containerColor = onPrimary,
                contentColor = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(10.dp))
            Text(
                "You can change this anytime in Settings.",
                style = MaterialTheme.typography.bodySmall,
                color = onPrimary.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ChoiceCard(
    emoji: String,
    title: String,
    tagline: String,
    recommended: Boolean,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val border = if (selected) AccentGold else Color.White.copy(alpha = 0.18f)
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = if (selected) 0.16f else 0.07f))
            .border(if (selected) 2.dp else 1.dp, border, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Text(emoji, fontSize = 34.sp)
        }
        Spacer(Modifier.height(10.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        Text(
            tagline,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        if (recommended) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(AccentGold.copy(alpha = if (selected) 1f else 0.25f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    "RECOMMENDED",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (selected) Color(0xFF3A2E00) else AccentGold
                )
            }
        } else {
            Text(
                if (selected) "Selected" else "Tap to choose",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.55f)
            )
        }
    }
}

@Composable
private fun SciencePoint(emoji: String, title: String, body: String, isLast: Boolean = false) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, fontSize = 15.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(Modifier.height(2.dp))
            Text(
                body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.72f)
            )
        }
    }
    if (!isLast) Spacer(Modifier.height(14.dp))
}
