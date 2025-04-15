package com.falcon.split.presentation.screens.mainNavigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.falcon.split.data.network.models_app.Group
import com.falcon.split.data.network.models_app.GroupType
import com.falcon.split.presentation.group.GroupState
import com.falcon.split.presentation.group.GroupViewModel
import com.falcon.split.presentation.history.HistoryItem
import com.falcon.split.presentation.screens.mainNavigation.AnimationComponents.UpwardFlipHeaderImage
import com.falcon.split.presentation.theme.CurrencyDisplay
import com.falcon.split.presentation.theme.LocalSplitColors
import com.falcon.split.presentation.theme.SplitCard
import com.falcon.split.presentation.theme.lDimens
import com.falcon.split.util.DateTimeUtil
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import split.composeapp.generated.resources.GroupPic
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.group_icon_filled
import split.composeapp.generated.resources.group_icon_outlined

enum class GroupFilter {
    ALL, ACTIVE, ARCHIVED
}

enum class GroupSortOption {
    NEWEST, BALANCE, ACTIVITY
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalResourceApi::class, ExperimentalFoundationApi::class)
@Composable
fun GroupsScreen(
    onCreateGroupClick: () -> Unit,
    onGroupClick: (Group) -> Unit,
    navControllerMain: NavHostController,
    viewModel: GroupViewModel
) {
    val colors = LocalSplitColors.current
    val groupsState by viewModel.groupState.collectAsState()
    val lazyState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // For pager state (needed for header animation)
    val pagerState = rememberPagerState { 1 }

    // Search state
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    // Filter and sort state
    var selectedFilter by remember { mutableStateOf(GroupFilter.ALL) }
    var selectedSortOption by remember { mutableStateOf(GroupSortOption.NEWEST) }
    var showFilterOptions by remember { mutableStateOf(false) }
    var showSortOptions by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = colors.backgroundPrimary,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateGroupClick,
                containerColor = colors.primary,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = "Add Group") },
                text = { Text("Create Group", style = MaterialTheme.typography.labelLarge) },
                expanded = !lazyState.isScrollInProgress
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.backgroundPrimary)
                .padding(padding)
        ) {
            when (groupsState) {
                is GroupState.Loading -> {
                    LoadingState()
                }

                is GroupState.Empty -> {
                    EmptyGroupsState(onCreateGroupClick)
                }

                is GroupState.Error -> {
                    ErrorState(
                        message = (groupsState as GroupState.Error).message,
                        onRetry = { viewModel.retryLoading() }
                    )
                }

                is GroupState.Success -> {
                    val groups = (groupsState as GroupState.Success).groups

                    // Apply filters and search
                    val filteredGroups = remember(groups, searchQuery, selectedFilter) {
                        var result = groups

                        // Apply search filter if query is not empty
                        if (searchQuery.isNotEmpty()) {
                            result = result.filter {
                                it.name.contains(searchQuery, ignoreCase = true)
                            }
                        }

                        // Apply group filter
                        result = when (selectedFilter) {
                            GroupFilter.ALL -> result
                            GroupFilter.ACTIVE -> result.filter { group ->
                                group.members.any { it.balance != 0.0 }
                            }
                            GroupFilter.ARCHIVED -> result.filter { false } // TODO: Add archived property to Group model
                        }

                        // Apply sorting
                        result = when (selectedSortOption) {
                            GroupSortOption.NEWEST -> result.sortedByDescending { it.createdAt }
                            GroupSortOption.BALANCE -> result.sortedByDescending {
                                it.members.find { member -> member.userId == viewModel.currentUserId }?.balance ?: 0.0
                            }
                            GroupSortOption.ACTIVITY -> result // TODO: Add lastActivity timestamp to Group model
                        }

                        result
                    }

                    GroupsContent(
                        groups = filteredGroups,
                        totalGroupCount = groups.size,
                        lazyState = lazyState,
                        pagerState = pagerState,
                        searchQuery = searchQuery,
                        isSearchActive = isSearchActive,
                        selectedFilter = selectedFilter,
                        selectedSortOption = selectedSortOption,
                        showFilterOptions = showFilterOptions,
                        showSortOptions = showSortOptions,
                        onSearchQueryChange = { searchQuery = it },
                        onSearchActiveChange = { isSearchActive = it },
                        onClearSearch = {
                            searchQuery = ""
                            focusManager.clearFocus()
                        },
                        onFilterChange = { selectedFilter = it },
                        onSortOptionChange = { selectedSortOption = it },
                        onToggleFilterOptions = { showFilterOptions = !showFilterOptions },
                        onToggleSortOptions = { showSortOptions = !showSortOptions },
                        onGroupClick = onGroupClick
                    )
                }

                is GroupState.GroupDetailSuccess -> {
                    // This state is handled in a different screen
                }
            }
        }
    }
}

@Composable
fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = LocalSplitColors.current.primary
        )
    }
}

