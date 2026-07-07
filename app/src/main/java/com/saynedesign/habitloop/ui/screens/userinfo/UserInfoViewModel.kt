package com.saynedesign.habitloop.ui.screens.userinfo

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saynedesign.habitloop.data.UserDao
import com.saynedesign.habitloop.data.UserEntity
import com.saynedesign.habitloop.data.PrimaryGoal
import com.saynedesign.habitloop.data.ProductivityTime
import com.saynedesign.habitloop.data.MotivationStyle
import com.saynedesign.habitloop.data.ExperienceLevel
import com.saynedesign.habitloop.data.WeekStartsOn
import com.saynedesign.habitloop.data.UnitSystem
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class UserInfoViewModel @Inject constructor(
    private val userDao: UserDao,
    @ApplicationContext private val context: Context
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
            is UserInfoContract.Event.OnWeightChange -> {
                _state.value = _state.value.copy(weight = event.weight)
            }
            is UserInfoContract.Event.OnImageSelected -> {
                _state.value = _state.value.copy(profileImageUri = event.uri)
            }
            is UserInfoContract.Event.OnToggleHeightUnit -> {
                _state.value = _state.value.copy(isMetric = !_state.value.isMetric)
            }
            is UserInfoContract.Event.OnToggleDatePicker -> {
                _state.value = _state.value.copy(isDatePickerVisible = !_state.value.isDatePickerVisible)
            }
            is UserInfoContract.Event.OnDateSelected -> {
                val dateMillis = event.dateMillis
                if (dateMillis != null) {
                    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    formatter.timeZone = TimeZone.getTimeZone("UTC")
                    val dateString = formatter.format(Date(dateMillis))
                    _state.value = _state.value.copy(dob = dateString, isDatePickerVisible = false)
                } else {
                    _state.value = _state.value.copy(isDatePickerVisible = false)
                }
            }
            is UserInfoContract.Event.OnPrimaryGoalChange -> {
                _state.value = _state.value.copy(primaryGoal = event.goal)
            }
            is UserInfoContract.Event.OnProductivityTimeChange -> {
                _state.value = _state.value.copy(preferredProductivityTime = event.time)
            }
            is UserInfoContract.Event.OnWeeklyGoalChange -> {
                _state.value = _state.value.copy(weeklyGoal = event.goal)
            }
            is UserInfoContract.Event.OnMotivationStyleChange -> {
                _state.value = _state.value.copy(motivationStyle = event.style)
            }
            is UserInfoContract.Event.OnExperienceLevelChange -> {
                _state.value = _state.value.copy(experienceLevel = event.level)
            }
            is UserInfoContract.Event.OnWeekStartsOnChange -> {
                _state.value = _state.value.copy(weekStartsOn = event.startsOn)
            }
            is UserInfoContract.Event.OnReminderWindowChange -> {
                _state.value = _state.value.copy(defaultReminderWindow = event.window)
            }
            is UserInfoContract.Event.OnTimezoneChange -> {
                _state.value = _state.value.copy(timezone = event.timezone)
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
                
                // Ensure height is saved in CM regardless of display unit
                val heightVal = currentState.height.toFloatOrNull() ?: 0f
                val heightInCm = if (currentState.isMetric) {
                    heightVal
                } else {
                    heightVal * 30.48f
                }

                // Convert weight to KG if Imperial (1 lb = 0.453592 kg)
                val weightVal = currentState.weight.toFloatOrNull()
                val weightInKg = if (weightVal != null) {
                    if (currentState.isMetric) {
                        weightVal
                    } else {
                        weightVal * 0.453592f
                    }
                } else {
                    null
                }

                // Copy image to internal storage if it exists
                val savedImageUri = if (currentState.profileImageUri != null) {
                    copyImageToInternalStorage(Uri.parse(currentState.profileImageUri))
                } else {
                    null
                }

                val user = UserEntity(
                    name = currentState.name,
                    dob = currentState.dob,
                    height = heightInCm,
                    profileImageUri = savedImageUri,
                    photoUri = savedImageUri,
                    dateOfBirth = currentState.dob.ifBlank { null },
                    weight = weightInKg,
                    unitSystem = if (currentState.isMetric) UnitSystem.METRIC else UnitSystem.IMPERIAL,
                    primaryGoal = currentState.primaryGoal,
                    weeklyGoal = currentState.weeklyGoal,
                    preferredProductivityTime = currentState.preferredProductivityTime,
                    motivationStyle = currentState.motivationStyle,
                    experienceLevel = currentState.experienceLevel,
                    weekStartsOn = currentState.weekStartsOn,
                    defaultReminderWindow = currentState.defaultReminderWindow,
                    timezone = currentState.timezone
                )
                userDao.insertUser(user)
                _effect.send(UserInfoContract.Effect.NavigateToHome)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, isError = true)
            }
        }
    }

    private suspend fun copyImageToInternalStorage(uri: Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val fileName = "profile_${System.currentTimeMillis()}.jpg"
                val file = File(context.filesDir, fileName)
                val outputStream = FileOutputStream(file)
                
                inputStream?.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }
                file.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}
