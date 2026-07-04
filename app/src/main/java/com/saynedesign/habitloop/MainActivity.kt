package com.saynedesign.habitloop

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.NavDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.saynedesign.habitloop.ui.screens.createhabit.CreateHabitContract
import com.saynedesign.habitloop.ui.screens.createhabit.CreateHabitScreen
import com.saynedesign.habitloop.ui.screens.createhabit.CreateHabitViewModel
import com.saynedesign.habitloop.ui.screens.achievements.AchievementsContract
import com.saynedesign.habitloop.ui.screens.achievements.AchievementsScreen
import com.saynedesign.habitloop.ui.screens.achievements.AchievementsViewModel
import com.saynedesign.habitloop.ui.screens.editprofile.EditProfileContract
import android.content.Intent
import android.net.Uri
import com.saynedesign.habitloop.ui.screens.about.AboutContract
import com.saynedesign.habitloop.ui.screens.about.AboutScreen
import com.saynedesign.habitloop.ui.screens.about.AboutViewModel
import com.saynedesign.habitloop.ui.screens.editprofile.EditProfileScreen
import com.saynedesign.habitloop.ui.screens.editprofile.EditProfileViewModel
import com.saynedesign.habitloop.ui.screens.habitdetail.HabitDetailContract
import com.saynedesign.habitloop.ui.screens.habitdetail.HabitDetailScreen
import com.saynedesign.habitloop.ui.screens.habitdetail.HabitDetailViewModel
import com.saynedesign.habitloop.ui.screens.home.HomeContract
import com.saynedesign.habitloop.ui.screens.home.HomeScreen
import com.saynedesign.habitloop.ui.screens.home.HomeViewModel
import com.saynedesign.habitloop.ui.screens.home.habits.HabitsContract
import com.saynedesign.habitloop.ui.screens.home.habits.HabitsViewModel
import com.saynedesign.habitloop.ui.screens.onboarding.OnboardingContract
import com.saynedesign.habitloop.ui.screens.onboarding.OnboardingScreen
import com.saynedesign.habitloop.ui.screens.onboarding.OnboardingViewModel
import com.saynedesign.habitloop.ui.screens.preferences.PreferencesContract
import com.saynedesign.habitloop.ui.screens.preferences.PreferencesScreen
import com.saynedesign.habitloop.ui.screens.preferences.PreferencesViewModel
import com.saynedesign.habitloop.ui.screens.profile.ProfileContract
import com.saynedesign.habitloop.ui.screens.profile.ProfileScreen
import com.saynedesign.habitloop.ui.screens.profile.ProfileViewModel
import com.saynedesign.habitloop.ui.screens.timer.TimerContract
import com.saynedesign.habitloop.ui.screens.timer.TimerScreen
import com.saynedesign.habitloop.ui.screens.timer.TimerViewModel
import com.saynedesign.habitloop.ui.screens.userinfo.UserInfoContract
import com.saynedesign.habitloop.ui.screens.userinfo.UserInfoScreen
import com.saynedesign.habitloop.ui.screens.userinfo.UserInfoViewModel
import com.saynedesign.habitloop.ui.theme.TheDogTailTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private enum class TransitionStyle {
        Modal, Standard
    }

    private fun NavDestination.routeName(): String {
        return route.orEmpty().substringBefore("?").substringBefore("/")
    }

    private fun NavBackStackEntry.transitionStyle(): TransitionStyle {
        return when (destination.routeName()) {
            "create_habit",
            "edit_profile",
            "preferences",
            "achievements",
            "timer",
            "appearance",
            "about",
                -> TransitionStyle.Modal
            else -> TransitionStyle.Standard
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        setContent {
            val mainViewModel = hiltViewModel<MainViewModel>()
            val themeState by mainViewModel.theme.collectAsState()
            val darkTheme = when (themeState) {
                "light" -> false
                "dark" -> true
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }
            TheDogTailTheme(darkTheme = darkTheme) {
                val navController = rememberNavController()
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

                    NavHost(
                        navController = navController,
                        startDestination = startDestination,
                        enterTransition = {
                            when (targetState.transitionStyle()) {
                                TransitionStyle.Modal -> {
                                    slideInVertically(
                                        initialOffsetY = { fullHeight -> fullHeight / 4 },
                                        animationSpec = tween(280)
                                    ) + fadeIn(animationSpec = tween(220))
                                }
                                TransitionStyle.Standard -> {
                                    slideInHorizontally(
                                        initialOffsetX = { fullWidth -> fullWidth / 3 },
                                        animationSpec = tween(320)
                                    ) + fadeIn(animationSpec = tween(240))
                                }
                            }
                        },
                        exitTransition = {
                            when (targetState.transitionStyle()) {
                                TransitionStyle.Modal -> {
                                    fadeOut(animationSpec = tween(140))
                                }
                                TransitionStyle.Standard -> {
                                    slideOutHorizontally(
                                        targetOffsetX = { fullWidth -> -(fullWidth / 6) },
                                        animationSpec = tween(260)
                                    ) + fadeOut(animationSpec = tween(200))
                                }
                            }
                        },
                        popEnterTransition = {
                            when (initialState.transitionStyle()) {
                                TransitionStyle.Modal -> {
                                    fadeIn(animationSpec = tween(180))
                                }
                                TransitionStyle.Standard -> {
                                    slideInHorizontally(
                                        initialOffsetX = { fullWidth -> -(fullWidth / 6) },
                                        animationSpec = tween(280)
                                    ) + fadeIn(animationSpec = tween(220))
                                }
                            }
                        },
                        popExitTransition = {
                            when (initialState.transitionStyle()) {
                                TransitionStyle.Modal -> {
                                    slideOutVertically(
                                        targetOffsetY = { fullHeight -> fullHeight / 4 },
                                        animationSpec = tween(260)
                                    ) + fadeOut(animationSpec = tween(220))
                                }
                                TransitionStyle.Standard -> {
                                    slideOutHorizontally(
                                        targetOffsetX = { fullWidth -> fullWidth / 3 },
                                        animationSpec = tween(300)
                                    ) + fadeOut(animationSpec = tween(220))
                                }
                            }
                        }
                    ) {
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
                                    }
                                }
                            }

                            OnboardingScreen(
                                state = state,
                                onEvent = viewModel::handleEvent
                            )
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
                            val homeViewModel = hiltViewModel<HomeViewModel>()
                            val homeState by homeViewModel.state.collectAsState()
                            
                            val habitsViewModel = hiltViewModel<HabitsViewModel>()

                            LaunchedEffect(Unit) {
                                homeViewModel.effect.collect { effect ->
                                    when (effect) {
                                        is HomeContract.Effect.NavigateToProfile -> {
                                            navController.navigate("profile")
                                        }
                                        is HomeContract.Effect.NavigateToEditProfile -> {
                                            navController.navigate("edit_profile")
                                        }
                                        is HomeContract.Effect.NavigateToPreferences -> {
                                            navController.navigate("preferences")
                                        }
                                        is HomeContract.Effect.NavigateToAppearance -> {
                                            navController.navigate("appearance")
                                        }
                                        is HomeContract.Effect.NavigateToAchievements -> {
                                            navController.navigate("achievements")
                                        }
                                        is HomeContract.Effect.NavigateToAbout -> {
                                            navController.navigate("about")
                                        }
                                    }
                                }
                            }

                            LaunchedEffect(Unit) {
                                habitsViewModel.effect.collect { effect ->
                                    when (effect) {
                                        is HabitsContract.Effect.NavigateToAddHabit -> {
                                            navController.navigate("create_habit")
                                        }
                                        is HabitsContract.Effect.NavigateToHabitDetails -> {
                                            navController.navigate("habit_details/${effect.habitId}")
                                        }
                                        is HabitsContract.Effect.NavigateToTimer -> {
                                            navController.navigate("timer/${effect.habitId}")
                                        }
                                        is HabitsContract.Effect.NavigateToEditHabit -> {
                                            navController.navigate("create_habit?habitId=${effect.habitId}")
                                        }
                                    }
                                }
                            }

                            HomeScreen(
                                state = homeState,
                                onEvent = homeViewModel::handleEvent
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
                                            Toast.makeText(this@MainActivity, effect.message, Toast.LENGTH_SHORT).show()
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
                                        is ProfileContract.Effect.NavigateToPreferences -> {
                                            navController.navigate("preferences")
                                        }
                                        is ProfileContract.Effect.NavigateToAppearance -> {
                                            navController.navigate("appearance")
                                        }
                                        is ProfileContract.Effect.NavigateToAchievements -> {
                                            navController.navigate("achievements")
                                        }
                                        is ProfileContract.Effect.NavigateToCreateHabit -> {
                                            navController.navigate("create_habit")
                                        }
                                        is ProfileContract.Effect.NavigateToStats -> {
                                            navController.navigate("home")
                                        }
                                        is ProfileContract.Effect.NavigateToAbout -> {
                                            navController.navigate("about")
                                        }
                                    }
                                }
                            }

                            ProfileScreen(
                                state = state,
                                onEvent = viewModel::handleEvent,
                                showBackButton = true
                            )
                        }

                        composable("about") {
                            val viewModel = hiltViewModel<AboutViewModel>()
                            val state by viewModel.state.collectAsState()

                            LaunchedEffect(Unit) {
                                viewModel.effect.collect { effect ->
                                    when (effect) {
                                        is AboutContract.Effect.NavigateBack -> {
                                            navController.popBackStack()
                                        }
                                        is AboutContract.Effect.ShowToast -> {
                                            Toast.makeText(this@MainActivity, effect.message, Toast.LENGTH_SHORT).show()
                                        }
                                        is AboutContract.Effect.OpenUrl -> {
                                            try {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(effect.url))
                                                startActivity(intent)
                                            } catch (e: Exception) {
                                                Toast.makeText(this@MainActivity, "Could not open link", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        is AboutContract.Effect.SendEmail -> {
                                            try {
                                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                                    data = Uri.parse("mailto:")
                                                    putExtra(Intent.EXTRA_EMAIL, arrayOf(effect.address))
                                                    putExtra(Intent.EXTRA_SUBJECT, effect.subject)
                                                    putExtra(Intent.EXTRA_TEXT, effect.body)
                                                }
                                                startActivity(Intent.createChooser(intent, "Send Email"))
                                            } catch (e: Exception) {
                                                Toast.makeText(this@MainActivity, "Could not open email client", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                }
                            }

                            AboutScreen(
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

                        composable("preferences") {
                            val viewModel = hiltViewModel<PreferencesViewModel>()
                            val state by viewModel.state.collectAsState()

                            LaunchedEffect(Unit) {
                                viewModel.effect.collect { effect ->
                                    when (effect) {
                                        is PreferencesContract.Effect.NavigateBack -> {
                                            navController.popBackStack()
                                        }
                                        is PreferencesContract.Effect.ShowToast -> {
                                            Toast.makeText(this@MainActivity, effect.message, Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }

                            PreferencesScreen(
                                state = state,
                                onEvent = viewModel::handleEvent
                            )
                        }

                        composable("appearance") {
                            val viewModel = hiltViewModel<com.saynedesign.habitloop.ui.screens.appearance.AppearanceViewModel>()
                            val state by viewModel.state.collectAsState()

                            LaunchedEffect(Unit) {
                                viewModel.effect.collect { effect ->
                                    when (effect) {
                                        is com.saynedesign.habitloop.ui.screens.appearance.AppearanceContract.Effect.NavigateBack -> {
                                            navController.popBackStack()
                                        }
                                        is com.saynedesign.habitloop.ui.screens.appearance.AppearanceContract.Effect.ShowToast -> {
                                            Toast.makeText(this@MainActivity, effect.message, Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }

                            com.saynedesign.habitloop.ui.screens.appearance.AppearanceScreen(
                                state = state,
                                onEvent = viewModel::handleEvent
                            )
                        }

                        composable("achievements") {
                            val viewModel = hiltViewModel<AchievementsViewModel>()
                            val state by viewModel.state.collectAsState()

                            LaunchedEffect(Unit) {
                                viewModel.effect.collect { effect ->
                                    when (effect) {
                                        is AchievementsContract.Effect.NavigateBack -> {
                                            navController.popBackStack()
                                        }
                                    }
                                }
                            }

                            AchievementsScreen(
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
