package com.falcon.split
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.falcon.split.contact.AndroidContactManager
import com.falcon.split.data.auth.GoBackendManager
import com.falcon.split.data.network.ApiClient
import com.falcon.split.data.network.createHttpClient
import com.falcon.split.presentation.sign_in.GoBackendAuthUiClient
import com.falcon.split.presentation.theme.SplitTheme
import com.falcon.split.screens.mainNavigation.Intents
import com.falcon.split.utils.OpenLink
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {

    private val goBackendManager =  GoBackendManager(applicationContext)

    private val goBackendAuthUiClient by lazy {
        GoBackendAuthUiClient(
            context = applicationContext,
            goBackendManager = goBackendManager
        )
    }

    private lateinit var contactManager: AndroidContactManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        contactManager = AndroidContactManager(this)
        ClipboardManager.init(applicationContext)
        Intents.init(applicationContext)
        OpenLink.init(applicationContext)

        installSplashScreen().apply {
            // Perform Some Code During Splash Screen
        }

        try {

        }
        catch (e: Exception) {
            println("ERROR_TAG: $e")
            // GEMINI API CALL FOR ("FIX FOR: $e")
            // LOG THAT
            // throw e
        }
        setContent {
            val requestSendForGetUserData = remember { mutableStateOf(false) }
            val prefs = remember {
                createDataStore(context = applicationContext)
            }
            val darkTheme = remember {
                mutableStateOf(
                    runBlocking {
                        isDarkThemeEnabled(prefs)
                    }
                )
            }
            val scope = rememberCoroutineScope()
            SplitTheme(
                darkTheme = darkTheme.value, onThemeUpdated = {
                    scope.launch {
                        darkTheme.value = !darkTheme.value
                        toggleDarkTheme(prefs)
                    }
                }
            ) {
                App(
                    client = remember {
                        ApiClient(createHttpClient(OkHttp.create()))
                    },
                    prefs = prefs,
                    contactManager = contactManager,
                    darkTheme = darkTheme,
                    goBackendManager = goBackendManager
                )
            }
        }
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)} passing\n      in a {@link RequestMultiplePermissions} object for the {@link ActivityResultContract} and\n      handling the result in the {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        contactManager.handlePermissionResult(requestCode, grantResults)
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        contactManager.handleActivityResult(requestCode, resultCode, data)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.let {
//             TODO: Handle new intent (if app is already running)
            val deepLinkNewsId = handleDeepLink(it)
            // Update your newsId state
        }
    }

    // Handle the deep link intent and extract the newsId
    private fun handleDeepLink(intent: Intent?): String {
        intent?.data?.let { uri ->
            if (uri.pathSegments.isNotEmpty() && uri.pathSegments[0] == "news") {
                return uri.lastPathSegment ?: ""
            }
        }
        return ""
    }
}