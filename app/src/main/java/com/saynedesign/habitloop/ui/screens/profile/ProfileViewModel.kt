package com.saynedesign.habitloop.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saynedesign.habitloop.data.HabitLogDao
import com.saynedesign.habitloop.data.HabitRestDayDao
import com.saynedesign.habitloop.data.UserDao
import com.saynedesign.habitloop.data.UserEntity
import com.saynedesign.habitloop.util.LevelSystem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userDao: UserDao,
    private val habitLogDao: HabitLogDao,
    private val habitRestDayDao: HabitRestDayDao
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileContract.State())
    val state = _state.asStateFlow()

    private val _effect = Channel<ProfileContract.Effect>()
    val effect = _effect.receiveAsFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                userDao.getUser(),
                habitLogDao.getAllLogs(),
                habitRestDayDao.getAllRestDays()
            ) { user, logs, restDays ->
                val xp = user?.totalXp ?: 0
                val level = LevelSystem.getLevelForXp(xp)
                val levelInfo = LevelSystem.getLevelInfo(level)
                val progress = LevelSystem.getProgressToNextLevel(xp)
                val nextXp = LevelSystem.getNextLevelRequirement(xp)

                // Calculate current streak & best streak
                val today = LocalDate.now()
                val logsByDay = logs.groupBy { it.dateEpochDay }
                val restDaysByEpoch = restDays.groupBy { it.dateEpochDay }

                var activeMomentum = 0
                var checkDate = today
                val todayEpoch = today.toEpochDay()
                val todayLogs = logsByDay[todayEpoch] ?: emptyList()
                val todayHasRest = restDaysByEpoch.containsKey(todayEpoch)
                if (todayLogs.isEmpty() && !todayHasRest) {
                    checkDate = checkDate.minusDays(1)
                }
                while (true) {
                    val epoch = checkDate.toEpochDay()
                    val dayLogs = logsByDay[epoch] ?: emptyList()
                    val dayHasRest = restDaysByEpoch.containsKey(epoch)
                    when {
                        dayHasRest && dayLogs.isEmpty() -> {
                            checkDate = checkDate.minusDays(1)
                        }
                        dayLogs.isNotEmpty() -> {
                            activeMomentum++
                            checkDate = checkDate.minusDays(1)
                        }
                        else -> break
                    }
                    if (activeMomentum > 3650) break
                }

                val minDate = logs.minOfOrNull { it.dateEpochDay } ?: today.toEpochDay()
                val maxDate = today.toEpochDay()
                var bestStreak = 0
                var runningStreak = 0
                for (dayEpoch in minDate..maxDate) {
                    val dayLogs = logsByDay[dayEpoch] ?: emptyList()
                    val dayHasRest = restDaysByEpoch.containsKey(dayEpoch)
                    if (dayLogs.isNotEmpty()) {
                        runningStreak++
                    } else if (dayHasRest) {
                        // Rest day: neutral
                    } else {
                        if (dayEpoch == maxDate) {
                            // Don't reset running streak on today yet
                        } else {
                            bestStreak = maxOf(bestStreak, runningStreak)
                            runningStreak = 0
                        }
                    }
                }
                bestStreak = maxOf(bestStreak, runningStreak)

                ProfileContract.State(
                    userName = user?.name ?: "",
                    userDob = user?.dob ?: "",
                    userHeight = user?.height ?: 0f,
                    profileImageUri = user?.profileImageUri,
                    isLoading = false,
                    level = level,
                    totalHabitCount = logs.size,
                    totalXp = xp,
                    xpProgress = progress,
                    levelName = levelInfo.name,
                    levelEmoji = levelInfo.emoji,
                    nextLevelXp = nextXp,
                    currentStreak = activeMomentum,
                    bestStreak = bestStreak,
                    totalCheckIns = logs.size,
                    badgesCount = level
                )
            }.collectLatest { newState ->
                _state.value = newState
            }
        }
    }

    fun handleEvent(event: ProfileContract.Event) {
        when (event) {
            ProfileContract.Event.OnBackClicked -> {
                viewModelScope.launch { _effect.send(ProfileContract.Effect.NavigateBack) }
            }
            ProfileContract.Event.OnEditProfileClicked -> {
                viewModelScope.launch { _effect.send(ProfileContract.Effect.NavigateToEditProfile) }
            }
            ProfileContract.Event.OnPreferencesClicked -> {
                viewModelScope.launch { _effect.send(ProfileContract.Effect.NavigateToPreferences) }
            }
            ProfileContract.Event.OnPersonalInfoClicked -> {
                // Map to Edit Profile for now
                viewModelScope.launch { _effect.send(ProfileContract.Effect.NavigateToEditProfile) }
            }
            ProfileContract.Event.OnLevelBannerClicked -> {
                viewModelScope.launch { _effect.send(ProfileContract.Effect.NavigateToAchievements) }
            }
            ProfileContract.Event.OnTrackNewHabitClicked -> {
                viewModelScope.launch { _effect.send(ProfileContract.Effect.NavigateToCreateHabit) }
            }
            ProfileContract.Event.OnViewStatsClicked -> {
                viewModelScope.launch { _effect.send(ProfileContract.Effect.NavigateToStats) }
            }
            ProfileContract.Event.OnAppAppearanceClicked -> {
                viewModelScope.launch { _effect.send(ProfileContract.Effect.NavigateToAppearance) }
            }
            else -> {} // Removed unused events for clean up
        }
    }
}
