package com.saynedesign.habitloop.ui.screens.home

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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
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
import com.saynedesign.habitloop.R
import com.saynedesign.habitloop.ui.screens.home.habits.HabitsContract
import com.saynedesign.habitloop.ui.screens.home.habits.HabitsScreen
import com.saynedesign.habitloop.ui.screens.home.habits.HabitsViewModel
import com.saynedesign.habitloop.ui.screens.profile.ProfileContract
import com.saynedesign.habitloop.ui.screens.profile.ProfileScreen
import com.saynedesign.habitloop.ui.screens.profile.ProfileViewModel
import com.saynedesign.habitloop.ui.screens.report.ReportScreen
import com.saynedesign.habitloop.ui.screens.report.ReportViewModel

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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
            ) {
                HomeBottomNavigation(
                    currentTab = state.currentTab,
                    modifier = Modifier.align(Alignment.BottomCenter),
                    onHomeClick = { onEvent(HomeContract.Event.OnHomeClicked) },
                    onProfileClick = { onEvent(HomeContract.Event.OnProfileClicked) },
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

                        LaunchedEffect(Unit) {
                            habitsViewModel.effect.collect { _ -> }
                        }

                        HabitsScreen(
                            state = habitsState,
                            onEvent = habitsViewModel::handleEvent,
                            modifier = Modifier.fillMaxSize(),
                            innerPadding = innerPadding
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HomeBottomNavigation(
    currentTab: HomeContract.HomeTab,
    modifier: Modifier = Modifier,
    onHomeClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onReportClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp),
        shape = RoundedCornerShape(36.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSystemInDarkTheme()) Color(0xFF1C202B) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            HomeBottomNavigationItem(
                selected = currentTab == HomeContract.HomeTab.HABITS,
                imageVector = Icons.Default.Home,
                label = "Home",
                onClick = onHomeClick
            )

            HomeBottomNavigationItem(
                selected = currentTab == HomeContract.HomeTab.REPORT,
                imageVector = Icons.Default.BarChart,
                label = "Stats",
                onClick = onReportClick
            )

            HomeBottomNavigationItem(
                selected = currentTab == HomeContract.HomeTab.PROFILE,
                imageVector = Icons.Default.Person,
                label = "Profile",
                onClick = onProfileClick
            )
        }
    }
}

@Composable
private fun HomeBottomNavigationItem(
    selected: Boolean,
    imageVector: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val primaryColor = Color(0xFF4B68FF)
    
    val bgBrush = if (selected) {
        if (isDark) Color(0xFF222635) else Color(0xFFEEF1FF)
    } else {
        Color.Transparent
    }

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(bgBrush)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = label,
                tint = if (selected) primaryColor else (if (isDark) Color(0xFF8B93A6) else Color(0xFF757575)),
                modifier = Modifier.size(24.dp)
            )
            
            if (selected) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
            }
        }
    }
}

private fun HomeContract.HomeTab.index(): Int {
    return when (this) {
        HomeContract.HomeTab.HABITS -> 0
        HomeContract.HomeTab.REPORT -> 1
        HomeContract.HomeTab.PROFILE -> 2
    }
}
