package com.falcon.split.data.repository

import com.falcon.split.data.network.KtorApiClient
import kotlinx.serialization.Serializable
import com.falcon.split.data.network.models.UserModelGoogleCloudBased

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

class GoBackendUserRepository(private val ktorApiClient: KtorApiClient) {

    suspend fun authenticateWithGoogle(googleToken: String): Result<UserModelGoogleCloudBased> {
        return try {
            val request = GoogleAuthRequest(googleToken)
            val response: UserModelGoogleCloudBased = ktorApiClient.post("api/auth/google", request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCurrentUser(): Result<UserResponse> {
        return try {
            val response: UserResponse = ktorApiClient.get("api/user")
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePhoneNumber(phoneNumber: String): Result<UserResponse> {
        return try {
            val request = UpdatePhoneRequest(phoneNumber)
            val response: UserResponse = ktorApiClient.patch("api/user/phone", request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserPhoneNumber(userId: String): Result<String> {
        return try {
            val response: Map<String, String> = ktorApiClient.get("api/user/$userId/phone")
            Result.success(response["phoneNumber"] ?: "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
