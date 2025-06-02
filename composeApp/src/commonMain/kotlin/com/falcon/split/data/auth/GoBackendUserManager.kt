package com.falcon.split.data.auth

import com.falcon.split.userManager.UserManager

class GoBackendUserManager(private val tokenManager: TokenManager) : UserManager {
    override suspend fun getCurrentUserId(): String? {
        return tokenManager.getUserId()
    }
}