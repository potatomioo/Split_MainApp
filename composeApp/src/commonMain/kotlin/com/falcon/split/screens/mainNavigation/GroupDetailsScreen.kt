package com.falcon.split.screens.mainNavigation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.falcon.split.data.network.models_app.Expense
import com.falcon.split.data.network.models_app.ExpenseSplit
import com.falcon.split.data.network.models_app.Group
import kotlinx.datetime.Clock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailsScreen(
    groupId: String,
    onNavigateBack: () -> Unit,
    onAddExpense: (String) -> Unit
) {
    // Dummy data for the group
    val group = remember {
        Group(
            groupId = groupId,
            name = "Weekend Trip to Goa",
            members = listOf("user1", "user2", "user3", "user4"),
            createdBy = "user1",
            createdAt = Clock.System.now()
        )
    }

    // Dummy data for members
    val memberNames = remember {
        mapOf(
            "user1" to "John Doe",
            "user2" to "Jane Smith",
            "user3" to "Mike Johnson",
            "user4" to "Sarah Wilson"
        )
    }

    // Dummy expenses
    val expenses = remember {
        listOf(
            Expense(
                expenseId = "1",
                groupId = groupId,
                description = "Hotel Booking",
                amount = 12000.0,
                paidBy = "user1",
                createdAt = Clock.System.now(),
                splitBetween = listOf(
                    ExpenseSplit("user1", 3000.0),
                    ExpenseSplit("user2", 3000.0),
                    ExpenseSplit("user3", 3000.0),
                    ExpenseSplit("user4", 3000.0)
                )
            ),
            Expense(
                expenseId = "2",
                groupId = groupId,
                description = "Dinner",
                amount = 4000.0,
                paidBy = "user2",
                createdAt = Clock.System.now(),
                splitBetween = listOf(
                    ExpenseSplit("user1", 1000.0),
                    ExpenseSplit("user2", 1000.0),
                    ExpenseSplit("user3", 1000.0),
                    ExpenseSplit("user4", 1000.0)
                )
            ),
            Expense(
                expenseId = "3",
                groupId = groupId,
                description = "Taxi",
                amount = 1600.0,
                paidBy = "user3",
                createdAt = Clock.System.now(),
                splitBetween = listOf(
                    ExpenseSplit("user1", 400.0),
                    ExpenseSplit("user2", 400.0),
                    ExpenseSplit("user3", 400.0),
                    ExpenseSplit("user4", 400.0)
                )
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(group.name) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Open settings */ }) {
                        Icon(Icons.Default.MoreVert, "More options")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAddExpense(groupId) }
            ) {
                Icon(Icons.Default.Add, "Add Expense")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                GroupSummaryCard(
                    totalAmount = expenses.sumOf { it.amount },
                    expenseCount = expenses.size,
                    memberCount = group.members.size
                )
            }

            item {
                MembersCard(
                    members = group.members,
                    memberNames = memberNames
                )
            }

            items(expenses) { expense ->
                ExpenseCard(
                    expense = expense,
                    paidByName = memberNames[expense.paidBy] ?: "Unknown"
                )
            }
        }
    }
}

@Composable
private fun GroupSummaryCard(
    totalAmount: Double,
    expenseCount: Int,
    memberCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Total Expenses",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                "₹${((totalAmount * 100).toInt() / 100.0)}",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("$expenseCount expenses")
                Text("$memberCount members")
            }
        }
    }
}

@Composable
private fun MembersCard(
    members: List<String>,
    memberNames: Map<String, String>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Members",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            members.forEach { memberId ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(memberNames[memberId] ?: "Unknown")
                }
            }
        }
    }
}

@Composable
private fun ExpenseCard(
    expense: Expense,
    paidByName: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
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
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "₹${((expense.amount * 100).toInt() / 100.0)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Paid by $paidByName",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}