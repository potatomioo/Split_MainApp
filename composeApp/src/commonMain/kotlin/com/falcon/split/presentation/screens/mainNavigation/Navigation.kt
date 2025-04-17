package com.falcon.split.presentation.screens.mainNavigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.falcon.split.MainViewModel
import com.falcon.split.MainViewModelFactory
import com.falcon.split.presentation.theme.LocalSplitColors
//import com.falcon.split.presentation.theme.getAppTypography
import com.falcon.split.data.network.ApiClient
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.group_icon_filled
import split.composeapp.generated.resources.group_icon_outlined
import split.composeapp.generated.resources.history_icon_filled
import split.composeapp.generated.resources.history_icon_outlined
import split.composeapp.generated.resources.home_icon_filled
import split.composeapp.generated.resources.home_icon_outlined
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.LayoutDirection
import coil3.compose.AsyncImage
import com.arkivanov.essenty.backhandler.BackCallback
import com.arkivanov.essenty.backhandler.BackHandler
import com.falcon.split.AppBackHandler
import com.falcon.split.UserModelGoogleFirebaseBased
import com.falcon.split.getFirebaseUserAsUserModel
import com.falcon.split.presentation.group.GroupViewModel
import com.falcon.split.presentation.screens.mainNavigation.history.HistoryScreen
import com.falcon.split.presentation.screens.mainNavigation.history.HistoryViewModel
import com.falcon.split.presentation.theme.SplitCard
import com.falcon.split.presentation.theme.SplitColors
import com.falcon.split.presentation.theme.getSplitTypography
import com.falcon.split.presentation.theme.lDimens
import com.falcon.split.toggleDarkTheme
import com.falcon.split.utils.rememberEmailUtils
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import split.composeapp.generated.resources.Split

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun NavHostMain(
    client: ApiClient,
    navControllerBottomNav: NavHostController = rememberNavController(),
    onNavigate: (rootName: String) -> Unit,
    prefs: DataStore<Preferences>,
    snackBarHostState: SnackbarHostState,
    navControllerMain: NavHostController,
    viewModel: GroupViewModel,
    historyViewModel: HistoryViewModel,
    darkTheme: MutableState<Boolean>
) {
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { 3 }
    )
    var selectedItemIndex by rememberSaveable {
        mutableStateOf(0)
    }
    val scope = rememberCoroutineScope()
    LaunchedEffect(pagerState.currentPage) {
        selectedItemIndex = pagerState.currentPage
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val openDrawer = remember { mutableStateOf(false) }

    // Get user model from prefs or viewModel
    var userModel by remember { mutableStateOf<UserModelGoogleFirebaseBased?>(null) }

    // Load user data
    LaunchedEffect(Unit) {
        userModel = getFirebaseUserAsUserModel(prefs)
    }

    LaunchedEffect(drawerState.currentValue) {
        openDrawer.value = (drawerState.currentValue == DrawerValue.Open)
    }

    val appBackHandler = remember { AppBackHandler() }


    val screens = listOf(
        BottomBarScreen.Home,
        BottomBarScreen.History,
        BottomBarScreen.Groups
    )

    val historyVM: MainViewModel = viewModel(
        factory = MainViewModelFactory(client, prefs)
    )


    val colors = LocalSplitColors.current

    val emailUtils = rememberEmailUtils()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            // Drawer content
            // Inside the ModalDrawerSheet in NavHostMain
            ModalDrawerSheet(
                modifier = Modifier.width(lDimens.dp330),
                drawerContainerColor = colors.backgroundSecondary
            ) {
                // User profile section remains the same
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.primary.copy(alpha = 0.1f))
                        .padding(top = lDimens.dp36, bottom = lDimens.dp24)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = lDimens.dp16),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Profile image with border
                        Box(
                            modifier = Modifier
                                .size(lDimens.dp75)
                                .clip(CircleShape)
                                .border(width = lDimens.dp3, color = colors.primary, shape = CircleShape)
                                .background(colors.backgroundSecondary),
                            contentAlignment = Alignment.Center
                        ) {
                            if (userModel?.profilePictureUrl != null) {
                                AsyncImage(
                                    model = userModel?.profilePictureUrl,
                                    contentDescription = "Profile picture",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "Profile",
                                    modifier = Modifier.size(lDimens.dp50),
                                    tint = colors.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(lDimens.dp16))

                        // User name
                        Text(
                            text = userModel?.username ?: "User",
                            style = MaterialTheme.typography.headlineMedium,
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Bold
                        )

                        // User email
                        Text(
                            text = userModel?.email ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(lDimens.dp16))


                LazyColumn (

                ){
                    item{
                        // Account section
                        SectionHeader("Account")

                        // Profile with subtitle
                        DrawerItemWithSubtitle(
                            icon = Icons.Default.Person,
                            title = "Profile",
                            subtitle = "Manage your account details",
                            tint = colors.primary,
                            onClick = {
                                navControllerMain.navigate(Routes.PROFILE.name)
                                scope.launch { drawerState.close() }
                            }
                        )

                        // Settings with subtitle
                        DrawerItemWithSubtitle(
                            icon = Icons.Default.Settings,
                            title = "Settings",
                            subtitle = "App preferences and options",
                            tint = colors.primary,
                            onClick = {
                                navControllerMain.navigate(Routes.SETTINGS.name)
                                scope.launch { drawerState.close() }
                            }
                        )

                        // Theme section
                        SectionHeader("Appearance")

                        // Theme options (Midnight and Skyhigh)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = lDimens.dp16, vertical = lDimens.dp8)
                        ) {
                            // Theme selection buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(lDimens.dp8)
                            ) {
                                // Midnight (Dark) theme button
                                ThemeOptionButton(
                                    name = "Midnight",
                                    icon = Icons.Default.Home,
                                    isSelected = darkTheme.value,
                                    colors = colors,
                                    onClick = {
                                        if (!darkTheme.value) {
                                            scope.launch {
                                                darkTheme.value = true
                                                toggleDarkTheme(prefs)
                                            }
                                        }
                                    }
                                )

                                // Skyhigh (Light) theme button
                                ThemeOptionButton(
                                    name = "Skyhigh",
                                    icon = Icons.Default.ThumbUp,
                                    isSelected = !darkTheme.value,
                                    colors = colors,
                                    onClick = {
                                        if (darkTheme.value) {
                                            scope.launch {
                                                darkTheme.value = false
                                                toggleDarkTheme(prefs)
                                            }
                                        }
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(lDimens.dp8))

                        // Support section
                        SectionHeader("Support")

                        // Contact Us with subtitle
                        DrawerItemWithSubtitle(
                            icon = Icons.Default.Email,
                            title = "Contact Us",
                            subtitle = "Reach out for help or feedback",
                            tint = colors.primary,
                            onClick = {
                                emailUtils.sendEmail(
                                    to = "deeptanshushuklaji@gmail.com",
                                    subject = "Regarding Split App",
                                )
                                scope.launch { drawerState.close() }
                            }
                        )

                        // About with subtitle
                        DrawerItemWithSubtitle(
                            icon = Icons.Default.Info,
                            title = "About",
                            subtitle = "App information and credits",
                            tint = colors.primary,
                            onClick = {
                                // Navigate to about screen or show dialog
                                scope.launch { drawerState.close() }
                            }
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        // App version at bottom
                        SplitCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = lDimens.dp16, vertical = lDimens.dp16)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(lDimens.dp16),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "Split",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = colors.primary,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    "V - 2025",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.textSecondary
                                )

                                Spacer(modifier = Modifier.height(lDimens.dp8))

                                Text(
                                    "Split Easy. Stay Friends.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )
                            }
                        }
                    }
                }
            }
        },
        gesturesEnabled = true
    ) {
        Scaffold(
            topBar = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = colors.backgroundSecondary)
                        .padding(top = lDimens.dp20, start = lDimens.dp12, bottom = lDimens.dp10)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(lDimens.dp30)
                                .clip(CircleShape)
                                .border(
                                    BorderStroke(lDimens.dp1, colors.primary),
                                    shape = CircleShape
                                )
                                .clickable {
                                    scope.launch {
                                        drawerState.open()
                                        openDrawer.value = true
                                    }
                                }
                                .background(colors.primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (userModel?.profilePictureUrl != null) {
                                AsyncImage(
                                    model = userModel!!.profilePictureUrl,
                                    contentDescription = "Profile",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "Profile",
                                    tint = colors.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(lDimens.dp5))

                        Box(
                            modifier = Modifier
                                .size(lDimens.dp30)
                                .border(
                                    BorderStroke(
                                        lDimens.dp1,
                                        colors.primary,
                                    ),
                                    shape = CircleShape
                                )
                                .clickable {

                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(Res.drawable.Split),
                                contentDescription = "Split Logo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.width(lDimens.dp10))

                        if (pagerState.currentPage == 0) {
                            // For Home screen, show greeting with colored firstName
                            val (greeting, firstName) = getGreetingParts(userModel?.username ?: "User")

                            // Row to place the greeting and name next to each other
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Regular greeting text
                                Text(
                                    text = greeting,
                                    fontSize = 20.sp,
                                    color = colors.textPrimary,
                                    style = getSplitTypography().headlineMedium
                                )

                                Text(
                                    text = firstName,
                                    fontSize = 20.sp,
                                    color = colors.primary,
                                    style = getSplitTypography().headlineMedium
                                )
                            }
                        } else {
                            // For other screens, show title
                            Text(
                                text = getTitle(pagerState.currentPage),
                                fontSize = 20.sp,
                                color = colors.textPrimary,
                                style = getSplitTypography().headlineMedium
                            )
                        }
                    }
                }
            },
            bottomBar = {
                AppBottomNavigationBar(
                    show = true,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        screens.forEach { item ->
                            AppBottomNavigationBarItem(
                                selectedIcon = item.selectedIcon,
                                unSelectedIcon = item.unSelectedIcon,
                                label = item.title,
                                onClick = {
                                    selectedItemIndex = item.index
                                    scope.launch {
                                        pagerState.animateScrollToPage(item.index)
                                    }
                                },
                                selected = mutableStateOf(selectedItemIndex == item.index),
                                hasUpdate = item.hasUpdate,
                                badgeCount = item.badgeCount
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                        end = innerPadding.calculateEndPadding(LayoutDirection.Ltr),
                        bottom = innerPadding.calculateBottomPadding(),
                        top = innerPadding.calculateTopPadding() - lDimens.dp3
                    )
            ) { page ->
                when (page) {
                    0 -> HomeScreen(
                        onNavigate,
                        prefs,
                        snackBarHostState,
                        navControllerBottomNav,
                        historyVM,
                        navControllerMain,
                        topPadding = innerPadding.calculateTopPadding(),
                        viewModel = viewModel,
                        historyViewModel,
                        pagerState
                    )
                    1 -> IntegratedHistoryScreen(
                        onNavigate,
                        prefs,
                        historyVM,
                        snackBarHostState,
                        navControllerMain,
                        historyViewModel = historyViewModel
                    )
                    2 -> GroupsScreen(
                        onCreateGroupClick = { navControllerMain.navigate("create_group") },
                        onGroupClick = { group -> navControllerMain.navigate("group_details/${group.id}") },
                        navControllerMain,
                        viewModel
                    )
                }
            }
        }
        DisposableEffect(drawerState.currentValue) {
            if (drawerState.currentValue == DrawerValue.Open) {
                val callback = BackCallback(onBack = {
                    scope.launch {
                        drawerState.close()
                        openDrawer.value = false
                    }
                })
                appBackHandler.register(callback)

                onDispose {
                    appBackHandler.unregister(callback)
                }
            } else {
                onDispose { }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    val colors = LocalSplitColors.current

    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = colors.primary,
        modifier = Modifier.padding(horizontal = lDimens.dp16, vertical = lDimens.dp8)
    )
}


@Composable
private fun DrawerItemWithSubtitle(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tint: Color,
    onClick: () -> Unit
) {
    val colors = LocalSplitColors.current

    Surface(
        onClick = onClick,
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = lDimens.dp16, vertical = lDimens.dp12),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(lDimens.dp24)
            )

            Spacer(modifier = Modifier.width(lDimens.dp16))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.textPrimary
                )

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun ThemeOptionButton(
    name: String,
    icon: ImageVector,
    isSelected: Boolean,
    colors: SplitColors,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
//        modifier = Modifier.weight(1F),
        color = if (isSelected) colors.primary else colors.backgroundPrimary,
        shape = RoundedCornerShape(lDimens.dp12),
        border = if (!isSelected) BorderStroke(lDimens.dp1, colors.divider) else null
    ) {
        Row(
            modifier = Modifier
                .padding(lDimens.dp12),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = name,
                tint = if (isSelected) Color.White else colors.textPrimary,
                modifier = Modifier.size(lDimens.dp20)
            )

            Spacer(modifier = Modifier.width(lDimens.dp4))

            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSelected) Color.White else colors.textPrimary
            )
        }
    }
}

