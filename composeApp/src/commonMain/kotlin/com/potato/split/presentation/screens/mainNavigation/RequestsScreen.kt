package com.potato.split.presentation.screens.mainNavigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import com.potato.split.data.network.models_app.Settlement
import com.potato.split.presentation.group.GroupViewModel
import com.potato.split.presentation.history.HistoryItem
import com.potato.split.presentation.theme.LocalSplitColors
import com.potato.split.presentation.theme.lDimens
import com.potato.split.util.DateTimeUtil
import kotlinx.coroutines.launch



@Composable
fun RequestsScreen(
    navController: NavController,
    viewModel: GroupViewModel
) {
    val currentUserId = viewModel.currentUserId
    val pendingSettlements by viewModel.pendingSettlements.collectAsState()
    val processingSettlementIds by viewModel.processingSettlementId.collectAsState()
    val colors = LocalSplitColors.current

    // Filter requests once
    val incomingRequests = pendingSettlements.filter { it.toUserId == currentUserId }
    val outgoingRequests = pendingSettlements.filter { it.fromUserId == currentUserId }

    val pagerState = rememberPagerState(initialPage = 0) {
        2
    }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit){
        viewModel.loadPendingSettlements()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = colors.backgroundSecondary,
            contentColor = colors.textPrimary
        ) {
            Tab(
                selected = pagerState.currentPage == 0,
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(0)
                    }
                },
                text = { Text("Requests to You",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center
                )
                }
            )
            Tab(
                selected = pagerState.currentPage == 1,
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(1)
                    }
                },
                text = { Text("Requests from You",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center
                ) }
            )
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val requests = if (page == 0) incomingRequests else outgoingRequests

            if (requests.isEmpty()) {
                // Show empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(lDimens.dp16),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyStateMessage(
                        message = if (page == 0) "No incoming payment requests" else "No pending requests from you",
                        submessage = if (page == 0) "You don't have any payment requests to approve." else "You haven't sent any payment requests that are pending approval."
                    )
                }
            } else {
                // Group requests by time period (same as HistoryScreen)
                val groupedRequests = groupRequestsByDate(requests)

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = lDimens.dp16),
                    verticalArrangement = Arrangement.spacedBy(lDimens.dp8)
                ) {
                    groupedRequests.forEach { (dateHeader, requestsForDate) ->
                        // Date header (same style as HistoryScreen)
                        item {
                            Text(
                                text = dateHeader,
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.textSecondary,
                                modifier = Modifier.padding(vertical = lDimens.dp8)
                            )
                        }

                        // Requests for this date (using existing PendingSettlementItem)
                        items(requestsForDate) { settlement ->
                            PendingSettlementItem(
                                settlement = settlement,
                                isIncoming = page == 0,
                                onApprove = if (page == 0) {
                                    { viewModel.approveSettlement(settlement.id) }
                                } else null,
                                onDecline = if (page == 0) {
                                    { viewModel.declineSettlement(settlement.id) }
                                } else null,
                                processingSettlements = processingSettlementIds
                            )
                        }
                    }

                    // Bottom spacer
                    item {
                        Spacer(modifier = Modifier.height(lDimens.dp80))
                    }
                }
            }
        }
    }
}

private fun groupRequestsByDate(requests: List<Settlement>): Map<String, List<Settlement>> {
    val grouped = requests.groupBy { settlement ->
        DateTimeUtil.formatRelativeDate(settlement.timestamp)
    }

    // Define the order we want (same as HistoryScreen)
    val order = listOf("Today", "Yesterday", "This Week", "This Month", "Earlier")

    // Create a new linked map with the desired order
    val result = linkedMapOf<String, List<Settlement>>()

    // Add entries in the specific order (if they exist)
    order.forEach { key ->
        grouped[key]?.let { result[key] = it }
    }

    // Add any remaining entries that don't fit the standard categories
    grouped.forEach { (key, value) ->
        if (!result.containsKey(key)) {
            result[key] = value
        }
    }

    return result
}