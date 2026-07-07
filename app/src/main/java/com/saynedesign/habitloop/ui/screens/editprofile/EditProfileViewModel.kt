package com.saynedesign.habitloop.ui.screens.editprofile

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
import kotlin.math.roundToInt

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val userDao: UserDao,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(EditProfileContract.State())
    val state: StateFlow<EditProfileContract.State> = _state.asStateFlow()

    private val _effect = Channel<EditProfileContract.Effect>()
    val effect = _effect.receiveAsFlow()

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val user = userDao.getUserOneShot()
            if (user != null) {
                // If weight was saved, show it in metric or imperial based on unitSystem
                val isMetric = user.unitSystem == UnitSystem.METRIC
                val heightDisplay = if (isMetric) {
                    user.height.roundToInt().toString()
                } else {
                    String.format(Locale.US, "%.1f", user.height * 0.0328084f)
                }
                
                val weightDisplay = if (user.weight != null) {
                    if (isMetric) {
                        user.weight.roundToInt().toString()
                    } else {
                        (user.weight * 2.20462f).roundToInt().toString()
                    }
                } else {
                    ""
                }

                _state.value = _state.value.copy(
                    userId = user.id,
                    name = user.name,
                    dob = user.dob,
                    height = heightDisplay,
                    weight = weightDisplay,
                    profileImageUri = user.profileImageUri ?: user.photoUri,
                    isMetric = isMetric,
                    primaryGoal = user.primaryGoal,
                    weeklyGoal = user.weeklyGoal,
                    preferredProductivityTime = user.preferredProductivityTime,
                    motivationStyle = user.motivationStyle,
                    experienceLevel = user.experienceLevel,
                    weekStartsOn = user.weekStartsOn,
                    defaultReminderWindow = user.defaultReminderWindow,
                    timezone = user.timezone,
                    isLoading = false
                )
            } else {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    fun handleEvent(event: EditProfileContract.Event) {
        when (event) {
            is EditProfileContract.Event.OnNameChange -> {
                _state.value = _state.value.copy(name = event.name)
            }
            is EditProfileContract.Event.OnDobChange -> {
                _state.value = _state.value.copy(dob = event.dob)
            }
            is EditProfileContract.Event.OnHeightChange -> {
                _state.value = _state.value.copy(height = event.height)
            }
            is EditProfileContract.Event.OnWeightChange -> {
                _state.value = _state.value.copy(weight = event.weight)
            }
            is EditProfileContract.Event.OnImageSelected -> {
                _state.value = _state.value.copy(profileImageUri = event.uri)
            }
            is EditProfileContract.Event.OnToggleHeightUnit -> {
                toggleHeightAndWeightUnit()
            }
            is EditProfileContract.Event.OnToggleDatePicker -> {
                _state.value = _state.value.copy(isDatePickerVisible = !_state.value.isDatePickerVisible)
            }
            is EditProfileContract.Event.OnDateSelected -> {
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
            is EditProfileContract.Event.OnPrimaryGoalChange -> {
                _state.value = _state.value.copy(primaryGoal = event.goal)
            }
            is EditProfileContract.Event.OnWeeklyGoalChange -> {
                _state.value = _state.value.copy(weeklyGoal = event.goal)
            }
            is EditProfileContract.Event.OnProductivityTimeChange -> {
                _state.value = _state.value.copy(preferredProductivityTime = event.time)
            }
            is EditProfileContract.Event.OnMotivationStyleChange -> {
                _state.value = _state.value.copy(motivationStyle = event.style)
            }
            is EditProfileContract.Event.OnExperienceLevelChange -> {
                _state.value = _state.value.copy(experienceLevel = event.level)
            }
            is EditProfileContract.Event.OnWeekStartsOnChange -> {
                _state.value = _state.value.copy(weekStartsOn = event.startsOn)
            }
            is EditProfileContract.Event.OnReminderWindowChange -> {
                _state.value = _state.value.copy(defaultReminderWindow = event.window)
            }
            is EditProfileContract.Event.OnTimezoneChange -> {
                _state.value = _state.value.copy(timezone = event.timezone)
            }
            is EditProfileContract.Event.OnToggleSection -> {
                _state.value = when (event.section) {
                    EditProfileContract.Section.PERSONAL -> _state.value.copy(personalExpanded = !_state.value.personalExpanded)
                    EditProfileContract.Section.HEALTH -> _state.value.copy(healthExpanded = !_state.value.healthExpanded)
                    EditProfileContract.Section.PREFERENCES -> _state.value.copy(preferencesExpanded = !_state.value.preferencesExpanded)
                    EditProfileContract.Section.MOTIVATION -> _state.value.copy(motivationExpanded = !_state.value.motivationExpanded)
                }
            }
            is EditProfileContract.Event.OnSave -> {
                saveUser()
            }
            is EditProfileContract.Event.OnBackClicked -> {
                viewModelScope.launch { _effect.send(EditProfileContract.Effect.NavigateBack) }
            }
        }
    }

    private fun toggleHeightAndWeightUnit() {
        val currentState = _state.value
        val currentHeight = currentState.height.toFloatOrNull()
        val currentWeight = currentState.weight.toFloatOrNull()
        
        val newIsMetric = !currentState.isMetric
        
        val newHeight = if (currentHeight != null) {
            if (newIsMetric) {
                // Convert FT to CM
                (currentHeight * 30.48f).roundToInt().toString()
            } else {
                // Convert CM to FT
                String.format(Locale.US, "%.1f", currentHeight * 0.0328084f)
            }
        } else {
            ""
        }

        val newWeight = if (currentWeight != null) {
            if (newIsMetric) {
                // Convert LBS to KG
                (currentWeight * 0.453592f).roundToInt().toString()
            } else {
                // Convert KG to LBS
                (currentWeight * 2.20462f).roundToInt().toString()
            }
        } else {
            ""
        }

        _state.value = currentState.copy(
            isMetric = newIsMetric,
            height = newHeight,
            weight = newWeight
        )
    }

    private fun saveUser() {
        val currentState = _state.value
        if (currentState.name.isBlank()) {
            viewModelScope.launch { _effect.send(EditProfileContract.Effect.ShowToast("Name cannot be blank")) }
            return
        }
        val heightVal = currentState.height.toFloatOrNull() ?: 0f
        if (heightVal <= 0) {
            viewModelScope.launch { _effect.send(EditProfileContract.Effect.ShowToast("Height must be positive")) }
            return
        }
        val weightVal = currentState.weight.toFloatOrNull()
        if (weightVal != null && weightVal <= 0) {
            viewModelScope.launch { _effect.send(EditProfileContract.Effect.ShowToast("Weight must be positive")) }
            return
        }
        if (!validateReminderWindow(currentState.defaultReminderWindow)) {
            viewModelScope.launch { _effect.send(EditProfileContract.Effect.ShowToast("Invalid Reminder Window format (e.g. 08:00-10:00)")) }
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val existingUser = userDao.getUserOneShot()
                
                // Height CM conversion
                val heightInCm = if (currentState.isMetric) {
                    heightVal
                } else {
                    heightVal * 30.48f
                }

                // Weight KG conversion
                val weightInKg = if (weightVal != null) {
                    if (currentState.isMetric) {
                        weightVal
                    } else {
                        weightVal * 0.453592f
                    }
                } else {
                    null
                }

                // Copy image to internal storage if changed
                val savedImageUri = if (currentState.profileImageUri != null && 
                                       currentState.profileImageUri != existingUser?.profileImageUri &&
                                       currentState.profileImageUri != existingUser?.photoUri) {
                    copyImageToInternalStorage(Uri.parse(currentState.profileImageUri))
                } else {
                    currentState.profileImageUri
                }

                val user = UserEntity(
                    id = currentState.userId,
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
                    timezone = currentState.timezone,
                    createdTimestamp = existingUser?.createdTimestamp ?: System.currentTimeMillis(),
                    journal = existingUser?.journal,
                    journalLastUpdated = existingUser?.journalLastUpdated,
                    totalXp = existingUser?.totalXp ?: 0,
                    currentLevel = existingUser?.currentLevel ?: 1
                )
                
                userDao.insertUser(user)
                _effect.send(EditProfileContract.Effect.ShowToast("Profile updated successfully"))
                _effect.send(EditProfileContract.Effect.NavigateBack)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, isError = true)
                _effect.send(EditProfileContract.Effect.ShowToast("Failed to update profile"))
            }
        }
    }

    private fun validateReminderWindow(window: String): Boolean {
        val regex = Regex("^\\d{2}:\\d{2}-\\d{2}:\\d{2}$")
        if (!regex.matches(window)) return false
        val parts = window.split("-")
        val start = parts[0].split(":")
        val end = parts[1].split(":")
        val startHour = start[0].toIntOrNull() ?: -1
        val startMin = start[1].toIntOrNull() ?: -1
        val endHour = end[0].toIntOrNull() ?: -1
        val endMin = end[1].toIntOrNull() ?: -1
        
        if (startHour !in 0..23 || startMin !in 0..59) return false
        if (endHour !in 0..23 || endMin !in 0..59) return false
        
        val startTotal = startHour * 60 + startMin
        val endTotal = endHour * 60 + endMin
        return startTotal < endTotal
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
