package com.codesmithslabs.thedogtail

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.codesmithslabs.thedogtail.ui.screens.createhabit.CreateHabitContract
import com.codesmithslabs.thedogtail.ui.screens.createhabit.CreateHabitScreen
import com.codesmithslabs.thedogtail.ui.screens.createhabit.CreateHabitViewModel
import com.codesmithslabs.thedogtail.ui.screens.editprofile.EditProfileContract
import com.codesmithslabs.thedogtail.ui.screens.editprofile.EditProfileScreen
import com.codesmithslabs.thedogtail.ui.screens.editprofile.EditProfileViewModel
import com.codesmithslabs.thedogtail.ui.screens.habitdetail.HabitDetailContract
import com.codesmithslabs.thedogtail.ui.screens.habitdetail.HabitDetailScreen
import com.codesmithslabs.thedogtail.ui.screens.habitdetail.HabitDetailViewModel
import com.codesmithslabs.thedogtail.ui.screens.home.HomeContract
import com.codesmithslabs.thedogtail.ui.screens.home.HomeScreen
import com.codesmithslabs.thedogtail.ui.screens.home.HomeViewModel
import com.codesmithslabs.thedogtail.ui.screens.onboarding.OnboardingContract
import com.codesmithslabs.thedogtail.ui.screens.onboarding.OnboardingScreen
import com.codesmithslabs.thedogtail.ui.screens.onboarding.OnboardingViewModel
import com.codesmithslabs.thedogtail.ui.screens.profile.ProfileContract
import com.codesmithslabs.thedogtail.ui.screens.profile.ProfileScreen
import com.codesmithslabs.thedogtail.ui.screens.profile.ProfileViewModel
import com.codesmithslabs.thedogtail.ui.screens.timer.TimerContract
import com.codesmithslabs.thedogtail.ui.screens.timer.TimerScreen
import com.codesmithslabs.thedogtail.ui.screens.timer.TimerViewModel
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
                val mainViewModel = hiltViewModel<MainViewModel>()
                val isLoading by mainViewModel.isLoading.collectAsState()
                val startDestination by mainViewModel.startDestination.collectAsState()

                if (!isLoading) {
                    // Handle notification click
                    LaunchedEffect(Unit) {
                        val habitId = intent.getLongExtra("habitId", -1L)
                        if (habitId != -1L) {
                            navController.navigate("habit_details/$habitId")
                            intent.removeExtra("habitId")
                        }
                    }

                    NavHost(navController = navController, startDestination = startDestination) {
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
                                            navController.navigate("create_habit")
                                        }

                                        is HomeContract.Effect.NavigateToHabitDetails -> {
                                            navController.navigate("habit_details/${effect.habitId}")
                                        }
                                        is HomeContract.Effect.NavigateToProfile -> {
                                            navController.navigate("profile")
                                        }
                                        is HomeContract.Effect.NavigateToTimer -> {
                                            navController.navigate("timer/${effect.habitId}")
                                        }
                                        is HomeContract.Effect.NavigateToEditHabit -> {
                                            navController.navigate("create_habit?habitId=${effect.habitId}")
                                        }
                                    }
                                }
                            }

                            HomeScreen(
                                state = state,
                                onEvent = viewModel::handleEvent
                            )
                        }
                        
                        composable(
                            "create_habit?habitId={habitId}",
                            arguments = listOf(navArgument("habitId") {
                                type = NavType.LongType
                                defaultValue = -1L
                            })
                        ) {
                            val viewModel = hiltViewModel<CreateHabitViewModel>()
                            val state by viewModel.state.collectAsState()

                            LaunchedEffect(Unit) {
                                viewModel.effect.collect { effect ->
                                    when (effect) {
                                        is CreateHabitContract.Effect.NavigateBack -> {
                                            navController.popBackStack()
                                        }
                                        is CreateHabitContract.Effect.ShowToast -> {
                                            // TODO: Show toast
                                        }
                                    }
                                }
                            }

                            CreateHabitScreen(
                                state = state,
                                onEvent = viewModel::handleEvent
                            )
                        }

                        composable("profile") {
                            val viewModel = hiltViewModel<ProfileViewModel>()
                            val state by viewModel.state.collectAsState()

                            LaunchedEffect(Unit) {
                                viewModel.effect.collect { effect ->
                                    when (effect) {
                                        is ProfileContract.Effect.NavigateBack -> {
                                            navController.popBackStack()
                                        }

                                        ProfileContract.Effect.NavigateToEditProfile -> {
                                            navController.navigate("edit_profile")
                                        }
                                        is ProfileContract.Effect.ShowToast -> {
                                            Toast.makeText(this@MainActivity, effect.message, Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }

                            ProfileScreen(
                                state = state,
                                onEvent = viewModel::handleEvent
                            )
                        }

                        composable("edit_profile") {
                            val viewModel = hiltViewModel<EditProfileViewModel>()
                            val state by viewModel.state.collectAsState()

                            LaunchedEffect(Unit) {
                                viewModel.effect.collect { effect ->
                                    when (effect) {
                                        is EditProfileContract.Effect.NavigateBack -> {
                                            navController.popBackStack()
                                        }
                                        is EditProfileContract.Effect.ShowToast -> {
                                            Toast.makeText(this@MainActivity, effect.message, Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }

                            EditProfileScreen(
                                state = state,
                                onEvent = viewModel::handleEvent
                            )
                        }

                        composable(
                            "timer/{habitId}",
                            arguments = listOf(navArgument("habitId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val viewModel = hiltViewModel<TimerViewModel>()
                            val state by viewModel.state.collectAsState()
                            val habitId = backStackEntry.arguments?.getLong("habitId") ?: 0L

                            LaunchedEffect(habitId) {
                                viewModel.handleEvent(TimerContract.Event.LoadHabit(habitId))
                            }

                            LaunchedEffect(Unit) {
                                viewModel.effect.collect { effect ->
                                    when (effect) {
                                        is TimerContract.Effect.NavigateBack -> {
                                            navController.popBackStack()
                                        }
                                    }
                                }
                            }

                            TimerScreen(
                                state = state,
                                onEvent = viewModel::handleEvent
                            )
                        }

                        composable(
                            "habit_details/{habitId}",
                            arguments = listOf(navArgument("habitId") { type = NavType.LongType })
                        ) {
                            val viewModel = hiltViewModel<HabitDetailViewModel>()
                            val state by viewModel.state.collectAsState()

                            LaunchedEffect(Unit) {
                                viewModel.effect.collect { effect ->
                                    when (effect) {
                                        is HabitDetailContract.Effect.NavigateBack -> {
                                            navController.popBackStack()
                                        }
                                        is HabitDetailContract.Effect.NavigateToEdit -> {
                                            navController.navigate("create_habit?habitId=${effect.habitId}")
                                        }
                                    }
                                }
                            }

                            HabitDetailScreen(
                                state = state,
                                onEvent = viewModel::handleEvent
                            )
                        }
                    }
                }
            }
        }
    }
}
