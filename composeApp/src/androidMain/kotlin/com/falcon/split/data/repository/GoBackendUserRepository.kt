package com.falcon.split.data.repository

import com.falcon.split.UserModelGoogleFirebaseBased
import com.falcon.split.data.network.ApiClient
import kotlinx.serialization.Serializable

@Serializable
data class GoogleAuthRequest(
    val googleToken: String
)

@Serializable
data class UpdatePhoneRequest(
    val phoneNumber: String
)

@Serializable
data class UserResponse(
    val uid: String,
    val email: String,
    val name: String,
    val phoneNumber: String,
    val profilePictureUrl: String = ""
)

@Serializable
data class UserModelGoogleCloudBased(
    val token: String,
    val uid: String,
    val email: String,
    val displayName: String,
    val phoneNumber: String,
    val photoUrl: String
)

class GoBackendUserRepository(private val apiClient: ApiClient) {

    suspend fun authenticateWithGoogle(googleToken: String): Result<UserModelGoogleCloudBased> {
        return try {
            val request = GoogleAuthRequest(googleToken)
            val response: UserModelGoogleCloudBased = apiClient.post("api/auth/google", request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCurrentUser(): Result<UserResponse> {
        return try {
            val response: UserResponse = apiClient.get("api/user")
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePhoneNumber(phoneNumber: String): Result<UserResponse> {
        return try {
            val request = UpdatePhoneRequest(phoneNumber)
            val response: UserResponse = apiClient.patch("api/user/phone", request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserPhoneNumber(userId: String): Result<String> {
        return try {
            val response: Map<String, String> = apiClient.get("api/user/$userId/phone")
            Result.success(response["phoneNumber"] ?: "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
