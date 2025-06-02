package com.falcon.split.AndroidUserManager

import com.falcon.split.userManager.UserManager
import com.falcon.split.data.auth.TokenManager

class GoBackendUserManager(private val tokenManager: TokenManager) : UserManager {
    override fun getCurrentUserId(): String? {
        return tokenManager.getUserId()
    }
}