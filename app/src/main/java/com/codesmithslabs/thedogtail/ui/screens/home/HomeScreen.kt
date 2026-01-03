package com.codesmithslabs.thedogtail.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import coil.compose.AsyncImage
import com.codesmithslabs.thedogtail.data.HabitEntity
import com.codesmithslabs.thedogtail.ui.components.HabitCard
import com.codesmithslabs.thedogtail.ui.components.HomeHeader
import com.codesmithslabs.thedogtail.ui.theme.BrandBlue
import com.codesmithslabs.thedogtail.ui.theme.BrandSurface
import com.codesmithslabs.thedogtail.ui.theme.TextPrimary
import com.codesmithslabs.thedogtail.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    state: HomeContract.State,
    onEvent: (HomeContract.Event) -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as android.app.Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BrandSurface,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onEvent(HomeContract.Event.OnAddHabitClicked) },
                containerColor = BrandBlue,
                contentColor = Color.White,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp),
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Habit",
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        bottomBar = {
            // Floating Dock Bottom Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
            ) {
                HomeBottomNavigation(modifier = Modifier.align(Alignment.BottomCenter))
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding()) // Only top padding from scaffold
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp) // Increased spacing
        ) {
            item {
                HomeHeader(
                    userName = state.userName,
                    subtitle = "Let's make habits together!",
                    profileImage = {
                        if (state.userImageUri != null) {
                            AsyncImage(
                                model = state.userImageUri, // Use URI string directly
                                contentDescription = "Profile Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = BrandBlue
                            )
                        }
                    },
                    onNotificationClick = { /* TODO */ },
                    modifier = Modifier.padding(horizontal = 0.dp)
                )
            }


            item {
                CalendarStrip(
                    selectedDate = state.selectedDate,
                    onDateSelected = { onEvent(HomeContract.Event.OnDateSelected(it)) }
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Habits",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    
                    // Simple + button
                     Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(BrandSurface, CircleShape)
                            .clickable { onEvent(HomeContract.Event.OnAddHabitClicked) },
                        contentAlignment = Alignment.Center
                    ) {
                         Icon(
                             imageVector = Icons.Default.Add,
                             contentDescription = "Add",
                             tint = TextSecondary
                         )
                    }
                }
            }
            
            items(state.habits) { habit ->
                HabitCard(
                    title = habit.title,
                    subtitle = when (habit.type) {
                        "NUMERIC" -> {
                            val target = if (habit.targetValue % 1.0 == 0.0) {
                                habit.targetValue.toInt().toString()
                            } else {
                                habit.targetValue.toString()
                            }
                            "Goal: $target ${habit.unit}"
                        }
                        "TIMER" -> "Timer Habit"
                        else -> "Simple Habit"
                    },
                    icon = when (habit.type) {
                        "NUMERIC" -> Icons.Default.List
                        "TIMER" -> Icons.Default.Timer
                        else -> Icons.Default.Check
                    },
                    iconTint = Color(habit.color),
                    onClick = { onEvent(HomeContract.Event.OnHabitClicked(habit.id)) }
                )
            }
            
            // Spacer for FAB and Bottom Bar
            item {
                Spacer(modifier = Modifier.height(120.dp))
            }
        }
    }
}

@Composable
fun HomeBottomNavigation(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(70.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = "Home",
                tint = BrandBlue,
                modifier = Modifier.size(28.dp).clickable { /*TODO*/ }
            )
            Icon(
                imageVector = Icons.Default.Explore,
                contentDescription = "Explore",
                tint = TextSecondary,
                modifier = Modifier.size(28.dp).clickable { /*TODO*/ }
            )
            
            // Spacer for FAB
            Spacer(modifier = Modifier.size(48.dp))

            Icon(
                imageVector = Icons.Default.BarChart,
                contentDescription = "Stats",
                tint = TextSecondary,
                modifier = Modifier.size(28.dp).clickable { /*TODO*/ }
            )
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile",
                tint = TextSecondary,
                modifier = Modifier.size(28.dp).clickable { /*TODO*/ }
            )
        }
    }
}