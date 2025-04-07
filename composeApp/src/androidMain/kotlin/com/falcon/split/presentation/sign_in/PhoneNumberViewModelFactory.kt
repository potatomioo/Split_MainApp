package com.falcon.split.presentation.sign_in


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.falcon.split.presentation.sign_in.GoogleAuthUiClient

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