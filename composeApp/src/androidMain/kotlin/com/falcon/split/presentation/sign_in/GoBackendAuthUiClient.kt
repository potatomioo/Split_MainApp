package com.falcon.split.presentation.sign_in

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import com.falcon.split.SignInResult
import com.falcon.split.UserModelGoogleFirebaseBased
import com.falcon.split.data.auth.GoBackendManager
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.BeginSignInRequest.GoogleIdTokenRequestOptions
import com.google.android.gms.auth.api.identity.SignInClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

class GoBackendAuthUiClient(
    private val context: Context,
    private val oneTapClient: SignInClient,
    private val goBackendManager: GoBackendManager
) : AuthUiClient {

    override suspend fun signIn(): IntentSender? {
        val result = try {
            oneTapClient.beginSignIn(
                buildSignInRequest()
            ).await()
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
            null
        }
        return result?.pendingIntent?.intentSender
    }

    override suspend fun signInWithIntent(intent: Intent): SignInResult {
        val credential = oneTapClient.getSignInCredentialFromIntent(intent)
        val googleIdToken = credential.googleIdToken

        return try {
            if (googleIdToken != null) {
                val result = goBackendManager.authenticateWithGoogle(googleIdToken)
                if (result.isSuccess) {
                    SignInResult(
                        data = result.getOrNull(),
                        errorMessage = null
                    )
                } else {
                    SignInResult(
                        data = null,
                        errorMessage = result.exceptionOrNull()?.message ?: "Authentication failed"
                    )
                }
            } else {
                SignInResult(
                    data = null,
                    errorMessage = "Failed to get Google ID token"
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
            SignInResult(
                data = null,
                errorMessage = e.message
            )
        }
    }

    override suspend fun signOut() {
        try {
            oneTapClient.signOut().await()
            goBackendManager.signOut()
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
        }
    }

    override fun getSignedInUser(): UserModelGoogleFirebaseBased? {
        return goBackendManager.getCurrentUser()
    }

    private fun buildSignInRequest(): BeginSignInRequest {
        return BeginSignInRequest.Builder()
            .setGoogleIdTokenRequestOptions(
                GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId("256895007811-hhkr06uk0k3q4sr78bj77cmql0j95918.apps.googleusercontent.com")
                    .build()
            )
            .setAutoSelectEnabled(true)
            .build()
    }

    override suspend fun updateUserWithPhoneNumber(phoneNumber: String): Result<Unit> {
        return try {
            val profileManager =
                goBackendManager.userProfileManager as? com.falcon.split.AndroidUserManager.GoBackendUserProfileManager
            profileManager?.updatePhoneNumber(phoneNumber)
                ?: Result.failure(Exception("Profile manager not available"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
