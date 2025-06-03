package com.falcon.split.data.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.falcon.split.HistoryRepository
import com.falcon.split.UserModelGoogleFirebaseBased
import com.falcon.split.clearToken
import com.falcon.split.data.ProfileManager.UserProfileManager
import com.falcon.split.data.network.KtorApiClient
import com.falcon.split.data.repository.ExpenseRepository
import com.falcon.split.data.repository.GoBackendExpenseRepository
import com.falcon.split.data.repository.GoBackendGroupRepository
import com.falcon.split.data.repository.GoBackendHistoryRepository
import com.falcon.split.data.repository.GoBackendUserRepository
import com.falcon.split.data.repository.GroupRepository
import com.falcon.split.getToken
import com.falcon.split.getUserEmail
import com.falcon.split.getUserId
import com.falcon.split.getUserName
import com.falcon.split.getUserPhone
import com.falcon.split.isLoggedIn
import com.falcon.split.saveToken
import com.falcon.split.saveUserInfo

class GoBackendManager(private val dataStore: DataStore<Preferences>) {

    private val ktorApiClient = KtorApiClient { getToken(dataStore) }

    // Repository instances
    private val userRepository = GoBackendUserRepository(ktorApiClient)
    val groupRepository: GroupRepository = GoBackendGroupRepository(ktorApiClient)
    val expenseRepository: ExpenseRepository = GoBackendExpenseRepository(ktorApiClient)
    val historyRepository: HistoryRepository = GoBackendHistoryRepository(ktorApiClient)

    // Manager instances
    val userProfileManager: UserProfileManager =
        GoBackendUserProfileManager(userRepository, dataStore)

    // Authentication methods
    suspend fun authenticateWithGoogle(googleToken: String): Result<UserModelGoogleFirebaseBased> {
        return try {
            val result = userRepository.authenticateWithGoogle(googleToken)
            if (result.isSuccess) {
                val user = result.getOrNull()!!
                // Save authentication data
                saveToken(user.token, dataStore)
                saveUserInfo(
                    userId = user.uid,
                    email = user.email,
                    name = user.displayName,
                    phone = user.phoneNumber,
                    dataStore = dataStore
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

    suspend fun isLogedIn(): Boolean {
        return isLoggedIn(dataStore)
    }

    suspend fun getCurrentUser(): UserModelGoogleFirebaseBased? {
        return if (isLoggedIn(dataStore)) {
            UserModelGoogleFirebaseBased(
                userId = getUserId(dataStore),
                username = getUserName(dataStore),
                email = getUserEmail(dataStore),
                phoneNumber = getUserPhone(dataStore),
                profilePictureUrl = null // Not stored currently
            )
        } else {
            null
        }
    }

    suspend fun signOut() {
        clearToken(dataStore)
        ktorApiClient.close()
    }
}
