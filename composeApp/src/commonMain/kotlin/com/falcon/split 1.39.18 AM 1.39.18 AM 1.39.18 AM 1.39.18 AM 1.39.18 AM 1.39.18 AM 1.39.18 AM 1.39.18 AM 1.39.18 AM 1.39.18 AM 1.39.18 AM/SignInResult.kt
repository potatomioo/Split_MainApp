package com.falcon.split

import kotlinx.serialization.Serializable

@Serializable
data class SignInResult(
    val data: UserModelGoogleFirebaseBased?,
    val errorMessage: String?
)

@Serializable
data class UserModelGoogleFirebaseBased(
    val userId: String? = null,
    val username: String? = null,
    val profilePictureUrl: String? = null,

    val email: String? = null,
    val upiId: String? = null,
    val phoneNumber: String? = null
)