package com.falcon.split.presentation.screens.mainNavigation

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.falcon.split.contact.ContactManager
import com.falcon.split.data.network.models_app.Group
import com.falcon.split.data.network.models_app.GroupMember
import com.falcon.split.data.network.models_app.SettlementState
import com.falcon.split.presentation.group.GroupState
import com.falcon.split.presentation.group.GroupViewModel
import com.falcon.split.presentation.theme.CurrencyDisplay
import com.falcon.split.presentation.theme.LocalSplitColors
import com.falcon.split.presentation.theme.SplitCard
import com.falcon.split.presentation.theme.lDimens
import com.falcon.split.utils.MemberNameResolver

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettleUpScreen(
    navController: NavHostController,
    viewModel: GroupViewModel,
    contactManager: ContactManager? = null,
    onNavigateBack: () -> Unit
) {
    val colors = LocalSplitColors.current

    // ViewModel state
    val groupState by viewModel.groupState.collectAsState()
    val settlementState by viewModel.settlementState.collectAsState()
    val currentUserId = viewModel.currentUserId ?: ""

    // Screen state
    var selectedGroup by remember { mutableStateOf<Group?>(null) }
    var showGroupDropdown by remember { mutableStateOf(false) }

    // Name resolver for displaying member names
    val nameResolver = remember { MemberNameResolver(contactManager) }

    // Load groups when screen is mounted
    LaunchedEffect(Unit) {
        viewModel.loadGroups()
    }

    // When groups are loaded, set the default selected group (most recent one)
    LaunchedEffect(groupState) {
        if (groupState is GroupState.Success) {
            val groups = (groupState as GroupState.Success).groups
            if (groups.isNotEmpty() && selectedGroup == null) {
                // Select the most recently created group by default
                selectedGroup = groups.maxByOrNull { it.createdAt ?: 0L }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(
                    "Settle Up",
                    color = colors.textPrimary
                ) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.backgroundSecondary,
                    titleContentColor = colors.textPrimary,
                    navigationIconContentColor = colors.textPrimary
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(colors.backgroundPrimary)
        ) {
            when (groupState) {
                is GroupState.Loading -> {
                    LoadingState()
                }

                is GroupState.Error -> {
                    ErrorState(
                        message = (groupState as GroupState.Error).message,
                        onRetry = { viewModel.retryLoading() }
                    )
                }

                is GroupState.Empty -> {
                    EmptyGroupsState(
                        message = "You don't have any groups yet",
                        submessage = "Create a group first to settle up payments",
                        onCreateGroup = { navController.navigate(Routes.CREATE_GROUP.name) }
                    )
                }

                is GroupState.Success -> {
                    val groups = (groupState as GroupState.Success).groups
                    if (groups.isEmpty()) {
                        EmptyGroupsState(
                            message = "You don't have any groups yet",
                            submessage = "Create a group first to settle up payments",
                            onCreateGroup = { navController.navigate(Routes.CREATE_GROUP.name) }
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = lDimens.dp16)
                        ) {
                            // Group selection dropdown
                            GroupSelectionDropdown(
                                selectedGroup = selectedGroup,
                                groups = groups,
                                showDropdown = showGroupDropdown,
                                onShowDropdownChange = { showGroupDropdown = it },
                                onGroupSelected = { group ->
                                    selectedGroup = group
                                    showGroupDropdown = false
                                }
                            )

                            Spacer(modifier = Modifier.height(lDimens.dp16))

                            // Show balances for the selected group
                            if (selectedGroup != null) {
                                BalancesList(
                                    group = selectedGroup!!,
                                    currentUserId = currentUserId,
                                    nameResolver = nameResolver,
                                    onSettleUp = { toUserId, amount ->
                                        viewModel.settleBalance(selectedGroup!!.id, toUserId, amount)
                                    },
                                    onPay = { toUserId, amount ->
                                        // Navigate to Payment screen
                                        navController.navigate("${Routes.PAYMENT_SCREEN.name}/$toUserId/$amount/${selectedGroup!!.id}")
                                    }
                                )
                            }
                        }
                    }
                }

                else -> {
                    // Fallback for unexpected states
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Unexpected state")
                    }
                }
            }

            // Show settlement result dialog
            when (settlementState) {
                is SettlementState.Success -> {
                    SettlementResultDialog(
                        success = true,
                        message = "Settlement request sent successfully.",
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
fun GroupSelectionDropdown(
    selectedGroup: Group?,
    groups: List<Group>,
    showDropdown: Boolean,
    onShowDropdownChange: (Boolean) -> Unit,
    onGroupSelected: (Group) -> Unit
) {
    val colors = LocalSplitColors.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = lDimens.dp8)
    ) {
        OutlinedTextField(
            value = selectedGroup?.name ?: "",
            onValueChange = { /* Read-only */ },
            label = { Text(
                "Select Group",
                color = colors.textSecondary
            ) },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { onShowDropdownChange(!showDropdown) }) {
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = "Select Group",
                        tint = colors.textPrimary
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = colors.cardBackground,
                unfocusedContainerColor = colors.cardBackground,
                focusedBorderColor = colors.primary,
                unfocusedBorderColor = colors.border
            )
        )

        DropdownMenu(
            expanded = showDropdown,
            onDismissRequest = { onShowDropdownChange(false) },
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            groups.forEach { group ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = group.name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.textPrimary
                        )
                    },
                    onClick = { onGroupSelected(group) }
                )
            }
        }
    }
}

