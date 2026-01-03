package com.codesmithslabs.thedogtail.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codesmithslabs.thedogtail.data.HabitDao
import com.codesmithslabs.thedogtail.data.UserDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val habitDao: HabitDao,
    private val userDao: UserDao
) : ViewModel() {

    private val _state = MutableStateFlow(HomeContract.State())
    val state: StateFlow<HomeContract.State> = _state.asStateFlow()

    private val _effect = Channel<HomeContract.Effect>()
    val effect = _effect.receiveAsFlow()

    init {
        loadUserData()
        loadHabits()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            // Get the single user (Flow<UserEntity?>)
            userDao.getUser().collect { user ->
                if (user != null) {
                    _state.value = _state.value.copy(
                        userName = user.name,
                        userImageUri = user.profileImageUri
                    )
                }
            }
        }
    }

    private fun loadHabits() {
        viewModelScope.launch {
            habitDao.getAllHabits().collect { habits ->
                _state.value = _state.value.copy(habits = habits)
            }
        }
    }

    fun handleEvent(event: HomeContract.Event) {
        when (event) {
            is HomeContract.Event.OnAddHabitClicked -> {
                sendEffect(HomeContract.Effect.NavigateToAddHabit)
            }
            is HomeContract.Event.OnHabitClicked -> {
                sendEffect(HomeContract.Effect.NavigateToHabitDetails(event.habitId))
            }
            is HomeContract.Event.OnDateSelected -> {
                _state.value = _state.value.copy(selectedDate = event.date)
            }
            is HomeContract.Event.OnToggleHabit -> {
                toggleHabit(event.habitId, event.isDone)
            }
            is HomeContract.Event.OnProfileClicked -> {
                sendEffect(HomeContract.Effect.NavigateToProfile)
            }
        }
    }

    private fun toggleHabit(habitId: Long, isDone: Boolean) {
        viewModelScope.launch {
            val habit = _state.value.habits.find { it.id == habitId } ?: return@launch
            val updatedHabit = habit.copy(isCompletedToday = isDone)
            habitDao.updateHabit(updatedHabit)
        }
    }

    private fun sendEffect(effect: HomeContract.Effect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}
