package com.codesmithslabs.thedogtail.ui.screens.profile

interface ProfileContract {
    data class State(
        val userName: String = "",
        val userDob: String = "",
        val userHeight: Float = 0f,
        val profileImageUri: String? = null,
        val isLoading: Boolean = true
    )

    sealed class Event {
        data object OnBackClicked : Event()
        data object OnEditProfileClicked : Event()
        data object OnBackupClicked : Event()
        data object OnLogoutClicked : Event()
    }

    sealed class Effect {
        data object NavigateBack : Effect()
        data object NavigateToEditProfile : Effect()
        data class ShowToast(val message: String) : Effect()
    }
}
