package com.saynedesign.habitloop.ui.screens.editprofile

interface EditProfileContract {
    data class State(
        val name: String = "",
        val dob: String = "",
        val height: String = "",
        val profileImageUri: String? = null,
        val isMetric: Boolean = true, // true for CM, false for FT
        val isDatePickerVisible: Boolean = false,
        val isLoading: Boolean = false,
        val isError: Boolean = false,
        val userId: Long = 0
    )

    sealed class Event {
        data class OnNameChange(val name: String) : Event()
        data class OnDobChange(val dob: String) : Event()
        data class OnHeightChange(val height: String) : Event()
        data class OnImageSelected(val uri: String?) : Event()
        data object OnToggleHeightUnit : Event()
        data object OnToggleDatePicker : Event()
        data class OnDateSelected(val dateMillis: Long?) : Event()
        data object OnSave : Event()
        data object OnBackClicked : Event()
    }

    sealed class Effect {
        data object NavigateBack : Effect()
        data class ShowToast(val message: String) : Effect()
    }
}
