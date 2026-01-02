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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.codesmithslabs.thedogtail.ui.components.HabitCard
import com.codesmithslabs.thedogtail.ui.components.HomeHeader
import com.codesmithslabs.thedogtail.ui.theme.BrandBlue
import com.codesmithslabs.thedogtail.ui.theme.BrandSurface
import com.codesmithslabs.thedogtail.ui.theme.SuccessGreen
import com.codesmithslabs.thedogtail.ui.theme.TextPrimary
import com.codesmithslabs.thedogtail.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    state: HomeContract.State,
    onEvent: (HomeContract.Event) -> Unit
) {
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
            HomeBottomNavigation()
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                HomeHeader(
                    userName = state.userName,
                    subtitle = "Let's make habits together!",
                    profileImage = {
                        // Placeholder for profile image
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White
                        )
                    },
                    onNotificationClick = { /* TODO */ },
                    modifier = Modifier.padding(horizontal = 0.dp) // Reset default padding from component
                )
            }

            item {
                // Tab selector (Today / Clubs)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color.White, shape = CircleShape)
                            .padding(4.dp)
                    ) {
                        Row {
                             Box(
                                modifier = Modifier
                                    .background(Color.White, shape = CircleShape)
                                    .padding(horizontal = 24.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "Today",
                                    color = BrandBlue,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 24.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "Clubs",
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            item {
                CalendarStrip(
                    selectedDate = state.selectedDate,
                    onDateSelected = { onEvent(HomeContract.Event.OnDateSelected(it)) }
                )
            }

            item {
                DailyGoalCard(completed = 1, total = 4)
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Challenges",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "VIEW ALL",
                        style = MaterialTheme.typography.labelSmall,
                        color = BrandBlue,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { /* TODO */ }
                    )
                }
            }

            item {
                ChallengeCard(
                    title = "Best Runners! \uD83E\uDD38\u200D\u2642\uFE0F",
                    timeLeft = "5 days 13 hours left",
                    icon = Icons.Default.DirectionsRun
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
                            .background(BrandSurface, CircleShape) // Slightly different background
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

            // Static habits for now + dynamic ones
            item {
                HabitCard(
                    title = "Drink the water",
                    subtitle = "500/2000 ML",
                    icon = Icons.Default.LocalDrink,
                    iconTint = BrandBlue,
                    onClick = { /* TODO */ },
                    rightContent = {
                        // Progress / Toggle
                         Icon(
                             imageVector = Icons.Default.Add,
                             contentDescription = null,
                             tint = TextSecondary,
                             modifier = Modifier
                                 .size(32.dp)
                                 .background(Color.White, CircleShape)
                                 .padding(4.dp)
                         )
                    }
                )
            }
            
            item {
                 HabitCard(
                    title = "Walk",
                    subtitle = "0/10000 STEPS",
                    icon = Icons.Default.DirectionsRun,
                    iconTint = Color(0xFFFFCC80),
                    onClick = { /* TODO */ },
                    rightContent = {
                        Switch(
                            checked = true,
                            onCheckedChange = {},
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = SuccessGreen,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = TextSecondary
                            )
                        )
                    }
                )
            }

             item {
                 HabitCard(
                    title = "Water Plants",
                    subtitle = "0/1 TIMES",
                    icon = Icons.Default.LocalFlorist,
                    iconTint = SuccessGreen,
                    onClick = { /* TODO */ },
                    rightContent = {
                        Icon(
                             imageVector = Icons.Default.Add,
                             contentDescription = null,
                             tint = TextSecondary,
                             modifier = Modifier
                                 .size(32.dp)
                                 .background(Color.White, CircleShape)
                                 .padding(4.dp)
                         )
                    }
                )
            }
            
            // Spacer for FAB and Bottom Bar
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun HomeBottomNavigation() {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = true,
            onClick = { /*TODO*/ },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = BrandBlue,
                indicatorColor = Color.Transparent,
                unselectedIconColor = TextSecondary
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = { /*TODO*/ },
            icon = { Icon(Icons.Default.Explore, contentDescription = "Explore") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = BrandBlue,
                indicatorColor = Color.Transparent,
                unselectedIconColor = TextSecondary
            )
        )
        // Spacer for FAB
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { },
            enabled = false
        )
        NavigationBarItem(
            selected = false,
            onClick = { /*TODO*/ },
            icon = { Icon(Icons.Default.BarChart, contentDescription = "Stats") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = BrandBlue,
                indicatorColor = Color.Transparent,
                unselectedIconColor = TextSecondary
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = { /*TODO*/ },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = BrandBlue,
                indicatorColor = Color.Transparent,
                unselectedIconColor = TextSecondary
            )
        )
    }
}
