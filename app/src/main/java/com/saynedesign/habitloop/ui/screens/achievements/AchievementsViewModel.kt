package com.saynedesign.habitloop.ui.screens.achievements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saynedesign.habitloop.data.UserDao
import com.saynedesign.habitloop.util.LevelSystem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AchievementsViewModel @Inject constructor(
    private val userDao: UserDao
) : ViewModel() {

    private val _state = MutableStateFlow(AchievementsContract.State())
    val state = _state.asStateFlow()

    private val _effect = Channel<AchievementsContract.Effect>()
    val effect = _effect.receiveAsFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            userDao.getUser().collect { user ->
                val xp = user?.totalXp ?: 0
                val levelInfo = LevelSystem.getLevelInfoForXp(xp)
                val currentLevel = levelInfo.level
                val progress = LevelSystem.getProgressToNextLevel(xp)
                
                _state.value = _state.value.copy(
                    totalXp = xp,
                    currentLevel = currentLevel,
                    levelName = levelInfo.name,
                    levelEmoji = levelInfo.emoji,
                    nextLevel = currentLevel + 1,
                    progressToNextLevel = progress
                )
            }
        }
    }

    fun handleEvent(event: AchievementsContract.Event) {
        when (event) {
            AchievementsContract.Event.OnBackClicked -> {
                viewModelScope.launch { _effect.send(AchievementsContract.Effect.NavigateBack) }
            }
        }
    }
}
