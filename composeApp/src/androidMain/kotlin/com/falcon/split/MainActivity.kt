package com.falcon.split
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.falcon.split.AndroidUserManager.AndroidUserProfileManager
import com.falcon.split.AndroidUserManager.FirebaseUserManager
import com.falcon.split.SpecificScreens.PhoneNumberBottomSheet
import com.falcon.split.contact.AndroidContactManager
import com.falcon.split.data.FirestoreManager
import com.falcon.split.data.ProfileManager.UserProfileManager
import com.falcon.split.data.config.BackendConfig
import com.falcon.split.data.network.ApiClient
import com.falcon.split.data.network.createHttpClient
import com.falcon.split.data.repository.FirebaseExpenseRepository
import com.falcon.split.data.repository.FirebaseGroupRepository
import com.falcon.split.data.repository.FirebaseHistoryRepository
import com.falcon.split.presentation.theme.SplitTheme
import com.falcon.split.presentation.screens.mainNavigation.Routes
import com.falcon.split.presentation.sign_in.AuthUiClient
import com.falcon.split.presentation.sign_in.GoogleAuthUiClient
import com.falcon.split.presentation.sign_in.GoBackendAuthUiClient
import com.falcon.split.presentation.sign_in.PhoneNumberViewModel
import com.falcon.split.presentation.sign_in.PhoneNumberViewModelFactory
import com.falcon.split.presentation.sign_in.SignInViewModel
import com.falcon.split.presentation.sign_in.UserState
import com.falcon.split.presentation.theme.lDimens
import com.falcon.split.screens.mainNavigation.Intents
import com.falcon.split.utils.OpenLink
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.FirebaseApp
import com.mmk.kmpauth.uihelper.google.GoogleSignInButton
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {

    // Backend configuration - centralized management
    private val backendConfig by lazy { BackendConfig(applicationContext) }

    // Use BackendConfig instead of hardcoded repositories
    private val groupRepository by lazy { backendConfig.groupRepository }
    private val expenseRepository by lazy { backendConfig.expenseRepository }
    private val historyRepository by lazy { backendConfig.historyRepository }
    private val userManager by lazy { backendConfig.userManager }
    private val userProfileManager by lazy { backendConfig.userProfileManager }

    // Authentication clients - choose based on backend configuration
    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }

    private val goBackendAuthUiClient by lazy {
        GoBackendAuthUiClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext),
            goBackendManager = backendConfig.goBackendManager
        )
    }

    private lateinit var contactManager: AndroidContactManager

    // Keep Firebase managers for backward compatibility during transition
    val firestoreManager = FirestoreManager()
    val androidUserProfileManager = AndroidUserProfileManager(firestoreManager = firestoreManager)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        contactManager = AndroidContactManager(this)
        ClipboardManager.init(applicationContext)
        Intents.init(applicationContext)
        OpenLink.init(applicationContext)

        installSplashScreen().apply {
            // Perform Some Code During Splash Screen
        }

        // Sign out function - uses BackendConfig
        val onSignOutFunction: () -> Unit = {
            lifecycleScope.launch {
                if (backendConfig.useGoBackend) {
                    goBackendAuthUiClient.signOut()
                } else {
                    googleAuthUiClient.signOut()
                }
                Toast.makeText(
                    applicationContext,
                    "Signed out",
                    Toast.LENGTH_LONG
                ).show()
            }
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
                        // Create a dummy ApiClient for backward compatibility
                        // The actual API calls now go through BackendConfig repositories
                        ApiClient { null }
                    },
                    prefs = prefs,
                    onSignOut = onSignOutFunction,
                    contactManager = contactManager,
                    AndroidSignInComposable = remember {
                        @Composable { navController ->
                            CallGoogleSignInAndroid(navController, requestSendForGetUserData, prefs)
                        }
                    },
                    AndroidProfileScreenComposable = remember {
                        @Composable { navController ->
                            CallProfileScreenInAndroid(navController)
                        }
                    },
                    groupRepository = groupRepository,
                    expenseRepository = expenseRepository,
                    historyRepository = historyRepository,
                    darkTheme = darkTheme,
                    userManager = userManager,
                    userProfileManager = userProfileManager
                )
            }
        }
    }

    @Composable
    fun CallGoogleSignInAndroid(
        navControllerCommon: NavHostController,
        requestSendForGetUserData: MutableState<Boolean>,
        prefs: DataStore<Preferences>
    ) {
        val viewModel = viewModel<SignInViewModel>()

        // Choose the appropriate auth client based on backend configuration
        val authClient =
            if (backendConfig.useGoBackend) goBackendAuthUiClient else googleAuthUiClient

        val phoneViewModel = viewModel<PhoneNumberViewModel>(
            factory = PhoneNumberViewModelFactory(authClient)
        )
        val state by viewModel.userDetails.collectAsStateWithLifecycle()

        LaunchedEffect(key1 = Unit) {
            if (authClient.getSignedInUser() != null) {
                phoneViewModel.showPhoneNumberDialog()
            }
        }

        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartIntentSenderForResult(),
            onResult = { result ->
                requestSendForGetUserData.value = true
                if (result.resultCode == RESULT_OK) {
                    lifecycleScope.launch {
                        val signInResult = authClient.signInWithIntent(
                            intent = result.data ?: return@launch
                        )
                        viewModel.onSignInResult(signInResult)
                    }
                }
            }
        )

        LaunchedEffect(state) {
            if (state is UserState.Success) {
                saveFirebaseUser(prefs, (state as UserState.Success).user)
                phoneViewModel.showPhoneNumberDialog()
                Toast.makeText(
                    applicationContext,
                    "Sign in Success",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        SignInScreen(
            state = state,
            viewModel = viewModel,
            phoneViewModel = phoneViewModel,
            navControllerCommon = navControllerCommon,
            requestSendForGetUserData = requestSendForGetUserData,
            onSignInClick = {
                viewModel.makeStateLoading()
                lifecycleScope.launch {
                    val signInIntentSender = authClient.signIn()
                    launcher.launch(
                        IntentSenderRequest.Builder(
                            signInIntentSender ?: return@launch
                        ).build()
                    )
                }
            },
            authClient = authClient
        )
    }

    @Composable
    fun CallProfileScreenInAndroid(navControllerCommon: NavHostController) {
        // Use the appropriate auth client based on backend configuration
        val authClient =
            if (backendConfig.useGoBackend) goBackendAuthUiClient else googleAuthUiClient
        val userData = authClient.getSignedInUser()

        val onSignOut = {
            lifecycleScope.launch {
                authClient.signOut()
                Toast.makeText(
                    applicationContext,
                    "Signed out",
                    Toast.LENGTH_LONG
                ).show()
                navControllerCommon.navigate(Routes.WELCOME_PAGE.name)
            }
        }
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (userData?.profilePictureUrl != null) {
                AsyncImage(
                    model = userData.profilePictureUrl,
                    contentDescription = "Profile picture",
                    modifier = Modifier
                        .size(lDimens.dp150)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(lDimens.dp16))
            }
            if (userData?.username != null) {
                Text(
                    text = userData.username,
                    textAlign = TextAlign.Center,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(lDimens.dp16))
            }
            Button(
                onClick = {
                    onSignOut()
                }
            ) {
                Text(text = "Sign out")
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

@Composable
fun SignInScreen(
    state: UserState,
    viewModel: SignInViewModel,
    phoneViewModel: PhoneNumberViewModel,
    navControllerCommon: NavHostController,
    requestSendForGetUserData: MutableState<Boolean>,
    onSignInClick: () -> Unit,
    authClient: AuthUiClient
) {
    val showPhoneDialog by phoneViewModel.showPhoneDialog.collectAsState()
    val isLoading by phoneViewModel.isLoading.collectAsState()
    val error by phoneViewModel.error.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(key1 = state) {
        if (state is UserState.Error) {
            Toast.makeText(
                context,
                state.error,
                Toast.LENGTH_LONG
            ).show()
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
            onClick = onSignInClick
        )
        Spacer(modifier = Modifier.height(lDimens.dp35))
    }

    if (requestSendForGetUserData.value) {
        val userState by viewModel.userDetails.collectAsState()
        when (userState) {
            is UserState.Error -> {
                val error = (userState as UserState.Error).error
                println("ERROR_TAG$error")
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                ) {
                    Text(
                        text = "Error loading user: $error",
                        modifier = Modifier.padding(lDimens.dp16)
                    )
                }
            }
            UserState.Loading -> {
                SignInProgressPopup()
            }
            is UserState.Success -> {
                // Phone dialog will be shown via LaunchedEffect in CallGoogleSignInAndroid
            }
        }
    }

    PhoneNumberBottomSheet(
        isVisible = showPhoneDialog,
        onDismiss = { phoneViewModel.hidePhoneNumberDialog() },
        onPhoneNumberSubmit = { phoneNumber ->
            phoneViewModel.submitPhoneNumber(phoneNumber) { success ->
                if (success) {
                    phoneViewModel.hidePhoneNumberDialog()
                    navControllerCommon.navigate(Routes.APP_CONTENT.name)
                } else {
                    Toast.makeText(
                        context,
                        "Failed to save phone number. Please try again.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    )

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }

    error?.let { errorMessage ->
        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
    }
}

@Composable
fun PhoneNumberScreen() {
    var showPhoneInput by remember { mutableStateOf(false) }
    Box(
        contentAlignment = Alignment.Center
    ){
        Button(
            onClick = {
                showPhoneInput = true
            }
        ) {
            Text("True")
        }
    }
    // Bottom sheet overlay
    PhoneNumberBottomSheet(
        isVisible = showPhoneInput,
        onDismiss = { showPhoneInput = false },
        onPhoneNumberSubmit = { phoneNumber ->
            // Handle the phone number
            showPhoneInput = false
        }
    )
}
