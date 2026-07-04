package com.saynedesign.habitloop.ui.screens.appearance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saynedesign.habitloop.data.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppearanceViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AppearanceContract.State())
    val state = _state.asStateFlow()

    private val _effect = Channel<AppearanceContract.Effect>()
    val effect = _effect.receiveAsFlow()

    init {
        viewModelScope.launch {
            preferencesRepository.appTheme.collectLatest { theme ->
                _state.update { it.copy(selectedTheme = theme) }
            }
        }
    }

    fun handleEvent(event: AppearanceContract.Event) {
        when (event) {
            AppearanceContract.Event.OnBackClicked -> {
                viewModelScope.launch {
                    _effect.send(AppearanceContract.Effect.NavigateBack)
                }
            }
            is AppearanceContract.Event.OnThemeSelected -> {
                viewModelScope.launch {
                    preferencesRepository.updateAppTheme(event.theme)
                    val displayMessage = when (event.theme) {
                        "light" -> "Light mode activated!"
                        "dark" -> "Dark mode activated!"
                        else -> "System default theme applied!"
                    }
                    _effect.send(AppearanceContract.Effect.ShowToast(displayMessage))
                }
            }
        }
    }
}
