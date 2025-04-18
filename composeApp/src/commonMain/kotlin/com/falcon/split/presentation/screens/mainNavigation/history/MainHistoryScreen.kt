package com.falcon.split.presentation.screens.mainNavigation.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.falcon.split.MainViewModel
import com.falcon.split.data.network.ApiClient
import com.falcon.split.data.network.models_app.SettlementStatus
import com.falcon.split.presentation.PullToRefresh.PullRefreshIndicator
import com.falcon.split.presentation.PullToRefresh.pullRefresh
import com.falcon.split.presentation.PullToRefresh.rememberPullRefreshState
import com.falcon.split.presentation.history.HistoryActionType
import com.falcon.split.presentation.history.HistoryFilterType
import com.falcon.split.presentation.history.HistoryItem
import com.falcon.split.presentation.history.HistoryState
import com.falcon.split.presentation.theme.CurrencyDisplay
import com.falcon.split.presentation.theme.LocalSplitColors
import com.falcon.split.presentation.theme.SplitCard
import com.falcon.split.presentation.theme.lDimens
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import com.falcon.split.presentation.theme.LocalSplitColors
import com.falcon.split.presentation.theme.SplitColors
import com.falcon.split.presentation.theme.getSplitTypography
import com.falcon.split.util.DateTimeUtil
import getExpenseIconByType
import getGroupIconByType
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import split.composeapp.generated.resources.ApproveSettlement
import split.composeapp.generated.resources.DeclineSettlement
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.Settlement

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    historyViewModel: HistoryViewModel,
    prefs: DataStore<Preferences>,
    newsViewModel: MainViewModel,
    snackBarHostState: SnackbarHostState,
    navControllerMain: NavHostController
) {
    val colors = LocalSplitColors.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    // State
    val historyState by historyViewModel.historyState.collectAsState()
    val historyItems by historyViewModel.historyItems.collectAsState()
    val pagination by historyViewModel.pagination.collectAsState()
    val filterType by historyViewModel.filterType.collectAsState()
    val searchQuery by historyViewModel.searchQuery.collectAsState()
    val isRefreshing by historyViewModel.isRefreshing.collectAsState()

    // UI state
    var showFilterOptions by remember { mutableStateOf(true) }
    val lazyListState = rememberLazyListState()

//    val pullRefreshState = rememberPullRefreshState(
//        refreshing = isRefreshing,
//        onRefresh = { historyViewModel.refreshHistory() }
//    )


    // Group history items by date
    val groupedHistoryItems = remember(historyItems) {
        groupHistoryItemsByDate(historyItems)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
//            .padding(padding)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
//                    .pullRefresh(pullRefreshState)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { historyViewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = lDimens.dp16, vertical = lDimens.dp8)
                    .height(lDimens.dp56)
                    .clip(RoundedCornerShape(lDimens.dp28))
                    .focusRequester(focusRequester),
                placeholder = {
                    Text(
                        text = "Search your activity",
                        style = getSplitTypography().bodyLarge,
                        color = colors.textSecondary
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search icon",
                        tint = colors.textSecondary
                    )
                },
                trailingIcon = {
                    AnimatedVisibility(
                        visible = searchQuery.isNotEmpty(),
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        IconButton(onClick = {
                            historyViewModel.setSearchQuery("")
                            focusManager.clearFocus()
                        }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search query",
                                tint = colors.textSecondary
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(lDimens.dp28),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colors.cardBackground,
                    unfocusedContainerColor = colors.cardBackground,
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.textSecondary.copy(alpha = 0.2f),
                    cursorColor = colors.primary,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary
                ),
                textStyle = TextStyle(color = colors.textPrimary)
            )

            // Filter Options
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = lDimens.dp16, vertical = lDimens.dp4),
                horizontalArrangement = Arrangement.spacedBy(lDimens.dp8)
            ) {
            }

            // Filter Options Row (when expanded)
            if(showFilterOptions == true){
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = lDimens.dp16, vertical = lDimens.dp4),
                    horizontalArrangement = Arrangement.spacedBy(lDimens.dp8)
                ) {
                    // Filter Options
                    Surface(
                        modifier = Modifier.clip(RoundedCornerShape(lDimens.dp16)),
                        onClick = { historyViewModel.setFilterType(HistoryFilterType.ALL) },
                        border = BorderStroke(
                            lDimens.dp2,
                            if (filterType == HistoryFilterType.ALL) colors.primary else colors.cardBackground
                        ),
                        shape = RoundedCornerShape(lDimens.dp16)
                    ) {
                        Text(
                            text = "All",
                            modifier = Modifier.padding(horizontal = lDimens.dp12, vertical = lDimens.dp8),
                            color = colors.textPrimary,
                            style = getSplitTypography().labelLarge
                        )
                    }

                    Surface(
                        modifier = Modifier.clip(RoundedCornerShape(lDimens.dp16)),
                        onClick = { historyViewModel.setFilterType(HistoryFilterType.EXPENSES) },
                        border = BorderStroke(
                            lDimens.dp2,
                            if (filterType == HistoryFilterType.EXPENSES) colors.primary else colors.cardBackground
                        ),
                        shape = RoundedCornerShape(lDimens.dp16)
                    ) {
                        Text(
                            text = "Expenses",
                            modifier = Modifier.padding(horizontal = lDimens.dp12, vertical = lDimens.dp8),
                            color = colors.textPrimary,
                            style = getSplitTypography().labelLarge
                        )
                    }

                    Surface(
                        modifier = Modifier.clip(RoundedCornerShape(lDimens.dp16)),
                        onClick = { historyViewModel.setFilterType(HistoryFilterType.SETTLEMENTS) },
                        border = BorderStroke(
                            lDimens.dp2,
                            if (filterType == HistoryFilterType.SETTLEMENTS) colors.primary else colors.cardBackground
                        ),
                        shape = RoundedCornerShape(lDimens.dp16)
                    ) {
                        Text(
                            text = "Settlements",
                            modifier = Modifier.padding(horizontal = lDimens.dp12, vertical = lDimens.dp8),
                            color = colors.textPrimary,
                            style = getSplitTypography().labelLarge
                        )
                    }
                }
            }

            // History Content
            when (historyState) {
                // Loading state
                is HistoryState.Loading -> {
                    if (historyItems.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = colors.primary)
                        }
                    } else {
                        // Show existing items while loading more
                        HistoryItemList(
                            groupedItems = groupedHistoryItems,
                            onMarkAsRead = { historyViewModel.markAsRead(it) },
                            onLoadMore = { historyViewModel.loadMoreHistory() },
                            isLoading = pagination.isLoading,
                            hasMoreItems = pagination.hasMoreItems
                        )
                    }
                }

                // Error state
                is HistoryState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(lDimens.dp16),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "Something went wrong",
                            style = MaterialTheme.typography.titleLarge,
                            color = colors.textPrimary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(lDimens.dp8))

                        Text(
                            (historyState as HistoryState.Error).message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(lDimens.dp24))

                        Button(
                            onClick = { historyViewModel.loadInitialHistory() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primary,
                                contentColor = Color.White
                            )
                        ) {
                            Text("Retry")
                        }
                    }
                }

                // Success state
                is HistoryState.Success -> {
                    if (historyItems.isEmpty()) {
                        // Empty state
                        EmptyState(searchQuery = searchQuery)
                    } else {
                        // History items list
                        HistoryItemList(
                            groupedItems = groupedHistoryItems,
                            onMarkAsRead = { historyViewModel.markAsRead(it) },
                            onLoadMore = { historyViewModel.loadMoreHistory() },
                            isLoading = pagination.isLoading,
                            hasMoreItems = pagination.hasMoreItems
                        )
                    }
                }
            }
        }