@Composable
fun ErrorState(
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

@OptIn(ExperimentalResourceApi::class)
@Composable
fun EmptyGroupsState(onCreateGroupClick: () -> Unit) {
    val colors = LocalSplitColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Group icon with background
        Surface(
            color = colors.primary.copy(alpha = 0.1f),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.size(80.dp)
        ) {
            Icon(
                painter = painterResource(Res.drawable.group_icon_filled),
                contentDescription = null,
                modifier = Modifier
                    .padding(lDimens.dp16)
                    .size(lDimens.dp48),
                tint = colors.primary
            )
        }

        Spacer(modifier = Modifier.height(lDimens.dp24))

        Text(
            "No Groups Yet",
            style = MaterialTheme.typography.titleLarge,
            color = colors.textPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(lDimens.dp8))

        Text(
            "Create a group to start splitting expenses with friends",
            style = MaterialTheme.typography.bodyLarge,
            color = colors.textSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(lDimens.dp32))

        Button(
            onClick = onCreateGroupClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary,
                contentColor = Color.White
            )
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create Group")
        }
    }
}

@OptIn(ExperimentalResourceApi::class, ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GroupsContent(
    groups: List<Group>,
    totalGroupCount: Int,
    lazyState: LazyListState,
    pagerState: PagerState,
    searchQuery: String,
    isSearchActive: Boolean,
    selectedFilter: GroupFilter,
    selectedSortOption: GroupSortOption,
    showFilterOptions: Boolean,
    showSortOptions: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onSearchActiveChange: (Boolean) -> Unit,
    onClearSearch: () -> Unit,
    onFilterChange: (GroupFilter) -> Unit,
    onSortOptionChange: (GroupSortOption) -> Unit,
    onToggleFilterOptions: () -> Unit,
    onToggleSortOptions: () -> Unit,
    onGroupClick: (Group) -> Unit
) {
    val colors = LocalSplitColors.current

    val groupedGroups = remember(groups) {
        groupItemsByDate(groups)
    }

    LazyColumn(
        state = lazyState,
        contentPadding = PaddingValues(bottom = 80.dp), // Space for FAB
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundPrimary)
    ) {
        // Search bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = lDimens.dp16, vertical = lDimens.dp8)
                    .height(56.dp),
                placeholder = {
                    Text(
                        "Search groups",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = colors.textSecondary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = onClearSearch) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = colors.textSecondary
                            )
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colors.cardBackground,
                    unfocusedContainerColor = colors.cardBackground,
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.border,
                    cursorColor = colors.primary,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary
                ),
                shape = RoundedCornerShape(lDimens.dp28),
                textStyle = TextStyle(color = colors.textPrimary)
            )
        }

        // Filter and Sort options
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = lDimens.dp16, vertical = lDimens.dp8),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Filter button
                FilterButton(
                    text = "Filter",
                    icon = Icons.Default.Home,
                    selected = showFilterOptions,
                    onClick = onToggleFilterOptions
                )

                // Sort button
                FilterButton(
                    text = "Sort",
                    icon = Icons.Default.Home,
                    selected = showSortOptions,
                    onClick = onToggleSortOptions
                )

                Spacer(modifier = Modifier.weight(1f))

                // Active filter indicator
                if (selectedFilter != GroupFilter.ALL) {
                    ActiveFilterChip(
                        text = when (selectedFilter) {
                            GroupFilter.ACTIVE -> "Active"
                            GroupFilter.ARCHIVED -> "Archived"
                            else -> ""
                        },
                        onClear = { onFilterChange(GroupFilter.ALL) }
                    )
                }
            }
        }

        // Filter options
        if (showFilterOptions) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = lDimens.dp16, vertical = lDimens.dp4),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterOption(
                        text = "All",
                        selected = selectedFilter == GroupFilter.ALL,
                        onClick = { onFilterChange(GroupFilter.ALL) }
                    )

                    FilterOption(
                        text = "Active",
                        selected = selectedFilter == GroupFilter.ACTIVE,
                        onClick = { onFilterChange(GroupFilter.ACTIVE) }
                    )

                    FilterOption(
                        text = "Archived",
                        selected = selectedFilter == GroupFilter.ARCHIVED,
                        onClick = { onFilterChange(GroupFilter.ARCHIVED) }
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = lDimens.dp16),
                    color = colors.divider
                )
            }
        }

        // Sort options
        if (showSortOptions) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = lDimens.dp16, vertical = lDimens.dp4),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterOption(
                        text = "Newest",
                        selected = selectedSortOption == GroupSortOption.NEWEST,
                        onClick = { onSortOptionChange(GroupSortOption.NEWEST) }
                    )

                    FilterOption(
                        text = "Balance",
                        selected = selectedSortOption == GroupSortOption.BALANCE,
                        onClick = { onSortOptionChange(GroupSortOption.BALANCE) }
                    )

                    FilterOption(
                        text = "Recent Activity",
                        selected = selectedSortOption == GroupSortOption.ACTIVITY,
                        onClick = { onSortOptionChange(GroupSortOption.ACTIVITY) }
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = lDimens.dp16),
                    color = colors.divider
                )
            }
        }

        // No results message
        if (groups.isEmpty() && searchQuery.isNotEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No groups found matching \"$searchQuery\"",
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.textSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Group items
        groupedGroups.forEach { (timePeriod,groups) ->
            item(key = "header_$timePeriod") {
                Text(
                    text = timePeriod,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                    modifier = Modifier.padding(vertical = lDimens.dp8, horizontal = lDimens.dp16)
                )
            }
            items(
                items = groups,
                key = {it.id}
            ) { group ->
                EnhancedGroupCard(
                    group = group,
                    currentUserId = "user1",
                    onClick = { onGroupClick(group) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = lDimens.dp16, vertical = lDimens.dp8)
                        .animateItemPlacement()
                )
            }
        }

        // Bottom spacer for FAB
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun FilterButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalSplitColors.current

    Surface(
        modifier = Modifier,
        shape = RoundedCornerShape(16.dp),
        color = if (selected) colors.primary else colors.cardBackground,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) Color.White else colors.textPrimary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = text,
                color = if (selected) Color.White else colors.textPrimary,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
fun ActiveFilterChip(
    text: String,
    onClear: () -> Unit
) {
    val colors = LocalSplitColors.current

    Surface(
        modifier = Modifier,
        shape = RoundedCornerShape(16.dp),
        color = colors.primary.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = text,
                color = colors.primary,
                style = MaterialTheme.typography.labelMedium
            )

            IconButton(
                onClick = onClear,
                modifier = Modifier.size(16.dp)
            ) {
                Icon(
                    Icons.Default.Clear,
                    contentDescription = "Clear filter",
                    tint = colors.primary,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@Composable
fun FilterOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalSplitColors.current

    Surface(
        modifier = Modifier.clip(RoundedCornerShape(16.dp)),
        color = if (selected) colors.primary else colors.cardBackground,
        onClick = onClick
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            color = if (selected) Color.White else colors.textPrimary,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalResourceApi::class)
@Composable
fun EnhancedGroupCard(
    group: Group,
    currentUserId: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalSplitColors.current

    // Find current user's balance in this group
    val userMember = group.members.find { it.userId == currentUserId }
    val userBalance = userMember?.balance ?: 0.0

    // Check if there are pending settlements
    val hasPendingSettlements = false

    // Format time since creation
    val dateTime = DateTimeUtil.formatStandardDate(group.createdAt)

    SplitCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(lDimens.dp16)
        ) {
            // Header row with group name and balance
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Group icon and name
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Group icon with background
                    val groupType = GroupType.fromString(group.groupType)
                    Surface(
                        shape = CircleShape,
                        modifier = Modifier.size(lDimens.dp40),
                        border = BorderStroke(
                            lDimens.dp1,
                            color = colors.primary
                        )
                    ) {
                        Image(
                            painter = painterResource(resource = groupType.iconRes),
                            contentDescription = groupType.displayName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .padding(lDimens.dp1)
                                .size(lDimens.dp24)
                                .clip(CircleShape),
                        )
                    }

                    Spacer(modifier = Modifier.width(lDimens.dp12))

                    Column {
                        Text(
                            text = group.name,
                            style = MaterialTheme.typography.titleLarge,
                            color = colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = "${group.members.size} members • $dateTime",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }
                }

                // Badge for pending settlements
                if (hasPendingSettlements) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(colors.warning, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Balance row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Balance display
                Column {
                    Text(
                        text = if (userBalance > 0) "You are owed" else if (userBalance < 0) "You owe" else "All settled up",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    CurrencyDisplay(
                        amount = kotlin.math.abs(userBalance),
                        isIncome = userBalance >= 0.0,
                        large = false
                    )
                }

                // View button
                IconButton(
                    onClick = onClick,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(colors.primary.copy(alpha = 0.1f))
                ) {
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = "View Group",
                        tint = colors.primary
                    )
                }
            }
        }
    }
}

private fun groupItemsByDate(items: List<Group>): Map<String, List<Group>> {

    val grouped = items.groupBy { item ->
        DateTimeUtil.formatRelativeDate(item.createdAt)
    }

    val order = listOf("Today", "Yesterday", "This Week", "This Month", "Earlier")

    // Create a new linked map with the desired order
    val result = linkedMapOf<String, List<Group>>()

    // Add entries in the specific order (if they exist)
    order.forEach { key ->
        grouped[key]?.let { result[key] = it }
    }

    grouped.forEach { (key, value) ->
        if (!result.containsKey(key)) {
            result[key] = value
        }
    }

    return result
}