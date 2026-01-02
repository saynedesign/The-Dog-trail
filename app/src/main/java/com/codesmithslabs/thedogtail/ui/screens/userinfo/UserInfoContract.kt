package com.codesmithslabs.thedogtail.ui.screens.userinfo

interface UserInfoContract {
    data class State(
        val name: String = "",
        val dob: String = "",
        val height: String = "",
        val isLoading: Boolean = false,
        val isError: Boolean = false
    )

    sealed class Event {
        data class OnNameChange(val name: String) : Event()
        data class OnDobChange(val dob: String) : Event()
        data class OnHeightChange(val height: String) : Event()
        data object OnSubmit : Event()
    }

    sealed class Effect {
        data object NavigateToHome : Effect()
    }
}
