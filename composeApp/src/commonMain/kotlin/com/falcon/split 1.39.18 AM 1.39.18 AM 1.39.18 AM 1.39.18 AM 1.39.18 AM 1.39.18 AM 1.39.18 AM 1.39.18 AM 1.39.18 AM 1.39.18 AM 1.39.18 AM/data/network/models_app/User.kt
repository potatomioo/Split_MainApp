package com.falcon.split.data.network.models_app

data class User(
    val userId: String = "",  // This will be their auth ID
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = ""
)