package com.falcon.split.presentation.screens.mainNavigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.falcon.split.presentation.theme.lDimens
import com.falcon.split.userManager.UserManager
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

enum class GroupDetailsTab(val icon: ImageVector, val title: String) {
    EXPENSES(Icons.Default.ThumbUp, "Expenses"),
    BALANCES(Icons.Default.ThumbUp, "Balances"),
    SETTLEMENTS(Icons.Default.ThumbUp, "Settlements")
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalResourceApi::class)
@Composable
fun GroupDetailsScreen(
    groupId: String,
    onNavigateBack: () -> Unit,
    navControllerMain: NavHostController,
    contactManager: ContactManager?,
    viewModel: GroupViewModel,
    userManager: UserManager
) {
    val colors = LocalSplitColors.current
    val scope = rememberCoroutineScope()

    // State
    val groupState by viewModel.groupState.collectAsState()
    val expenseState by viewModel.expenseState.collectAsState()
    val settlementState by viewModel.settlementState.collectAsState()
    val settlements by viewModel.settlements.collectAsState()
    val pendingSettlements by viewModel.pendingSettlements.collectAsState()

    // Options menu state
    var showOptionsMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Tab state
    val pagerState = rememberPagerState(pageCount = { GroupDetailsTab.values().size })
    val currentTab = GroupDetailsTab.values()[pagerState.currentPage]

    // Count pending settlements for badge
    val pendingSettlementsCount = pendingSettlements.count { it.toUserId == viewModel.currentUserId }

    // Load data when screen is mounted
    LaunchedEffect(groupId) {
        viewModel.loadGroupDetails(groupId)
        viewModel.loadGroupExpenses(groupId)
        viewModel.loadSettlementHistory(groupId)
        viewModel.loadPendingSettlements()
    }

    // Create MemberNameResolver
    val nameResolver = remember { MemberNameResolver(contactManager) }

    Scaffold(
        topBar = {
            GroupDetailsTopBar(
                groupState = groupState,
                onNavigateBack = onNavigateBack,
                onShowOptions = { showOptionsMenu = true }
            )
        },
        floatingActionButton = {
            GroupDetailsFAB(
                currentTab = currentTab,
                onAddExpense = { navControllerMain.navigate("create_expense?groupId=$groupId") }
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
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
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
                                                        Text(pendingSettlementsCount.toString())
                                                    }
                                                }
                                            ) {
                                                Text(tab.title)
                                            }
                                        } else {
                                            Text(tab.title)
                                        }
                                    },
                                    icon = { Icon(tab.icon, contentDescription = null) }
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
                                    nameResolver = nameResolver
                                )

                                1 -> BalancesTab(
                                    group = group,
                                    currentUserId = viewModel.currentUserId ?: "",
                                    nameResolver = nameResolver,
                                    onSettleUp = { toUserId, amount ->
                                        viewModel.settleBalance(group.id, toUserId, amount)
                                    }
                                )

                                2 -> SettlementsTab(
                                    pendingSettlements = pendingSettlements,
                                    settlementHistory = settlements,
                                    currentUserId = viewModel.currentUserId ?: "",
                                    nameResolver = nameResolver,
                                    onApprove = { settlementId ->
                                        viewModel.approveSettlement(settlementId)
                                    },
                                    onDecline = { settlementId ->
                                        viewModel.declineSettlement(settlementId)
                                    }
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

            // Options Menu
            if (showOptionsMenu) {
                GroupOptionsMenu(
                    onDismiss = { showOptionsMenu = false },
                    onDelete = {
                        showOptionsMenu = false
                        showDeleteDialog = true
                    }
                )
            }

            // Delete Confirmation Dialog
            if (showDeleteDialog) {
                DeleteConfirmationDialog(
                    onConfirm = {
                        showDeleteDialog = false
                        viewModel.deleteGroup(groupId)
                        onNavigateBack()
                    },
                    onDismiss = { showDeleteDialog = false }
                )
            }

            // Settlement Success/Error Dialog
            when (settlementState) {
                is SettlementState.Success -> {
                    SettlementResultDialog(
                        success = true,
                        message = "Operation completed successfully.",
                        onDismiss = { viewModel.resetSettlementState() }
                    )
                }

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
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Something went wrong",
            style = MaterialTheme.typography.titleLarge,
            color = colors.textPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

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
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, "Back")
            }
        },
        actions = {
            IconButton(onClick = onShowOptions) {
                Icon(Icons.Default.MoreVert, "More options")
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
fun GroupDetailsFAB(
    currentTab: GroupDetailsTab,
    onAddExpense: () -> Unit
) {
    val colors = LocalSplitColors.current

    when (currentTab) {
        GroupDetailsTab.EXPENSES -> {
            // Add Expense FAB
            FloatingActionButton(
                onClick = onAddExpense,
                containerColor = colors.primary,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 8.dp
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Expense")
            }
        }

        GroupDetailsTab.BALANCES -> {
            // Settle Up FAB
            ExtendedFloatingActionButton(
                onClick = onAddExpense, // For now, still navigate to add expense
                containerColor = colors.primary,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.ThumbUp, contentDescription = null) },
                text = { Text("Settle Up") },
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 8.dp
                )
            )
        }

        GroupDetailsTab.SETTLEMENTS -> {
            // No FAB for settlements tab
        }
    }
}

