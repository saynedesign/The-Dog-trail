package com.codesmithslabs.thedogtail

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.codesmithslabs.thedogtail.ui.screens.onboarding.OnboardingScreen
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.codesmithslabs.thedogtail.ui.screens.home.HomeContract
import com.codesmithslabs.thedogtail.ui.screens.home.HomeScreen
import com.codesmithslabs.thedogtail.ui.screens.home.HomeViewModel
import com.codesmithslabs.thedogtail.ui.screens.onboarding.OnboardingContract
import com.codesmithslabs.thedogtail.ui.screens.onboarding.OnboardingViewModel
import com.codesmithslabs.thedogtail.ui.screens.userinfo.UserInfoContract
import com.codesmithslabs.thedogtail.ui.screens.userinfo.UserInfoScreen
import com.codesmithslabs.thedogtail.ui.screens.userinfo.UserInfoViewModel
import com.codesmithslabs.thedogtail.ui.theme.TheDogTailTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TheDogTailTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "onboarding") {
                    composable("onboarding") {
                        val viewModel = hiltViewModel<OnboardingViewModel>()
                        val state by viewModel.state.collectAsState()

                        LaunchedEffect(Unit) {
                            viewModel.effect.collect { effect ->
                                when (effect) {
                                    is OnboardingContract.Effect.NavigateToUserInfo -> {
                                        navController.navigate("user_info")
                                    }
                                    is OnboardingContract.Effect.NavigateToHome -> {
                                        navController.navigate("home") {
                                            popUpTo("onboarding") { inclusive = true }
                                        }
                                    }
                                    is OnboardingContract.Effect.NavigateToLogin -> {
                                        // TODO: Navigate to Login
                                    }
                                }
                            }
                        }

                        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                            // Pass innerPadding if needed, or handle in screen
                            OnboardingScreen(
                                state = state,
                                onEvent = viewModel::handleEvent
                            )
                        }
                    }

                    composable("user_info") {
                        val viewModel = hiltViewModel<UserInfoViewModel>()
                        val state by viewModel.state.collectAsState()

                        LaunchedEffect(Unit) {
                            viewModel.effect.collect { effect ->
                                when (effect) {
                                    is UserInfoContract.Effect.NavigateToHome -> {
                                        navController.navigate("home") {
                                            popUpTo("onboarding") { inclusive = true }
                                        }
                                    }
                                }
                            }
                        }

                        UserInfoScreen(
                            state = state,
                            onEvent = viewModel::handleEvent
                        )
                    }

                    composable("home") {
                        val viewModel = hiltViewModel<HomeViewModel>()
                        val state by viewModel.state.collectAsState()

                        LaunchedEffect(Unit) {
                            viewModel.effect.collect { effect ->
                                when (effect) {
                                    is HomeContract.Effect.NavigateToAddHabit -> {
                                        // navController.navigate("create_habit")
                                    }
                                    is HomeContract.Effect.NavigateToHabitDetails -> {
                                        // navController.navigate("habit_details/${effect.habitId}")
                                    }
                                }
                            }
                        }

                        HomeScreen(
                            state = state,
                            onEvent = viewModel::handleEvent
                        )
                    }
                }
            }
        }
    }
}
