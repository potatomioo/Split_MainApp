package com.falcon.split.data.network.models


import com.falcon.split.utils.NetworkError

// UI State for News
sealed class UserState {
    object Loading : UserState()
    data class Success(val user: UserModelGoogleCloudBased) : UserState()
    data class Error(val error: NetworkError) : UserState()
}