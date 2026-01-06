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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
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

@OptIn(ExperimentalMaterial3Api::class)
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
                HomeBottomNavigation(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    onProfileClick = { onEvent(HomeContract.Event.OnProfileClicked) }
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding()) // Only top padding from scaffold
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp) // Reduced spacing
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
            
            items(state.habits, key = { it.id }) { habit ->
                val log = state.habitLogs[habit.id]
                
                // Swipe to Edit (Left) and Delete (Right) or vice versa
                // SwipeToDismissBox implementation
                
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = {
                        when (it) {
                            SwipeToDismissBoxValue.StartToEnd -> {
                                // Swipe Right -> Edit
                                onEvent(HomeContract.Event.OnEditHabitClicked(habit.id))
                                false // Don't dismiss, just trigger action
                            }
                            SwipeToDismissBoxValue.EndToStart -> {
                                // Swipe Left -> Delete
                                onEvent(HomeContract.Event.OnDeleteHabitClicked(habit.id))
                                true // Dismiss and delete
                            }
                            SwipeToDismissBoxValue.Settled -> false
                        }
                    }
                )

                SwipeToDismissBox(
                    state = dismissState,
                    backgroundContent = {
                        val color = when (dismissState.targetValue) {
                            SwipeToDismissBoxValue.StartToEnd -> BrandBlue // Edit
                            SwipeToDismissBoxValue.EndToStart -> Color.Red // Delete
                            else -> Color.Transparent
                        }
                        val alignment = when (dismissState.targetValue) {
                            SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                            SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                            else -> Alignment.CenterStart
                        }
                        val icon = when (dismissState.targetValue) {
                            SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Edit
                            SwipeToDismissBoxValue.EndToStart -> Icons.Default.Delete
                            else -> Icons.Default.Edit
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 8.dp) // Match card padding
                                .background(color, RoundedCornerShape(20.dp))
                                .padding(horizontal = 20.dp),
                            contentAlignment = alignment
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    },
                    content = {
                        HabitCard(
                            title = habit.title,
                            subtitle = when (habit.type) {
                                "NUMERIC" -> {
                                    val current = log?.value ?: 0f
                                    val target = if (habit.targetValue % 1.0 == 0.0) {
                                        habit.targetValue.toInt().toString()
                                    } else {
                                        habit.targetValue.toString()
                                    }
                                    // Show current / target
                                    val currentStr = if (current % 1.0 == 0.0) current.toInt().toString() else current.toString()
                                    "$currentStr / $target ${habit.unit}"
                                }
                                "TIMER" -> {
                                     val currentMinutes = log?.value?.toInt() ?: 0
                                     if (currentMinutes > 0) "$currentMinutes min completed" else "Timer Habit"
                                }
                                else -> "Simple Habit"
                            },
                            icon = when (habit.type) {
                                "NUMERIC" -> Icons.AutoMirrored.Filled.List
                                "TIMER" -> Icons.Default.Timer
                                else -> Icons.Default.Check
                            },
                            iconTint = Color(habit.color),
                            onClick = { 
                                if (habit.type == "TIMER") {
                                    onEvent(HomeContract.Event.OnTimerClicked(habit.id))
                                } else {
                                    onEvent(HomeContract.Event.OnHabitClicked(habit.id)) 
                                }
                            },
                            rightContent = {
                                when (habit.type) {
                                    "NUMERIC" -> {
                                        val current = log?.value ?: 0f
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            IconButton(
                                                onClick = { onEvent(HomeContract.Event.OnUpdateHabitValue(habit.id, current - 1)) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = TextSecondary)
                                            }
                                            
                                            Text(
                                                text = if (current % 1.0 == 0.0) current.toInt().toString() else current.toString(),
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                            
                                            IconButton(
                                                onClick = { onEvent(HomeContract.Event.OnUpdateHabitValue(habit.id, current + 1)) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.Add, contentDescription = "Increase", tint = BrandBlue)
                                            }
                                        }
                                    }
                                    "TIMER" -> {
                                        IconButton(
                                            onClick = { onEvent(HomeContract.Event.OnTimerClicked(habit.id)) },
                                            modifier = Modifier.background(BrandBlue.copy(alpha = 0.1f), CircleShape)
                                        ) {
                                            Icon(Icons.Default.PlayArrow, contentDescription = "Start Timer", tint = BrandBlue)
                                        }
                                    }
                                    else -> {
                                        Checkbox(
                                            checked = log != null, // Simple existence check for YES_NO
                                            onCheckedChange = { isChecked ->
                                                onEvent(HomeContract.Event.OnToggleHabit(habit.id, isChecked))
                                            },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = BrandBlue,
                                                uncheckedColor = TextSecondary,
                                                checkmarkColor = Color.White
                                            )
                                        )
                                    }
                                }
                            }
                        )
                    }
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
fun HomeBottomNavigation(
    modifier: Modifier = Modifier,
    onProfileClick: () -> Unit = {}
) {
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
                modifier = Modifier.size(28.dp).clickable { onProfileClick() }
            )
        }
    }
}
