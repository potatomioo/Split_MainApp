package com.falcon.split.presentation.screens.mainNavigation

import ContactPicker
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavHostController
import com.falcon.split.contact.ContactManager
import com.falcon.split.data.network.models_app.Expense
import com.falcon.split.data.network.models_app.ExpenseSplit
import com.falcon.split.data.network.models_app.Group
import com.falcon.split.data.network.models_app.GroupMember
import com.falcon.split.data.network.models_app.Settlement
import com.falcon.split.data.network.models_app.SettlementState
import com.falcon.split.data.network.models_app.SettlementStatus
import com.falcon.split.presentation.expense.ExpenseState
import com.falcon.split.presentation.group.GroupState
import com.falcon.split.presentation.group.GroupViewModel
import com.falcon.split.presentation.theme.CurrencyDisplay
import com.falcon.split.presentation.theme.LocalSplitColors
import com.falcon.split.presentation.theme.SplitCard
import com.falcon.split.presentation.theme.SplitColors
import com.falcon.split.presentation.theme.lDimens
import com.falcon.split.userManager.UserManager
import com.falcon.split.util.DateTimeUtil
import com.falcon.split.utils.MemberNameResolver
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.group_icon_filled

enum class GroupDetailsTab(val title: String) {
    EXPENSES("Expenses"),
    BALANCES("Balances"),
    SETTLEMENTS("Request")
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalResourceApi::class)
@Composable
fun GroupDetailsScreen(
    groupId: String,
    onNavigateBack: () -> Unit,
    navControllerMain: NavHostController,
    contactManager: ContactManager?,
    viewModel: GroupViewModel,
    userManager: UserManager,
    snackbarHostState: SnackbarHostState
) {
    val colors = LocalSplitColors.current
    val scope = rememberCoroutineScope()

    // State
    val groupState by viewModel.groupState.collectAsState()
    val expenseState by viewModel.expenseState.collectAsState()
    val settlementState by viewModel.settlementState.collectAsState()
    val settlements by viewModel.settlements.collectAsState()
    val pendingSettlements by viewModel.pendingSettlements.collectAsState()
    val processingSettlementIds by viewModel.processingSettlementId.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }

    // Tab state
    val pagerState = rememberPagerState(pageCount = { GroupDetailsTab.values().size })
    val currentTab = GroupDetailsTab.values()[pagerState.currentPage]

    // Count pending settlements for badge
    val pendingSettlementsCount = pendingSettlements.count { it.toUserId == viewModel.currentUserId }

    var isExpanded by remember { mutableStateOf(false) }

    var deleteErrorMessage by remember { mutableStateOf<String?>(null) }

    val deleteSuccess by viewModel.deleteSuccess.collectAsState()


    // Load data when screen is mounted
    LaunchedEffect(groupId) {
        viewModel.loadGroupDetails(groupId)
        viewModel.loadGroupExpenses(groupId)
        viewModel.loadSettlementHistory(groupId)
        viewModel.loadPendingSettlements()
    }

    LaunchedEffect(Unit) {
        viewModel.deleteErrorEvent.collect { errorMessage ->
            deleteErrorMessage = errorMessage
        }
    }

    LaunchedEffect(deleteSuccess) {
        if (deleteSuccess) {
            onNavigateBack()
        }
    }

    // Create MemberNameResolver
    val nameResolver = remember { MemberNameResolver(contactManager) }
    var showContactPicker by remember { mutableStateOf(false) }


    if (showContactPicker) {
        ContactPicker(
            contactManager = contactManager!!
        ) { contact ->
            showContactPicker = false

            // If a contact was selected, add it to the group
            if (contact != null) {
                // Handle adding member
                scope.launch {
                    try {
                        viewModel.addMembersToGroup(
                            groupId = groupId,
                            newMembers = listOf(contact.contactNumber)
                        )
                        snackbarHostState.showSnackbar("Added ${contact.contactName} to the group")
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar("Failed to add member: ${e.message}")
                    }
                }
            }
        }
    }

    if (deleteErrorMessage != null) {
        AlertDialog(
            onDismissRequest = { deleteErrorMessage = null },
            title = { Text("Cannot Delete Group", color = colors.textPrimary) },
            text = { Text(deleteErrorMessage!!, color = colors.textSecondary) },
            confirmButton = {
                Button(
                    onClick = { deleteErrorMessage = null },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary
                    )
                ) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            GroupDetailsTopBar(
                groupState = groupState,
                onNavigateBack = onNavigateBack,
                onShowOptions = { showDeleteDialog = true }
            )
        },
        floatingActionButton = {
            ExpandableFloatingActionButton(
                expanded = isExpanded,
                onExpandedChange = { isExpanded = it },
                onAddExpense = {
                    navControllerMain.navigate("create_expense?groupId=$groupId")
                },
                onAddMember = {
                    if (contactManager != null) {
                        showContactPicker = true
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
            when (groupState) {
                is GroupState.Loading -> {
                    LoadingView()
                }

                is GroupState.Error -> {
                    ErrorView(
                        message = (groupState as GroupState.Error).message,
                        onRetry = { viewModel.loadGroupDetails(groupId) }
                    )
                }

                is GroupState.GroupDetailSuccess -> {
                    val group = (groupState as GroupState.GroupDetailSuccess).group

                    Column(modifier = Modifier.fillMaxSize()) {
                        // Group summary card
                        GroupSummaryCard(
                            group = group,
                            modifier = Modifier.padding(horizontal = lDimens.dp16, vertical = lDimens.dp8)
                        )

                        // Tab row
                        TabRow(
                            selectedTabIndex = pagerState.currentPage,
                            indicator = { tabPositions ->
                                TabRowDefaults.Indicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                                    color = colors.primary
                                )
                            }
                        ) {
                            GroupDetailsTab.values().forEachIndexed { index, tab ->
                                Tab(
                                    selected = pagerState.currentPage == index,
                                    onClick = {
                                        scope.launch {
                                            pagerState.animateScrollToPage(index)
                                        }
                                    },
                                    text = {
                                        if (tab == GroupDetailsTab.SETTLEMENTS && pendingSettlementsCount > 0) {
                                            BadgedBox(
                                                badge = {
                                                    Badge {
                                                        Text(pendingSettlementsCount.toString(),color = colors.textPrimary)
                                                    }
                                                }
                                            ) {
                                                Text(tab.title, color = colors.textPrimary)
                                            }
                                        } else {
                                            Text(tab.title,color = colors.textPrimary)
                                        }
                                    },
                                )
                            }
                        }

                        // Pager content
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.weight(1f)
                        ) { page ->
                            when (page) {
                                0 -> ExpensesTab(
                                    expenseState = expenseState,
                                    group = group,
                                    nameResolver = nameResolver,
                                    settlementHistory = settlements
                                )

                                1 -> BalancesTab(
                                    group = group,
                                    currentUserId = viewModel.currentUserId ?: "",
                                    nameResolver = nameResolver,
                                    onSettleUp = { toUserId, amount ->
                                        viewModel.settleBalance(group.id, toUserId, amount)
                                    },{},
                                    colors = colors
                                )

                                2 -> RequestsTab(
                                    pendingSettlements = pendingSettlements,
                                    currentUserId = viewModel.currentUserId ?: "",
                                    nameResolver = nameResolver,
                                    onApprove = { settlementId ->
                                        viewModel.approveSettlement(settlementId)
                                    },
                                    onDecline = { settlementId ->
                                        viewModel.declineSettlement(settlementId)
                                    },
                                    processingSettlementIds = processingSettlementIds
                                )
                            }
                        }
                    }
                }

                else -> {
                    // Unexpected state, show loading
                    LoadingView()
                }
            }

            // Delete Confirmation Dialog
            if (showDeleteDialog) {
                DeleteConfirmationDialog(
                    onConfirm = {
                        showDeleteDialog = false
                        viewModel.deleteGroup(groupId)
                    },
                    onDismiss = { showDeleteDialog = false }
                )
            }

            // Settlement Success/Error Dialog
            when (settlementState) {
                is SettlementState.Error -> {
                    SettlementResultDialog(
                        success = false,
                        message = (settlementState as SettlementState.Error).message,
                        onDismiss = { viewModel.resetSettlementState() }
                    )
                }
                else -> {}
            }
        }
        if (isExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = colors.backgroundPrimary.copy(alpha = 0.75f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { isExpanded = false }
                    )
            )
        }
    }
}

