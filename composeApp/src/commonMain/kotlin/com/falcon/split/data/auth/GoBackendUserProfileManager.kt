package com.falcon.split.data.auth

import com.falcon.split.data.ProfileManager.UserProfileManager
import com.falcon.split.data.repository.GoBackendUserRepository

class GoBackendUserProfileManager(
    private val tokenManager: TokenManager,
    private val userRepository: GoBackendUserRepository
) : UserProfileManager {

    override suspend fun updateUserUpiId(upiId: String): Result<Unit> {
        // For now, we'll return success since UPI functionality 
        // would need to be implemented in the Go backend
        // This can be extended when UPI support is added to the backend
        return Result.success(Unit)
    }

    suspend fun updatePhoneNumber(phoneNumber: String): Result<Unit> {
        return try {
            val result = userRepository.updatePhoneNumber(phoneNumber)
            if (result.isSuccess) {
                // Update local storage with new phone number
                val userId = tokenManager.getUserId() ?: ""
                val email = tokenManager.getUserEmail() ?: ""
                val name = tokenManager.getUserName() ?: ""
                tokenManager.saveUserInfo(userId, email, name, phoneNumber)
            }
            result.map { }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}