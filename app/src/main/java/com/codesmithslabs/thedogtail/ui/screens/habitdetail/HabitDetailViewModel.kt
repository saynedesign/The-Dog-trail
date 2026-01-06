package com.codesmithslabs.thedogtail.ui.screens.habitdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codesmithslabs.thedogtail.data.HabitDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.codesmithslabs.thedogtail.data.HabitLogDao
import java.time.LocalDate

@HiltViewModel
class HabitDetailViewModel @Inject constructor(
    private val habitDao: HabitDao,
    private val habitLogDao: HabitLogDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val habitId: Long = checkNotNull(savedStateHandle["habitId"]).toString().toLong()

    private val _state = MutableStateFlow(HabitDetailContract.State(isLoading = true))
    val state = _state.asStateFlow()

    private val _effect = Channel<HabitDetailContract.Effect>()
    val effect = _effect.receiveAsFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                // Load Habit
                val habit = habitDao.getHabitById(habitId)
                
                // Load Logs
                habitLogDao.getLogsForHabit(habitId).collect { logs ->
                    val streak = calculateStreak(logs)
                    val completions = logs.size
                    val totalVal = logs.sumOf { it.value?.toDouble() ?: 0.0 }.toFloat()
                    // Simple completion rate based on last 30 days? Or total possible days since creation?
                    // Let's do last 30 days consistency
                    val rate = calculateConsistency(logs)

                    _state.value = _state.value.copy(
                        habit = habit,
                        logs = logs,
                        currentStreak = streak,
                        totalCompletions = completions,
                        totalValue = totalVal,
                        completionRate = rate,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, isError = true)
            }
        }
    }

    private fun calculateStreak(logs: List<com.codesmithslabs.thedogtail.data.HabitLogEntity>): Int {
        if (logs.isEmpty()) return 0
        
        val today = LocalDate.now().toEpochDay()
        val logDates = logs.map { it.dateEpochDay }.toSet()
        
        var currentStreak = 0
        var checkDate = today
        
        // If not done today, check yesterday to start streak (unless today is done)
        if (!logDates.contains(checkDate)) {
             checkDate--
             if (!logDates.contains(checkDate)) return 0 // Streak broken or not started
        }

        while (logDates.contains(checkDate)) {
            currentStreak++
            checkDate--
        }
        return currentStreak
    }

    private fun calculateConsistency(logs: List<com.codesmithslabs.thedogtail.data.HabitLogEntity>): Int {
        if (logs.isEmpty()) return 0
        val today = LocalDate.now()
        val thirtyDaysAgo = today.minusDays(29).toEpochDay()
        val todayEpoch = today.toEpochDay()
        
        val logsInLast30Days = logs.count { it.dateEpochDay in thirtyDaysAgo..todayEpoch }
        return ((logsInLast30Days / 30f) * 100).toInt()
    }

    fun handleEvent(event: HabitDetailContract.Event) {
        when (event) {
            HabitDetailContract.Event.OnBackClicked -> {
                viewModelScope.launch { _effect.send(HabitDetailContract.Effect.NavigateBack) }
            }
            HabitDetailContract.Event.OnDeleteClicked -> {
                viewModelScope.launch {
                    _state.value.habit?.let { habitDao.deleteHabit(it) }
                    _effect.send(HabitDetailContract.Effect.NavigateBack)
                }
            }
            HabitDetailContract.Event.OnEditClicked -> {
                viewModelScope.launch { 
                    _effect.send(HabitDetailContract.Effect.NavigateToEdit(habitId)) 
                }
            }
        }
    }
}
