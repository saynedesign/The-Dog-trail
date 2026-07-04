package com.saynedesign.habitloop.ui.screens.about

interface AboutContract {
    data class State(
        val versionName: String = "1.0.1",
        val versionCode: String = "2",
        val feedbackSubject: String = "General Feedback",
        val feedbackMessage: String = "",
        val feedbackEmail: String = "",
        val isSubmitting: Boolean = false,
        val expandedFaqIndices: Set<Int> = emptySet()
    )

    sealed class Event {
        data object OnBackClicked : Event()
        data class OnFaqToggled(val index: Int) : Event()
        data class OnSubjectChanged(val subject: String) : Event()
        data class OnMessageChanged(val message: String) : Event()
        data class OnEmailChanged(val email: String) : Event()
        data object OnSubmitFeedback : Event()
        data object OnEmailSupportClicked : Event()
        data object OnRateAppClicked : Event()
        data object OnPrivacyPolicyClicked : Event()
    }

    sealed class Effect {
        data object NavigateBack : Effect()
        data class ShowToast(val message: String) : Effect()
        data class OpenUrl(val url: String) : Effect()
        data class SendEmail(val address: String, val subject: String, val body: String) : Effect()
    }
}
