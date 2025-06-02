package com.falcon.split.presentation.sign_in

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import com.falcon.split.SignInResult
import com.falcon.split.UserModelGoogleFirebaseBased
import com.falcon.split.data.auth.GoBackendManager
import kotlinx.coroutines.CancellationException

class GoBackendAuthUiClient(
    private val context: Context,
    private val goBackendManager: GoBackendManager
) : AuthUiClient {

    override suspend fun signIn(): IntentSender? {
        // Return null for now - we'll handle Google sign-in differently
        // The actual sign-in will be triggered by the GoogleSignInButton
        return null
    }

    override suspend fun signInWithIntent(intent: Intent): SignInResult {
        // This method won't be used with normal Google Cloud sign-in
        return SignInResult(
            data = null,
            errorMessage = "Use authenticateWithGoogleToken instead"
        )
    }

    suspend fun authenticateWithGoogleToken(googleIdToken: String): SignInResult {
        return try {
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
            goBackendManager.signOut()
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
        }
    }

    override fun getSignedInUser(): UserModelGoogleFirebaseBased? {
        return goBackendManager.getCurrentUser()
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
