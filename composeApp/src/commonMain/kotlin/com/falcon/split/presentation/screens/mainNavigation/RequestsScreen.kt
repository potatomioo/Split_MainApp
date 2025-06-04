package com.falcon.split.presentation.screens.mainNavigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import com.falcon.split.presentation.group.GroupViewModel
import com.falcon.split.presentation.theme.LocalSplitColors
import com.falcon.split.presentation.theme.lDimens
import kotlinx.coroutines.launch

//@Composable
//fun RequestsScreen(
//    navController: NavController,
//    viewModel: GroupViewModel
//){
//    val currentUserId = viewModel.currentUserId
//    val pendingSettlements by viewModel.pendingSettlements.collectAsState()
//    val processingSettlementIds by viewModel.processingSettlementId.collectAsState()
//    val colors = LocalSplitColors.current
//
//    LazyColumn {
//        item {
//            SectionHeader(
//                title = "Pending Settlements",
//                actionText = null,
//                onActionClick = null
//            )
//
//            val incomingRequests = pendingSettlements.filter { it.toUserId == currentUserId }
//            val outgoingRequests = pendingSettlements.filter { it.fromUserId == currentUserId }
//
//            if (incomingRequests.isEmpty() && outgoingRequests.isEmpty()) {
//                EmptyStateMessage(
//                    message = "No pending settlements",
//                    submessage = "You don't have any payment requests to approve or pending"
//                )
//            } else {
//                Column(
//                    modifier = Modifier.padding(horizontal = lDimens.dp16),
//                    verticalArrangement = Arrangement.spacedBy(lDimens.dp8)
//                ) {
//                    // Show incoming requests that need approval
//                    if (incomingRequests.isNotEmpty()) {
//                        Text(
//                            "Requests For You",
//                            style = MaterialTheme.typography.titleMedium,
//                            color = colors.textPrimary,
//                            modifier = Modifier.padding(top = lDimens.dp8, bottom = lDimens.dp4)
//                        )
//
//                        incomingRequests.forEach { settlement ->
//                            PendingSettlementItem(
//                                settlement = settlement,
//                                isIncoming = true,
//                                onApprove = { viewModel.approveSettlement(settlement.id) },
//                                onDecline = { viewModel.declineSettlement(settlement.id) },
//                                processingSettlements = processingSettlementIds
//                            )
//                        }
//                    }
//
//                    // Show outgoing requests that are pending approval
//                    if (outgoingRequests.isNotEmpty()) {
//                        Text(
//                            "Your Pending Requests",
//                            style = MaterialTheme.typography.titleMedium,
//                            color = colors.textPrimary,
//                            modifier = Modifier.padding(top = lDimens.dp8, bottom = lDimens.dp4)
//                        )
//
//                        outgoingRequests.forEach { settlement ->
//                            PendingSettlementItem(
//                                settlement = settlement,
//                                isIncoming = false,
//                                onApprove = null,
//                                onDecline = null,
//                                processingSettlements = processingSettlementIds
//                            )
//                        }
//                    }
//                }
//            }
//        }
//    }
//}





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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = lDimens.dp16, end = lDimens.dp16, top = lDimens.dp16),
                verticalArrangement = Arrangement.spacedBy(lDimens.dp8)
            ) {
//                item {
//                    // This section header will appear at the top of each pager page
//                    SectionHeader(
//                        title = if (page == 0) "Incoming Requests" else "Outgoing Requests",
//                        actionText = null,
//                        onActionClick = null
//                    )
//                }

                if (page == 0) { // "Requests to You" pager
                    if (incomingRequests.isEmpty()) {
                        item {
                            EmptyStateMessage(
                                message = "No incoming payment requests",
                                submessage = "You don't have any payment requests to approve."
                            )
                        }
                    } else {
                        items(incomingRequests.size) { index ->
                            val settlement = incomingRequests[index]
                            PendingSettlementItem(
                                settlement = settlement,
                                isIncoming = true,
                                onApprove = { viewModel.approveSettlement(settlement.id) },
                                onDecline = { viewModel.declineSettlement(settlement.id) },
                                processingSettlements = processingSettlementIds
                            )
                        }
                    }
                } else { // "Requests from You" pager
                    if (outgoingRequests.isEmpty()) {
                        item {
                            EmptyStateMessage(
                                message = "No pending requests from you",
                                submessage = "You haven't sent any payment requests that are pending approval."
                            )
                        }
                    } else {
                        items(outgoingRequests.size) { index ->
                            val settlement = outgoingRequests[index]
                            PendingSettlementItem(
                                settlement = settlement,
                                isIncoming = false,
                                onApprove = null, // No action for outgoing requests here
                                onDecline = null, // No action for outgoing requests here
                                processingSettlements = processingSettlementIds
                            )
                        }
                    }
                }
            }
        }
    }
}