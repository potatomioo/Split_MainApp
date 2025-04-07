package com.falcon.split.presentation.sign_in

import kotlinx.serialization.Serializable

@Serializable
data class UserModel(
    val userId: String,
    val userName: String,
    val name: String,
    val email: String,
    val profileImageUrl: String,
    val token: String,
    val upiId: String? = null
)