@Composable
fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = LocalSplitColors.current.primary)
    }
}

@Composable
fun ErrorView(
    message: String,
    onRetry: () -> Unit
) {
    val colors = LocalSplitColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(lDimens.dp16),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Something went wrong",
            style = MaterialTheme.typography.titleLarge,
            color = colors.textPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(lDimens.dp8))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(lDimens.dp24))

        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary,
                contentColor = Color.White
            )
        ) {
            Text("Retry")
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun GroupDetailsTopBar(
    groupState: GroupState,
    onNavigateBack: () -> Unit,
    onShowOptions: () -> Unit
) {
    val colors = LocalSplitColors.current

    androidx.compose.material3.TopAppBar(
        title = {
            Text(
                when (groupState) {
                    is GroupState.GroupDetailSuccess -> (groupState as GroupState.GroupDetailSuccess).group.name
                    else -> "Group Details"
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = colors.textPrimary
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, "Back")
            }
        },
        actions = {
            IconButton(onClick = onShowOptions) {
                Icon(Icons.Default.Delete, "Delete")
            }
        },
        colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
            containerColor = colors.backgroundSecondary,
            titleContentColor = colors.textPrimary,
            navigationIconContentColor = colors.textPrimary,
            actionIconContentColor = colors.textPrimary
        )
    )
}

