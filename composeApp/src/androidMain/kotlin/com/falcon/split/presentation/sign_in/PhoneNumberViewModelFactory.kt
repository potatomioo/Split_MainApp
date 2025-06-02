package com.falcon.split.presentation.sign_in


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class PhoneNumberViewModelFactory(
    private val authUiClient: AuthUiClient
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PhoneNumberViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PhoneNumberViewModel(authUiClient) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
