package com.falcon.split.presentation.sign_in

import com.falcon.split.UserModelGoogleFirebaseBased

sealed class UserState {
    object Loading : UserState()
    data class Success(val user: UserModelGoogleFirebaseBased) : UserState()
    data class Error(val error: String) : UserState()
}