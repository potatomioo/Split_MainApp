package com.falcon.split.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserModelGoogleCloudBased(
    @SerialName("uid") val userId: String,
    @SerialName("displayName") val userName: String,
    @SerialName("email") val email: String,
    @SerialName("phoneNumber") val phoneNumber: String? = null,
    @SerialName("photoUrl") val profileImageUrl: String? = null,
    @SerialName("token") val token: String,
    val upiId: String? = null
)