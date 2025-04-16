package com.falcon.split.presentation.screens.mainNavigation


import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.navigation.NavHostController
import com.falcon.split.MainViewModel
import com.falcon.split.data.network.models_app.Expense
import com.falcon.split.data.network.models_app.Group
import com.falcon.split.data.network.models_app.GroupType
import com.falcon.split.data.network.models_app.Settlement
import com.falcon.split.data.network.models_app.SettlementStatus
import com.falcon.split.presentation.expense.ExpenseState
import com.falcon.split.presentation.group.GroupState
import com.falcon.split.presentation.group.GroupViewModel
import com.falcon.split.presentation.history.HistoryState
import com.falcon.split.presentation.screens.mainNavigation.AnimationComponents.UpwardFlipHeaderImage
import com.falcon.split.presentation.screens.mainNavigation.history.HistoryItemCard
import com.falcon.split.presentation.screens.mainNavigation.history.HistoryViewModel
import com.falcon.split.presentation.theme.CurrencyDisplay
import com.falcon.split.presentation.theme.LocalSplitColors
import com.falcon.split.presentation.theme.SplitCard
import com.falcon.split.presentation.theme.SplitColors
import com.falcon.split.presentation.theme.lDimens
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import split.composeapp.generated.resources.ExclusiveFeature
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
    viewModel: GroupViewModel,
    historyViewModel: HistoryViewModel,
    pagerState: PagerState
    ) {
    val colors = LocalSplitColors.current
    val scope = rememberCoroutineScope()

    // Get state from GroupViewModel
    val groupState by viewModel.groupState.collectAsState()
    val expenseState by viewModel.expenseState.collectAsState()
    val pendingSettlements by viewModel.pendingSettlements.collectAsState()

    // User data
    val currentUserId = viewModel.currentUserId

    //ProcessingSettlement
    val processingSettlementIds by viewModel.processingSettlementId.collectAsState()

    // Load data when screen is mounted
    LaunchedEffect(Unit) {
        viewModel.loadGroups()  // This loads all groups the user is part of
        viewModel.loadPendingSettlements()
        historyViewModel.loadRecentHistory(4)

        // Get expenses for all groups this user is part of
        if (groupState is GroupState.Success) {
            val groups = (groupState as GroupState.Success).groups
            groups.forEach { group ->
                viewModel.loadGroupExpenses(group.id)
            }
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

    Scaffold(
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
                    Text(
                        "Add expense",
                        style = MaterialTheme.typography.labelLarge
                    )
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
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(lDimens.dp150)
                            .padding(lDimens.dp0),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = lDimens.dp0
                        ),
                        colors = CardDefaults.cardColors(colors.backgroundPrimary)
                    ) {
                        Image(
                            painter = painterResource(Res.drawable.HomePic),
                            contentDescription = "Header Image",
                            modifier = Modifier.fillMaxWidth(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = lDimens.dp16, top = lDimens.dp20, end = lDimens.dp16)
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
                        onClick = { navControllerMain.navigate(Routes.SETTLE_UP.name) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Recent Activity section
            item {
                SectionHeader(
                    title = "Recent Activity",
                    actionText = "View All",
                    onActionClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(1)
                        }
                    }
                )

                val recentHistoryItems by historyViewModel.recentHistoryItems.collectAsState()
                val historyState by historyViewModel.historyState.collectAsState()

                when {
                    // Show loading state when historyState is Loading and recentHistoryItems is empty
                    historyState is HistoryState.Loading && recentHistoryItems.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(lDimens.dp100),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = colors.primary)
                        }
                    }

                    // Show empty state when we have loaded but have no items
                    recentHistoryItems.isEmpty() -> {
                        EmptyStateMessage(
                            message = "No recent activity",
                            submessage = "Your recent activity will appear here"
                        )
                    }

                    // Show the history items when we have them
                    else -> {
                        Column(
                            modifier = Modifier.padding(horizontal = lDimens.dp16),
                            verticalArrangement = Arrangement.spacedBy(lDimens.dp8)
                        ) {
                            recentHistoryItems.forEach { historyItem ->
                                HistoryItemCard(
                                    historyItem = historyItem,
                                    onMarkAsRead = { historyViewModel.markAsRead(it) }
                                )
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(lDimens.dp200)
                        .padding(lDimens.dp20)
                        .clickable {

                        },
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = lDimens.dp0
                    ),
                    colors = CardDefaults.cardColors(colors.backgroundPrimary)
                ) {
                    Image(
                        painter = painterResource(Res.drawable.ExclusiveFeature),
                        contentDescription = "FeatureImage",
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // Your Groups section
            item {
                SectionHeader(
                    title = "Your Groups",
                    actionText = "See All",
                    onActionClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(2)
                        }
                    }
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
                            // Show horizontal row of group cards, limited to 10 most recent ones
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = lDimens.dp16),
                                horizontalArrangement = Arrangement.spacedBy(lDimens.dp12)
                            ) {
                                // Create new group card as first item
                                item {
                                    CreateGroupCard(
                                        onClick = { navControllerMain.navigate("create_group") }
                                    )
                                }

                                // Limit to 10 most recent groups
                                val recentGroups = groups.sortedByDescending { it.createdAt }.take(10)

                                items(recentGroups) { group ->
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

            // Bottom spacer for FAB
            item {
                Spacer(modifier = Modifier.height(lDimens.dp80))
            }
        }
    }
}


@Composable
fun PendingSettlementItem(
    settlement: Settlement,
    isIncoming: Boolean,
    onApprove: (() -> Unit)?,
    onDecline: (() -> Unit)?,
    processingSettlements: Set<String> = emptySet()
) {
    val colors = LocalSplitColors.current
    val isProcessing = processingSettlements.contains(settlement.id)

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

            if (isIncoming && onApprove != null && onDecline != null) {
                Spacer(modifier = Modifier.height(lDimens.dp12))

                if (isProcessing) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = lDimens.dp8),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(lDimens.dp24),
                            color = colors.primary
                        )
                    }
                } else {
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
                            modifier = Modifier.padding(end = lDimens.dp8),
                            enabled = !isProcessing
                        ) {
                            Text("Decline",
                                color = colors.textPrimary
                            )
                        }

                        Button(
                            onClick = onApprove,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.success,
                                contentColor = Color.White
                            ),
                            enabled = !isProcessing
                        ) {
                            Text("Approve")
                        }
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
            overflow = TextOverflow.Ellipsis,
            color = colors.textPrimary
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
    val interactionSource = remember { MutableInteractionSource() }

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
                modifier = Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onActionClick
                )
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
            .width(lDimens.dp130),
        colors = CardDefaults.cardColors(
            containerColor = colors.cardBackground
        ),
        shape = RoundedCornerShape(lDimens.dp12),
        elevation = CardDefaults.cardElevation(
            defaultElevation = lDimens.dp2
        ),
        border = BorderStroke(
            lDimens.dp1,
            colors.primary
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(lDimens.dp12),  // Reduced padding
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Group icon
            val groupType = GroupType.fromString(group.groupType)
            Box(
                modifier = Modifier
                    .size(lDimens.dp48)  // Smaller icon
                    .background(
                        color = colors.primary.copy(alpha = 0.1f),
                        shape = CircleShape
                    )
                    .border(
                        width = lDimens.dp1,
                        color = colors.primary.copy(alpha = 0.3f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(resource = groupType.iconRes),
                    contentDescription = groupType.displayName,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(lDimens.dp40)  // Smaller image
                        .clip(CircleShape),
                )
            }

            Spacer(modifier = Modifier.height(lDimens.dp8))  // Reduced spacing

            // Group name
            Text(
                text = group.name,
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(lDimens.dp8))  // Reduced spacing

            // Balance (only showing balance, not member count)
            CurrencyDisplay(
                amount = group.totalAmount ?: 0.0,
                isIncome = (group.totalAmount ?: 0.0) >= 0,
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupCard(
    onClick: () -> Unit
) {
    val colors = LocalSplitColors.current

    Card(
        onClick = onClick,
        modifier = Modifier
            .width(lDimens.dp130),
        colors = CardDefaults.cardColors(
            containerColor = colors.cardBackground
        ),
        shape = RoundedCornerShape(lDimens.dp12),
        elevation = CardDefaults.cardElevation(
            defaultElevation = lDimens.dp2
        ),
        border = BorderStroke(
            lDimens.dp1,
            colors.primary
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(lDimens.dp12),  // Reduced padding
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Group icon
            Box(
                modifier = Modifier
                    .size(lDimens.dp48)
                    .background(
                        color = colors.primary.copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Group",
                    tint = colors.primary,
                    modifier = Modifier.size(lDimens.dp28)
                )
            }

            Spacer(modifier = Modifier.height(lDimens.dp8))  // Reduced spacing

            // Group name
            Text(
                text = "Create New",
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(lDimens.dp8))  // Reduced spacing

            // Balance (only showing balance, not member count)
            Text(
                text = "Group",
                style = MaterialTheme.typography.titleSmall,
                color = colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}


@Composable
fun PremiumCard(
    navControllerMain: NavHostController
){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = lDimens.dp16)
            .border(
                width = lDimens.dp2,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFD4AF37), // Gold start
                        Color(0xFFF5F5DC), // Gold middle (lighter)
                        Color(0xFFD4AF37)  // Gold end
                    )
                ),
                shape = RoundedCornerShape(lDimens.dp12)
            ),
        shape = RoundedCornerShape(lDimens.dp12),
        elevation = CardDefaults.cardElevation(defaultElevation = lDimens.dp4),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E24) // Dark background that makes gold pop
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { navControllerMain.navigate("premium_subscription") }
                .padding(lDimens.dp16),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side - Icon/Image
            Box(
                modifier = Modifier
                    .size(lDimens.dp60)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFD4AF37), Color(0xFFAA8C25)),
                            radius = 40f
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Premium",
                    tint = Color.White,
                    modifier = Modifier.size(lDimens.dp36)
                )
            }

            Spacer(modifier = Modifier.width(lDimens.dp16))

            // Right side - Text content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Join Split Premium",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFFD4AF37), // Gold text
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(lDimens.dp4))

                Text(
                    text = "Unlock exclusive features and remove limits",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
            }

            // Arrow icon
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "View Premium",
                tint = Color(0xFFD4AF37) // Gold tint
            )
        }
    }
}