@Composable
fun BalancesList(
    group: Group,
    currentUserId: String,
    nameResolver: MemberNameResolver,
    onSettleUp: (toUserId: String, amount: Double) -> Unit,
    onPay: (toUserId: String, amount: Double) -> Unit
) {
    var showSettleDialog by remember { mutableStateOf(false) }
    var selectedMember by remember { mutableStateOf<GroupMember?>(null) }

    // Get current user's member object
    val currentUserMember = group.members.find { it.userId == currentUserId }

    // Get individual balances
    val individualBalances = currentUserMember?.individualBalances ?: emptyMap()

    // All other members (excluding current user)
    val otherMembers = group.members.filter { it.userId != currentUserId }

    Column {
        Text(
            "Balances in ${group.name}",
            style = MaterialTheme.typography.titleLarge,
            color = LocalSplitColors.current.textPrimary
        )

        Spacer(modifier = Modifier.height(lDimens.dp16))

        // Current user's overall balance
        if (currentUserMember != null) {
            UserBalanceCard(
                member = currentUserMember,
                isCurrentUser = true,
                nameResolver = nameResolver
            )

            Spacer(modifier = Modifier.height(lDimens.dp16))
        }

        // List of group members with settlement options
        if (otherMembers.isEmpty()) {
            Text(
                text = "No other members in this group",
                style = MaterialTheme.typography.bodyLarge,
                color = LocalSplitColors.current.textSecondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = lDimens.dp24),
                textAlign = TextAlign.Center
            )
        } else {
            Text(
                "Members",
                style = MaterialTheme.typography.titleMedium,
                color = LocalSplitColors.current.textPrimary
            )

            Spacer(modifier = Modifier.height(lDimens.dp8))

            LazyColumn(
                contentPadding = PaddingValues(bottom = lDimens.dp16),
                verticalArrangement = Arrangement.spacedBy(lDimens.dp8)
            ) {
                items(otherMembers) { member ->
                    // Calculate balance with this member
                    val currentUserBalance = if (member.userId != null) {
                        individualBalances[member.userId] ?: 0.0
                    } else {
                        // For members without userIds, check their individualBalances directly
                        -(member.individualBalances[currentUserId] ?: 0.0) // Negate because the perspective is reversed
                    }

                    val isIncoming = currentUserBalance > 0  // Positive means they owe me
                    val isRegistered = member.userId != null
                    val memberName = nameResolver.resolveDisplayName(member)

                    SettlementMemberCard(
                        name = memberName,
                        amount = kotlin.math.abs(currentUserBalance),
                        isIncoming = isIncoming,
                        isRegistered = isRegistered,
                        onSettleUp = if (!isIncoming && isRegistered && currentUserBalance != 0.0) {
                            {
                                selectedMember = member
                                showSettleDialog = true
                            }
                        } else null,
                        onPay = if (!isIncoming && isRegistered && currentUserBalance != 0.0) {
                            {
                                onPay(member.userId!!, kotlin.math.abs(currentUserBalance))
                            }
                        } else null,
                        onRemind = if (isIncoming && isRegistered && currentUserBalance != 0.0) {
                            { /* Handle remind functionality */ }
                        } else null
                    )
                }
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
fun SettlementMemberCard(
    name: String,
    amount: Double,
    isIncoming: Boolean,
    isRegistered: Boolean,
    onSettleUp: (() -> Unit)?,
    onPay: (() -> Unit)?,
    onRemind: (() -> Unit)?
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Member info
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar/icon
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = colors.primary.copy(alpha = 0.1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(lDimens.dp12))

                    Column {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.textPrimary
                        )

                        if (amount > 0) {
                            Text(
                                text = if (isIncoming) "owes you" else "you owe",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.textSecondary
                            )
                        } else {
                            Text(
                                text = "all settled up",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.success
                            )
                        }
                    }
                }

                // Amount
                CurrencyDisplay(
                    amount = amount,
                    isIncome = isIncoming,
                    large = false
                )
            }

            // Action buttons for users who owe money
            if (amount > 0 && (onSettleUp != null || onPay != null || onRemind != null)) {
                Spacer(modifier = Modifier.height(lDimens.dp12))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Remind button (for incoming debts)
                    if (onRemind != null) {
                        Button(
                            onClick = onRemind,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primary
                            ),
                            modifier = Modifier.padding(end = lDimens.dp8)
                        ) {
                            Text("Remind")
                        }
                    }

                    // Pay button (for outgoing debts)
                    if (onPay != null) {
                        OutlinedButton(
                            onClick = onPay,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = colors.success
                            ),
                            modifier = Modifier.padding(end = lDimens.dp8)
                        ) {
                            Text(
                                "Pay",
                                color = colors.textPrimary
                            )
                        }
                    }

                    // Settle Up button (for outgoing debts)
                    if (onSettleUp != null) {
                        Button(
                            onClick = onSettleUp,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.success
                            )
                        ) {
                            Text("Settle Up")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyGroupsState(
    message: String,
    submessage: String,
    onCreateGroup: () -> Unit
) {
    val colors = LocalSplitColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(lDimens.dp24),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            color = colors.primary.copy(alpha = 0.1f),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.size(80.dp)
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier
                    .padding(lDimens.dp16)
                    .size(lDimens.dp48),
                tint = colors.primary
            )
        }

        Spacer(modifier = Modifier.height(lDimens.dp24))

        Text(
            message,
            style = MaterialTheme.typography.titleLarge,
            color = colors.textPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(lDimens.dp8))

        Text(
            submessage,
            style = MaterialTheme.typography.bodyLarge,
            color = colors.textSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(lDimens.dp32))

        Button(
            onClick = onCreateGroup,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary,
                contentColor = Color.White
            )
        ) {
            Text("Create Group")
        }
    }
}
