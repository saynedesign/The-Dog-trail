package com.saynedesign.habitloop.ui.screens.about

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AboutViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(AboutContract.State())
    val state = _state.asStateFlow()

    private val _effect = Channel<AboutContract.Effect>()
    val effect = _effect.receiveAsFlow()

    init {
        loadVersionInfo()
    }

    private fun loadVersionInfo() {
        try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            val versionName = packageInfo.versionName ?: "1.0.1"
            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toString()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toString()
            }
            _state.update {
                it.copy(
                    versionName = versionName,
                    versionCode = versionCode
                )
            }
        } catch (e: Exception) {
            // Keep default values if loading fails
        }
    }

    fun handleEvent(event: AboutContract.Event) {
        when (event) {
            AboutContract.Event.OnBackClicked -> {
                viewModelScope.launch {
                    _effect.send(AboutContract.Effect.NavigateBack)
                }
            }
            is AboutContract.Event.OnFaqToggled -> {
                _state.update { currentState ->
                    val currentSet = currentState.expandedFaqIndices
                    val newSet = if (currentSet.contains(event.index)) {
                        currentSet - event.index
                    } else {
                        currentSet + event.index
                    }
                    currentState.copy(expandedFaqIndices = newSet)
                }
            }
            is AboutContract.Event.OnSubjectChanged -> {
                _state.update { it.copy(feedbackSubject = event.subject) }
            }
            is AboutContract.Event.OnMessageChanged -> {
                _state.update { it.copy(feedbackMessage = event.message) }
            }
            is AboutContract.Event.OnEmailChanged -> {
                _state.update { it.copy(feedbackEmail = event.email) }
            }
            AboutContract.Event.OnSubmitFeedback -> {
                val message = _state.value.feedbackMessage.trim()
                if (message.isEmpty()) {
                    viewModelScope.launch {
                        _effect.send(AboutContract.Effect.ShowToast("Feedback message cannot be empty"))
                    }
                    return
                }
                viewModelScope.launch {
                    val subject = "Habit Loop Feedback: ${_state.value.feedbackSubject}"
                    val emailBody = StringBuilder().apply {
                        append("Feedback Message:\n")
                        append(message)
                        append("\n\n")
                        if (_state.value.feedbackEmail.isNotBlank()) {
                            append("Reply-To Email: ${_state.value.feedbackEmail}\n\n")
                        }
                        append("---\n")
                        append("App Version: ${_state.value.versionName} (${_state.value.versionCode})\n")
                        append("Device: ${Build.MANUFACTURER} ${Build.MODEL}\n")
                        append("Android SDK: ${Build.VERSION.SDK_INT}")
                    }.toString()

                    _effect.send(
                        AboutContract.Effect.SendEmail(
                            address = "admin@sayne.design",
                            subject = subject,
                            body = emailBody
                        )
                    )
                    _state.update {
                        it.copy(
                            feedbackMessage = "",
                            feedbackEmail = ""
                        )
                    }
                }
            }
            AboutContract.Event.OnEmailSupportClicked -> {
                viewModelScope.launch {
                    _effect.send(
                        AboutContract.Effect.SendEmail(
                            address = "admin@sayne.design",
                            subject = "Habit Loop Support Request",
                            body = "\n\n---\nApp Version: ${_state.value.versionName} (${_state.value.versionCode})\nDevice: ${Build.MANUFACTURER} ${Build.MODEL}\nAndroid SDK: ${Build.VERSION.SDK_INT}"
                        )
                    )
                }
            }
            AboutContract.Event.OnRateAppClicked -> {
                viewModelScope.launch {
                    _effect.send(AboutContract.Effect.OpenUrl("market://details?id=${context.packageName}"))
                }
            }
            AboutContract.Event.OnPrivacyPolicyClicked -> {
                viewModelScope.launch {
                    _effect.send(AboutContract.Effect.OpenUrl("https://docs.google.com/document/u/2/d/e/2PACX-1vROOaomZIYD22XWLfhB7VVV_5lIQtX39qTyUWjIkupsNhoBwDhHHHdtseaGfrz5-alooiZLI6usSS_n/pub?pli=1"))
                }
            }
        }
    }
}
