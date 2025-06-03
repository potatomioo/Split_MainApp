package com.falcon.split.data.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.falcon.split.HistoryRepository
import com.falcon.split.UserModelGoogleFirebaseBased
import com.falcon.split.data.ProfileManager.UserProfileManager
import com.falcon.split.data.network.KtorApiClient
import com.falcon.split.data.repository.ExpenseRepository
import com.falcon.split.data.repository.GoBackendExpenseRepository
import com.falcon.split.data.repository.GoBackendGroupRepository
import com.falcon.split.data.repository.GoBackendHistoryRepository
import com.falcon.split.data.repository.GoBackendUserRepository
import com.falcon.split.data.repository.GroupRepository

class GoBackendManager(dataStore: DataStore<Preferences>) {

    private val tokenManager = TokenManager(dataStore)

    private val ktorApiClient = KtorApiClient { tokenManager.getToken() }

    // Repository instances
    private val userRepository = GoBackendUserRepository(ktorApiClient)
    val groupRepository: GroupRepository = GoBackendGroupRepository(ktorApiClient)
    val expenseRepository: ExpenseRepository = GoBackendExpenseRepository(ktorApiClient)
    val historyRepository: HistoryRepository = GoBackendHistoryRepository(ktorApiClient)

    // Manager instances
    val userProfileManager: UserProfileManager =
        GoBackendUserProfileManager(tokenManager, userRepository)

    // Authentication methods
    suspend fun authenticateWithGoogle(googleToken: String): Result<UserModelGoogleFirebaseBased> {
        return try {
            val result = userRepository.authenticateWithGoogle(googleToken)
            if (result.isSuccess) {
                val user = result.getOrNull()!!
                // Save authentication data
                tokenManager.saveToken(user.token)
                tokenManager.saveUserInfo(
                    userId = user.uid,
                    email = user.email,
                    name = user.displayName,
                    phone = user.phoneNumber
                )

                // Return user model in expected format
                val userModel = UserModelGoogleFirebaseBased(
                    userId = user.uid,
                    username = user.displayName,
                    email = user.email,
                    phoneNumber = user.phoneNumber,
                    profilePictureUrl = user.photoUrl
                )
                Result.success(userModel)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Authentication failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun isLoggedIn(): Boolean {
        return tokenManager.isLoggedIn()
    }

    suspend fun getCurrentUser(): UserModelGoogleFirebaseBased? {
        return if (tokenManager.isLoggedIn()) {
            UserModelGoogleFirebaseBased(
                userId = tokenManager.getUserId(),
                username = tokenManager.getUserName(),
                email = tokenManager.getUserEmail(),
                phoneNumber = tokenManager.getUserPhone(),
                profilePictureUrl = null // Not stored currently
            )
        } else {
            null
        }
    }

    suspend fun signOut() {
        tokenManager.clearToken()
        ktorApiClient.close()
    }
}
