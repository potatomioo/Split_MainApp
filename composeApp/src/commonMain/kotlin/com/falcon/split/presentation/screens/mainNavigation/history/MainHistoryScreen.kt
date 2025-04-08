package com.falcon.split.presentation.screens.mainNavigation.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.falcon.split.MainViewModel
import com.falcon.split.data.network.ApiClient
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onNavigate: (rootName: String) -> Unit,
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
    val unreadCount by historyViewModel.unreadCount.collectAsState()

    // UI state
    var showFilterOptions by remember { mutableStateOf(false) }
    val lazyListState = rememberLazyListState()

//    val pullRefreshState = rememberPullRefreshState(
//        refreshing = isRefreshing,
//        onRefresh = { historyViewModel.refreshHistory() }
//    )


    // Group history items by date
    val groupedHistoryItems = remember(historyItems) {
        groupHistoryItemsByDate(historyItems)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Activity",
                        color = colors.textPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.backgroundSecondary,
                    titleContentColor = colors.textPrimary
                ),
                actions = {
                    // Mark all as read button
                    if (unreadCount > 0) {
                        Button(
                            onClick = { historyViewModel.markAllAsRead() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primary.copy(alpha = 0.1f),
                                contentColor = colors.primary
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text("Mark All Read")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
                        .clip(RoundedCornerShape(28.dp))
                        .focusRequester(focusRequester),
                    placeholder = {
                        Text(
                            text = "Search your activity",
                            style = MaterialTheme.typography.bodyMedium,
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
                    shape = RoundedCornerShape(28.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colors.cardBackground,
                        unfocusedContainerColor = colors.cardBackground,
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.textSecondary.copy(alpha = 0.2f),
                        cursorColor = colors.primary,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    )
                )

                // Filter Options
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = lDimens.dp16, vertical = lDimens.dp4),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Filter Button
                    Surface(
                        modifier = Modifier.height(lDimens.dp36),
                        shape = RoundedCornerShape(lDimens.dp18),
                        color = if (showFilterOptions) colors.primary else colors.cardBackground,
                        onClick = { showFilterOptions = !showFilterOptions }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = if (showFilterOptions) Color.White else colors.textPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Filter",
                                color = if (showFilterOptions) Color.White else colors.textPrimary,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Active Filter Chip
                    if (filterType != HistoryFilterType.ALL) {
                        Surface(
                            modifier = Modifier.height(36.dp),
                            shape = RoundedCornerShape(18.dp),
                            color = colors.primary.copy(alpha = 0.1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(start = 12.dp, end = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = when(filterType) {
                                        HistoryFilterType.EXPENSES -> "Expenses Only"
                                        HistoryFilterType.SETTLEMENTS -> "Settlements Only"
                                        else -> ""
                                    },
                                    color = colors.primary,
                                    style = MaterialTheme.typography.labelMedium
                                )

                                IconButton(
                                    onClick = { historyViewModel.setFilterType(HistoryFilterType.ALL) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = "Clear filter",
                                        tint = colors.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Filter Options Row (when expanded)
                AnimatedVisibility(visible = showFilterOptions) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = lDimens.dp16, vertical = lDimens.dp4),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Filter Options
                        Surface(
                            modifier = Modifier.clip(RoundedCornerShape(16.dp)),
                            color = if (filterType == HistoryFilterType.ALL) colors.primary else colors.cardBackground,
                            onClick = { historyViewModel.setFilterType(HistoryFilterType.ALL) }
                        ) {
                            Text(
                                text = "All",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                color = if (filterType == HistoryFilterType.ALL) Color.White else colors.textPrimary,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }

                        Surface(
                            modifier = Modifier.clip(RoundedCornerShape(16.dp)),
                            color = if (filterType == HistoryFilterType.EXPENSES) colors.primary else colors.cardBackground,
                            onClick = { historyViewModel.setFilterType(HistoryFilterType.EXPENSES) }
                        ) {
                            Text(
                                text = "Expenses",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                color = if (filterType == HistoryFilterType.EXPENSES) Color.White else colors.textPrimary,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }

                        Surface(
                            modifier = Modifier.clip(RoundedCornerShape(16.dp)),
                            color = if (filterType == HistoryFilterType.SETTLEMENTS) colors.primary else colors.cardBackground,
                            onClick = { historyViewModel.setFilterType(HistoryFilterType.SETTLEMENTS) }
                        ) {
                            Text(
                                text = "Settlements",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                color = if (filterType == HistoryFilterType.SETTLEMENTS) Color.White else colors.textPrimary,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = lDimens.dp16),
                        color = colors.divider
                    )
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

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                (historyState as HistoryState.Error).message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.textSecondary,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(24.dp))

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
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Display each time period section
        groupedItems.forEach { (timePeriod, items) ->
            // Time period header
            item(key = "header_$timePeriod") {
                Text(
                    text = timePeriod,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                    modifier = Modifier.padding(vertical = 8.dp)
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
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isLoading) {
                        // Show progress indicator when loading
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
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
                            modifier = Modifier.padding(horizontal = 32.dp)
                        ) {
                            Text("Load More")
                        }
                    }
                }
            }
        }

        // Bottom spacer
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun HistoryItemCard(
    historyItem: HistoryItem,
    onMarkAsRead: (String) -> Unit
) {
    val colors = LocalSplitColors.current

    // Mark unread items with a colored border
    val cardModifier = if (!historyItem.read) {
        Modifier
            .fillMaxWidth()
            .shadow(elevation = 1.dp, shape = RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(colors.cardBackground)
            .clickable { onMarkAsRead(historyItem.id) }
    } else {
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.cardBackground)
    }

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
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(getHistoryItemBackgroundColor(historyItem.actionType, colors)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getHistoryItemIcon(historyItem.actionType),
                    contentDescription = null,
                    tint = getHistoryItemIconColor(historyItem.actionType, colors),
                    modifier = Modifier.size(24.dp)
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

                // Time and group info (if applicable)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Time
                    Text(
                        text = formatTime(historyItem.timestamp),
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

            // Unread indicator
            if (!historyItem.read) {
                Box(
                    modifier = Modifier
                        .padding(start = lDimens.dp8)
                        .size(8.dp)
                        .background(colors.primary, CircleShape)
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
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .padding(16.dp),
            tint = colors.textSecondary.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (searchQuery.isEmpty()) "No Activity Yet" else "No Results Found",
            style = MaterialTheme.typography.titleLarge,
            color = colors.textPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

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
    val now = kotlinx.datetime.Clock.System.now()
    val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
    val yesterday = today.minus(kotlinx.datetime.DatePeriod(days = 1))

    // Group by time period
    val grouped = items.groupBy { item ->
        val instant = Instant.fromEpochMilliseconds(item.timestamp)
        val itemDate = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date

        when {
            itemDate == today -> "Today"
            itemDate == yesterday -> "Yesterday"
            today.minus(kotlinx.datetime.DatePeriod(days = 7)) < itemDate -> "This Week"
            today.minus(kotlinx.datetime.DatePeriod(days = 30)) < itemDate -> "This Month"
            else -> "Earlier"
        }
    }

    // Define the order we want
    val order = listOf("Today", "Yesterday", "This Week", "This Month", "Earlier")

    // Create a new linked map with the desired order
    val result = linkedMapOf<String, List<HistoryItem>>()

    // Add entries in the specific order (if they exist)
    order.forEach { key ->
        grouped[key]?.let { result[key] = it }
    }

    return result
}

// Format timestamp to human-readable time
private fun formatTime(timestamp: Long): String {
    val instant = Instant.fromEpochMilliseconds(timestamp)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

    return "${localDateTime.hour}:${localDateTime.minute.toString().padStart(2, '0')}"
}

// Get icon for history item based on action type
@Composable
private fun getHistoryItemIcon(actionType: HistoryActionType): androidx.compose.ui.graphics.vector.ImageVector {
    return when (actionType) {
        HistoryActionType.GROUP_CREATED -> Icons.Default.Search // Replace with appropriate icons
        HistoryActionType.GROUP_DELETED -> Icons.Default.Search
        HistoryActionType.EXPENSE_ADDED -> Icons.Default.Search
        HistoryActionType.SETTLEMENT_REQUESTED -> Icons.Default.Search
        HistoryActionType.SETTLEMENT_APPROVED -> Icons.Default.Search
        HistoryActionType.SETTLEMENT_DECLINED -> Icons.Default.Search
        HistoryActionType.MEMBER_ADDED -> Icons.Default.Search
    }
}

// Get background color for history item based on action type
private fun getHistoryItemBackgroundColor(actionType: HistoryActionType, colors: SplitColors): Color {
    return when (actionType) {
        HistoryActionType.GROUP_CREATED,
        HistoryActionType.MEMBER_ADDED -> colors.primary.copy(alpha = 0.1f)

        HistoryActionType.GROUP_DELETED -> colors.error.copy(alpha = 0.1f)

        HistoryActionType.EXPENSE_ADDED -> colors.info.copy(alpha = 0.1f)

        HistoryActionType.SETTLEMENT_REQUESTED -> colors.warning.copy(alpha = 0.1f)

        HistoryActionType.SETTLEMENT_APPROVED -> colors.success.copy(alpha = 0.1f)

        HistoryActionType.SETTLEMENT_DECLINED -> colors.error.copy(alpha = 0.1f)
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


//For time setup, converting milliseconds in timestamp and back to milliseconds while reading data.