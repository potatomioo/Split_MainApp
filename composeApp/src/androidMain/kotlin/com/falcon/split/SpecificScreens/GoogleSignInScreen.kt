package com.falcon.split.SpecificScreens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.falcon.split.Auth.AuthManager
import com.falcon.split.LottieAnimationSpec
import com.falcon.split.LottieAnimationView
import com.falcon.split.presentation.sign_in.GoogleAuthUiClient
import com.falcon.split.presentation.theme.lDimens
import com.google.android.gms.auth.api.identity.Identity
import com.mmk.kmpauth.uihelper.google.GoogleSignInButton
import kotlinx.coroutines.launch


@Composable
fun GoogleSignInScreen(
    authManager: AuthManager,
    onBackToWelcome: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Local state for Google Sign-In
    var isSigningIn by remember { mutableStateOf(false) }
    var signInError by remember { mutableStateOf<String?>(null) }

    // Google Sign-In setup
    val googleAuthUiClient = remember {
        GoogleAuthUiClient(
            context = context,
            oneTapClient = Identity.getSignInClient(context)
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                scope.launch {
                    val signInResult = googleAuthUiClient.signInWithIntent(
                        intent = result.data ?: return@launch
                    )
                    val success = authManager.handleGoogleSignInResult(signInResult)
                    if (!success) {
                        signInError = signInResult.errorMessage
                    }
                    isSigningIn = false
                }
            } else {
                isSigningIn = false
            }
        }
    )

    // Handle system back button
    BackHandler {
        if (!isSigningIn) {
            onBackToWelcome()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LottieAnimationView(LottieAnimationSpec("login_animation.json"))
        Spacer(modifier = Modifier.height(lDimens.dp60))

        GoogleSignInButton(
            onClick = {
                if (!isSigningIn) {
                    isSigningIn = true
                    signInError = null
                    scope.launch {
                        val signInIntentSender = googleAuthUiClient.signIn()
                        launcher.launch(
                            IntentSenderRequest.Builder(
                                signInIntentSender ?: return@launch
                            ).build()
                        )
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(lDimens.dp35))

        // Show error if any
        signInError?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(lDimens.dp16)
            )
        }
    }

    // Show loading overlay
    if (isSigningIn) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(lDimens.dp16))
                Text(
                    text = "Signing in...",
                    color = Color.White
                )
            }
        }
    }
}