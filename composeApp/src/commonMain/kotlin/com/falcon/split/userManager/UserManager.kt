package com.falcon.split.userManager

interface UserManager {
    suspend fun getCurrentUserId(): String?
}