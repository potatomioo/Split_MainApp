package com.falcon.split.presentation.screens.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.navigation.NavHostController
import com.falcon.split.MainViewModel
import com.falcon.split.data.network.models_app.Transaction
import com.falcon.split.data.network.models_app.TransactionStatus
import com.falcon.split.data.network.models_app.TransactionType
import com.falcon.split.presentation.theme.LocalSplitColors
import com.falcon.split.presentation.theme.lDimens
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

/**
 * History screen that displays all transaction history
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onNavigate: (rootName: String) -> Unit,
    prefs: DataStore<Preferences>,
    newsViewModel: MainViewModel,
    snackBarHostState: SnackbarHostState,
    navControllerMain: NavHostController
) {
    val colors = LocalSplitColors.current
    val scope = rememberCoroutineScope()

    // State
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(TransactionFilter.ALL) }
    var showFilterOptions by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }

    // Sample Transactions (replace with real data from your repository)
    val transactions = remember { getSampleTransactions() }
    val filteredTransactions = remember(transactions, searchQuery, selectedFilter) {
        var result = transactions

        // Apply search filter
        if (searchQuery.isNotEmpty()) {
            result = result.filter {
                it.description.contains(searchQuery, ignoreCase = true) ||
                        it.groupName.contains(searchQuery, ignoreCase = true)
            }
        }

        // Apply type filter
        when (selectedFilter) {
            TransactionFilter.ALL -> result
            TransactionFilter.EXPENSES -> result.filter { it.type == TransactionType.EXPENSE }
            TransactionFilter.SETTLEMENTS -> result.filter { it.type == TransactionType.SETTLEMENT }
        }
    }

    // Group transactions by date
    val groupedTransactions = remember(filteredTransactions) {
        groupTransactionsByDate(filteredTransactions)
    }

    // UI state
    val lazyListState = rememberLazyListState()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundPrimary)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = lDimens.dp16, vertical = lDimens.dp8)
                    .height(lDimens.dp56)
                    .clip(RoundedCornerShape(28.dp))
                    .focusRequester(focusRequester),
                placeholder = {
                    Text(
                        text = "Search transactions",
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
                    AnimatedVisibility(visible = searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            searchQuery = ""
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
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        focusManager.clearFocus()
                    }
                ),
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
                            imageVector = Icons.Default.ThumbUp,
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

                AnimatedVisibility(
                    visible = selectedFilter != TransactionFilter.ALL,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    // Active Filter Chip
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
                                text = when(selectedFilter) {
                                    TransactionFilter.EXPENSES -> "Expenses Only"
                                    TransactionFilter.SETTLEMENTS -> "Settlements Only"
                                    else -> ""
                                },
                                color = colors.primary,
                                style = MaterialTheme.typography.labelMedium
                            )

                            IconButton(
                                onClick = { selectedFilter = TransactionFilter.ALL },
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

            // Filter Options Row
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
                        color = if (selectedFilter == TransactionFilter.ALL) colors.primary else colors.cardBackground,
                        onClick = { selectedFilter = TransactionFilter.ALL }
                    ) {
                        Text(
                            text = "All",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            color = if (selectedFilter == TransactionFilter.ALL) Color.White else colors.textPrimary,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    Surface(
                        modifier = Modifier.clip(RoundedCornerShape(16.dp)),
                        color = if (selectedFilter == TransactionFilter.EXPENSES) colors.primary else colors.cardBackground,
                        onClick = { selectedFilter = TransactionFilter.EXPENSES }
                    ) {
                        Text(
                            text = "Expenses",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            color = if (selectedFilter == TransactionFilter.EXPENSES) Color.White else colors.textPrimary,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    Surface(
                        modifier = Modifier.clip(RoundedCornerShape(16.dp)),
                        color = if (selectedFilter == TransactionFilter.SETTLEMENTS) colors.primary else colors.cardBackground,
                        onClick = { selectedFilter = TransactionFilter.SETTLEMENTS }
                    ) {
                        Text(
                            text = "Settlements",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            color = if (selectedFilter == TransactionFilter.SETTLEMENTS) Color.White else colors.textPrimary,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            // Transaction List
            if (filteredTransactions.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ThumbUp,
                        contentDescription = null,
                        modifier = Modifier
                            .size(80.dp)
                            .padding(16.dp),
                        tint = colors.textSecondary.copy(alpha = 0.5f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (searchQuery.isEmpty()) "No Transactions Yet" else "No Results Found",
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.textPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (searchQuery.isEmpty())
                            "Your transaction history will appear here once you start creating expenses or settlements."
                        else
                            "Try adjusting your search query to find what you're looking for.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // Transactions list
                LazyColumn(
                    state = lazyListState,
                    contentPadding = PaddingValues(horizontal = lDimens.dp16, vertical = lDimens.dp8),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (searchQuery.isNotEmpty()) {
                        item {
                            Text(
                                "Search results",
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.textPrimary,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        items(filteredTransactions) { transaction ->
                            TransactionCard(transaction = transaction)
                        }
                    } else {
                        groupedTransactions.forEach { (timePeriod, transactions) ->
                            item {
                                Text(
                                    text = timePeriod,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = colors.textPrimary,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }

                            items(transactions) { transaction ->
                                TransactionCard(transaction = transaction)
                            }
                        }
                    }

                    // Add padding at the bottom
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        // Refresh button
        FloatingActionButton(
            onClick = {
                scope.launch {
                    isRefreshing = true
                    // Simulate refresh
                    kotlinx.coroutines.delay(1500)
                    isRefreshing = false
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = colors.primary,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
        }

        // Loading indicator
        if (isRefreshing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = colors.primary)
            }
        }
    }
}

@Composable
fun TransactionCard(
    transaction: Transaction
) {
    val colors = LocalSplitColors.current

    // Format date and time
    val dateTime = remember(transaction.timestamp) {
        val instant = Instant.fromEpochMilliseconds(transaction.timestamp)
        val local = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        "${local.hour}:${local.minute.toString().padStart(2, '0')}"
    }

    // Determine if this is an income or expense for the current user
    // In a real app, you would compare with the current user's ID
    val isIncome = transaction.paidByUserId != "current_user_id"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Navigate to transaction details */ },
        colors = CardDefaults.cardColors(
            containerColor = colors.cardBackground
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left section with icon and description
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Transaction type icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            when (transaction.type) {
                                TransactionType.EXPENSE -> colors.primary.copy(alpha = 0.1f)
                                TransactionType.SETTLEMENT -> colors.success.copy(alpha = 0.1f)
                                TransactionType.ADJUSTMENT -> colors.warning.copy(alpha = 0.1f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (transaction.type) {
                            TransactionType.EXPENSE -> Icons.Default.ThumbUp
                            TransactionType.SETTLEMENT -> Icons.Default.ThumbUp
                            TransactionType.ADJUSTMENT -> Icons.Default.Edit
                        },
                        contentDescription = null,
                        tint = when (transaction.type) {
                            TransactionType.EXPENSE -> colors.primary
                            TransactionType.SETTLEMENT -> colors.success
                            TransactionType.ADJUSTMENT -> colors.warning
                        },
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Description and group info
                Column {
                    Text(
                        text = transaction.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = transaction.groupName,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )

                    if (transaction.type == TransactionType.SETTLEMENT) {
                        val statusText = when (transaction.status) {
                            TransactionStatus.PENDING -> "Pending"
                            TransactionStatus.COMPLETED -> "Completed"
                            TransactionStatus.CANCELLED -> "Cancelled"
                        }
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.bodySmall,
                            color = when (transaction.status) {
                                TransactionStatus.PENDING -> colors.warning
                                TransactionStatus.COMPLETED -> colors.success
                                TransactionStatus.CANCELLED -> colors.error
                            }
                        )
                    }
                }
            }

            // Right section with amount and time
            Column(
                horizontalAlignment = Alignment.End
            ) {
                // Amount
                Text(
                    text = "₹${transaction.amount}",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isIncome) colors.success else colors.error,
                    fontWeight = FontWeight.SemiBold
                )

                // Time
                Text(
                    text = dateTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }
        }
    }
}

