package com.falcon.split.presentation.sign_in

import com.falcon.split.data.network.models.UserModelGoogleCloudBased

sealed class UserState {
    object Loading : UserState()
    data class Success(val user: UserModelGoogleCloudBased) : UserState()
    data class Error(val error: String) : UserState()
}