@Composable
fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalSplitColors.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Group", color = colors.textPrimary) },
        text = { Text("Are you sure you want to delete this group? This action cannot be undone.", color = colors.textSecondary) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                border = BorderStroke(lDimens.dp1, colors.error)
            ) {
                Text("Delete", color = colors.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = colors.textSecondary)
            }
        }
    )
}

@Composable
fun SettlementResultDialog(
    success: Boolean,
    message: String,
    onDismiss: () -> Unit
) {
    val colors = LocalSplitColors.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (success) "Success" else "Error",
                color = if (success) colors.success else colors.error
            )
        },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

@Composable
fun GroupSummaryCard(
    group: Group,
    modifier: Modifier = Modifier
) {
    val colors = LocalSplitColors.current

    SplitCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(lDimens.dp16)
        ) {
            Text(
                "Total Expenses",
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary
            )

            Text(
                "₹${group.totalAmount ?: 0.0}",
                style = MaterialTheme.typography.headlineMedium,
                color = colors.primary,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = lDimens.dp8),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${group.expenses.size} expenses", color = colors.textSecondary)
                Text("${group.members.size} members", color = colors.textSecondary)
            }
        }
    }
}


//Animation wala floating button
@Composable
fun ExpandableFloatingActionButton(
    modifier: Modifier = Modifier,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onAddExpense: () -> Unit,
    onAddMember: () -> Unit
) {
    val colors = LocalSplitColors.current

    // Rotation animation for the FAB icon
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "rotationAnimation"
    )

    Box(modifier = modifier) {
        // Main FAB that rotates to form an X when expanded
        FloatingActionButton(
            onClick = { onExpandedChange(!expanded) },
            containerColor = colors.primary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(lDimens.dp56)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = if (expanded) "Close menu" else "Open menu",
                modifier = Modifier.rotate(rotation)
            )
        }

        // "Add Member" option
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(animationSpec = tween(durationMillis = 300)) +
                    slideInVertically(animationSpec = tween(durationMillis = 300)) { it },
            exit = fadeOut(animationSpec = tween(durationMillis = 300)) +
                    slideOutVertically(animationSpec = tween(durationMillis = 300)) { it },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = lDimens.dp200, end = lDimens.dp8)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth(0.5f)
            ) {
                Text(
                    "Add a member",
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.textPrimary,
                    modifier = Modifier.padding(end = lDimens.dp16)
                )

                FloatingActionButton(
                    onClick = {
                        onAddMember()
                        onExpandedChange(false)
                    },
                    containerColor = colors.cardBackground,
                    contentColor = colors.primary,
                    modifier = Modifier.size(lDimens.dp48)
                ) {
                    Icon(Icons.Default.Person, contentDescription = "Add Member")
                }
            }
        }

        // "Add Expense" option
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(animationSpec = tween(durationMillis = 300)) +
                    slideInVertically(animationSpec = tween(durationMillis = 300)) { it },
            exit = fadeOut(animationSpec = tween(durationMillis = 300)) +
                    slideOutVertically(animationSpec = tween(durationMillis = 300)) { it },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = lDimens.dp130, end = lDimens.dp8)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth(0.5f)
            ) {
                Text(
                    "Add an expense",
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.textPrimary,
                    modifier = Modifier.padding(end = lDimens.dp16)
                )

                FloatingActionButton(
                    onClick = {
                        onAddExpense()
                        onExpandedChange(false)
                    },
                    containerColor = colors.cardBackground,
                    contentColor = colors.primary,
                    modifier = Modifier.size(lDimens.dp48)
                ) {
                    Icon(Icons.Default.ThumbUp, contentDescription = "Add Expense")
                }
            }
        }
    }
}


