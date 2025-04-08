package com.falcon.split.presentation.screens.mainNavigation

import ContactPicker
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.falcon.split.presentation.group.CreateGroupState
import com.falcon.split.presentation.group.CreateGroupViewModel
import com.falcon.split.contact.ContactManager
import com.falcon.split.presentation.theme.LocalSplitColors
import com.falcon.split.presentation.theme.lDimens
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.group_icon_filled

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(
    onGroupCreated: (String) -> Unit,
    onNavigateBack: () -> Unit,
    contactManager: ContactManager,
    viewModel: CreateGroupViewModel = viewModel()
) {
    val colors = LocalSplitColors.current

    var groupName by remember { mutableStateOf("") }
    var showContactPicker by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val selectedContacts by viewModel.selectedContacts.collectAsState()
    val state by viewModel.state.collectAsState()

    // Handle state changes
    LaunchedEffect(state) {
        when (state) {
            is CreateGroupState.Success -> {
                onGroupCreated((state as CreateGroupState.Success).groupId)
            }
            is CreateGroupState.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = (state as CreateGroupState.Error).message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
            else -> {}
        }
    }

    if (showContactPicker) {
        ContactPicker(
            contactManager = contactManager
        ) { contact ->
            // Ensure the contact is not null before adding
            contact?.let { nonNullContact ->
                viewModel.addContact(nonNullContact)
            }
            showContactPicker = false
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(
                    "Create New Group",
                    color = colors.textPrimary
                ) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(lDimens.dp16),
            verticalArrangement = Arrangement.spacedBy(lDimens.dp16)
        ) {
            // Group Name Section
            OutlinedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(lDimens.dp16)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(Res.drawable.group_icon_filled),
                        contentDescription = null,
                        modifier = Modifier
                            .size(lDimens.dp48)
                            .padding(horizontal = lDimens.dp12),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.height(lDimens.dp16))
                    OutlinedTextField(
                        value = groupName,
                        onValueChange = { groupName = it },
                        label = { Text(
                            "Group Name",
                            color = colors.textPrimary
                        ) },
                        placeholder = { Text("Enter group name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Member Selection Section
            OutlinedCard(
                modifier = Modifier.fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .padding(lDimens.dp16)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Select Members",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.textPrimary
                        )
                        Button(
                            onClick = { showContactPicker = true },
                            contentPadding = PaddingValues(horizontal = lDimens.dp12)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Add member",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(lDimens.dp4))
                            Text("Add Member")
                        }
                    }

                    if (selectedContacts.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(lDimens.dp8))
                        Text(
                            "${selectedContacts.size} members selected",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(lDimens.dp8))
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(lDimens.dp8)
                        ) {
                            items(selectedContacts) { contact ->
                                Card(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(lDimens.dp12),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(lDimens.dp12),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.Person,
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Column {
                                                Text(
                                                    text = contact.contactName,
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    color = colors.textPrimary
                                                )
                                                Text(
                                                    text = contact.contactNumber,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = colors.textSecondary
                                                )
                                            }
                                        }
                                        IconButton(
                                            onClick = { viewModel.removeContact(contact) }
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Remove member",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = lDimens.dp32),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Add members to your group",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Create Group Button
            Button(
                onClick = { viewModel.createGroup(groupName) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(lDimens.dp56),
                enabled = groupName.isNotEmpty() && selectedContacts.isNotEmpty() &&
                        state !is CreateGroupState.Loading
            ) {
                if (state is CreateGroupState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = colors.primary
                    )
                    Spacer(modifier = Modifier.width(lDimens.dp8))
                    Text(
                        "Create Group",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textPrimary
                    )
                }
            }
        }
    }
}