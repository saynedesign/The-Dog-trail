package com.saynedesign.habitloop.ui.screens.appearance

class AppearanceContract {
    data class State(
        val selectedTheme: String = "system"
    )

    sealed class Event {
        data object OnBackClicked : Event()
        data class OnThemeSelected(val theme: String) : Event()
    }

    sealed class Effect {
        data object NavigateBack : Effect()
        data class ShowToast(val message: String) : Effect()
    }
}