enum class TransactionFilter {
    ALL, EXPENSES, SETTLEMENTS
}

// Helper function to group transactions by date period
private fun groupTransactionsByDate(transactions: List<Transaction>): Map<String, List<Transaction>> {
    val now = kotlinx.datetime.Clock.System.now()
    val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
    val yesterday = today.minus(kotlinx.datetime.DatePeriod(days = 1))

    // Group by time period
    val grouped = transactions.groupBy { transaction ->
        val instant = Instant.fromEpochMilliseconds(transaction.timestamp)
        val transactionDate = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date

        when {
            transactionDate == today -> "Today"
            transactionDate == yesterday -> "Yesterday"
            today.minus(kotlinx.datetime.DatePeriod(days = 7)) < transactionDate -> "This Week"
            today.minus(kotlinx.datetime.DatePeriod(days = 30)) < transactionDate -> "This Month"
            else -> "Earlier"
        }
    }

    // Define the order we want
    val order = listOf("Today", "Yesterday", "This Week", "This Month", "Earlier")

    // Create a new linked map with the desired order
    val result = linkedMapOf<String, List<Transaction>>()

    // Add entries in the specific order (if they exist)
    order.forEach { key ->
        grouped[key]?.let { result[key] = it }
    }

    return result
}

// Sample data for testing
private fun getSampleTransactions(): List<Transaction> {
    val currentTime = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
    val oneDayMs = 24 * 60 * 60 * 1000L

    return listOf(
        Transaction(
            id = "1",
            groupId = "group1",
            groupName = "Weekend Trip",
            description = "Dinner at Restaurant",
            amount = 120.50,
            type = TransactionType.EXPENSE,
            timestamp = currentTime - 3 * 60 * 60 * 1000, // 3 hours ago
            paidByUserId = "user1",
            paidByUserName = "Alex",
            status = TransactionStatus.COMPLETED
        ),
        Transaction(
            id = "2",
            groupId = "group2",
            groupName = "Apartment",
            description = "Monthly Rent",
            amount = 450.00,
            type = TransactionType.EXPENSE,
            timestamp = currentTime - 8 * 60 * 60 * 1000, // 8 hours ago
            paidByUserId = "user2",
            paidByUserName = "John",
            status = TransactionStatus.COMPLETED
        ),
        Transaction(
            id = "3",
            groupId = "group1",
            groupName = "Weekend Trip",
            description = "Settlement",
            amount = 55.25,
            type = TransactionType.SETTLEMENT,
            timestamp = currentTime - oneDayMs, // Yesterday
            paidByUserId = "user3",
            paidByUserName = "Emma",
            status = TransactionStatus.PENDING
        ),
        Transaction(
            id = "4",
            groupId = "group3",
            groupName = "Office Lunch",
            description = "Pizza Order",
            amount = 78.00,
            type = TransactionType.EXPENSE,
            timestamp = currentTime - 2 * oneDayMs, // 2 days ago
            paidByUserId = "user1",
            paidByUserName = "Alex",
            status = TransactionStatus.COMPLETED
        ),
        Transaction(
            id = "5",
            groupId = "group2",
            groupName = "Apartment",
            description = "Electricity Bill",
            amount = 96.50,
            type = TransactionType.EXPENSE,
            timestamp = currentTime - 5 * oneDayMs, // 5 days ago
            paidByUserId = "user1",
            paidByUserName = "Alex",
            status = TransactionStatus.COMPLETED
        ),
        Transaction(
            id = "6",
            groupId = "group1",
            groupName = "Weekend Trip",
            description = "Settlement",
            amount = 120.75,
            type = TransactionType.SETTLEMENT,
            timestamp = currentTime - 10 * oneDayMs, // 10 days ago
            paidByUserId = "user4",
            paidByUserName = "Sarah",
            status = TransactionStatus.COMPLETED
        ),
        Transaction(
            id = "7",
            groupId = "group3",
            groupName = "Office Lunch",
            description = "Coffee Run",
            amount = 32.40,
            type = TransactionType.EXPENSE,
            timestamp = currentTime - 15 * oneDayMs, // 15 days ago
            paidByUserId = "user2",
            paidByUserName = "John",
            status = TransactionStatus.COMPLETED
        ),
        Transaction(
            id = "8",
            groupId = "group4",
            groupName = "Movie Night",
            description = "Tickets",
            amount = 64.00,
            type = TransactionType.EXPENSE,
            timestamp = currentTime - 28 * oneDayMs, // 28 days ago
            paidByUserId = "user3",
            paidByUserName = "Emma",
            status = TransactionStatus.COMPLETED
        )
    )
}