fun getTitle(currentPage: Int): String {
    return when (currentPage) {
        0 -> "Home"
        1 -> "History"
        2 -> "Groups"
        else -> ""
    }
}

fun navigateTo(
    routeName: String,
    navController: NavController
) {
    when (routeName) {
        "BACK_CLICK_ROUTE" -> {
            navController.popBackStack()
        }

        else -> {
            navController.navigate(routeName)
        }
    }
}

@Composable
fun IntegratedHistoryScreen(
    onNavigate: (rootName: String) -> Unit,
    prefs: DataStore<Preferences>,
    newsViewModel: MainViewModel,
    snackBarHostState: androidx.compose.material3.SnackbarHostState,
    navControllerMain: NavHostController,
    modifier: Modifier = Modifier,
    historyViewModel: HistoryViewModel
) {
    // Call the new History Screen implementation
    HistoryScreen(
        historyViewModel = historyViewModel,
        prefs = prefs,
        newsViewModel = newsViewModel,
        snackBarHostState = snackBarHostState,
        navControllerMain = navControllerMain,
    )
}

sealed class AppScreen(val route: String) {
    data object Detail : AppScreen("nav_detail")
}

sealed class BottomBarScreen(
    val index: Int,
    val route: String,
    var title: String,
    val unSelectedIcon: DrawableResource,
    val selectedIcon: DrawableResource,
    val hasUpdate: MutableState<Boolean>? = null,
    val badgeCount: MutableState<Int>? = null
) {
    data object Home : BottomBarScreen(
        index = 0,
        route = "HOME",
        title = "Home",
        unSelectedIcon = Res.drawable.home_icon_outlined,
        selectedIcon = Res.drawable.home_icon_filled,
        badgeCount = mutableStateOf(0),
    )

    data object History : BottomBarScreen(
        index = 1,
        route = "REELS",
        title = "History",
        unSelectedIcon = Res.drawable.history_icon_outlined,
        selectedIcon = Res.drawable.history_icon_filled,
        badgeCount = mutableStateOf(0)
    )

    data object Groups : BottomBarScreen(
        index = 2,
        route = "PROFILE",
        title = "Groups",
        unSelectedIcon = Res.drawable.group_icon_outlined,
        selectedIcon = Res.drawable.group_icon_filled,
        hasUpdate = mutableStateOf(false)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    title: String,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalSplitColors.current

    TopAppBar(
        title = { Text(title, color = colors.textPrimary) },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = colors.backgroundSecondary
        ),
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "back_button",
                        tint = colors.textPrimary
                    )
                }
            }
        }
    )
}

