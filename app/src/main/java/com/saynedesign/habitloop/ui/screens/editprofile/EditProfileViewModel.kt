package com.saynedesign.habitloop.ui.screens.editprofile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saynedesign.habitloop.data.UserDao
import com.saynedesign.habitloop.data.UserEntity
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
                _state.value = _state.value.copy(
                    userId = user.id,
                    name = user.name,
                    dob = user.dob,
                    height = user.height.roundToInt().toString(), // Default to CM/Metric initially
                    profileImageUri = user.profileImageUri,
                    isMetric = true, // We store as CM, so start as Metric
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
            is EditProfileContract.Event.OnImageSelected -> {
                _state.value = _state.value.copy(profileImageUri = event.uri)
            }
            is EditProfileContract.Event.OnToggleHeightUnit -> {
                toggleHeightUnit()
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
            is EditProfileContract.Event.OnSave -> {
                saveUser()
            }
            is EditProfileContract.Event.OnBackClicked -> {
                viewModelScope.launch { _effect.send(EditProfileContract.Effect.NavigateBack) }
            }
        }
    }

    private fun toggleHeightUnit() {
        val currentState = _state.value
        val currentHeight = currentState.height.toFloatOrNull()
        
        if (currentHeight == null) {
            _state.value = currentState.copy(isMetric = !currentState.isMetric)
            return
        }

        val newIsMetric = !currentState.isMetric
        val newHeight = if (newIsMetric) {
            // Convert FT to CM (1 ft = 30.48 cm)
            (currentHeight * 30.48f).roundToInt().toString()
        } else {
            // Convert CM to FT (1 cm = 0.0328084 ft)
            // Keeping 1 decimal place for FT
            String.format(Locale.US, "%.1f", currentHeight * 0.0328084f)
        }

        _state.value = currentState.copy(
            isMetric = newIsMetric,
            height = newHeight
        )
    }

    private fun saveUser() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val currentState = _state.value
                val existingUser = userDao.getUserOneShot()
                
                // Ensure height is saved in CM regardless of display unit
                val heightInCm = if (currentState.isMetric) {
                    currentState.height.toFloatOrNull() ?: 0f
                } else {
                    (currentState.height.toFloatOrNull() ?: 0f) * 30.48f
                }

                // Copy image to internal storage if it exists and changed
                val savedImageUri = if (currentState.profileImageUri != null && 
                                       currentState.profileImageUri != existingUser?.profileImageUri) {
                    copyImageToInternalStorage(Uri.parse(currentState.profileImageUri))
                } else {
                    currentState.profileImageUri
                }

                val user = UserEntity(
                    id = currentState.userId, // Important: Use existing ID
                    name = currentState.name,
                    dob = currentState.dob,
                    height = heightInCm,
                    profileImageUri = savedImageUri,
                    createdTimestamp = existingUser?.createdTimestamp ?: System.currentTimeMillis(),
                    journal = existingUser?.journal,
                    journalLastUpdated = existingUser?.journalLastUpdated
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