@Composable
fun GroupOptionsMenu(
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = LocalSplitColors.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .padding(top = 56.dp, end = 8.dp),
        contentAlignment = Alignment.TopEnd
    ) {
        Surface(
            color = colors.cardBackground,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .width(200.dp)
                .padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                MenuItem(
                    text = "Delete Group",
                    icon = Icons.Default.Delete,
                    iconTint = colors.error,
                    textColor = colors.error,
                    onClick = onDelete
                )
            }
        }
    }
}

@Composable
fun MenuItem(
    text: String,
    icon: ImageVector,
    iconTint: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .padding(8.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )

        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = textColor
        )
    }
}

@Composable
fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalSplitColors.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Group") },
        text = { Text("Are you sure you want to delete this group? This action cannot be undone.") },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text("Delete", color = colors.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
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
                .padding(16.dp)
        ) {
            Text(
                "Total Expenses",
                style = MaterialTheme.typography.titleMedium,
                color = colors.textSecondary
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
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${group.expenses.size} expenses")
                Text("${group.members.size} members")
            }
        }
    }
}

@Composable
fun ExpensesTab(
    expenseState: ExpenseState,
    group: Group,
    nameResolver: MemberNameResolver
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
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

                if (expenses.isEmpty()) {
                    EmptyState(
                        title = "No expenses yet",
                        message = "Add an expense to get started",
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    // Group expenses by date
                    val groupedExpenses = expenses.groupBy {
                        formatDateHeader(it.expenseId.toLongOrNull() ?: 0L)
                    }

                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 80.dp), // Space for FAB
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        groupedExpenses.forEach { (date, expensesForDate) ->
                            item {
                                Text(
                                    text = date,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = LocalSplitColors.current.textSecondary,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }

                            items(expensesForDate) { expense ->
                                ExpenseCard(
                                    expense = expense,
                                    group = group,
                                    nameResolver = nameResolver
                                )
                            }
                        }

                        // Bottom spacer for FAB
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
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
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    expense.description,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary
                )

                CurrencyDisplay(
                    amount = expense.amount,
                    isIncome = true
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Find the member who paid
            val paidByMember = group.members.find { it.userId == expense.paidByUserId }
            val payerName = if (paidByMember != null) {
                nameResolver.resolveDisplayName(paidByMember)
            } else {
                expense.paidByUserName ?: "Unknown"
            }

            Text(
                "Paid by $payerName",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary
            )

            // Show date if available
            Text(
                formatDateTime(expense.expenseId.toLongOrNull() ?: 0L),
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
    onSettleUp: (toUserId: String, amount: Double) -> Unit
) {
    var showSettleDialog by remember { mutableStateOf(false) }
    var selectedMember by remember { mutableStateOf<GroupMember?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 80.dp), // Space for FAB
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Current user's balance overview
            item {
                Text(
                    "Your Balance",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )

                val currentUserMember = group.members.find { it.userId == currentUserId }
                if (currentUserMember != null) {
                    UserBalanceCard(
                        member = currentUserMember,
                        isCurrentUser = true,
                        nameResolver = nameResolver
                    )
                }
            }

            // Other members' balances
            item {
                Text(
                    "Group Members",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }

            // List all members except current user
            val otherMembers = group.members.filter { it.userId != currentUserId }
            items(otherMembers) { member ->
                val currentUserBalance = group.members
                    .find { it.userId == currentUserId }
                    ?.individualBalances
                    ?.get(member.userId) ?: 0.0

                MemberBalanceCard(
                    member = member,
                    currentUserBalance = currentUserBalance,
                    nameResolver = nameResolver,
                    onSettleUp = {
                        selectedMember = member
                        showSettleDialog = true
                    }
                )
            }

            // Bottom spacer for FAB
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Settle up dialog
    if (showSettleDialog && selectedMember != null) {
        val balance = group.members
            .find { it.userId == currentUserId }
            ?.individualBalances
            ?.get(selectedMember!!.userId) ?: 0.0

        SettleUpDialog(
            memberName = nameResolver.resolveDisplayName(selectedMember!!),
            amount = kotlin.math.abs(balance),
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
                .padding(16.dp)
        ) {
            Text(
                nameResolver.resolveDisplayName(member),
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

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
                    shape = RoundedCornerShape(16.dp)
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
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}
@Composable
fun MemberBalanceCard(
    member: GroupMember,
    currentUserBalance: Double,
    nameResolver: MemberNameResolver,
    onSettleUp: () -> Unit
) {
    val colors = LocalSplitColors.current

    SplitCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    nameResolver.resolveDisplayName(member),
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (currentUserBalance < 0) {
                        Text(
                            "You owe",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.error
                        )
                    } else if (currentUserBalance > 0) {
                        Text(
                            "Owes you",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.success
                        )
                    } else {
                        Text(
                            "Settled up",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary
                        )
                    }

                    CurrencyDisplay(
                        amount = kotlin.math.abs(currentUserBalance),
                        isIncome = currentUserBalance > 0
                    )
                }
            }

            // Settle button only if current user owes this member
            if (currentUserBalance < 0) {
                Button(
                    onClick = onSettleUp,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Settle Up")
                }
            }
        }
    }
}

@Composable
fun SettlementsTab(
    pendingSettlements: List<Settlement>,
    settlementHistory: List<Settlement>,
    currentUserId: String,
    nameResolver: MemberNameResolver,
    onApprove: (String) -> Unit,
    onDecline: (String) -> Unit
) {
    val colors = LocalSplitColors.current

    // Filter settlements
    val incomingRequests = pendingSettlements.filter { it.toUserId == currentUserId && it.status == SettlementStatus.PENDING }
    val outgoingRequests = pendingSettlements.filter { it.fromUserId == currentUserId && it.status == SettlementStatus.PENDING }
    val completedSettlements = settlementHistory.filter { it.status != SettlementStatus.PENDING }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Pending Requests Section (people requesting money from you)
        item {
            Text(
                "Requests For You",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
        }

        if (incomingRequests.isEmpty()) {
            item {
                EmptyState(
                    title = "No pending requests",
                    message = "You don't have any payment requests to approve",
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        } else {
            items(incomingRequests) { settlement ->
                PendingSettlementCard(
                    settlement = settlement,
                    isIncoming = true,
                    onApprove = { onApprove(settlement.id) },
                    onDecline = { onDecline(settlement.id) }
                )
            }
        }

        // Your Pending Requests Section (your requests to others)
        item {
            Text(
                "Your Pending Requests",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 24.dp, bottom = 4.dp)
            )
        }

        if (outgoingRequests.isEmpty()) {
            item {
                EmptyState(
                    title = "No outgoing requests",
                    message = "You haven't requested any payments yet",
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        } else {
            items(outgoingRequests) { settlement ->
                PendingSettlementCard(
                    settlement = settlement,
                    isIncoming = false,
                    onApprove = null,
                    onDecline = null
                )
            }
        }

        // Settlement History Section
        item {
            Text(
                "Settlement History",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 24.dp, bottom = 4.dp)
            )
        }

        if (completedSettlements.isEmpty()) {
            item {
                EmptyState(
                    title = "No settlement history",
                    message = "Past settlements will appear here",
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        } else {
            items(completedSettlements) { settlement ->
                SettlementHistoryCard(settlement = settlement)
            }
        }

        // Bottom spacer
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun PendingSettlementCard(
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
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        if (isIncoming) "Payment Request" else "Your Request",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textPrimary
                    )

                    Text(
                        if (isIncoming)
                            "${settlement.fromUserName ?: "Someone"} requested payment"
                        else
                            "Requested from ${settlement.toUserName ?: "someone"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary
                    )

                    Text(
                        formatDateTime(settlement.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }

                CurrencyDisplay(
                    amount = settlement.amount,
                    isIncome = false
                )
            }

            // Action buttons for incoming requests
            if (isIncoming && onApprove != null && onDecline != null) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onDecline,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.cardBackground,
                            contentColor = colors.error
                        ),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Decline")
                    }

                    Button(
                        onClick = onApprove,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
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
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${settlement.fromUserName ?: "Someone"} paid ${settlement.toUserName ?: "someone"}",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary
                )

                Text(
                    formatDateTime(settlement.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                    modifier = Modifier.size(24.dp)
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
                            modifier = Modifier.size(16.dp)
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
        title = { Text("Settle Up with $memberName") },
        text = {
            Column {
                Text(
                    "How much are you settling?",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

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
                    label = { Text("Amount") },
                    prefix = { Text("₹") },
                    isError = !isValidAmount,
                    singleLine = true
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
                Text("Settle")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
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
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = colors.primary.copy(alpha = 0.1f),
            modifier = Modifier.size(56.dp)
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
                    .padding(16.dp)
                    .size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

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
    val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val expenseDate = dateTime.date

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