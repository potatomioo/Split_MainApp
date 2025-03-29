@file:OptIn(ExperimentalCoilApi::class)

package com.falcon.split

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.setSingletonImageLoaderFactory
import coil3.request.crossfade
import coil3.util.DebugLogger
import com.arkivanov.essenty.backhandler.BackCallback
import com.arkivanov.essenty.backhandler.BackHandler
import com.falcon.split.presentation.theme.LocalSplitColors
import com.falcon.split.presentation.expense.CreateExpenseViewModel
import com.falcon.split.presentation.group.CreateGroupViewModel
import com.falcon.split.presentation.group.GroupViewModel
import com.falcon.split.contact.ContactManager
import com.falcon.split.data.network.ApiClient
import com.falcon.split.data.network.models.UserState
import com.falcon.split.presentation.screens.WelcomePage
import com.falcon.split.presentation.screens.mainNavigation.CreateExpense
import com.falcon.split.presentation.screens.mainNavigation.CreateGroupScreen
import com.falcon.split.presentation.screens.mainNavigation.GroupDetailsScreen
import com.falcon.split.presentation.screens.mainNavigation.NavHostMain
import com.falcon.split.presentation.screens.mainNavigation.ProfileScreen
import com.falcon.split.presentation.screens.mainNavigation.Routes
import com.falcon.split.presentation.screens.mainNavigation.navigateTo
import com.falcon.split.data.Repository.ExpenseRepository
import com.falcon.split.data.Repository.GroupRepository
import com.falcon.split.data.Repository.HistoryRepository
import com.falcon.split.presentation.screens.mainNavigation.SettingScreen
import com.falcon.split.presentation.theme.lDimens
import com.falcon.split.screens.mainNavigation.PaymentScreen
import com.falcon.split.userManager.UserManager
import com.falcon.split.utils.rememberEmailUtils
import com.mmk.kmpauth.google.GoogleAuthCredentials
import com.mmk.kmpauth.google.GoogleAuthProvider
import com.mmk.kmpauth.google.GoogleButtonUiContainer
import com.mmk.kmpauth.uihelper.google.GoogleSignInButton
import dev.icerock.moko.permissions.PermissionState
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.nunito_regular_1
import split.composeapp.generated.resources.profile_icon
import split.composeapp.generated.resources.settings_icon

// Custom BackHandler implementation
class AppBackHandler : BackHandler {
    private val callbacks = mutableListOf<BackCallback>()

    override fun register(callback: BackCallback) {
        callbacks.add(callback)
    }

    override fun unregister(callback: BackCallback) {
        callbacks.remove(callback)
    }

    // Method to trigger back behavior
    fun onBackPressed(): Boolean {
        val callback = callbacks.lastOrNull() ?: return false
        callback.onBack()
        return true
    }
}

