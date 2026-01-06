package com.codesmithslabs.thedogtail.ui.screens.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codesmithslabs.thedogtail.data.HabitDao
import com.codesmithslabs.thedogtail.data.HabitLogDao
import com.codesmithslabs.thedogtail.data.HabitLogEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor(
    private val habitDao: HabitDao,
    private val habitLogDao: HabitLogDao
) : ViewModel() {

    private val _state = MutableStateFlow(TimerContract.State())
    val state = _state.asStateFlow()

    private val _effect = Channel<TimerContract.Effect>()
    val effect = _effect.receiveAsFlow()

    private var timerJob: Job? = null

    fun handleEvent(event: TimerContract.Event) {
        when (event) {
            is TimerContract.Event.LoadHabit -> loadHabit(event.habitId)
            TimerContract.Event.OnStart -> startTimer()
            TimerContract.Event.OnPause -> pauseTimer()
            TimerContract.Event.OnReset -> resetTimer()
            TimerContract.Event.OnFinish -> finishSession()
            TimerContract.Event.OnBackClicked -> sendEffect(TimerContract.Effect.NavigateBack)
            is TimerContract.Event.OnDurationChange -> updateDuration(event.minutes)
        }
    }

    private fun loadHabit(habitId: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, habitId = habitId)
            val habit = habitDao.getHabitById(habitId)
            if (habit != null) {
                // Use habit's target value as duration if it's a timer type and has a value?
                // For now default to 25 or existing state
                _state.value = _state.value.copy(
                    habitTitle = habit.title,
                    isLoading = false
                )
            } else {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    private fun startTimer() {
        if (_state.value.isRunning) return
        _state.value = _state.value.copy(isRunning = true)
        timerJob = viewModelScope.launch {
            while (_state.value.timeLeftSeconds > 0) {
                delay(1000)
                _state.value = _state.value.copy(timeLeftSeconds = _state.value.timeLeftSeconds - 1)
            }
            _state.value = _state.value.copy(isRunning = false, isCompleted = true)
            // Auto-finish or wait for user? "once the timer complete we can mark it as task done"
            finishSession() 
        }
    }

    private fun pauseTimer() {
        timerJob?.cancel()
        _state.value = _state.value.copy(isRunning = false)
    }

    private fun resetTimer() {
        pauseTimer()
        _state.value = _state.value.copy(
            timeLeftSeconds = _state.value.totalTimeSeconds,
            isCompleted = false
        )
    }

    private fun updateDuration(minutes: Int) {
        val seconds = minutes * 60L
        _state.value = _state.value.copy(
            totalTimeSeconds = seconds,
            timeLeftSeconds = seconds
        )
    }

    private fun finishSession() {
        viewModelScope.launch {
            val today = LocalDate.now().toEpochDay()
            // Mark as done for today
            // Value could be duration in minutes?
            val durationMinutes = _state.value.totalTimeSeconds / 60f
            habitLogDao.insertLog(
                HabitLogEntity(
                    habitId = _state.value.habitId,
                    dateEpochDay = today,
                    value = durationMinutes
                )
            )
            sendEffect(TimerContract.Effect.NavigateBack)
        }
    }

    private fun sendEffect(effect: TimerContract.Effect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}
