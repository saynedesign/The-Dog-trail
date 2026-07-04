package com.saynedesign.habitloop.ui.screens.home

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
class HomeViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(HomeContract.State())
    val state: StateFlow<HomeContract.State> = _state.asStateFlow()

    private val _effect = Channel<HomeContract.Effect>()
    val effect = _effect.receiveAsFlow()

    fun handleEvent(event: HomeContract.Event) {
        when (event) {
            is HomeContract.Event.OnProfileClicked -> {
                _state.value = _state.value.copy(currentTab = HomeContract.HomeTab.PROFILE)
            }
            is HomeContract.Event.OnReportClicked -> {
                _state.value = _state.value.copy(currentTab = HomeContract.HomeTab.REPORT)
            }
            is HomeContract.Event.OnHomeClicked -> {
                _state.value = _state.value.copy(currentTab = HomeContract.HomeTab.HABITS)
            }
            is HomeContract.Event.OnEditProfileRequested -> {
                sendEffect(HomeContract.Effect.NavigateToEditProfile)
            }
            is HomeContract.Event.OnPreferencesRequested -> {
                sendEffect(HomeContract.Effect.NavigateToPreferences)
            }
            is HomeContract.Event.OnAchievementsRequested -> {
                sendEffect(HomeContract.Effect.NavigateToAchievements)
            }
            is HomeContract.Event.OnAppearanceRequested -> {
                sendEffect(HomeContract.Effect.NavigateToAppearance)
            }
        }
    }

    private fun sendEffect(effect: HomeContract.Effect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}
