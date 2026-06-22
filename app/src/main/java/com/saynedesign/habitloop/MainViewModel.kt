package com.saynedesign.habitloop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saynedesign.habitloop.data.UserDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userDao: UserDao
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _startDestination = MutableStateFlow("onboarding")
    val startDestination = _startDestination.asStateFlow()

    init {
        checkUserExistence()
    }

    private fun checkUserExistence() {
        viewModelScope.launch {
            // Check if we have any user
            val user = userDao.getUserOneShot()
            if (user != null) {
                _startDestination.value = "home"
            } else {
                _startDestination.value = "onboarding"
            }
            _isLoading.value = false
        }
    }
}
