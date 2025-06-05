package com.falcon.split.data.network.models

import kotlinx.serialization.Serializable

@Serializable
data class UserModelGoogleCloudBased(
    val userId: String,
    val userName: String,
    val email: String,
    val phoneNumber: String? = null,
    val profileImageUrl: String? = null,
    val token: String,
    val upiId: String? = null
)