@Composable
fun ExpensesTab(
    expenseState: ExpenseState,
    group: Group,
    nameResolver: MemberNameResolver,
    settlementHistory: List<Settlement> // Add settlement history parameter
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = lDimens.dp16)
    ) {
        when (expenseState) {
            is ExpenseState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = LocalSplitColors.current.primary
                )
            }

            is ExpenseState.Error -> {
                Text(
                    text = "Error loading expenses: ${expenseState.message}",
                    modifier = Modifier.align(Alignment.Center),
                    color = LocalSplitColors.current.error,
                    textAlign = TextAlign.Center
                )
            }

            is ExpenseState.Success -> {
                val expenses = expenseState.expenses

                if (expenses.isEmpty() && settlementHistory.isEmpty()) {
                    EmptyState(
                        title = "No transactions yet",
                        message = "Add an expense to get started",
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    // Create a combined list of expenses and settlements
                    val combinedItems = mutableListOf<TransactionItem>()

                    // Add expenses
                    expenses.forEach { expense ->
                        combinedItems.add(
                            TransactionItem.ExpenseItem(
                                expense = expense,
                                timestamp = expense.createdAt
                            )
                        )
                    }

                    // Add settlements
                    settlementHistory.forEach { settlement ->
                        if (settlement.status != SettlementStatus.PENDING) {
                            combinedItems.add(
                                TransactionItem.SettlementItem(
                                    settlement = settlement,
                                    timestamp = settlement.timestamp
                                )
                            )
                        }
                    }

                    // Sort combined items by timestamp (most recent first)
                    combinedItems.sortByDescending { it.timestamp }

                    // Group by date
                    val groupedItems = combinedItems.groupBy {
                        formatDateHeader(it.timestamp)
                    }

                    LazyColumn(
                        contentPadding = PaddingValues(bottom = lDimens.dp80), // Space for FAB
                        verticalArrangement = Arrangement.spacedBy(lDimens.dp8)
                    ) {
                        groupedItems.forEach { (date, itemsForDate) ->
                            item {
                                Text(
                                    text = date,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = LocalSplitColors.current.textSecondary,
                                    modifier = Modifier.padding(vertical = lDimens.dp8)
                                )
                            }

                            items(itemsForDate) { item ->
                                when (item) {
                                    is TransactionItem.ExpenseItem -> {
                                        ExpenseCard(
                                            expense = item.expense,
                                            group = group,
                                            nameResolver = nameResolver
                                        )
                                    }
                                    is TransactionItem.SettlementItem -> {
                                        SettlementHistoryInExpensesCard(
                                            settlement = item.settlement
                                        )
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
        }
    }
}

sealed class TransactionItem {
    abstract val timestamp: Long

    data class ExpenseItem(
        val expense: Expense,
        override val timestamp: Long = expense.createdAt
    ) : TransactionItem()

    data class SettlementItem(
        val settlement: Settlement,
        override val timestamp: Long = settlement.timestamp
    ) : TransactionItem()
}

@Composable
fun SettlementHistoryInExpensesCard(
    settlement: Settlement
) {
    val colors = LocalSplitColors.current

    SplitCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(lDimens.dp16)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = when (settlement.status) {
                        SettlementStatus.APPROVED -> "Settlement"
                        SettlementStatus.DECLINED -> "Declined Settlement"
                        else -> "Settlement"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary
                )

                CurrencyDisplay(
                    amount = settlement.amount,
                    isIncome = false
                )
            }

            Spacer(modifier = Modifier.height(lDimens.dp4))

            // Transaction details
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text =
                        when (settlement.status) {
                            SettlementStatus.APPROVED -> "${settlement.fromUserName ?: "Someone"} paid ${settlement.toUserName ?: "someone"}"
                            SettlementStatus.DECLINED -> "${settlement.toUserName ?: "someone"} declined a settlement of ${settlement.fromUserName ?: "Someone"}"
                            else -> "Unknown status"
                        },
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )

                Spacer(modifier = Modifier.width(lDimens.dp8))

                // Status indicator
//                Surface(
//                    color = when (settlement.status) {
//                        SettlementStatus.APPROVED -> colors.success.copy(alpha = 0.1f)
//                        SettlementStatus.DECLINED -> colors.error.copy(alpha = 0.1f)
//                        else -> colors.textSecondary.copy(alpha = 0.1f)
//                    },
//                    shape = CircleShape,
//                    modifier = Modifier.size(lDimens.dp16)
//                ) {
//                    Box(contentAlignment = Alignment.Center) {
//                        Icon(
//                            when (settlement.status) {
//                                SettlementStatus.APPROVED -> Icons.Default.Check
//                                SettlementStatus.DECLINED -> Icons.Default.Close
//                                else -> Icons.Default.MoreVert
//                            },
//                            contentDescription = null,
//                            tint = when (settlement.status) {
//                                SettlementStatus.APPROVED -> colors.success
//                                SettlementStatus.DECLINED -> colors.error
//                                else -> colors.textSecondary
//                            },
//                            modifier = Modifier.size(lDimens.dp12)
//                        )
//                    }
//                }
            }

            // Show date
            Text(
                formatDateTime(settlement.timestamp),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )
        }
    }
}

