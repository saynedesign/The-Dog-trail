package com.codesmithslabs.thedogtail.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codesmithslabs.thedogtail.data.HabitLogDao
import com.codesmithslabs.thedogtail.data.UserDao
import com.codesmithslabs.thedogtail.data.UserEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userDao: UserDao,
    private val habitLogDao: HabitLogDao
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileContract.State())
    val state = _state.asStateFlow()

    private val _effect = Channel<ProfileContract.Effect>()
    val effect = _effect.receiveAsFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            userDao.getUser().collectLatest { user ->
                _state.update { currentState ->
                    currentState.copy(
                        userName = user?.name ?: "",
                        userDob = user?.dob ?: "",
                        userHeight = user?.height ?: 0f,
                        profileImageUri = user?.profileImageUri,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun handleEvent(event: ProfileContract.Event) {
        when (event) {
            ProfileContract.Event.OnBackClicked -> {
                viewModelScope.launch { _effect.send(ProfileContract.Effect.NavigateBack) }
            }
            ProfileContract.Event.OnEditProfileClicked -> {
                viewModelScope.launch { _effect.send(ProfileContract.Effect.NavigateToEditProfile) }
            }
            ProfileContract.Event.OnBackupClicked -> {
                viewModelScope.launch { _effect.send(ProfileContract.Effect.ShowToast("Backup feature coming soon!")) }
            }
            ProfileContract.Event.OnLogoutClicked -> {
                viewModelScope.launch { _effect.send(ProfileContract.Effect.ShowToast("Logout feature coming soon!")) }
            }
            ProfileContract.Event.OnPreferencesClicked -> {
                viewModelScope.launch { _effect.send(ProfileContract.Effect.NavigateToPreferences) }
            }
            ProfileContract.Event.OnPersonalInfoClicked -> {
                // Map to Edit Profile for now
                viewModelScope.launch { _effect.send(ProfileContract.Effect.NavigateToEditProfile) }
            }
            ProfileContract.Event.OnAccountSecurityClicked -> {
                viewModelScope.launch { _effect.send(ProfileContract.Effect.ShowToast("Account & Security coming soon!")) }
            }
            ProfileContract.Event.OnLinkedAccountsClicked -> {
                viewModelScope.launch { _effect.send(ProfileContract.Effect.ShowToast("Linked Accounts coming soon!")) }
            }
            ProfileContract.Event.OnAppAppearanceClicked -> {
                viewModelScope.launch { _effect.send(ProfileContract.Effect.ShowToast("App Appearance coming soon!")) }
            }
            ProfileContract.Event.OnDataAnalyticsClicked -> {
                viewModelScope.launch { _effect.send(ProfileContract.Effect.ShowToast("Data & Analytics coming soon!")) }
            }
            ProfileContract.Event.OnHelpSupportClicked -> {
                viewModelScope.launch { _effect.send(ProfileContract.Effect.ShowToast("Help & Support coming soon!")) }
            }
            ProfileContract.Event.OnLevelBannerClicked -> {
                viewModelScope.launch { _effect.send(ProfileContract.Effect.ShowToast("Level Details coming soon!")) }
            }
        }
    }
}
