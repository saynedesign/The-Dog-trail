package com.codesmithslabs.thedogtail.ui.screens.home

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.codesmithslabs.thedogtail.R
import com.codesmithslabs.thedogtail.ui.screens.home.habits.HabitsContract
import com.codesmithslabs.thedogtail.ui.screens.home.habits.HabitsScreen
import com.codesmithslabs.thedogtail.ui.screens.home.habits.HabitsViewModel
import com.codesmithslabs.thedogtail.ui.screens.mood.MoodStatsScreen
import com.codesmithslabs.thedogtail.ui.screens.mood.MoodViewModel
import com.codesmithslabs.thedogtail.ui.screens.profile.ProfileContract
import com.codesmithslabs.thedogtail.ui.screens.profile.ProfileScreen
import com.codesmithslabs.thedogtail.ui.screens.profile.ProfileViewModel
import com.codesmithslabs.thedogtail.ui.screens.report.ReportScreen
import com.codesmithslabs.thedogtail.ui.screens.report.ReportViewModel

@Composable
fun HomeScreen(
    state: HomeContract.State,
    onEvent: (HomeContract.Event) -> Unit
) {
    val view = LocalView.current
    val isDarkTheme = isSystemInDarkTheme()
    
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as android.app.Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDarkTheme
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            // Floating Dock Bottom Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
            ) {
                HomeBottomNavigation(
                    currentTab = state.currentTab,
                    modifier = Modifier.align(Alignment.BottomCenter),
                    onHomeClick = { onEvent(HomeContract.Event.OnHomeClicked) },
                    onProfileClick = { onEvent(HomeContract.Event.OnProfileClicked) },
                    onMoodClick = { onEvent(HomeContract.Event.OnMoodClicked) },
                    onReportClick = { onEvent(HomeContract.Event.OnReportClicked) }
                )
            }
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = state.currentTab,
            transitionSpec = {
                val direction = if (targetState.index() > initialState.index()) 1 else -1
                (slideInHorizontally(
                    initialOffsetX = { fullWidth -> (fullWidth / 3) * direction },
                    animationSpec = tween(260)
                ) + fadeIn(animationSpec = tween(220))).togetherWith(
                    slideOutHorizontally(
                        targetOffsetX = { fullWidth -> -(fullWidth / 4) * direction },
                        animationSpec = tween(220)
                    ) + fadeOut(animationSpec = tween(180))
                )
            },
            label = "home_tab_transition"
        ) { currentTab ->
            Box(modifier = Modifier.fillMaxSize()) {
                when (currentTab) {
                    HomeContract.HomeTab.MOOD -> {
                        val moodViewModel = hiltViewModel<MoodViewModel>()
                        val moodState by moodViewModel.state.collectAsState()

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = innerPadding.calculateBottomPadding())
                        ) {
                            MoodStatsScreen(
                                state = moodState,
                                onEvent = moodViewModel::handleEvent,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    HomeContract.HomeTab.REPORT -> {
                        val reportViewModel = hiltViewModel<ReportViewModel>()
                        val reportState by reportViewModel.state.collectAsState()

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = innerPadding.calculateBottomPadding())
                        ) {
                            ReportScreen(
                                state = reportState,
                                onEvent = reportViewModel::handleEvent,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    HomeContract.HomeTab.PROFILE -> {
                        val profileViewModel = hiltViewModel<ProfileViewModel>()
                        val profileState by profileViewModel.state.collectAsState()
                        val context = LocalContext.current

                        LaunchedEffect(Unit) {
                            profileViewModel.effect.collect { effect ->
                                when (effect) {
                                    is ProfileContract.Effect.NavigateToEditProfile -> {
                                        onEvent(HomeContract.Event.OnEditProfileRequested)
                                    }
                                    is ProfileContract.Effect.NavigateToPreferences -> {
                                        onEvent(HomeContract.Event.OnPreferencesRequested)
                                    }
                                    is ProfileContract.Effect.NavigateToAchievements -> {
                                        onEvent(HomeContract.Event.OnAchievementsRequested)
                                    }
                                    is ProfileContract.Effect.ShowToast -> {
                                        Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                                    }
                                    else -> {}
                                }
                            }
                        }

                        ProfileScreen(
                            state = profileState,
                            onEvent = profileViewModel::handleEvent,
                            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
                            showBackButton = false
                        )
                    }

                    HomeContract.HomeTab.HABITS -> {
                        val habitsViewModel = hiltViewModel<HabitsViewModel>()
                        val habitsState by habitsViewModel.state.collectAsState()

                        // Forward navigation effects triggered by HabitsViewModel upwards
                        LaunchedEffect(Unit) {
                            habitsViewModel.effect.collect { effect ->
                                // Note: Depending on your navigation setup, you may need a callback in HomeScreen 
                                // to handle these effects directly or forward them to the NavGraph.
                                // Typically, these callbacks are passed as parameters to the top-level Screen components.
                                // For now, we will handle them in the host component where this Screen is declared.
                            }
                        }

                        HabitsScreen(
                            state = habitsState,
                            onEvent = habitsViewModel::handleEvent,
                            modifier = Modifier.fillMaxSize(),
                            innerPadding = innerPadding
                        )
                    }
                } // End when
            } // End Box wrapping AnimatedContent child
        }
    }
}

@Composable
fun HomeBottomNavigation(
    currentTab: HomeContract.HomeTab,
    modifier: Modifier = Modifier,
    onHomeClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onMoodClick: () -> Unit = {},
    onReportClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(70.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            HomeBottomNavigationItem(
                selected = currentTab == HomeContract.HomeTab.HABITS,
                imageVector = Icons.Default.Home,
                contentDescription = stringResource(R.string.home_nav_home),
                onClick = onHomeClick
            )

            HomeBottomNavigationItem(
                selected = currentTab == HomeContract.HomeTab.MOOD,
                imageVector = Icons.Default.Face,
                contentDescription = stringResource(R.string.home_nav_mood),
                onClick = onMoodClick
            )

            HomeBottomNavigationItem(
                selected = currentTab == HomeContract.HomeTab.REPORT,
                imageVector = Icons.Default.BarChart,
                contentDescription = stringResource(R.string.home_nav_stats),
                onClick = onReportClick
            )

            HomeBottomNavigationItem(
                selected = currentTab == HomeContract.HomeTab.PROFILE,
                imageVector = Icons.Default.Person,
                contentDescription = stringResource(R.string.home_nav_profile),
                onClick = onProfileClick
            )
        }
    }
}

@Composable
private fun HomeBottomNavigationItem(
    selected: Boolean,
    imageVector: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    val tint by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(220),
        label = "bottom_nav_tint"
    )
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.12f else 1f,
        animationSpec = tween(220),
        label = "bottom_nav_scale"
    )

    IconButton(onClick = onClick) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier
                .size(28.dp)
                .scale(scale)
        )
    }
}

private fun HomeContract.HomeTab.index(): Int {
    return when (this) {
        HomeContract.HomeTab.HABITS -> 0
        HomeContract.HomeTab.MOOD -> 1
        HomeContract.HomeTab.REPORT -> 2
        HomeContract.HomeTab.PROFILE -> 3
    }
}
