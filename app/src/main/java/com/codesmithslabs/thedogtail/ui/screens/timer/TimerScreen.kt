package com.codesmithslabs.thedogtail.ui.screens.timer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.codesmithslabs.thedogtail.R
import com.codesmithslabs.thedogtail.ui.theme.BrandBackground
import com.codesmithslabs.thedogtail.ui.theme.BrandBlue
import com.codesmithslabs.thedogtail.ui.theme.BrandSurface
import com.codesmithslabs.thedogtail.ui.theme.TextPrimary
import com.codesmithslabs.thedogtail.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    state: TimerContract.State,
    onEvent: (TimerContract.Event) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.habitTitle, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { onEvent(TimerContract.Event.OnBackClicked) }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandBackground)
            )
        },
        containerColor = BrandBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Timer Display
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(280.dp)
            ) {
                CircularProgressIndicator(
                    progress = { state.timeLeftSeconds.toFloat() / state.totalTimeSeconds.toFloat() },
                    modifier = Modifier.fillMaxSize(),
                    color = BrandBlue,
                    trackColor = BrandSurface,
                    strokeWidth = 12.dp,
                    strokeCap = StrokeCap.Round
                )
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val minutes = state.timeLeftSeconds / 60
                    val seconds = state.timeLeftSeconds % 60
                    Text(
                        text = String.format("%02d:%02d", minutes, seconds),
                        style = MaterialTheme.typography.displayLarge,
                        color = TextPrimary
                    )
                    Text(
                        text = if (state.isRunning) {
                            stringResource(R.string.timer_focusing)
                        } else {
                            stringResource(R.string.timer_ready)
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = TextSecondary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (state.isRunning) {
                    Button(
                        onClick = { onEvent(TimerContract.Event.OnPause) },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandSurface, contentColor = TextPrimary),
                        shape = CircleShape,
                        modifier = Modifier.size(64.dp),
                         contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                    ) {
                        Icon(
                            Icons.Default.Stop,
                            contentDescription = stringResource(R.string.timer_pause),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                } else {
                    Button(
                        onClick = { onEvent(TimerContract.Event.OnStart) },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                        shape = CircleShape,
                        modifier = Modifier.size(80.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = stringResource(R.string.timer_start),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
                
                Button(
                    onClick = { onEvent(TimerContract.Event.OnReset) },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandSurface, contentColor = TextPrimary),
                    shape = CircleShape,
                    modifier = Modifier.size(64.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.timer_reset),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Settings
            if (!state.isRunning) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.timer_duration_minutes, state.totalTimeSeconds / 60),
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Slider(
                        value = (state.totalTimeSeconds / 60).toFloat(),
                        onValueChange = { onEvent(TimerContract.Event.OnDurationChange(it.toInt())) },
                        valueRange = 5f..120f,
                        steps = 23, // 5, 10, ... 120 (steps = (range/step) - 1) -> (115/5) - 1 = 22
                        colors = SliderDefaults.colors(
                            thumbColor = BrandBlue,
                            activeTrackColor = BrandBlue,
                            inactiveTrackColor = BrandSurface
                        )
                    )
                }
            }
        }
    }
}