@Composable
fun GroupCreationItem(group: Group, currentUserId : String) {
    val colors = LocalSplitColors.current
    val creationTime = formatExpenseDateTime(group.createdAt ?: 0L)
    val isCreatedByCurrentUser = group.createdBy == currentUserId

    SplitCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(lDimens.dp12),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Activity icon
            Box(
                modifier = Modifier
                    .size(lDimens.dp40)
                    .clip(CircleShape)
                    .background(colors.success.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.ThumbUp,
                    contentDescription = null,
                    tint = colors.success,
                    modifier = Modifier.size(lDimens.dp24)
                )
            }

            Spacer(modifier = Modifier.width(lDimens.dp12))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isCreatedByCurrentUser) "You" else "Someone",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary
                    )

                    Text(
                        text = if (isCreatedByCurrentUser)
                            " created a new group"
                        else
                            " added you to a group",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textPrimary
                    )
                }

                Text(
                    text = group.name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = colors.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${group.members.size} members • $creationTime",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }
        }
    }
}

@Composable
fun SettlementActivityItem(settlement: Settlement, currentUserId: String) {
    val colors = LocalSplitColors.current
    val isIncoming = settlement.toUserId == currentUserId

    SplitCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(lDimens.dp12),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Activity icon - different color based on status
            Box(
                modifier = Modifier
                    .size(lDimens.dp40)
                    .clip(CircleShape)
                    .background(
                        when (settlement.status) {
                            SettlementStatus.PENDING -> colors.warning.copy(alpha = 0.1f)
                            SettlementStatus.APPROVED -> colors.success.copy(alpha = 0.1f)
                            SettlementStatus.DECLINED -> colors.error.copy(alpha = 0.1f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    when (settlement.status) {
                        SettlementStatus.PENDING -> Icons.Default.ThumbUp
                        SettlementStatus.APPROVED -> Icons.Default.ThumbUp
                        SettlementStatus.DECLINED -> Icons.Default.ThumbUp
                    },
                    contentDescription = null,
                    tint = when (settlement.status) {
                        SettlementStatus.PENDING -> colors.warning
                        SettlementStatus.APPROVED -> colors.success
                        SettlementStatus.DECLINED -> colors.error
                    },
                    modifier = Modifier.size(lDimens.dp24)
                )
            }

            Spacer(modifier = Modifier.width(lDimens.dp12))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Change text based on settlement status and direction
                when (settlement.status) {
                    SettlementStatus.PENDING -> {
                        if (isIncoming) {
                            SpannedText(
                                "${settlement.fromUserName ?: "Someone"} ",
                                "requested payment from you",
                                colors
                            )
                        } else {
                            SpannedText(
                                "You ",
                                "requested payment from ${settlement.toUserName ?: "someone"}",
                                colors
                            )
                        }
                    }
                    SettlementStatus.APPROVED -> {
                        if (isIncoming) {
                            SpannedText(
                                "You ",
                                "approved payment to ${settlement.fromUserName ?: "someone"}",
                                colors
                            )
                        } else {
                            SpannedText(
                                "${settlement.toUserName ?: "Someone"} ",
                                "approved your payment request",
                                colors
                            )
                        }
                    }
                    SettlementStatus.DECLINED -> {
                        if (isIncoming) {
                            SpannedText(
                                "You ",
                                "declined payment to ${settlement.fromUserName ?: "someone"}",
                                colors
                            )
                        } else {
                            SpannedText(
                                "${settlement.toUserName ?: "Someone"} ",
                                "declined your payment request",
                                colors
                            )
                        }
                    }
                }

                Text(
                    text = formatExpenseDateTime(settlement.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }

            Spacer(modifier = Modifier.width(lDimens.dp8))

            // Amount
            CurrencyDisplay(
                amount = settlement.amount,
                isIncome = isIncoming
            )
        }
    }
}

@Composable
fun SpannedText(boldPart: String, regularPart: String, colors: SplitColors) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = boldPart,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = colors.textPrimary
        )
        Text(
            text = regularPart,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textPrimary
        )
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


private fun formatExpenseDateTime(timestamp: Long): String {
    if (timestamp == 0L) return ""

    val instant = Instant.fromEpochMilliseconds(timestamp)
    val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

    return "${dateTime.month.name.take(3)} ${dateTime.dayOfMonth}, ${dateTime.hour}:${dateTime.minute.toString().padStart(2, '0')}"
}
