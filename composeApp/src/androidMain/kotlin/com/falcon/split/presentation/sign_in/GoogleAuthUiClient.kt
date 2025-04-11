package com.falcon.split.presentation.sign_in

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import com.falcon.split.SignInResult
import com.falcon.split.UserModelGoogleFirebaseBased
import com.falcon.split.data.FirestoreManager
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.BeginSignInRequest.GoogleIdTokenRequestOptions
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

class GoogleAuthUiClient(
    private val context: Context,
    private val oneTapClient: SignInClient,
    private val firestoreManager: FirestoreManager = FirestoreManager()
) {
    private val auth = Firebase.auth

    suspend fun signIn(): IntentSender? {
        val result = try {
            oneTapClient.beginSignIn(
                buildSignInRequest()
            ).await()
        } catch(e: Exception) {
            e.printStackTrace()
            if(e is CancellationException) throw e
            null
        }
        return result?.pendingIntent?.intentSender
    }

    suspend fun signInWithIntent(intent: Intent): SignInResult {
        val credential = oneTapClient.getSignInCredentialFromIntent(intent)
        val googleIdToken = credential.googleIdToken
        val googleCredentials = GoogleAuthProvider.getCredential(googleIdToken, null)

        return try {
            val firebaseUser = auth.signInWithCredential(googleCredentials).await().user
            SignInResult(
                data = firebaseUser?.run {
                    UserModelGoogleFirebaseBased(
                        userId = uid,
                        username = displayName,
                        profilePictureUrl = photoUrl?.toString(),
                        email = email
                    )
                },
                errorMessage = null
            )
        } catch(e: Exception) {
            e.printStackTrace()
            if(e is CancellationException) throw e
            SignInResult(
                data = null,
                errorMessage = e.message
            )
        }
    }

    suspend fun signOut() {
        try {
            oneTapClient.signOut().await()
            auth.signOut()
        } catch(e: Exception) {
            e.printStackTrace()
            if(e is CancellationException) throw e
        }
    }

    fun getSignedInUser(): UserModelGoogleFirebaseBased? = auth.currentUser?.run {
        UserModelGoogleFirebaseBased(
            userId = uid,  // Use Firebase UID
            username = displayName,
            profilePictureUrl = photoUrl?.toString(),
            email = email
        )
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
    suspend fun updateUserWithPhoneNumber(phoneNumber: String): Result<Unit> {
        val currentUser = getSignedInUser() ?: return Result.failure(
            IllegalStateException("No signed in user found")
        )
        return firestoreManager.createOrUpdateUser(currentUser, phoneNumber)
    }
}