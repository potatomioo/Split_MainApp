package com.potato.split.presentation.sign_in


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class PhoneNumberViewModelFactory(
    private val googleAuthUiClient: GoogleAuthUiClient
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PhoneNumberViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PhoneNumberViewModel(googleAuthUiClient) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}