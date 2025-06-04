package com.falcon.split.presentation.screens.mainNavigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.falcon.split.presentation.group.GroupViewModel
import com.falcon.split.presentation.theme.LocalSplitColors
import com.falcon.split.presentation.theme.lDimens

@Composable
fun RequestsScreen(
    navController: NavController,
    viewModel: GroupViewModel
){
    val currentUserId = viewModel.currentUserId
    val pendingSettlements by viewModel.pendingSettlements.collectAsState()
    val processingSettlementIds by viewModel.processingSettlementId.collectAsState()
    val colors = LocalSplitColors.current

    LazyColumn {
        item {
            SectionHeader(
                title = "Pending Settlements",
                actionText = null,
                onActionClick = null
            )

            val incomingRequests = pendingSettlements.filter { it.toUserId == currentUserId }
            val outgoingRequests = pendingSettlements.filter { it.fromUserId == currentUserId }

            if (incomingRequests.isEmpty() && outgoingRequests.isEmpty()) {
                EmptyStateMessage(
                    message = "No pending settlements",
                    submessage = "You don't have any payment requests to approve or pending"
                )
            } else {
                Column(
                    modifier = Modifier.padding(horizontal = lDimens.dp16),
                    verticalArrangement = Arrangement.spacedBy(lDimens.dp8)
                ) {
                    // Show incoming requests that need approval
                    if (incomingRequests.isNotEmpty()) {
                        Text(
                            "Requests For You",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.textPrimary,
                            modifier = Modifier.padding(top = lDimens.dp8, bottom = lDimens.dp4)
                        )

                        incomingRequests.forEach { settlement ->
                            PendingSettlementItem(
                                settlement = settlement,
                                isIncoming = true,
                                onApprove = { viewModel.approveSettlement(settlement.id) },
                                onDecline = { viewModel.declineSettlement(settlement.id) },
                                processingSettlements = processingSettlementIds
                            )
                        }
                    }

                    // Show outgoing requests that are pending approval
                    if (outgoingRequests.isNotEmpty()) {
                        Text(
                            "Your Pending Requests",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.textPrimary,
                            modifier = Modifier.padding(top = lDimens.dp8, bottom = lDimens.dp4)
                        )

                        outgoingRequests.forEach { settlement ->
                            PendingSettlementItem(
                                settlement = settlement,
                                isIncoming = false,
                                onApprove = null,
                                onDecline = null,
                                processingSettlements = processingSettlementIds
                            )
                        }
                    }
                }
            }
        }
    }
}