package com.falcon.split.data.config

import android.content.Context
import com.falcon.split.AndroidUserManager.AndroidUserProfileManager
import com.falcon.split.AndroidUserManager.FirebaseUserManager
import com.falcon.split.data.ProfileManager.UserProfileManager
import com.falcon.split.data.repository.ExpenseRepository
import com.falcon.split.data.repository.GroupRepository
import com.falcon.split.data.auth.GoBackendManager
import com.falcon.split.HistoryRepository
import com.falcon.split.userManager.UserManager


class BackendConfig(private val context: Context) {

    // Lazy initialization
    val goBackendManager by lazy { GoBackendManager(context) }

    val groupRepository: GroupRepository by lazy {
        goBackendManager.groupRepository
    }

    val expenseRepository: ExpenseRepository by lazy {
        goBackendManager.expenseRepository
    }

    val historyRepository: HistoryRepository by lazy {
        goBackendManager.historyRepository
    }

    val userManager: UserManager by lazy {
        goBackendManager.userManager
    }

    val userProfileManager: UserProfileManager by lazy {
        goBackendManager.userProfileManager
    }

    // Authentication methods
    suspend fun authenticateWithGoogle(googleToken: String) =
        goBackendManager.authenticateWithGoogle(googleToken)


    fun isLoggedIn(): Boolean {
        return goBackendManager.isLoggedIn()
    }

    fun signOut() {
        goBackendManager.signOut()
    }
}
