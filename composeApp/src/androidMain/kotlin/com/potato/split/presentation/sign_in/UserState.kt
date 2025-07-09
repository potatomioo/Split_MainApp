package com.potato.split.presentation.sign_in

import com.potato.split.UserModelGoogleFirebaseBased

sealed class UserState {
    object Loading : UserState()
    data class Success(val user: UserModelGoogleFirebaseBased) : UserState()
    data class Error(val error: String) : UserState()
}