@Composable
fun ExpenseCard(
    expense: Expense,
    group: Group,
    nameResolver: MemberNameResolver
) {
    val colors = LocalSplitColors.current

    SplitCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(lDimens.dp16)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "${expense.description}",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary
                )

                CurrencyDisplay(
                    amount = expense.amount,
                    isIncome = true
                )
            }

            Spacer(modifier = Modifier.height(lDimens.dp4))

            // Find the member who paid
            val paidByMember = group.members.find { it.userId == expense.paidByUserId }
            val payerName = if (paidByMember != null) {
                nameResolver.resolveDisplayName(paidByMember)
            } else {
                expense.paidByUserName ?: "Unknown"
            }

            Text(
                "$payerName added a new expense",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary
            )
            // Show date if available
            Text(
                DateTimeUtil.formatStandardDate(expense.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )
        }
    }
}

@Composable
fun BalancesTab(
    group: Group,
    currentUserId: String,
    nameResolver: MemberNameResolver,
    onSettleUp: (toUserId: String, amount: Double) -> Unit,
    onRemind: (userId: String) -> Unit,
    colors: SplitColors
) {
    var showSettleDialog by remember { mutableStateOf(false) }
    var selectedMember by remember { mutableStateOf<GroupMember?>(null) }

    // Get current user's member object
    val currentUserMember = group.members.find { it.userId == currentUserId }

    // Get individual balances
    val individualBalances = currentUserMember?.individualBalances ?: emptyMap()

    // All other members (excluding current user)
    val otherMembers = group.members.filter { it.userId != currentUserId }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = lDimens.dp16)
    ) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = lDimens.dp80), // Space for FAB
            verticalArrangement = Arrangement.spacedBy(lDimens.dp8)
        ) {
            // Current user's balance overview
            item {
                Text(
                    "Your Balance",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = lDimens.dp16, bottom = lDimens.dp8),
                    color = colors.textPrimary
                )

                if (currentUserMember != null) {
                    UserBalanceCard(
                        member = currentUserMember,
                        isCurrentUser = true,
                        nameResolver = nameResolver
                    )
                }
            }

            // All Members section
            item {
                Text(
                    "All Members",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = lDimens.dp16, bottom = lDimens.dp8),
                    color = colors.textPrimary
                )
            }

            items(otherMembers) { member ->
                // Modified balance calculation to handle null userIds
                val currentUserBalance = if (member.userId != null) {
                    individualBalances[member.userId] ?: 0.0
                } else {
                    // For members without userIds, check their individualBalances directly
                    -(member.individualBalances[currentUserId] ?: 0.0) // Negate because the perspective is reversed
                }

                // Debug for verification
                println("DEBUG: Member ${member.name} - balance: $currentUserBalance")

                val isIncoming = currentUserBalance > 0  // positive means they owe me
                val isRegistered = member.userId != null

                MemberBalanceCard(
                    member = member,
                    amount = kotlin.math.abs(currentUserBalance),
                    isIncoming = isIncoming,
                    nameResolver = nameResolver,
                    onAction = when {
                        !isRegistered -> {
                            // For unregistered users (null userId), show Invite button
                            { /* Handle invite action here - perhaps share app link via SMS */ }
                        }
                        currentUserBalance > 0 -> {
                            { onRemind(member.userId ?: "") }
                        }
                        currentUserBalance < 0 -> { // I owe them money
                            {
                                selectedMember = member
                                showSettleDialog = true
                            }
                        }
                        else -> null // We're settled up
                    },
                    actionButtonText = when {
                        !isRegistered -> "Invite"
                        currentUserBalance > 0 -> "Remind"
                        currentUserBalance < 0 -> "Settle Up"
                        else -> null
                    }
                )
            }

            // Bottom spacer for FAB
            item {
                Spacer(modifier = Modifier.height(lDimens.dp80))
            }
        }
    }

    // Settle up dialog
    if (showSettleDialog && selectedMember != null) {
        val amountIOwe = kotlin.math.abs(individualBalances[selectedMember!!.userId] ?: 0.0)

        SettleUpDialog(
            memberName = nameResolver.resolveDisplayName(selectedMember!!),
            amount = amountIOwe,
            onDismiss = { showSettleDialog = false },
            onConfirm = { amount ->
                onSettleUp(selectedMember!!.userId!!, amount)
                showSettleDialog = false
            }
        )
    }
}

