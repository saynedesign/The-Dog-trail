package com.codesmithslabs.thedogtail.ui.screens.achievements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codesmithslabs.thedogtail.data.HabitLogDao
import com.codesmithslabs.thedogtail.util.LevelSystem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AchievementsViewModel @Inject constructor(
    private val habitLogDao: HabitLogDao
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
            habitLogDao.countAllLogs().collect { count ->
                val currentLevel = LevelSystem.getLevelForHabitCount(count)
                val progress = LevelSystem.getProgressToNextLevel(count)
                
                _state.value = _state.value.copy(
                    totalHabitCount = count,
                    currentLevel = currentLevel,
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
