package com.falcon.split.presentation.sign_in

import android.content.Intent
import android.content.IntentSender
import com.falcon.split.SignInResult
import com.falcon.split.UserModelGoogleFirebaseBased

interface AuthUiClient {
    suspend fun signIn(): IntentSender?
    suspend fun signInWithIntent(intent: Intent): SignInResult
    suspend fun signOut()
    fun getSignedInUser(): UserModelGoogleFirebaseBased?
    suspend fun updateUserWithPhoneNumber(phoneNumber: String): Result<Unit>
}