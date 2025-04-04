package com.falcon.split.presentation.screens.mainNavigation


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.navigation.NavHostController
import com.falcon.split.MainViewModel
import com.falcon.split.data.network.models_app.Expense
import com.falcon.split.data.network.models_app.Group
import com.falcon.split.data.network.models_app.GroupMember
import com.falcon.split.data.network.models_app.Settlement
import com.falcon.split.data.network.models_app.SettlementStatus
import com.falcon.split.presentation.expense.ExpenseState
import com.falcon.split.presentation.group.GroupState
import com.falcon.split.presentation.group.GroupViewModel
import com.falcon.split.presentation.screens.mainNavigation.AnimationComponents.UpwardFlipHeaderImage
import com.falcon.split.presentation.theme.CurrencyDisplay
import com.falcon.split.presentation.theme.LocalSplitColors
import com.falcon.split.presentation.theme.SplitCard
import com.falcon.split.presentation.theme.lDimens
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import split.composeapp.generated.resources.HomePic
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.group_icon_outlined

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalResourceApi::class)
@Composable
fun HomeScreen(
    onNavigate: (rootName: String) -> Unit,
    prefs: DataStore<Preferences>,
    snackBarHostState: SnackbarHostState,
    navControllerBottomNav: NavHostController,
    mainViewModel: MainViewModel,
    navControllerMain: NavHostController,
    topPadding : Dp,
    viewModel: GroupViewModel
) {
    val colors = LocalSplitColors.current
    val scope = rememberCoroutineScope()

    // Get state from GroupViewModel
    val groupState by viewModel.groupState.collectAsState()
    val expenseState by viewModel.expenseState.collectAsState()
    val pendingSettlements by viewModel.pendingSettlements.collectAsState()

    // User data
    val currentUserId = viewModel.currentUserId
    val userName = "You" // Replace with actual user name if available

    // Load data when screen is mounted
    LaunchedEffect(Unit) {
        viewModel.loadGroups()
        viewModel.loadPendingSettlements()

        // Add this line to load the user's expenses
        if (currentUserId != null) {
            viewModel.loadUserExpenses(currentUserId)
        }
    }

    // Calculate total balances
    val totalToReceive = remember(groupState) {
        if (groupState is GroupState.Success) {
            val groups = (groupState as GroupState.Success).groups
            groups.sumOf { group ->
                val userMember = group.members.find { it.userId == currentUserId }
                if (userMember?.balance != null && userMember.balance > 0) userMember.balance else 0.0
            }
        } else {
            0.0
        }
    }

    val totalToPay = remember(groupState) {
        if (groupState is GroupState.Success) {
            val groups = (groupState as GroupState.Success).groups
            groups.sumOf { group ->
                val userMember = group.members.find { it.userId == currentUserId }
                if (userMember?.balance != null && userMember.balance < 0) -userMember.balance else 0.0
            }
        } else {
            0.0
        }
    }

    // Get time-based greeting
    val greeting = getGreeting(userName)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(greeting) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.backgroundPrimary,
                    titleContentColor = colors.textPrimary
                ),
                actions = {
                    IconButton(onClick = { /* Open search */ }) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = colors.textPrimary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navControllerMain.navigate("create_expense") },
                containerColor = colors.primary,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = lDimens.dp6,
                    pressedElevation = lDimens.dp12
                )
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(lDimens.dp8),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                    Text("Add expense", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = lDimens.dp0,
                top = padding.calculateTopPadding(),
                end = lDimens.dp0,
                bottom = padding.calculateBottomPadding() + lDimens.dp80
            ),
            verticalArrangement = Arrangement.spacedBy(lDimens.dp16),
            modifier = Modifier
                .fillMaxSize()
                .background(colors.backgroundPrimary)
        ) {
            // Balance header with image background
            item {
                Box {
                    UpwardFlipHeaderImage(
                        Res.drawable.HomePic,
                        rememberPagerState { 1 }
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = lDimens.dp16, vertical = lDimens.dp16)
                    ) {
                        // Balance overview card
                        SplitCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = lDimens.dp100) // Positioned below the header image
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(lDimens.dp16)
                                    .fillMaxWidth()
                            ) {
                                Text(
                                    "Your balance",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = colors.textSecondary
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = lDimens.dp16),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // You'll get
                                    Column(
                                        horizontalAlignment = Alignment.Start
                                    ) {
                                        Text(
                                            "You'll get",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = colors.textSecondary
                                        )
                                        Spacer(modifier = Modifier.height(lDimens.dp4))
                                        CurrencyDisplay(
                                            amount = totalToReceive,
                                            isIncome = true,
                                            large = true
                                        )
                                    }

                                    // You'll pay
                                    Column(
                                        horizontalAlignment = Alignment.End
                                    ) {
                                        Text(
                                            "You'll pay",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = colors.textSecondary
                                        )
                                        Spacer(modifier = Modifier.height(lDimens.dp4))
                                        CurrencyDisplay(
                                            amount = totalToPay,
                                            isIncome = false,
                                            large = true
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Quick actions row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = lDimens.dp16),
                    horizontalArrangement = Arrangement.spacedBy(lDimens.dp8)
                ) {
                    // Create group button
                    QuickActionButton(
                        icon = Icons.Default.Person,
                        text = "New Group",
                        onClick = { navControllerMain.navigate("create_group") },
                        modifier = Modifier.weight(1f)
                    )

                    // Settle up button
                    QuickActionButton(
                        icon = Icons.Default.Person,
                        text = "Settle Up",
                        onClick = { /* Navigate to settle up screen */ },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Recent Activity section
            // Recent Activity section
            item {
                SectionHeader(
                    title = "Recent Activity",
                    actionText = "View All",
                    onActionClick = { /* Navigate to full history */ }
                )

                when (expenseState) {
                    is ExpenseState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(lDimens.dp100),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = colors.primary)
                        }
                    }
                    is ExpenseState.Success -> {
                        val expenses = (expenseState as ExpenseState.Success).expenses
                        if (expenses.isEmpty()) {
                            EmptyStateMessage(
                                message = "No recent activity",
                                submessage = "Your recent expenses and settlements will appear here"
                            )
                        } else {
                            // Show most recent 3 expenses
                            Column(
                                modifier = Modifier.padding(horizontal = lDimens.dp16),
                                verticalArrangement = Arrangement.spacedBy(lDimens.dp8)
                            ) {
                                // Sort expenses by ID (assuming ID contains timestamp) and take recent ones
                                expenses.sortedByDescending { it.expenseId.toLongOrNull() ?: 0L }
                                    .take(3)
                                    .forEach { expense ->
                                        currentUserId?.let {
                                            RecentActivityItem(expense = expense,
                                                it
                                            )
                                        }
                                    }
                            }
                        }
                    }
                    is ExpenseState.Error -> {
                        EmptyStateMessage(
                            message = "Couldn't load activity",
                            submessage = "There was an error loading your recent activity"
                        )
                    }
                }
            }

            // Your Groups section
            item {
                SectionHeader(
                    title = "Your Groups",
                    actionText = "See All",
                    onActionClick = { /* Navigate to groups screen */ }
                )

                when (groupState) {
                    is GroupState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(lDimens.dp100),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = colors.primary)
                        }
                    }
                    is GroupState.Success -> {
                        val groups = (groupState as GroupState.Success).groups
                        if (groups.isEmpty()) {
                            EmptyStateMessage(
                                message = "No groups yet",
                                submessage = "Create a group to start tracking expenses with friends"
                            )
                        } else {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = lDimens.dp16),
                                horizontalArrangement = Arrangement.spacedBy(lDimens.dp12)
                            ) {
                                items(groups) { group ->
                                    GroupCard(
                                        group = group,
                                        onClick = {
                                            navControllerMain.navigate("group_details/${group.id}")
                                        }
                                    )
                                }
                            }
                        }
                    }
                    is GroupState.Error -> {
                        EmptyStateMessage(
                            message = "Couldn't load groups",
                            submessage = "There was an error loading your groups"
                        )
                    }
                    else -> {}
                }
            }

            // Pending Settlements section
            // Pending Settlements section
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
                                    onDecline = { viewModel.declineSettlement(settlement.id) }
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
                                    onDecline = null
                                )
                            }
                        }
                    }
                }
            }

            // Bottom spacer for FAB
            item {
                Spacer(modifier = Modifier.height(lDimens.dp80))
            }
        }
    }
}