//            PullRefreshIndicator(
//                refreshing = isRefreshing,
//                state = pullRefreshState,
//                modifier = Modifier.align(Alignment.TopCenter)
//            )
    }
}

@Composable
fun HistoryItemList(
    groupedItems: Map<String, List<HistoryItem>>,
    onMarkAsRead: (String) -> Unit,
    onLoadMore: () -> Unit,
    isLoading: Boolean,
    hasMoreItems: Boolean
) {
    val colors = LocalSplitColors.current

    LazyColumn(
        contentPadding = PaddingValues(horizontal = lDimens.dp16, vertical = lDimens.dp8),
        verticalArrangement = Arrangement.spacedBy(lDimens.dp8)
    ) {
        // Display each time period section
        groupedItems.forEach { (timePeriod, items) ->
            // Time period header
            item(key = "header_$timePeriod") {
                Text(
                    text = timePeriod,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                    modifier = Modifier.padding(vertical = lDimens.dp8)
                )
            }

            // History items
            items(
                items = items,
                key = { it.id }
            ) { historyItem ->
                HistoryItemCard(
                    historyItem = historyItem,
                    onMarkAsRead = onMarkAsRead
                )
            }
        }

        // Load more item
        if (hasMoreItems || isLoading) {
            item(key = "load_more") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = lDimens.dp16),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isLoading) {
                        // Show progress indicator when loading
                        CircularProgressIndicator(
                            modifier = Modifier.size(lDimens.dp32),
                            color = colors.primary
                        )
                    } else {
                        // Show load more button when not loading
                        Button(
                            onClick = {
                                println("DEBUG UI: Load More clicked")
                                onLoadMore()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primary,
                                contentColor = Color.White
                            ),
                            modifier = Modifier.padding(horizontal = lDimens.dp32)
                        ) {
                            Text("Load More")
                        }
                    }
                }
            }
        }

        // Bottom spacer
        item {
            Spacer(modifier = Modifier.height(lDimens.dp80))
        }
    }
}

