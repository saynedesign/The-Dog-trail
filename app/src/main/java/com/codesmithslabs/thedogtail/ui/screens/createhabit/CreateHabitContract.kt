package com.codesmithslabs.thedogtail.ui.screens.createhabit

interface CreateHabitContract {
    data class State(
        val habitName: String = "",
        val habitIcon: String? = null,
        val habitType: HabitType = HabitType.NUMERIC,
        val target: String = "10",
        val unitName: String = "Pages",
        val isAtLeast: Boolean = true, // At least vs At most
        val currentPreviewValue: Float = 4f, // For the slider preview
        val isLoading: Boolean = false,
        val isError: Boolean = false
    )

    enum class HabitType {
        NUMERIC, YES_NO, TIMER
    }

    sealed class Event {
        data class OnNameChange(val name: String) : Event()
        data class OnTypeChange(val type: HabitType) : Event()
        data class OnTargetChange(val target: String) : Event()
        data class OnUnitNameChange(val unitName: String) : Event()
        data class OnGoalTypeChange(val isAtLeast: Boolean) : Event() // At least / At most
        data object OnSaveClicked : Event()
        data object OnBackClicked : Event()
    }

    sealed class Effect {
        data object NavigateBack : Effect()
        data class ShowToast(val message: String) : Effect()
    }
}
