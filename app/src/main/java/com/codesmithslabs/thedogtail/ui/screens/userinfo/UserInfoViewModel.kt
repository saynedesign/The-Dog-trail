package com.codesmithslabs.thedogtail.ui.screens.userinfo

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codesmithslabs.thedogtail.data.UserDao
import com.codesmithslabs.thedogtail.data.UserEntity
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
            is UserInfoContract.Event.OnImageSelected -> {
                _state.value = _state.value.copy(profileImageUri = event.uri)
            }
            is UserInfoContract.Event.OnToggleHeightUnit -> {
                toggleHeightUnit()
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
            is UserInfoContract.Event.OnSubmit -> {
                saveUser()
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
                
                // Ensure height is saved in CM regardless of display unit
                val heightInCm = if (currentState.isMetric) {
                    currentState.height.toFloatOrNull() ?: 0f
                } else {
                    (currentState.height.toFloatOrNull() ?: 0f) * 30.48f
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
                    profileImageUri = savedImageUri
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
