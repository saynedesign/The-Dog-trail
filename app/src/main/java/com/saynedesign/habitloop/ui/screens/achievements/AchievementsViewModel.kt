package com.saynedesign.habitloop.ui.screens.achievements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saynedesign.habitloop.data.UserDao
import com.saynedesign.habitloop.data.HabitLogDao
import com.saynedesign.habitloop.data.HabitRestDayDao
import com.saynedesign.habitloop.util.LevelSystem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class AchievementsViewModel @Inject constructor(
    private val userDao: UserDao,
    private val habitLogDao: HabitLogDao,
    private val habitRestDayDao: HabitRestDayDao
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
            combine(
                userDao.getUser(),
                habitLogDao.getAllLogs(),
                habitRestDayDao.getAllRestDays()
            ) { user, logs, restDays ->
                val xp = user?.totalXp ?: 0
                val level = LevelSystem.getLevelForXp(xp)
                val levelInfo = LevelSystem.getLevelInfo(level)
                val nextLevel = level + 1
                val isMaxLevel = level >= 10
                val nextLevelInfo = if (isMaxLevel) null else LevelSystem.getLevelInfo(nextLevel)
                val progress = if (isMaxLevel) 1.0f else LevelSystem.getProgressToNextLevel(xp)
                
                // Calculate current streak
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

                AchievementsContract.State(
                    totalXp = xp,
                    currentLevel = level,
                    levelName = levelInfo.name,
                    levelEmoji = levelInfo.emoji,
                    nextLevel = if (isMaxLevel) level else nextLevel,
                    nextLevelName = nextLevelInfo?.name ?: "Max Rank",
                    nextLevelEmoji = nextLevelInfo?.emoji ?: "👑",
                    nextLevelXp = nextLevelInfo?.requiredXp ?: levelInfo.requiredXp,
                    progressToNextLevel = progress,
                    currentStreak = activeMomentum,
                    totalCheckIns = logs.size,
                    badgesCount = level,
                    profileImageUri = user?.profileImageUri,
                    isLoading = false
                )
            }.collectLatest { newState ->
                _state.value = newState
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
