package com.saynedesign.habitloop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saynedesign.habitloop.data.UserDao
import com.saynedesign.habitloop.data.XpEventDao
import com.saynedesign.habitloop.util.LevelSystem
import com.saynedesign.habitloop.data.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userDao: UserDao,
    private val xpEventDao: XpEventDao,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _startDestination = MutableStateFlow("onboarding")
    val startDestination = _startDestination.asStateFlow()

    val theme: StateFlow<String> = preferencesRepository.appTheme
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "system"
        )

    init {
        checkUserExistence()
    }

    private fun checkUserExistence() {
        viewModelScope.launch {
            // Check if we have any user
            val user = userDao.getUserOneShot()
            if (user != null) {
                try {
                    val eventSum = xpEventDao.getTotalXpOneShot()
                    if (eventSum != user.totalXp) {
                        val correctedLevel = LevelSystem.getLevelForXp(eventSum)
                        userDao.updateXp(eventSum, correctedLevel)
                    }
                } catch (_: Exception) {}
                _startDestination.value = "home"
            } else {
                _startDestination.value = "onboarding"
            }
            _isLoading.value = false
        }
    }
}
