package com.falcon.split.data.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.falcon.split.data.ProfileManager.UserProfileManager
import com.falcon.split.data.repository.GoBackendUserRepository
import com.falcon.split.getUserEmail
import com.falcon.split.getUserId
import com.falcon.split.getUserName
import com.falcon.split.saveUserInfo

class GoBackendUserProfileManager(
    private val userRepository: GoBackendUserRepository,
    private val dataStore: DataStore<Preferences>
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
                val userId = getUserId(dataStore) ?: ""
                val email = getUserEmail(dataStore) ?: ""
                val name = getUserName(dataStore) ?: ""
                saveUserInfo(dataStore, userId, email, name, phoneNumber)
            }
            result.map { }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}