package com.falcon.split.presentation.screens.mainNavigation


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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.navigation.NavHostController
import com.falcon.split.MainViewModel
import com.falcon.split.data.network.models_app.Group
import com.falcon.split.data.network.models_app.GroupMember
import com.falcon.split.data.network.models_app.Settlement
import com.falcon.split.data.network.models_app.SettlementStatus
import com.falcon.split.presentation.screens.AnimationComponents.UpwardFlipHeaderImage
import com.falcon.split.presentation.theme.CurrencyDisplay
import com.falcon.split.presentation.theme.LocalSplitColors
import com.falcon.split.presentation.theme.SplitCard
import com.falcon.split.presentation.theme.lDimens
import kotlinx.datetime.Clock
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
    topPadding: androidx.compose.ui.unit.Dp
) {
    val colors = LocalSplitColors.current

    // Sample data until connected to repository
    val userName = "Alex"
    val totalToReceive = 475.0
    val totalToPay = 181.67

    // Get time-based greeting
    val greeting = getGreeting(userName)

    // Sample data for recent activity
    val recentActivity = remember { getSampleRecentActivity() }

    // Sample data for user's groups
    val userGroups = remember { getSampleGroups() }

    // Sample data for pending settlements
    val pendingSettlements = remember { getSampleSettlements() }

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
                    defaultElevation = 6.dp,
                    pressedElevation = lDimens.dp12
                )
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                start = 0.dp,
                top = padding.calculateTopPadding(),
                end = 0.dp,
                bottom = padding.calculateBottomPadding() + 80.dp // Extra space for FAB
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
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
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                    ) {
                        // Balance overview card
                        SplitCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 100.dp) // Positioned below the header image
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
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
                                        .padding(top = 16.dp),
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
                                        Spacer(modifier = Modifier.height(4.dp))
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
                                        Spacer(modifier = Modifier.height(4.dp))
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
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Create group button
                    QuickActionButton(
                        icon = Icons.Default.Person,
                        text = "New Group",
                        onClick = { navControllerMain.navigate("create_group") },
                        modifier = Modifier.weight(1f)
                    )

                    // Add expense button
                    QuickActionButton(
                        icon = Icons.Default.Person,
                        text = "Add Expense",
                        onClick = { navControllerMain.navigate("create_expense") },
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
            item {
                SectionHeader(
                    title = "Recent Activity",
                    actionText = "View All",
                    onActionClick = { /* Navigate to full history */ }
                )

                if (recentActivity.isEmpty()) {
                    EmptyStateMessage(
                        message = "No recent activity",
                        submessage = "Your recent expenses and settlements will appear here"
                    )
                } else {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Group by date
                        val groupedActivity = recentActivity.groupBy { it.date }

                        groupedActivity.forEach { (date, activities) ->
                            Text(
                                text = date,
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.textSecondary,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            activities.forEach { activity ->
                                ActivityItem(activity)
                            }
                        }
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

                if (userGroups.isEmpty()) {
                    EmptyStateMessage(
                        message = "No groups yet",
                        submessage = "Create a group to start tracking expenses with friends"
                    )
                } else {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(userGroups) { group ->
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

            // Pending Settlements section
            item {
                if (pendingSettlements.isNotEmpty()) {
                    SectionHeader(
                        title = "Pending Settlements",
                        actionText = null,
                        onActionClick = null
                    )

                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        pendingSettlements.forEach { settlement ->
                            SettlementItem(settlement)
                        }
                    }
                }
            }

            // Bottom spacer for FAB
            item {
                Spacer(modifier = Modifier.height(80.dp))
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
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = colors.cardBackground,
            contentColor = colors.textPrimary
        ),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
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
            .padding(horizontal = 16.dp, vertical = 8.dp),
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

@Composable
fun ActivityItem(activity: RecentActivity) {
    val colors = LocalSplitColors.current

    SplitCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = activity.groupName,
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textSecondary
                )

                Text(
                    text = activity.description,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = activity.participants,
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
                    amount = activity.amount,
                    isIncome = activity.isIncome
                )

                Text(
                    text = activity.time,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }
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
            .width(150.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.cardBackground
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Group icon
            Icon(
                painter = painterResource(Res.drawable.group_icon_outlined),
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier
                    .size(40.dp)
                    .padding(4.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

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

            Spacer(modifier = Modifier.height(8.dp))

            // Balance
            CurrencyDisplay(
                amount = group.totalAmount ?: 0.0,
                isIncome = (group.totalAmount ?: 0.0) >= 0
            )
        }
    }
}

@Composable
fun SettlementItem(settlement: Settlement) {
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
                        text = "Payment Request",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textPrimary
                    )

                    Text(
                        text = "${settlement.fromUserName ?: "Someone"} requested",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary
                    )
                }

                CurrencyDisplay(
                    amount = settlement.amount,
                    isIncome = false
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ElevatedButton(
                    onClick = { /* Decline settlement */ },
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = colors.cardBackground,
                        contentColor = colors.error
                    ),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text("Decline")
                }

                ElevatedButton(
                    onClick = { /* Approve settlement */ },
                    colors = ButtonDefaults.elevatedButtonColors(
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

@Composable
fun EmptyStateMessage(
    message: String,
    submessage: String
) {
    val colors = LocalSplitColors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

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

// Sample data classes and functions
data class RecentActivity(
    val groupName: String,
    val description: String,
    val amount: Double,
    val isIncome: Boolean,
    val participants: String,
    val date: String,
    val time: String
)

// Sample data functions
fun getSampleRecentActivity(): List<RecentActivity> {
    return listOf(
        RecentActivity(
            groupName = "Weekend Trip",
            description = "Dinner at Restaurant",
            amount = 120.0,
            isIncome = false,
            participants = "You paid, 4 people involved",
            date = "Today",
            time = "7:30 PM"
        ),
        RecentActivity(
            groupName = "Apartment",
            description = "Monthly Rent",
            amount = 350.0,
            isIncome = true,
            participants = "John paid you",
            date = "Today",
            time = "2:15 PM"
        ),
        RecentActivity(
            groupName = "Office Lunch",
            description = "Pizza Party",
            amount = 45.75,
            isIncome = false,
            participants = "Sarah paid, you owe",
            date = "Yesterday",
            time = "1:00 PM"
        ),
        RecentActivity(
            groupName = "Movie Night",
            description = "Tickets & Popcorn",
            amount = 32.50,
            isIncome = true,
            participants = "Mike paid you",
            date = "Yesterday",
            time = "9:20 AM"
        )
    )
}

fun getSampleGroups(): List<Group> {
    // Create sample group members
    val members1 = listOf(
        GroupMember(userId = "user1", phoneNumber = "1234567890", name = "You", balance = 50.0),
        GroupMember(userId = "user2", phoneNumber = "2345678901", name = "John", balance = -25.0),
        GroupMember(userId = "user3", phoneNumber = "3456789012", name = "Sarah", balance = -25.0)
    )

    val members2 = listOf(
        GroupMember(userId = "user1", phoneNumber = "1234567890", name = "You", balance = -180.0),
        GroupMember(userId = "user4", phoneNumber = "4567890123", name = "Mike", balance = 60.0),
        GroupMember(userId = "user5", phoneNumber = "5678901234", name = "Emma", balance = 60.0),
        GroupMember(userId = "user6", phoneNumber = "6789012345", name = "Dave", balance = 60.0)
    )

    val members3 = listOf(
        GroupMember(userId = "user1", phoneNumber = "1234567890", name = "You", balance = 0.0),
        GroupMember(userId = "user7", phoneNumber = "7890123456", name = "Lisa", balance = 0.0),
        GroupMember(userId = "user8", phoneNumber = "8901234567", name = "Tom", balance = 0.0)
    )

    return listOf(
        Group(
            id = "group1",
            name = "Weekend Trip",
            createdBy = "user1",
            members = members1,
            createdAt = Clock.System.now().toEpochMilliseconds() - 1000000,
            totalAmount = 50.0
        ),
        Group(
            id = "group2",
            name = "Apartment",
            createdBy = "user4",
            members = members2,
            createdAt = Clock.System.now().toEpochMilliseconds() - 5000000,
            totalAmount = -180.0
        ),
        Group(
            id = "group3",
            name = "Movie Night",
            createdBy = "user1",
            members = members3,
            createdAt = Clock.System.now().toEpochMilliseconds() - 10000000,
            totalAmount = 0.0
        )
    )
}

fun getSampleSettlements(): List<Settlement> {
    return listOf(
        Settlement(
            id = "settlement1",
            groupId = "group1",
            fromUserId = "user2",
            toUserId = "user1",
            amount = 25.0,
            timestamp = Clock.System.now().toEpochMilliseconds() - 100000,
            status = SettlementStatus.PENDING,
            fromUserName = "John",
            toUserName = "You"
        ),
        Settlement(
            id = "settlement2",
            groupId = "group2",
            fromUserId = "user1",
            toUserId = "user4",
            amount = 60.0,
            timestamp = Clock.System.now().toEpochMilliseconds() - 200000,
            status = SettlementStatus.PENDING,
            fromUserName = "You",
            toUserName = "Mike"
        )
    )
}