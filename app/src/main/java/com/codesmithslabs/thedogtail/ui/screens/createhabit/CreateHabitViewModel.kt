package com.codesmithslabs.thedogtail.ui.screens.createhabit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateHabitViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(CreateHabitContract.State())
    val state: StateFlow<CreateHabitContract.State> = _state.asStateFlow()

    private val _effect = Channel<CreateHabitContract.Effect>()
    val effect = _effect.receiveAsFlow()

    fun handleEvent(event: CreateHabitContract.Event) {
        when (event) {
            is CreateHabitContract.Event.OnNameChange -> {
                _state.value = _state.value.copy(habitName = event.name)
            }
            is CreateHabitContract.Event.OnTypeChange -> {
                _state.value = _state.value.copy(habitType = event.type)
            }
            is CreateHabitContract.Event.OnTargetChange -> {
                _state.value = _state.value.copy(target = event.target)
            }
            is CreateHabitContract.Event.OnUnitNameChange -> {
                _state.value = _state.value.copy(unitName = event.unitName)
            }
            is CreateHabitContract.Event.OnGoalTypeChange -> {
                _state.value = _state.value.copy(isAtLeast = event.isAtLeast)
            }
            is CreateHabitContract.Event.OnSaveClicked -> {
                // TODO: Save logic
                viewModelScope.launch {
                    _effect.send(CreateHabitContract.Effect.NavigateBack)
                }
            }
            is CreateHabitContract.Event.OnBackClicked -> {
                viewModelScope.launch {
                    _effect.send(CreateHabitContract.Effect.NavigateBack)
                }
            }
        }
    }
}