@Composable
fun UserBalanceCard(
    member: GroupMember,
    isCurrentUser: Boolean,
    nameResolver: MemberNameResolver
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
            Text(
                nameResolver.resolveDisplayName(member),
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary
            )

            Spacer(modifier = Modifier.height(lDimens.dp8))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Balance
                Column {
                    Text(
                        "Total Balance",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary
                    )

                    CurrencyDisplay(
                        amount = member.balance,
                        isIncome = member.balance >= 0,
                        large = true
                    )
                }

                // Status indicator
                Surface(
                    color = when {
                        member.balance > 0 -> colors.success.copy(alpha = 0.1f)
                        member.balance < 0 -> colors.error.copy(alpha = 0.1f)
                        else -> colors.textSecondary.copy(alpha = 0.1f)
                    },
                    shape = RoundedCornerShape(lDimens.dp16)
                ) {
                    Text(
                        when {
                            member.balance > 0 -> "You are owed"
                            member.balance < 0 -> "You owe"
                            else -> "All settled up"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = when {
                            member.balance > 0 -> colors.success
                            member.balance < 0 -> colors.error
                            else -> colors.textSecondary
                        },
                        modifier = Modifier.padding(horizontal = lDimens.dp12, vertical = lDimens.dp6)
                    )
                }
            }
        }
    }
}

