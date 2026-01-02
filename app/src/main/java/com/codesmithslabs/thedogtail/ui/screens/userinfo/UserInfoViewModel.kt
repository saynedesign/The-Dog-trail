package com.codesmithslabs.thedogtail.ui.screens.userinfo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codesmithslabs.thedogtail.data.UserDao
import com.codesmithslabs.thedogtail.data.UserEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserInfoViewModel @Inject constructor(
    private val userDao: UserDao
) : ViewModel() {

    private val _state = MutableStateFlow(UserInfoContract.State())
    val state: StateFlow<UserInfoContract.State> = _state.asStateFlow()

    private val _effect = Channel<UserInfoContract.Effect>()
    val effect = _effect.receiveAsFlow()

    fun handleEvent(event: UserInfoContract.Event) {
        when (event) {
            is UserInfoContract.Event.OnNameChange -> {
                _state.value = _state.value.copy(name = event.name)
            }
            is UserInfoContract.Event.OnDobChange -> {
                _state.value = _state.value.copy(dob = event.dob)
            }
            is UserInfoContract.Event.OnHeightChange -> {
                _state.value = _state.value.copy(height = event.height)
            }
            is UserInfoContract.Event.OnSubmit -> {
                saveUser()
            }
        }
    }

    private fun saveUser() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val currentState = _state.value
                val user = UserEntity(
                    name = currentState.name,
                    dob = currentState.dob,
                    height = currentState.height.toFloatOrNull() ?: 0f
                )
                userDao.insertUser(user)
                _effect.send(UserInfoContract.Effect.NavigateToHome)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, isError = true)
            }
        }
    }
}
