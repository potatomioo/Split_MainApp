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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import com.falcon.split.data.network.ApiClient
import com.falcon.split.data.network.models.UserState
import com.falcon.split.screens.WelcomePage
import com.falcon.split.screens.mainNavigation.CreateExpense
import com.falcon.split.screens.mainNavigation.CreateGroupScreen
import com.falcon.split.screens.mainNavigation.GroupDetailsScreen
import com.falcon.split.screens.mainNavigation.NavHostMain
import com.falcon.split.screens.mainNavigation.ProfileScreen
import com.falcon.split.screens.mainNavigation.navigateTo
import com.mmk.kmpauth.google.GoogleAuthCredentials
import com.mmk.kmpauth.google.GoogleAuthProvider
import com.mmk.kmpauth.google.GoogleButtonUiContainer
import com.mmk.kmpauth.uihelper.google.GoogleSignInButton
import dev.icerock.moko.permissions.PermissionState
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.nunito_regular_1
import split.composeapp.generated.resources.profile_icon
import split.composeapp.generated.resources.settings_icon

@Composable
@Preview
fun App(
    client: ApiClient,
    prefs: DataStore<Preferences>
) {
    MaterialTheme {

    }
    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }
    val factory = rememberPermissionsControllerFactory()
    val controller = remember(factory) {
        factory.createPermissionsController()
    }
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
    setSingletonImageLoaderFactory { context ->
        getAsyncImageLoader(context)
    }
    var authReady by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        GoogleAuthProvider.create(
            credentials = GoogleAuthCredentials(
                serverId = "35186900267-7tn7qmjqo7mnc1bv4jl1kb6sudomedrd.apps.googleusercontent.com"
            )
        )
        authReady = true
    }
    val navControllerMain = rememberNavController()
    val newsViewModel: MainViewModel = viewModel(
        factory = MainViewModelFactory(client, prefs)
    )
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState)
        }
    ) {
        val startDestination = runBlocking {
            if (getUserAsUserModel(prefs) != null) "app_content" else "welcome_page"
        }
        NavHost(navController = navControllerMain, startDestination = startDestination) {
            composable("welcome_page") {
                WelcomePage(navControllerMain)
            }
            composable("signin") {
                LaunchedEffect(Unit) {
                    val user = getUserAsUserModel(prefs)
                    if (user != null) {
                        navControllerMain.navigate("app_content")
                    }
                }
                val requestSendForGetUserData = remember { mutableStateOf(false) }
                if (authReady) {
                    Column (
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LottieAnimationView(LottieAnimationSpec("login_animation.json"))
                        Spacer(
                            modifier = Modifier.height(60.dp)
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
                            modifier = Modifier.height(35.dp)
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
                                    modifier = Modifier.padding(16.dp)
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
                                navControllerMain.navigate("app_content")
                            }
                        }
                    }
                }
            }
            composable("app_content") {
                val openUserOptionsMenu = remember { mutableStateOf(false) } // In Future Replace It With Bottom - Sheet
                NavHostMain(
                    client = client,
                    onNavigate = { routeName ->
                        navigateTo(routeName, navControllerMain)
                    },
                    prefs = prefs,
                    openUserOptionsMenu = openUserOptionsMenu,
                    snackBarHostState = snackBarHostState,
                    navControllerMain = navControllerMain
                )
                if (openUserOptionsMenu.value) {
                    OptionMenuPopup(
                        openUserOptionsMenu,
                        navControllerMain
                    )
                }
            }
            composable("create_group") {
                CreateGroupScreen(
                    onGroupCreated = { group ->
                        // Handle the new group
                        // Navigate back
                    },
                    onNavigateBack = {
                        // Navigate back
                    }
                )
            }
            composable("create_expense") {
                CreateExpense(navControllerMain, {}) {

                }
            }
            composable("profile") {
                ProfileScreen(
                    navControllerMain,
                    prefs
                ) {
                    scope.launch {
                        deleteUser(prefs)
                        navControllerMain.navigate("welcome_page")
                    }
                }
            }
            composable(
                route = "group_details/{groupId}",
                arguments = listOf(navArgument("groupId") { type = NavType.StringType })
            ) { backStackEntry ->
                val groupId = backStackEntry.arguments?.getString("groupId") ?: return@composable
                GroupDetailsScreen(
                    groupId = groupId,
                    onNavigateBack = { navControllerMain.popBackStack() },
                    onAddExpense = { groupId ->
                        navControllerMain.navigate("add_expense/$groupId")
                    }
                )
            }
        }
    }

}



@Composable
fun OptionMenuPopup(
    openUserOptionsMenu: MutableState<Boolean>,
    navController: NavHostController
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 25.dp, end = 15.dp),
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
                    .padding(top = 4.dp),
                shape = RoundedCornerShape(18.dp),
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable {
                                openUserOptionsMenu.value = false
                                navController.navigate("profile")
                            }
                            .padding(vertical = 16.dp, horizontal = 24.dp)
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.profile_icon),
                            contentDescription = "Profile Icon",
                            modifier = Modifier
                                .size(24.dp),
                        )
                        Text(
                            text = "Profile",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFF000000),
                            modifier = Modifier
                                .padding(start = 8.dp)
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable {
                                openUserOptionsMenu.value = false
                                navController.navigate("settings")
                            }
                            .padding(vertical = 16.dp, horizontal = 24.dp)
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.settings_icon),
                            contentDescription = "Settings Icon",
                            modifier = Modifier
                                .size(24.dp),
                        )
                        Text(
                            text = "Settings",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFF000000),
                            modifier = Modifier
                                .padding(start = 8.dp)
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
            modifier = Modifier.padding(horizontal = 8.dp)
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
            .padding(bottom = 40.dp),
        contentAlignment = Alignment.Center,
    ) {
        Popup(
            alignment = Alignment.Center,
        ) {
            Card(
                Modifier
                    .background(Color.White)
                    .padding(horizontal = 18.dp)
                    .padding(top = 4.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(8.dp)
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
                    Spacer(modifier = Modifier.height(40.dp))
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