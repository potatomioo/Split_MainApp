package com.falcon.split.data.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.falcon.split.HistoryRepository
import com.falcon.split.UserModelGoogleFirebaseBased
import com.falcon.split.clearToken
import com.falcon.split.data.ProfileManager.UserProfileManager
import com.falcon.split.data.network.KtorApiClient
import com.falcon.split.data.network.models.UserState
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
import com.falcon.split.utils.NetworkError
import com.falcon.split.utils.Result
import com.falcon.split.utils.map
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class GoBackendManager(private val dataStore: DataStore<Preferences>) {

    private val _userDetails = MutableStateFlow<UserState>(UserState.Loading)
    val userDetails: StateFlow<UserState> = _userDetails

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
    suspend fun authenticateWithGoogle(googleToken: String): Result<UserModelGoogleFirebaseBased, NetworkError> {
        return try {
            _userDetails.value = UserState.Loading
            val result = userRepository.authenticateWithGoogle(googleToken)
            delay(2700) // TODO: Remove this delay later
            
            // Convert Kotlin Result to custom Result
            val customResult = if (result.isSuccess) {
                val user = result.getOrNull()!!
                Result.Success(user)
            } else {
                Result.Error(NetworkError.UNKNOWN)
            }

            _userDetails.value = when (customResult) {
                is Result.Success -> {
                    println("DEBUG_TAG" + "User ID: " + customResult.data.uid)
                    // Convert to network model for UserState  
                    val networkUser =
                        com.falcon.split.data.network.models.UserModelGoogleCloudBased(
                            userId = customResult.data.uid,
                            userName = customResult.data.displayName,
                            name = customResult.data.displayName,
                            email = customResult.data.email,
                            profileImageUrl = customResult.data.photoUrl,
                            token = customResult.data.token,
                            upiId = null
                        )
                    UserState.Success(networkUser)
                }
                is Result.Error -> {
                    println("DEBUG_TAG" + "error ID: " + customResult.error.name)
                    UserState.Error(customResult.error)
                }
            }

            // Use map extension function to transform the result
            customResult.map { user ->
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
                UserModelGoogleFirebaseBased(
                    userId = user.uid,
                    username = user.displayName,
                    email = user.email,
                    phoneNumber = user.phoneNumber,
                    profilePictureUrl = user.photoUrl
                )
            }
        } catch (e: Exception) {
            Result.Error(NetworkError.UNKNOWN)
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