@Composable
fun HistoryItemCard(
    historyItem: HistoryItem,
    onMarkAsRead: (String) -> Unit
) {
    val colors = LocalSplitColors.current

    val icon = getHistoryItemIcon(historyItem)

    // Mark unread items with a colored border
    val cardModifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(lDimens.dp12))
        .background(colors.cardBackground)

    SplitCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = cardModifier.padding(lDimens.dp16),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Action icon
            Box(
                modifier = Modifier
                    .size(lDimens.dp40)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(resource = icon),
                    contentDescription = "Icon",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(lDimens.dp56)
                        .clip(CircleShape),
                )
            }

            Spacer(modifier = Modifier.width(lDimens.dp16))

            // Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = lDimens.dp8)
            ) {
                // Description
                Text(
                    text = historyItem.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Date and time info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(lDimens.dp4)
                ) {
                    // Time and date
                    Text(
                        text = DateTimeUtil.formatTime(historyItem.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )

                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )

                    Text(
                        text = DateTimeUtil.formatStandardDate(historyItem.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )

                    // Group name if available
                    historyItem.groupName?.let {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )

                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Amount for expense or settlement items
            if (historyItem.expenseAmount != null || historyItem.settlementAmount != null) {
                val amount = historyItem.expenseAmount ?: historyItem.settlementAmount ?: 0.0
                val isIncome = when (historyItem.actionType) {
                    HistoryActionType.EXPENSE_ADDED -> historyItem.actionByUserId != "currentUserId" // This needs to be updated with actual logic to determine if it's an income
                    HistoryActionType.SETTLEMENT_APPROVED -> historyItem.targetUserId == "currentUserId" // Same here
                    else -> false
                }

                CurrencyDisplay(
                    amount = amount,
                    isIncome = isIncome
                )
            }
        }
    }
}

@Composable
fun EmptyState(searchQuery: String) {
    val colors = LocalSplitColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(lDimens.dp32),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            modifier = Modifier
                .size(lDimens.dp80)
                .padding(lDimens.dp16),
            tint = colors.textSecondary.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(lDimens.dp16))

        Text(
            text = if (searchQuery.isEmpty()) "No Activity Yet" else "No Results Found",
            style = MaterialTheme.typography.titleLarge,
            color = colors.textPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(lDimens.dp8))

        Text(
            text = if (searchQuery.isEmpty())
                "Your activity history will appear here as you use the app."
            else
                "Try adjusting your search query to find what you're looking for.",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
            textAlign = TextAlign.Center
        )
    }
}

// Helper functions

// Group history items by date period (Today, Yesterday, This Week, etc.)
private fun groupHistoryItemsByDate(items: List<HistoryItem>): Map<String, List<HistoryItem>> {

    val grouped = items.groupBy { item ->
        DateTimeUtil.formatRelativeDate(item.timestamp)
    }


    // Define the order we want
    val order = listOf("Today", "Yesterday", "This Week", "This Month", "Earlier")

    // Create a new linked map with the desired order
    val result = linkedMapOf<String, List<HistoryItem>>()

    // Add entries in the specific order (if they exist)
    order.forEach { key ->
        grouped[key]?.let { result[key] = it }
    }

    grouped.forEach { (key, value) ->
        if (!result.containsKey(key)) {
            result[key] = value
        }
    }

    return result
}

// Format timestamp to human-readable time
private fun formatTime(timestamp: Long): String {
    return DateTimeUtil.formatTime(timestamp)
}


private fun getHistoryItemIcon(historyItem: HistoryItem): DrawableResource {
    return when (historyItem.actionType) {
        HistoryActionType.GROUP_CREATED,
        HistoryActionType.GROUP_DELETED -> {
            // Use the group icon based on group type
            getGroupIconByType(historyItem.groupType)
        }

        HistoryActionType.EXPENSE_ADDED -> {
            // Use the expense icon based on expense type
            getExpenseIconByType(historyItem.expenseType)
        }

        HistoryActionType.SETTLEMENT_REQUESTED -> {
            Res.drawable.Settlement
        }

        HistoryActionType.SETTLEMENT_APPROVED -> {
            Res.drawable.ApproveSettlement
        }

        HistoryActionType.SETTLEMENT_DECLINED -> {
            Res.drawable.DeclineSettlement
        }

        HistoryActionType.MEMBER_ADDED -> {
            // Default to a generic member icon
            Res.drawable.Settlement // Or whatever icon you want to use for member added
        }
    }
}

// Get icon color for history item based on action type
private fun getHistoryItemIconColor(actionType: HistoryActionType, colors: SplitColors): Color {
    return when (actionType) {
        HistoryActionType.GROUP_CREATED,
        HistoryActionType.MEMBER_ADDED -> colors.primary

        HistoryActionType.GROUP_DELETED -> colors.error

        HistoryActionType.EXPENSE_ADDED -> colors.info

        HistoryActionType.SETTLEMENT_REQUESTED -> colors.warning

        HistoryActionType.SETTLEMENT_APPROVED -> colors.success

        HistoryActionType.SETTLEMENT_DECLINED -> colors.error
    }
}


@Composable
fun getSettlementIcon(status: SettlementStatus): Painter {
    return when (status) {
        SettlementStatus.PENDING -> painterResource(Res.drawable.Settlement)
        SettlementStatus.APPROVED -> painterResource(Res.drawable.ApproveSettlement)
        SettlementStatus.DECLINED -> painterResource(Res.drawable.DeclineSettlement)
    }
}

//For time setup, converting milliseconds in timestamp and back to milliseconds while reading data.