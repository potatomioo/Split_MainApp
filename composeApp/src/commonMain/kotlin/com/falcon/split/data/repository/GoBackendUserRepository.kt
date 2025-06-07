package com.falcon.split.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.falcon.split.data.network.KtorApiClient
import com.falcon.split.data.network.models.UserModelGoogleCloudBased
import com.falcon.split.getToken
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GoogleAuthRequest(
    @SerialName("googleToken") val googleToken: String
)

@Serializable
data class UpdatePhoneRequest(
    @SerialName("phoneNumber") val phoneNumber: String
)

@Serializable
data class UserResponse(
    @SerialName("uid") val uid: String,
    @SerialName("email") val email: String,
    @SerialName("name") val name: String,
    @SerialName("phoneNumber") val phoneNumber: String,
    @SerialName("profilePictureUrl") val profilePictureUrl: String = ""
)

class GoBackendUserRepository(
    private val ktorApiClient: KtorApiClient,
    private val dataStore: DataStore<Preferences>
) {

    suspend fun authenticateWithGoogle(googleToken: String): Result<UserModelGoogleCloudBased> {
        return try {
            val request = GoogleAuthRequest(googleToken)
            val token = getToken(dataStore)
            val response: UserModelGoogleCloudBased =
                ktorApiClient.post("api/auth/google", token, request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCurrentUser(): Result<UserResponse> {
        return try {
            val token = getToken(dataStore)
            val response: UserResponse = ktorApiClient.get("api/user", token)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePhoneNumber(phoneNumber: String): Result<UserResponse> {
        return try {
            val request = UpdatePhoneRequest(phoneNumber)
            val token = getToken(dataStore)
            val response: UserResponse = ktorApiClient.patch("api/user/phone", token, request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserPhoneNumber(userId: String): Result<String> {
        return try {
            val token = getToken(dataStore)
            val response: Map<String, String> = ktorApiClient.get("api/user/$userId/phone", token)
            Result.success(response["phoneNumber"] ?: "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
