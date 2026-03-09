package com.codesmithslabs.thedogtail.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codesmithslabs.thedogtail.data.HabitLogDao
import com.codesmithslabs.thedogtail.data.UserDao
import com.codesmithslabs.thedogtail.data.UserEntity
import com.codesmithslabs.thedogtail.util.LevelSystem
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
    private val habitLogDao: HabitLogDao
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
                habitLogDao.countAllLogs()
            ) { user, count ->
                val xp = user?.totalXp ?: 0
                val level = LevelSystem.getLevelForXp(xp)
                val levelInfo = LevelSystem.getLevelInfo(level)
                val progress = LevelSystem.getProgressToNextLevel(xp)
                val nextXp = LevelSystem.getNextLevelRequirement(xp)
                ProfileContract.State(
                    userName = user?.name ?: "",
                    userDob = user?.dob ?: "",
                    userHeight = user?.height ?: 0f,
                    profileImageUri = user?.profileImageUri,
                    isLoading = false,
                    level = level,
                    totalHabitCount = count,
                    totalXp = xp,
                    xpProgress = progress,
                    levelName = levelInfo.name,
                    levelEmoji = levelInfo.emoji,
                    nextLevelXp = nextXp
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
            else -> {} // Removed unused events for clean up
        }
    }
}
