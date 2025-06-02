package com.falcon.split.data.auth

import android.content.Context
import android.content.SharedPreferences
import com.falcon.split.AndroidUserManager.GoBackendUserManager
import com.falcon.split.AndroidUserManager.GoBackendUserProfileManager
import com.falcon.split.UserModelGoogleFirebaseBased
import com.falcon.split.data.Repository.ExpenseRepository
import com.falcon.split.data.Repository.GroupRepository
import com.falcon.split.data.network.ApiClient
import com.falcon.split.data.repository.GoBackendExpenseRepository
import com.falcon.split.data.repository.GoBackendGroupRepository
import com.falcon.split.data.repository.GoBackendHistoryRepository
import com.falcon.split.data.repository.GoBackendUserRepository
import com.falcon.split.data.repository.UserModelGoogleCloudBased
import com.falcon.split.HistoryRepository
import com.falcon.split.userManager.UserManager
import com.falcon.split.data.ProfileManager.UserProfileManager

class GoBackendManager(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "splitor_prefs", Context.MODE_PRIVATE
    )

    private val tokenManager = TokenManager(sharedPreferences)

    private val apiClient = ApiClient { tokenManager.getToken() }

    // Repository instances
    private val userRepository = GoBackendUserRepository(apiClient)
    val groupRepository: GroupRepository = GoBackendGroupRepository(apiClient)
    val expenseRepository: ExpenseRepository = GoBackendExpenseRepository(apiClient)
    val historyRepository: HistoryRepository = GoBackendHistoryRepository(apiClient)

    // Manager instances
    val userManager: UserManager = GoBackendUserManager(tokenManager)
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

    fun isLoggedIn(): Boolean {
        return tokenManager.isLoggedIn()
    }

    fun getCurrentUser(): UserModelGoogleFirebaseBased? {
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

    fun signOut() {
        tokenManager.clearToken()
        apiClient.close()
    }
}
