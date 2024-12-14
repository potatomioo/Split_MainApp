package com.falcon.split.screens.mainNavigation

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.unit.dp
import com.falcon.split.data.network.models_app.Group
import kotlinx.datetime.Clock
import org.jetbrains.compose.resources.painterResource
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.group_icon_filled

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(
    onGroupCreated: (Group) -> Unit,
    onNavigateBack: () -> Unit
) {
    var groupName by remember { mutableStateOf("") }
    var showMemberSelection by remember { mutableStateOf(false) }
    var selectedMembers by remember { mutableStateOf(setOf<String>()) }
    
    // Dummy data for users - Replace with actual data from your ViewModel
    val dummyUsers = listOf(
        "John Doe" to "user1",
        "Jane Smith" to "user2",
        "Mike Johnson" to "user3",
        "Sarah Wilson" to "user4",
        "Alex Brown" to "user5"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create New Group") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Group Icon and Name Section
            OutlinedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
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
                    OutlinedTextField(
                        value = groupName,
                        onValueChange = { groupName = it },
                        label = { Text("Group Name") },
                        placeholder = { Text("Enter group name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Member Selection Section
            OutlinedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Select Members",
                            style = MaterialTheme.typography.titleMedium
                        )
                        IconButton(onClick = { showMemberSelection = !showMemberSelection }) {
                            Icon(
                                if (showMemberSelection) Icons.Default.KeyboardArrowDown
                                else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Toggle selection"
                            )
                            Image(
                                painter = painterResource(Res.drawable.group_icon_filled),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(48.dp)
                                    .padding(horizontal = 12.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }

                    AnimatedVisibility(visible = showMemberSelection) {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 200.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(dummyUsers) { (name, id) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(name)
                                    Checkbox(
                                        checked = selectedMembers.contains(id),
                                        onCheckedChange = { checked ->
                                            selectedMembers = if (checked) {
                                                selectedMembers + id
                                            } else {
                                                selectedMembers - id
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Selected Members Summary
                    if (selectedMembers.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "${selectedMembers.size} members selected",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Selected Members Preview
            if (selectedMembers.isNotEmpty()) {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Selected Members",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        selectedMembers.forEach { memberId ->
                            val memberName = dummyUsers.find { it.second == memberId }?.first ?: ""
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Person, null, 
                                         modifier = Modifier.size(16.dp))
                                    Text(memberName)
                                }
                                IconButton(
                                    onClick = { 
                                        selectedMembers = selectedMembers - memberId 
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove member",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Create Group Button
            Button(
                onClick = {
                    val group = Group(
                        groupId = Clock.System.now().toEpochMilliseconds().toString(),
                        name = groupName,
                        members = selectedMembers.toList(),
                        createdBy = "currentUserId", // Replace with actual current user ID
                        createdAt = Clock.System.now()
                    )
                    onGroupCreated(group)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = groupName.isNotEmpty() && selectedMembers.isNotEmpty()
            ) {
                Icon(
                    Icons.Default.AddCircle,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Create Group")
            }
        }
    }
}