@Composable
fun RecentActivityItem(
    expense: Expense,
    currentUserId: String
) {
    val colors = LocalSplitColors.current
    val currentUserId =

    SplitCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(lDimens.dp12),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Show the group ID as a fallback if no better info is available
                Text(
                    text = expense.groupId,
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textSecondary
                )

                Text(
                    text = expense.description,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = if (expense.paidByUserId == currentUserId) "You paid" else "${expense.paidByUserName ?: "Someone"} paid",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                CurrencyDisplay(
                    amount = expense.amount,
                    isIncome = expense.paidByUserId != currentUserId
                )

                Text(
                    text = formatExpenseDateTime(expense.expenseId.toLongOrNull() ?: 0L),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }
        }
    }
}

@Composable
fun PendingSettlementItem(
    settlement: Settlement,
    isIncoming: Boolean,
    onApprove: (() -> Unit)?,
    onDecline: (() -> Unit)?
) {
    val colors = LocalSplitColors.current

    SplitCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(lDimens.dp16)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isIncoming) "Payment Request" else "Your Request",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textPrimary
                    )

                    Text(
                        text = if (isIncoming)
                            "${settlement.fromUserName ?: "Someone"} requested payment"
                        else
                            "Requested from ${settlement.toUserName ?: "someone"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary
                    )
                }

                CurrencyDisplay(
                    amount = settlement.amount,
                    isIncome = false
                )
            }

            // Only show action buttons for incoming requests
            if (isIncoming && onApprove != null && onDecline != null) {
                Spacer(modifier = Modifier.height(lDimens.dp12))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDecline,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = colors.error
                        ),
                        border = BorderStroke(lDimens.dp1, colors.error),
                        modifier = Modifier.padding(end = lDimens.dp8)
                    ) {
                        Text("Decline")
                    }

                    Button(
                        onClick = onApprove,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.success,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Approve")
                    }
                }
            }
        }
    }
}


