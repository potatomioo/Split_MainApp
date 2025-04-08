package com.falcon.split.presentation.screens.mainNavigation

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
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.LayoutDirection
import com.falcon.split.presentation.group.GroupViewModel
import com.falcon.split.presentation.screens.mainNavigation.history.HistoryScreen
import com.falcon.split.presentation.screens.mainNavigation.history.HistoryViewModel
import com.falcon.split.presentation.theme.lDimens
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun NavHostMain(
    client: ApiClient,
    navControllerBottomNav: NavHostController = rememberNavController(),
    onNavigate: (rootName: String) -> Unit,
    prefs: DataStore<Preferences>,
    openUserOptionsMenu: MutableState<Boolean>,
    snackBarHostState: SnackbarHostState,
    navControllerMain: NavHostController,
    viewModel: GroupViewModel,
    historyViewModel: HistoryViewModel
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

    val screens = listOf(
        BottomBarScreen.Home,
        BottomBarScreen.History,
        BottomBarScreen.Groups
    )

    val historyVM: MainViewModel = viewModel(
        factory = MainViewModelFactory(client, prefs)
    )


    val colors = LocalSplitColors.current
    val isDarkTheme = isSystemInDarkTheme()

    Scaffold(
        topBar = {
            val title = getTitle(pagerState.currentPage)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = colors.backgroundSecondary)
                    .padding(start = lDimens.dp12, top = lDimens.dp12)
            ) {
                Text(
                    text = title,
                    fontSize = 23.sp,
//                    style = getAppTypography(isDarkTheme).titleLarge,
                    color = colors.textPrimary
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        openUserOptionsMenu.value = true
                    }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Settings",
                            modifier = Modifier.rotate(90F),
                            tint = colors.textPrimary
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
                    top = innerPadding.calculateTopPadding() - 3.dp
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
                    historyViewModel
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
        onNavigate = onNavigate,
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
        badgeCount = mutableStateOf(12)
    )

    data object History : BottomBarScreen(
        index = 1,
        route = "REELS",
        title = "History",
        unSelectedIcon = Res.drawable.history_icon_outlined,
        selectedIcon = Res.drawable.history_icon_filled,
    )

    data object Groups : BottomBarScreen(
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
                        .height(1.dp),
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