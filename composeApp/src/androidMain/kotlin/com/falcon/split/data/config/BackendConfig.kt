package com.falcon.split.data.config

import android.content.Context
import com.falcon.split.AndroidUserManager.AndroidUserProfileManager
import com.falcon.split.AndroidUserManager.FirebaseUserManager
import com.falcon.split.data.FirestoreManager
import com.falcon.split.data.ProfileManager.UserProfileManager
import com.falcon.split.data.Repository.ExpenseRepository
import com.falcon.split.data.Repository.GroupRepository
import com.falcon.split.data.auth.GoBackendManager
import com.falcon.split.data.repository.FirebaseExpenseRepository
import com.falcon.split.data.repository.FirebaseGroupRepository
import com.falcon.split.data.repository.FirebaseHistoryRepository
import com.falcon.split.HistoryRepository
import com.falcon.split.userManager.UserManager

/**
 * Configuration class that manages switching between Firebase and Go backend
 */
class BackendConfig(private val context: Context) {

    // Set this to true to use Go backend, false for Firebase
    // MIGRATION FLAG: Change this to true to use Go backend
    val useGoBackend = false // Start with Firebase, then switch to true for Go backend

    // Lazy initialization
    val goBackendManager by lazy { GoBackendManager(context) }
    private val firestoreManager by lazy { FirestoreManager() }

    val groupRepository: GroupRepository by lazy {
        if (useGoBackend) {
            goBackendManager.groupRepository
        } else {
            FirebaseGroupRepository()
        }
    }

    val expenseRepository: ExpenseRepository by lazy {
        if (useGoBackend) {
            goBackendManager.expenseRepository
        } else {
            FirebaseExpenseRepository()
        }
    }

    val historyRepository: HistoryRepository by lazy {
        if (useGoBackend) {
            goBackendManager.historyRepository
        } else {
            FirebaseHistoryRepository()
        }
    }

    val userManager: UserManager by lazy {
        if (useGoBackend) {
            goBackendManager.userManager
        } else {
            FirebaseUserManager()
        }
    }

    val userProfileManager: UserProfileManager by lazy {
        if (useGoBackend) {
            goBackendManager.userProfileManager
        } else {
            AndroidUserProfileManager(firestoreManager)
        }
    }

    // Authentication methods
    suspend fun authenticateWithGoogle(googleToken: String) =
        if (useGoBackend) {
            goBackendManager.authenticateWithGoogle(googleToken)
        } else {
            // Firebase authentication is handled differently
            // This would need to be implemented based on existing Firebase auth flow
            throw NotImplementedError("Firebase auth flow should use existing GoogleAuthUiClient")
        }

    fun isLoggedIn(): Boolean {
        return if (useGoBackend) {
            goBackendManager.isLoggedIn()
        } else {
            // Check Firebase auth state
            com.google.firebase.auth.FirebaseAuth.getInstance().currentUser != null
        }
    }

    fun signOut() {
        if (useGoBackend) {
            goBackendManager.signOut()
        } else {
            // Firebase sign out would be handled by GoogleAuthUiClient
        }
    }
}