@Composable
fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalSplitColors.current

    ElevatedButton(
        onClick = onClick,
        modifier = modifier.height(lDimens.dp48),
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = colors.cardBackground,
            contentColor = colors.textPrimary
        ),
        contentPadding = PaddingValues(horizontal = lDimens.dp8)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(lDimens.dp18)
        )
        Spacer(modifier = Modifier.width(lDimens.dp4))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    actionText: String?,
    onActionClick: (() -> Unit)?
) {
    val colors = LocalSplitColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = lDimens.dp16, vertical = lDimens.dp8),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = colors.textPrimary
        )

        if (actionText != null && onActionClick != null) {
            Text(
                text = actionText,
                style = MaterialTheme.typography.labelMedium,
                color = colors.primary,
                modifier = Modifier.clickable { onActionClick() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalResourceApi::class)
@Composable
fun GroupCard(
    group: Group,
    onClick: () -> Unit
) {
    val colors = LocalSplitColors.current

    Card(
        onClick = onClick,
        modifier = Modifier
            .width(lDimens.dp150),
        colors = CardDefaults.cardColors(
            containerColor = colors.cardBackground
        ),
        shape = RoundedCornerShape(lDimens.dp12)
    ) {
        Column(
            modifier = Modifier.padding(lDimens.dp12),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Group icon
            Icon(
                painter = painterResource(Res.drawable.group_icon_outlined),
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier
                    .size(lDimens.dp40)
                    .padding(lDimens.dp4)
            )

            Spacer(modifier = Modifier.height(lDimens.dp8))

            // Group name
            Text(
                text = group.name,
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            // Member count
            Text(
                text = "${group.members.size} members",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )

            Spacer(modifier = Modifier.height(lDimens.dp8))

            // Balance
            CurrencyDisplay(
                amount = group.totalAmount ?: 0.0,
                isIncome = (group.totalAmount ?: 0.0) >= 0
            )
        }
    }
}

@Composable
fun EmptyStateMessage(
    message: String,
    submessage: String
) {
    val colors = LocalSplitColors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(lDimens.dp32),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(lDimens.dp4))

        Text(
            text = submessage,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
            textAlign = TextAlign.Center
        )
    }
}

// Helper function to get appropriate greeting based on time of day
@Composable
fun getGreeting(name: String): String {
    val currentDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val hour = currentDateTime.hour

    return when {
        hour < 12 -> "Good morning, $name"
        hour < 17 -> "Good afternoon, $name"
        else -> "Good evening, $name"
    }
}


private fun formatExpenseDateTime(timestamp: Long): String {
    if (timestamp == 0L) return ""

    val instant = Instant.fromEpochMilliseconds(timestamp)
    val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

    return "${dateTime.month.name.take(3)} ${dateTime.dayOfMonth}, ${dateTime.hour}:${dateTime.minute.toString().padStart(2, '0')}"
}