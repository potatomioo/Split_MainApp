package com.potato.split.Auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.potato.split.SignInResult
import com.potato.split.data.FirestoreManager
import com.potato.split.deleteUser
import com.potato.split.getFirebaseUserAsUserModel
import com.potato.split.presentation.sign_in.GoogleAuthUiClient
import com.potato.split.saveFirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthManager(
    private val prefs: DataStore<Preferences>,
    private val googleAuthUiClient: GoogleAuthUiClient,
    private val firestoreManager: FirestoreManager
) {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState = _authState.asStateFlow()

    sealed class AuthState {
        object Loading : AuthState()
        object ShowWelcome : AuthState()  // Add this
        object SignInInProgress : AuthState()  // Add this
        object GoogleSignedIn : AuthState()
        object FullyAuthenticated : AuthState()
    }

    suspend fun initializeAuthState() {
        _authState.value = AuthState.Loading

        val firebaseUser = googleAuthUiClient.getSignedInUser()
        val savedUser = getFirebaseUserAsUserModel(prefs)

        when {
            firebaseUser == null -> _authState.value = AuthState.ShowWelcome
            savedUser?.phoneNumber == null -> _authState.value = AuthState.GoogleSignedIn
            else -> _authState.value = AuthState.FullyAuthenticated
        }
    }

    fun startSignInFlow() {
        _authState.value = AuthState.SignInInProgress
    }

    suspend fun handleGoogleSignInResult(result: SignInResult): Boolean {
        return if (result.data != null) {
            _authState.value = AuthState.GoogleSignedIn
            true
        } else {
            _authState.value = AuthState.SignInInProgress // Back to sign-in on error
            false
        }
    }

    suspend fun completePhoneNumberSetup(phoneNumber: String): Boolean {
        return try {
            val user = googleAuthUiClient.getSignedInUser()
            if (user != null) {
                firestoreManager.createOrUpdateUser(user, phoneNumber)
                    .onSuccess {
                        val userWithPhone = user.copy(phoneNumber = phoneNumber)
                        saveFirebaseUser(prefs, userWithPhone)
                        _authState.value = AuthState.FullyAuthenticated
                    }
                    .isSuccess
            } else false
        } catch (e: Exception) {
            false
        }
    }

    suspend fun signOut() {
        googleAuthUiClient.signOut()
        deleteUser(prefs)
        _authState.value = AuthState.ShowWelcome
    }

    fun goBackToWelcome() {
        _authState.value = AuthState.ShowWelcome
    }
}