@Composable
fun BottomNavigationBar(
    navController: NavHostController,
) {
    val homeItem = BottomBarScreen.Home
    val reelsItem = BottomBarScreen.History
    val profileItem = BottomBarScreen.Groups

    val screens = listOf(
        homeItem,
        reelsItem,
        profileItem
    )
    var selectedItemIndex by rememberSaveable {
        mutableStateOf(1)
    }
    AppBottomNavigationBar(
        show = true,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            screens.forEach { item->
                AppBottomNavigationBarItem(
                    selectedIcon = item.selectedIcon,
                    unSelectedIcon = item.unSelectedIcon,
                    label = item.title,
                    onClick = {
                        selectedItemIndex = item.index
                        navigateBottomBar(navController, item.route)
                    },
                    selected = mutableStateOf(selectedItemIndex == item.index),
                    hasUpdate = item.hasUpdate,
                    badgeCount = item.badgeCount
                )
            }
        }
    }
}

@Composable
fun AppBottomNavigationBar(
    modifier: Modifier = Modifier,
    show: Boolean,
    content: @Composable (RowScope.() -> Unit),
) {
    val colors = LocalSplitColors.current

    Surface(
        color = colors.backgroundSecondary,
        contentColor = colors.textPrimary,
        modifier = modifier.windowInsetsPadding(BottomAppBarDefaults.windowInsets)
    ) {
        if (show) {
            Column {
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(lDimens.dp1),
                    color = colors.textSecondary.copy(alpha = 0.2f)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(65.dp)
                        .selectableGroup(),
                    verticalAlignment = Alignment.CenterVertically,
                    content = content
                )
            }
        }
    }
}

