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

@HiltViewModel
class HabitDetailViewModel @Inject constructor(
    private val habitDao: HabitDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val habitId: Long = checkNotNull(savedStateHandle["habitId"]).toString().toLong()

    private val _state = MutableStateFlow(HabitDetailContract.State(isLoading = true))
    val state = _state.asStateFlow()

    private val _effect = Channel<HabitDetailContract.Effect>()
    val effect = _effect.receiveAsFlow()

    init {
        loadHabit()
    }

    private fun loadHabit() {
        viewModelScope.launch {
            try {
                val habit = habitDao.getHabitById(habitId)
                _state.value = _state.value.copy(habit = habit, isLoading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, isError = true)
            }
        }
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
                // TODO: Implement Edit
            }
        }
    }
}
