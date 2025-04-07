package com.falcon.split.data.network.models

import kotlinx.serialization.Serializable

@Serializable
data class UserModelGoogleCloudBased(
    val userId: String,
    val userName: String,
    val name: String,
    val email: String,
    val profileImageUrl: String,
    val token: String,
    val upiId: String? = null
)