@Composable
fun MemberBalanceCard(
    member: GroupMember,
    amount: Double,
    isIncoming: Boolean,
    nameResolver: MemberNameResolver,
    onAction: (() -> Unit)?,
    actionButtonText: String?
) {
    val colors = LocalSplitColors.current

    SplitCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(lDimens.dp16),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = nameResolver.resolveDisplayName(member),
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(lDimens.dp4)
                ) {
                    if (amount > 0) {
                        Text(
                            text = if (isIncoming) "Owes you" else "You owe",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isIncoming) colors.success else colors.error
                        )

                        CurrencyDisplay(
                            amount = amount,
                            isIncome = isIncoming
                        )
                    } else {
                        Text(
                            text = "Settled up",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary
                        )
                    }
                }
            }

            // Action button (Settle Up or Remind)
            if (actionButtonText != null && onAction != null && amount > 0) {
                val buttonColors = if (isIncoming) {
                    // Remind button (green)
                    ButtonDefaults.buttonColors(
                        containerColor = colors.success,
                        contentColor = Color.White
                    )
                } else {
                    // Settle Up button (primary color)
                    ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = Color.White
                    )
                }

                val icon = if (isIncoming) {
                    Icons.Default.ThumbUp
                } else {
                    Icons.Default.ThumbUp
                }

                Button(
                    onClick = onAction,
                    colors = buttonColors,
                    contentPadding = PaddingValues(horizontal = lDimens.dp12),
                    modifier = Modifier.height(lDimens.dp36)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(lDimens.dp16)
                    )
                    Spacer(modifier = Modifier.width(lDimens.dp4))
                    Text(actionButtonText)
                }
            }
        }
    }
}