@Composable
@Preview
fun App(
    client: ApiClient,
    prefs: DataStore<Preferences>,
    contactManager: ContactManager? = null,
    onSignOut: (() -> Unit)? = null,
    AndroidProfileScreenComposable: @Composable() ((navController: NavHostController) -> Unit)? = null,
    AndroidSignInComposable: @Composable() ((navController: NavHostController) -> Unit)? = null,
    groupRepository: GroupRepository? = null,
    expenseRepository: ExpenseRepository? = null,
    historyRepository: HistoryRepository? = null,
    darkTheme: MutableState<Boolean>?,
    userManager: UserManager
) {
    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }
    val factory = rememberPermissionsControllerFactory()
    val controller = remember(factory) {
        factory.createPermissionsController()
    }

    // Create a shared BackHandler instance
    val appBackHandler = remember { AppBackHandler() }

    BindEffect(controller)
    // Request Permission For Notification
    val viewModel = viewModel {
        PermissionsViewModel(controller)
    }

    when(viewModel.notificationPermissionState) {
        PermissionState.Granted -> {
            println("Notification Permission Granted")
        }
        PermissionState.DeniedAlways -> {
            LaunchedEffect(Unit) {
                snackBarHostState.showSnackbar(
                    message = "Notification Permission Denied Permanently",
                    actionLabel = "Settings",
                    duration = SnackbarDuration.Indefinite,  // Make it stay indefinitely
                    withDismissAction = false  // Remove dismiss action
                ).let { result ->
                    when (result) {
                        SnackbarResult.ActionPerformed -> {
                            controller.openAppSettings()
                        }
                        else -> {
                            // Should never reach here since we removed dismiss action
                        }
                    }
                }
            }
        }
        else -> {
            viewModel.provideOrRequestNotificationPermission()
        }
    }

    when(viewModel.contactPermissionState) {
        PermissionState.Granted -> {
            println("Contact Permission Granted")
        }
        PermissionState.DeniedAlways -> {
            LaunchedEffect(Unit) {
                snackBarHostState.showSnackbar(
                    message = "Contact Permission Denied Permanently",
                    actionLabel = "Settings",
                    duration = SnackbarDuration.Indefinite,  // Make it stay indefinitely
                    withDismissAction = false  // Remove dismiss action
                ).let { result ->
                    when (result) {
                        SnackbarResult.ActionPerformed -> {
                            controller.openAppSettings()
                        }
                        else -> {
                            // Should never reach here since we removed dismiss action
                        }
                    }
                }
            }
        }
        else -> {
            viewModel.provideOrRequestContactPermission()
        }
    }

    setSingletonImageLoaderFactory { context ->
        getAsyncImageLoader(context)
    }
    var authReady by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        GoogleAuthProvider.create(
            credentials = GoogleAuthCredentials(
                serverId = "280065041634-08vjbkjqvjmpl24nuplrl47dicvl92v3.apps.googleusercontent.com"
            )
        )
        authReady = true
    }
    val navControllerMain = rememberNavController()
    val newsViewModel: MainViewModel = viewModel(
        factory = MainViewModelFactory(client, prefs)
    )

    // Handle system back button for the appBackHandler
    DisposableEffect(Unit) {
        // This would be platform-specific and needs to be implemented
        // For Android, you'd need to handle Activity.onBackPressed
        // For iOS, you'd need to handle a navigation controller delegate

        onDispose { }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState)
        }
    ) {
        var startDestination = runBlocking {
            if (getFirebaseUserAsUserModel(prefs) != null) Routes.APP_CONTENT.name else Routes.WELCOME_PAGE.name
        }
//        startDestination = Routes.SETTINGS.name // TODO: Remove Later
        NavHost(navController = navControllerMain, startDestination = startDestination) {
            composable(Routes.PAYMENT_SCREEN.name) {
                PaymentScreen(
                    paymentAmount = 1000,
                    personName = "John Doe",
                    paymentUpiId = "john@okhdfcbank",
                    snackBarHostState = snackBarHostState,
                ) {
                    navControllerMain.popBackStack()
                }
            }
            composable(Routes.WELCOME_PAGE.name) {
                WelcomePage(navControllerMain)
            }
            composable(Routes.SIGN_IN.name) {
                AndroidSignInComposable?.invoke(navControllerMain) // Firebase Based Google Sign-In Android Specific Only
//                GoogleCloudBasedGoogleSignInForKMM(prefs, navControllerMain, authReady, newsViewModel, scope) // Google Cloud Based Google Sign In For KMM, Works In KMM but need to setup separate server for JWT Token Conversion As Google Auth Id Provided By It is Temporary.
            }
            composable(Routes.APP_CONTENT.name) {
                val openUserOptionsMenu = remember { mutableStateOf(false) } // In Future Replace It With Bottom - Sheet
                val groupViewModel = remember { GroupViewModel(
                    groupRepository!!,
                    expenseRepository!!,
                    userManager
                ) }
                if (historyRepository != null) {
                    NavHostMain(
                        client = client,
                        onNavigate = { routeName ->
                            navigateTo(routeName, navControllerMain)
                        },
                        prefs = prefs,
                        openUserOptionsMenu = openUserOptionsMenu,
                        snackBarHostState = snackBarHostState,
                        navControllerMain = navControllerMain,
                        viewModel = groupViewModel,
                        historyRepository = historyRepository
                    )
                }
                if (openUserOptionsMenu.value) {
                    OptionMenuPopup(
                        openUserOptionsMenu,
                        navControllerMain
                    )
                }
            }
            composable(Routes.CREATE_GROUP.name) {
                val createGroupViewModel = remember { CreateGroupViewModel(groupRepository!!) }
                CreateGroupScreen(
                    onGroupCreated = { groupId ->
                        scope.launch {
                            snackBarHostState.showSnackbar(
                                message = "Group created successfully",
                                duration = SnackbarDuration.Short
                            )
                        }
                        // Just navigate back to the previous screen instead of popping
                        navControllerMain.navigate(Routes.APP_CONTENT.name) {
                            popUpTo(Routes.APP_CONTENT.name) { inclusive = false }
                        }
                    },
                    onNavigateBack = {
                        navControllerMain.popBackStack()
                    },
                    contactManager = contactManager!!,
                    viewModel = createGroupViewModel
                )
            }
            composable(
                route = "create_expense?groupId={groupId}",
                        /* Now Either Call
                            navControllerMain.navigate("create_expense")
                            OR
                            navControllerMain.navigate("create_expense?groupId=$groupId")
                        */
                arguments = listOf(navArgument("groupId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) { backStackEntry ->
                val groupId = backStackEntry.arguments?.getString("groupId")
                val createExpenseViewModel = remember{ CreateExpenseViewModel(groupRepository!!,expenseRepository!!) }
                CreateExpense(
                    navControllerMain = navControllerMain,
                    onNavigateBack = { navControllerMain.popBackStack() },
                    viewModel = createExpenseViewModel,
                    backHandler = appBackHandler, // Pass the shared BackHandler
                    groupId = groupId // If coming from GroupDetailsScreen, this will be non-null else null in case of coming from HomeScreen
                )
            }
            composable(Routes.PROFILE.name) {
                ProfileScreen(
                    navControllerMain,
                    prefs
                ) {
                    scope.launch {
                        deleteUser(prefs)
                        onSignOut?.invoke()
                        navControllerMain.navigate(Routes.WELCOME_PAGE.name)
                    }
                }
            }
            composable(Routes.SETTINGS.name){
                val emailUtils = rememberEmailUtils()
                SettingScreen(
                    navController = navControllerMain,
                    onNavigateBack = {navControllerMain.popBackStack()},
                    emailUtils = emailUtils,
                    prefs = prefs,
                    darkTheme = darkTheme!!
                )
            }
            composable(
                route = "group_details/{groupId}",
                arguments = listOf(navArgument("groupId") { type = NavType.StringType })
            ) { backStackEntry ->
                val groupId = backStackEntry.arguments?.getString("groupId") ?: return@composable
                val groupViewModel = remember { GroupViewModel(
                    groupRepository!!,
                    expenseRepository!!,
                    userManager
                ) }
                GroupDetailsScreen(
                    groupId = groupId,
                    onNavigateBack = { navControllerMain.popBackStack() },
//                    onAddExpense = { groupId ->
//                        navControllerMain.navigate("add_expense/$groupId")
//                    },
                    navControllerMain = navControllerMain,
                    contactManager = contactManager,
                    viewModel = groupViewModel,
                    userManager = userManager
                )
            }
        }
    }

}

@Deprecated("This method has been deprecated in favor of using Firebase Based Google-SignIn")
@Composable
private fun GoogleCloudBasedGoogleSignInForKMM( // Don't Remove This, More Mentioned At Line 184
    prefs: DataStore<Preferences>,
    navControllerMain: NavHostController,
    authReady: Boolean,
    newsViewModel: MainViewModel,
    scope: CoroutineScope
) {
    LaunchedEffect(Unit) {
        val user = getUserAsUserModel(prefs)
        if (user != null) {
            navControllerMain.navigate(Routes.APP_CONTENT.name)
        }
    }
    val requestSendForGetUserData = remember { mutableStateOf(false) }
    if (authReady) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LottieAnimationView(LottieAnimationSpec("login_animation.json"))
            Spacer(
                modifier = Modifier.height(lDimens.dp60)
            )
            GoogleButtonUiContainer(
                onGoogleSignInResult = { googleUser ->
                    newsViewModel.getUserDetailsFromGoogleAuthToken(googleUser?.idToken.toString())
                    println("Google Token:")
                    println(googleUser?.idToken)
                    requestSendForGetUserData.value = true
                }
            ) {
                GoogleSignInButton(
                    onClick = {
                        this.onClick()
                    }
                )
            }
            Spacer(
                modifier = Modifier.height(lDimens.dp35)
            )
        }
    }
    if (requestSendForGetUserData.value) {
        val userState by newsViewModel.userDetails.collectAsState()
        when (userState) {
            is UserState.Error -> {
                val error = (userState as UserState.Error).error
                println("ERROR_TAG" + error.name)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                ) {
                    Text(
                        text = "Error loading user: ${error.name}",
                        modifier = Modifier.padding(lDimens.dp16)
                    )
                }
            }

            is UserState.Loading -> {
                SignInProgressPopup()
            }

            is UserState.Success -> {
                // Saving the User Details and navigate further
                val user = (userState as UserState.Success).user
                scope.launch {
                    saveUser(prefs, user)
                    navControllerMain.navigate(Routes.APP_CONTENT.name)
                }
            }
        }
    }
}
@Composable
fun OptionMenuPopup(
    openUserOptionsMenu: MutableState<Boolean>,
    navController: NavHostController
) {
    val splitColors = LocalSplitColors.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 25.dp, end = lDimens.dp15),
        contentAlignment = Alignment.TopEnd,
    ) {
        Popup(
            alignment = Alignment.TopEnd,
            onDismissRequest = {
                openUserOptionsMenu.value = false
            }) {
            Card(
                modifier = Modifier
                    .padding(horizontal = 5.dp)
                    .padding(top = lDimens.dp4),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = splitColors.cardBackground
                )
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable {
                                openUserOptionsMenu.value = false
                                navController.navigate(Routes.PROFILE.name)
                            }
                            .padding(vertical = lDimens.dp16, horizontal = 24.dp)
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.profile_icon),
                            contentDescription = "Profile Icon",
                            modifier = Modifier
                                .size(24.dp),
                            tint = splitColors.textPrimary
                        )
                        Text(
                            text = "Profile",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = splitColors.textPrimary,
                            modifier = Modifier
                                .padding(start = lDimens.dp8)
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable {
                                openUserOptionsMenu.value = false
                                navController.navigate(Routes.SETTINGS.name)
                            }
                            .padding(vertical = lDimens.dp16, horizontal = 24.dp)
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.settings_icon),
                            contentDescription = "Settings Icon",
                            modifier = Modifier
                                .size(24.dp),
                            tint = splitColors.textPrimary
                        )
                        Text(
                            text = "Settings",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = splitColors.textPrimary,
                            modifier = Modifier
                                .padding(start = lDimens.dp8)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LineWithText() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 64.dp, end = 64.dp)
    ) {
        Divider(
            modifier = Modifier
                .height(1.dp)
                .weight(1f),
            color = Color.LightGray
        )
        Text(
            text = "OR",
            color = Color.LightGray,
            modifier = Modifier.padding(horizontal = lDimens.dp8)
        )
        Divider(
            modifier = Modifier
                .height(1.dp)
                .weight(1f),
            color = Color.LightGray
        )
    }
}

@Composable
fun SignInProgressPopup(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(Color.White)
            .fillMaxSize()
            .padding(bottom = lDimens.dp40),
        contentAlignment = Alignment.Center,
    ) {
        Popup(
            alignment = Alignment.Center,
        ) {
            Card(
                Modifier
                    .background(Color.White)
                    .padding(horizontal = 18.dp)
                    .padding(top = lDimens.dp4)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(lDimens.dp8)
            ) {
                Column(
                    Modifier
                        .background(Color.White)
                        .padding(36.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.size(180.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        LottieAnimationView(LottieAnimationSpec("google_loading.json"))
                    }
                    Spacer(modifier = Modifier.height(lDimens.dp40))
                    Text(
                        text = "Please Wait Via We Fetch Details Linked To Your Account..",
                        fontSize = 20.sp,
                        fontFamily = FontFamily(Font(Res.font.nunito_regular_1, weight = FontWeight.Normal, style = FontStyle.Normal)),
                    )
                }
            }
        }
    }
}

fun getAsyncImageLoader(context: PlatformContext) = ImageLoader.Builder(context).crossfade(true).logger(
    DebugLogger()
).build()