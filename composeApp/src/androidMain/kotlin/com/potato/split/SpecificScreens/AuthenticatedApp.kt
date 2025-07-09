package com.potato.split.SpecificScreens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.potato.split.App
import com.potato.split.Auth.AuthManager
import com.potato.split.Auth.AuthManager.AuthState
import com.potato.split.HistoryRepository
import com.potato.split.contact.AndroidContactManager
import com.potato.split.data.ProfileManager.UserProfileManager
import com.potato.split.data.Repository.ExpenseRepository
import com.potato.split.data.Repository.GroupRepository
import com.potato.split.data.network.ApiClient
import com.potato.split.presentation.screens.WelcomePage
import com.potato.split.userManager.UserManager

@Composable
fun AuthenticatedApp(
    authManager: AuthManager,
    client: ApiClient,
    prefs: DataStore<Preferences>,
    contactManager: AndroidContactManager,
    onSignOut: () -> Unit,
    groupRepository: GroupRepository? = null,
    expenseRepository: ExpenseRepository? = null,
    historyRepository: HistoryRepository? = null,
    darkTheme: MutableState<Boolean>?,
    userManager: UserManager,
    userProfileManager : UserProfileManager
) {
    val authState by authManager.authState.collectAsState()

    LaunchedEffect(Unit) {
        authManager.initializeAuthState()
    }

    when (authState) {
        AuthState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        AuthState.ShowWelcome -> {
            WelcomePage(
                onGetStarted = { authManager.startSignInFlow() }
            )
        }

        AuthState.SignInInProgress -> {
            GoogleSignInScreen(
                authManager = authManager,
                onBackToWelcome = { authManager.goBackToWelcome() }
            )
        }

        AuthState.GoogleSignedIn -> {
            PhoneNumberScreen(
                authManager = authManager
            )
        }

        AuthState.FullyAuthenticated -> {
            App(
                client = client,
                prefs = prefs,
                contactManager = contactManager,
                onSignOut = onSignOut,
                groupRepository = groupRepository,
                expenseRepository = expenseRepository,
                historyRepository = historyRepository,
                darkTheme = darkTheme,
                userManager = userManager,
                userProfileManager = userProfileManager,
            )
        }
    }
}