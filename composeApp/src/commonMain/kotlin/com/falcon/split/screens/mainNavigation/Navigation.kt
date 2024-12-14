package com.falcon.split.screens.mainNavigation

import GroupsScreenWithDummyData
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.CutCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.falcon.split.MainViewModel
import com.falcon.split.MainViewModelFactory
import com.falcon.split.data.network.ApiClient
import com.falcon.split.screens.mainNavigation.history.HistoryScreen
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.group_icon_filled
import split.composeapp.generated.resources.group_icon_outlined
import split.composeapp.generated.resources.history_icon_filled
import split.composeapp.generated.resources.history_icon_outlined
import split.composeapp.generated.resources.home_icon_filled
import split.composeapp.generated.resources.home_icon_outlined
import split.composeapp.generated.resources.nunito_semibold_1
import split.composeapp.generated.resources.refresh

@Composable
fun NavHostMain(
    client: ApiClient,
    navControllerBottomNav: NavHostController = rememberNavController(),
    onNavigate: (rootName: String) -> Unit,
    prefs: DataStore<Preferences>,
    openUserOptionsMenu: MutableState<Boolean>,
    snackBarHostState: SnackbarHostState,
    navControllerMain: NavHostController
) {
    val backStackEntry by navControllerBottomNav.currentBackStackEntryAsState()
    val currentScreen = backStackEntry?.destination
    val newsViewModel: MainViewModel = viewModel(
        factory = MainViewModelFactory(client, prefs)
    )
    Scaffold(
        topBar = {
            val title = getTitle(currentScreen)
            val FONT_NUNITO_SEMIBOLD_1  = org.jetbrains.compose.resources.Font(Res.font.nunito_semibold_1, weight = FontWeight.Normal, style = FontStyle.Normal)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, top = 12.dp)
            ) {
                Text(
                    text = getTitle(currentScreen),
                    fontSize = 23.sp,
                    style = TextStyle(
                        fontFamily = FontFamily(FONT_NUNITO_SEMIBOLD_1),
                        fontSize = 24.sp,
                    )
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            //TODO()
                        },
                        content = {
                            Icon(
                                painter = painterResource(Res.drawable.refresh),
                                contentDescription = "refresh",
                                modifier = Modifier
                                    .size(25.dp)
                                    .clip(CutCornerShape(CornerSize(3.dp))),
                            )
                        },
                    )
                    IconButton(onClick = {
                        openUserOptionsMenu.value = true
                    }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Settings",
                            modifier = Modifier
                                .rotate(90F)
                                .clickable {
                                    openUserOptionsMenu.value = true
                                }
                        )
                    }
                }
            }
        },
        bottomBar = {
            BottomNavigationBar(navControllerBottomNav)
        }
    ) { innerPadding ->
        NavHost(
            navController = navControllerBottomNav,
            startDestination = BottomBarScreen.Reels.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(500)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(500)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(500)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(500)
                )
            }
        ) {


            composable(route = BottomBarScreen.Home.route) {
                HomeScreem(onNavigate, prefs, snackBarHostState, navControllerBottomNav, newsViewModel, navControllerMain)
            }
            composable(route = BottomBarScreen.Reels.route) {
                HistoryScreen(onNavigate, prefs, newsViewModel, snackBarHostState, navControllerMain)
            }
            composable(route = BottomBarScreen.Profile.route) {
                GroupsScreenWithDummyData(
                    onCreateGroupClick = {
                        navControllerMain.navigate("create_group")
                    },
                    onGroupClick = { group ->
                        navControllerMain.navigate("group_details/${group.groupId}")
                    }
                )
            }
        }
    }
}



fun getTitle(currentScreen: NavDestination?): String {
    return when (currentScreen?.route) {
        BottomBarScreen.Home.route -> {
            "Home"
        }

        BottomBarScreen.Reels.route -> {
            "History"
        }

        BottomBarScreen.Profile.route -> {
            "Groups"
        }

        else -> {
            ""
        }
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
        badgeCount = mutableStateOf(12)
    )

    data object Reels : BottomBarScreen(
        index = 1,
        route = "REELS",
        title = "History",
        unSelectedIcon = Res.drawable.history_icon_outlined,
        selectedIcon = Res.drawable.history_icon_filled,
    )

    data object Profile : BottomBarScreen(
        index = 2,
        route = "PROFILE",
        title = "Groups",
        unSelectedIcon = Res.drawable.group_icon_outlined,
        selectedIcon = Res.drawable.group_icon_filled,
        hasUpdate = mutableStateOf(true)
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
    TopAppBar(
        title = { Text(title) },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "back_button"
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
    val reelsItem = BottomBarScreen.Reels
    val profileItem = BottomBarScreen.Profile

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
    Surface(
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        modifier = modifier.windowInsetsPadding(BottomAppBarDefaults.windowInsets)
    ) {
        if (show) {
            Column {
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
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
    BadgedBox(
        badge = {
            if (badgeCount != null && badgeCount.value != 0) {
                Badge {
                    Text(text = badgeCount.value.toString())
                }
            } else if (hasUpdate?.value != null){
                Badge()
            }
        }
    ) {
        Column(
            modifier = modifier
                .clickable(
                    onClick = onClick,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
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
                modifier = modifier.then(
                    Modifier.clickable {
                        onClick()
                    }
                        .size(24.dp)
                )
            )

            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected.value) {
                    FontWeight.SemiBold
                } else {
                    FontWeight.Normal
                }
            )
        }
    }
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
        BottomBarScreen.Reels.route,
        BottomBarScreen.Profile.route,
        -> true

        else -> false
    }

val items = listOf("feed", "news", "timeline")