@Composable
fun RequestsTab(
    pendingSettlements: List<Settlement>,
    currentUserId: String,
    nameResolver: MemberNameResolver,
    onApprove: (String) -> Unit,
    onDecline: (String) -> Unit,
    processingSettlementIds: Set<String> = emptySet(),
) {
    val colors = LocalSplitColors.current

    // Filter settlements - only include pending ones
    val incomingRequests = pendingSettlements.filter { it.toUserId == currentUserId && it.status == SettlementStatus.PENDING }
    val outgoingRequests = pendingSettlements.filter { it.fromUserId == currentUserId && it.status == SettlementStatus.PENDING }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = lDimens.dp16, vertical = lDimens.dp8),
        verticalArrangement = Arrangement.spacedBy(lDimens.dp8)
    ) {
        // Incoming Requests Section (people requesting money from you)
        item {
            Text(
                "Requests For You",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = lDimens.dp8, bottom = lDimens.dp4),
                color = colors.textPrimary
            )
        }

        if (incomingRequests.isEmpty()) {
            item {
                EmptyState(
                    title = "No pending requests",
                    message = "You don't have any payment requests to approve",
                    modifier = Modifier.padding(vertical = lDimens.dp16)
                )
            }
        } else {
            items(incomingRequests) { settlement ->
                PendingSettlementItem(
                    settlement = settlement,
                    isIncoming = true,
                    onApprove = { onApprove(settlement.id) },
                    onDecline = { onDecline(settlement.id) },
                    processingSettlements = processingSettlementIds
                )
            }
        }

        // Outgoing Requests Section (your requests to others)
        item {
            Text(
                "Your Pending Requests",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = lDimens.dp24, bottom = lDimens.dp4),
                color = colors.textPrimary
            )
        }

        if (outgoingRequests.isEmpty()) {
            item {
                EmptyState(
                    title = "No outgoing requests",
                    message = "You haven't requested any payments yet",
                    modifier = Modifier.padding(vertical = lDimens.dp16)
                )
            }
        } else {
            items(outgoingRequests) { settlement ->
                PendingSettlementItem(
                    settlement = settlement,
                    isIncoming = false,
                    onApprove = null,
                    onDecline = null,
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


@Composable
fun SettlementHistoryCard(
    settlement: Settlement
) {
    val colors = LocalSplitColors.current

    SplitCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(lDimens.dp16),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${settlement.fromUserName ?: "Someone"} paid ${settlement.toUserName ?: "someone"}",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary
                )

                Text(
                    text = formatDateTime(settlement.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(lDimens.dp8)
            ) {
                CurrencyDisplay(
                    amount = settlement.amount,
                    isIncome = false
                )

                // Status indicator
                Surface(
                    color = when (settlement.status) {
                        SettlementStatus.APPROVED -> colors.success.copy(alpha = 0.1f)
                        SettlementStatus.DECLINED -> colors.error.copy(alpha = 0.1f)
                        else -> colors.textSecondary.copy(alpha = 0.1f)
                    },
                    shape = CircleShape,
                    modifier = Modifier.size(lDimens.dp24)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            when (settlement.status) {
                                SettlementStatus.APPROVED -> Icons.Default.Check
                                SettlementStatus.DECLINED -> Icons.Default.Close
                                else -> Icons.Default.MoreVert
                            },
                            contentDescription = null,
                            tint = when (settlement.status) {
                                SettlementStatus.APPROVED -> colors.success
                                SettlementStatus.DECLINED -> colors.error
                                else -> colors.textSecondary
                            },
                            modifier = Modifier.size(lDimens.dp16)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettleUpDialog(
    memberName: String,
    amount: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    val colors = LocalSplitColors.current
    var settlementAmount by remember { mutableStateOf(amount.toString()) }
    var isValidAmount by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Settle Up with $memberName", color = colors.textPrimary) },
        text = {
            Column {
                Text(
                    "How much are you settling?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )

                Spacer(modifier = Modifier.height(lDimens.dp16))

                OutlinedTextField(
                    value = settlementAmount,
                    onValueChange = {
                        settlementAmount = it
                        isValidAmount = try {
                            val amountValue = it.toDouble()
                            amountValue > 0 && amountValue <= amount
                        } catch (e: Exception) {
                            false
                        }
                    },
                    label = { Text("Amount", color = colors.textSecondary) },
                    prefix = { Text("₹", color = colors.textPrimary) },
                    isError = !isValidAmount,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colors.cardBackground,
                        unfocusedContainerColor = colors.cardBackground,
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.border,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    ),
                    textStyle = TextStyle(color = colors.textPrimary)
                )

                if (!isValidAmount) {
                    Text(
                        "Please enter a valid amount (maximum ₹$amount)",
                        color = colors.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    try {
                        val amountValue = settlementAmount.toDouble()
                        if (amountValue > 0 && amountValue <= amount) {
                            onConfirm(amountValue)
                        }
                    } catch (e: Exception) {
                        // Invalid amount format
                    }
                },
                enabled = isValidAmount && settlementAmount.isNotEmpty()
            ) {
                Text("Settle", color = colors.textPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss,border = BorderStroke(
                width = lDimens.dp1,
                color = colors.error
            )) {
                Text("Cancel", color = colors.error)
            }
        }
    )
}

@Composable
fun EmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    val colors = LocalSplitColors.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(lDimens.dp16),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = colors.primary.copy(alpha = 0.1f),
            modifier = Modifier.size(lDimens.dp56)
        ) {
            Icon(
                imageVector = when {
                    title.contains("expense", ignoreCase = true) -> Icons.Default.ThumbUp
                    title.contains("request", ignoreCase = true) -> Icons.Default.ThumbUp
                    else -> Icons.Default.ThumbUp
                },
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier
                    .padding(lDimens.dp16)
                    .size(lDimens.dp24)
            )
        }

        Spacer(modifier = Modifier.height(lDimens.dp16))

        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(lDimens.dp4))

        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
            textAlign = TextAlign.Center
        )
    }
}

// Helper functions for date formatting
fun formatDateHeader(timestamp: Long): String {
    if (timestamp == 0L) return "Unknown Date"

    val now = Clock.System.now()
    val instant = Instant.fromEpochMilliseconds(timestamp)
    val nowDate = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
    val expenseDate = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date

    return when {
        expenseDate == nowDate -> "Today"
        expenseDate == nowDate.minus(kotlinx.datetime.DatePeriod(days = 1)) -> "Yesterday"
        nowDate.minus(kotlinx.datetime.DatePeriod(days = 7)) < expenseDate -> "This Week"
        nowDate.minus(kotlinx.datetime.DatePeriod(days = 30)) < expenseDate -> "This Month"
        else -> "${expenseDate.month} ${expenseDate.year}"
    }
}

fun formatDateTime(timestamp: Long): String {
    if (timestamp == 0L) return ""

    val instant = Instant.fromEpochMilliseconds(timestamp)
    val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

    return "${dateTime.month.name.take(3)} ${dateTime.dayOfMonth}, ${dateTime.hour}:${dateTime.minute.toString().padStart(2, '0')}"
}