import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.falcon.split.data.network.models_app.Group
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import org.jetbrains.compose.resources.painterResource
import split.composeapp.generated.resources.Res
wimport split.composeapp.generated.resources.group_icon_filled

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsScreen(
    onCreateGroupClick: () -> Unit,
    onGroupClick: (Group) -> Unit,
    groups: List<Group>,
    isLoading: Boolean = false
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Groups") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateGroupClick,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Group")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (groups.isEmpty()) {
                EmptyGroupsView(
                    onCreateGroupClick = onCreateGroupClick,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(groups) { group ->
                        GroupCard(
                            group = group,
                            onClick = { onGroupClick(group) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroupCard(
    group: Group,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Image(
                            painter = painterResource(Res.drawable.group_icon_filled),
                            contentDescription = null,
                            modifier = Modifier
                                .size(48.dp)
                                .padding(horizontal = 12.dp),
                            contentScale = ContentScale.Fit
                        )
                    }

                    Column {
                        Text(
                            text = group.name,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${group.members.size} members",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onClick) {
                    Icon(
                        Icons.Default.KeyboardArrowRight,
                        contentDescription = "View Group",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyGroupsView(
    onCreateGroupClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(Res.drawable.group_icon_filled),
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .padding(horizontal = 12.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "No Groups Yet",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Create a group to start splitting expenses with friends",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        FilledTonalButton(
            onClick = onCreateGroupClick
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text("Create Group")
        }
    }
}

@Composable
fun GroupsScreenWithDummyData(
    onCreateGroupClick: () -> Unit,
    onGroupClick: (Group) -> Unit
) {
    // Dummy groups data
    val dummyGroups = remember {
        listOf(
            Group(
                groupId = "1",
                name = "Weekend Trip to Goa",
                members = listOf("user1", "user2", "user3", "user4"),
                createdBy = "user1",
                createdAt = Clock.System.now()
            ),
            Group(
                groupId = "2",
                name = "House Expenses",
                members = listOf("user1", "user2"),
                createdBy = "user2",
                createdAt = Clock.System.now()
            ),
            Group(
                groupId = "3",
                name = "Movie Night",
                members = listOf("user1", "user2", "user3", "user5"),
                createdBy = "user1",
                createdAt = Clock.System.now()
            ),
            Group(
                groupId = "4",
                name = "Office Lunch Group",
                members = listOf("user1", "user4", "user5", "user6", "user7"),
                createdBy = "user4",
                createdAt = Clock.System.now()
            ),
            Group(
                groupId = "5",
                name = "Flatmates",
                members = listOf("user1", "user2", "user3"),
                createdBy = "user3",
                createdAt = Clock.System.now()
            )
        )
    }

    // Using dummy loading state
    var isLoading by remember { mutableStateOf(false) }

    // Simulate loading when screen first appears
    LaunchedEffect(Unit) {
        isLoading = true
        delay(1000) // Simulate network delay
        isLoading = false
    }

    GroupsScreen(
        groups = dummyGroups,
        isLoading = isLoading,
        onCreateGroupClick = onCreateGroupClick,
        onGroupClick = onGroupClick
    )
}