@Composable
fun AppBottomNavigationBarItem(
    modifier: Modifier = Modifier,
    selectedIcon: DrawableResource,
    unSelectedIcon: DrawableResource,
    hasUpdate: MutableState<Boolean>?,
    label: String,
    onClick: () -> Unit,
    selected: MutableState<Boolean>,
    badgeCount: MutableState<Int>? = null
) {
    val colors = LocalSplitColors.current
    val interactionSource = remember { MutableInteractionSource() }


    BadgedBox(
        badge = {
            if (badgeCount != null && badgeCount.value != 0) {
                Badge {
                    Text(text = badgeCount.value.toString())
                }
            } else if (hasUpdate?.value != null){
                Badge()
            }
        },
        modifier = Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,  // Remove the ripple effect
            onClick = onClick
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(lDimens.dp4)
        ) {
            Image(
                painter = painterResource(
                    if (selected.value) {
                        selectedIcon
                    } else {
                        unSelectedIcon
                    }
                ),
                contentDescription = unSelectedIcon.toString(),
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.tint(colors.textPrimary),
                modifier = modifier.then(
                    Modifier
                        .size(lDimens.dp20)
                ),
            )

            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textPrimary,
                fontWeight = if (selected.value) {
                    FontWeight.SemiBold
                } else {
                    FontWeight.Normal
                }
            )
        }
    }
}


// Update the function to return a Pair of strings (greeting and firstName)
@Composable
fun getGreetingParts(name: String): Pair<String, String> {
    val currentDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val hour = currentDateTime.hour

    // Extract first name (everything before the first space)
    val firstName = if (name.contains(" ")) {
        name.substring(0, name.indexOf(" "))
    } else {
        name // If there's no space, use the whole name
    }

    val greeting = when {
        hour < 12 -> "Good morning, "
        hour < 17 -> "Good afternoon, "
        else -> "Good evening, "
    }

    return Pair(greeting, firstName)
}


private fun navigateBottomBar(navController: NavController, destination: String) {
    navController.navigate(destination) {
        navController.graph.startDestinationRoute?.let { route ->
            popUpTo(BottomBarScreen.Home.route) {
                saveState = true
            }
        }
        launchSingleTop = true
        restoreState = true
    }
}

private val NavController.shouldShowBottomBar
    get() = when (this.currentBackStackEntry?.destination?.route) {
        BottomBarScreen.Home.route,
        BottomBarScreen.History.route,
        BottomBarScreen.Groups.route,
            -> true

        else -> false
    }

val items = listOf("feed", "news", "timeline")