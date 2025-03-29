package com.falcon.split.presentation.sign_in

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.falcon.split.data.FirestoreManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PhoneNumberViewModel(
    private val googleAuthUiClient: GoogleAuthUiClient,
    private val firestoreManager: FirestoreManager = FirestoreManager()
) : ViewModel() {
    private val _showPhoneDialog = MutableStateFlow(false)
    val showPhoneDialog = _showPhoneDialog.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun showPhoneNumberDialog() {
        _showPhoneDialog.value = true
    }

    fun hidePhoneNumberDialog() {
        _showPhoneDialog.value = false
    }

    fun submitPhoneNumber(phoneNumber: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val user = googleAuthUiClient.getSignedInUser()
                Log.d("PhoneNumberViewModel", "Current user: $user")

                if (user == null) {
                    Log.e("PhoneNumberViewModel", "No signed in user found")
                    _error.value = "No signed in user found"
                    onComplete(false)
                    return@launch
                }

                firestoreManager.createOrUpdateUser(user, phoneNumber)
                    .onSuccess {
                        Log.d("PhoneNumberViewModel", "Successfully saved phone number")
                        _error.value = null
                        onComplete(true)
                    }
                    .onFailure { exception ->
                        Log.e("PhoneNumberViewModel", "Failed to save phone number", exception)
                        _error.value = "Failed to save phone number: ${exception.message}"
                        onComplete(false)
                    }
            } catch (e: Exception) {
                Log.e("PhoneNumberViewModel", "Error in submitPhoneNumber", e)
                _error.value = "Error: ${e.message}"
                onComplete(false)
            } finally {
                _isLoading.value = false
            }
        }
    }
}