package com.codesmithslabs.thedogtail

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.codesmithslabs.thedogtail.onboarding.OnboardingScreen
import com.codesmithslabs.thedogtail.onboarding.OnboardingViewModel
import com.codesmithslabs.thedogtail.ui.theme.TheDogTailTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TheDogTailTheme {
                val viewModel = viewModel<OnboardingViewModel>()
                val state by viewModel.state.collectAsState()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Just showing OnboardingScreen for now as requested
                    // In a real app, we'd have a NavHost here
                    OnboardingScreen(
                        state = state,
                        onEvent = viewModel::handleEvent
                    )
                }
            }
        }
    }
}
