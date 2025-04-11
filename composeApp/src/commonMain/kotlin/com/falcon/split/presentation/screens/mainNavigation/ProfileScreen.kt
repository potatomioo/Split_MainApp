package com.falcon.split.presentation.screens.mainNavigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.falcon.split.ClipboardManager
import com.falcon.split.UserModelGoogleFirebaseBased
import com.falcon.split.data.ProfileManager.UserProfileManager
import com.falcon.split.getFirebaseUserAsUserModel
import com.falcon.split.presentation.theme.LocalSplitColors
import com.falcon.split.presentation.theme.SplitCard
import com.falcon.split.presentation.theme.lDimens
import com.falcon.split.saveFirebaseUser
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.picture_preview

@OptIn(ExperimentalMaterial3Api::class, ExperimentalResourceApi::class)
@Composable
fun ProfileScreen(
    navController: NavHostController,
    prefs: DataStore<Preferences>,
    userProfileManager: UserProfileManager,
    onSignOut: () -> Unit
) {
    val colors = LocalSplitColors.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    // State for user data
    var userModel by remember { mutableStateOf<UserModelGoogleFirebaseBased?>(null) }

    // State for edit UPI dialog
    var showUpiDialog by remember { mutableStateOf(false) }
    var upiId by remember { mutableStateOf("") }

    // State for sign out confirmation
    var showSignOutConfirmation by remember { mutableStateOf(false) }

    // Load user data
    LaunchedEffect(Unit) {
        userModel = getFirebaseUserAsUserModel(prefs)
        upiId = userModel?.upiId ?: ""
    }

    LaunchedEffect(userModel) {
        upiId = userModel?.upiId ?: ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(
                    "Profile",
                    color = colors.textPrimary
                ) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.backgroundSecondary,
                    titleContentColor = colors.textPrimary,
                    navigationIconContentColor = colors.textPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = lDimens.dp16, vertical = lDimens.dp16),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Image
            Box(
                modifier = Modifier
                    .padding(vertical = lDimens.dp16)
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(colors.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                if (userModel?.profilePictureUrl != null) {
                    AsyncImage(
                        model = userModel?.profilePictureUrl,
                        contentDescription = "Profile picture",
                        modifier = Modifier.fillMaxSize()
                            .border(
                                BorderStroke(lDimens.dp2, colors.primary),
                                shape = CircleShape
                            ),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Profile",
                        modifier = Modifier.size(60.dp),
                        tint = colors.primary
                    )
                }
            }

            // User Name
            Text(
                text = userModel?.username ?: "User",
                style = MaterialTheme.typography.headlineLarge,
                color = colors.textPrimary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = lDimens.dp8)
            )

            Spacer(modifier = Modifier.height(lDimens.dp24))

            // Profile Information
            SplitCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(lDimens.dp16)) {
                    Text(
                        "Personal Information",
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.textPrimary,
                        modifier = Modifier.padding(bottom = lDimens.dp16)
                    )

                    //Name
                    ProfileInfoItem(
                        icon = Icons.Default.Person,
                        label = "Name",
                        value = userModel?.username ?: "",
                        onCopy = {
                            ClipboardManager.copyToClipboard(userModel?.username ?: "")
                            scope.launch {
                                snackbarHostState.showSnackbar("User Name copied to clipboard")
                            }
                        }
                    )

                    // Email
                    ProfileInfoItem(
                        icon = Icons.Default.Email,
                        label = "Email",
                        value = userModel?.email ?: "",
                        onCopy = {
                            ClipboardManager.copyToClipboard(userModel?.email ?: "")
                            scope.launch {
                                snackbarHostState.showSnackbar("Email copied to clipboard")
                            }
                        }
                    )

                    // Phone
                    ProfileInfoItem(
                        icon = Icons.Default.Phone,
                        label = "Phone",
                        value = userModel?.phoneNumber ?: "",
                        onCopy = {
                            ClipboardManager.copyToClipboard(userModel?.phoneNumber ?: "")
                            scope.launch {
                                snackbarHostState.showSnackbar("Phone number copied to clipboard")
                            }
                        }
                    )

                    // UPI ID
                    ProfileInfoItem(
                        icon = Icons.Default.ThumbUp,
                        label = "UPI ID",
                        value = if (userModel?.upiId.isNullOrBlank()) "Not set" else userModel?.upiId!!,
                        onCopy = {
                            if (userModel?.upiId != null) {
                                ClipboardManager.copyToClipboard(userModel?.upiId ?: "")
                                scope.launch {
                                    snackbarHostState.showSnackbar("UPI ID copied to clipboard")
                                }
                            }
                        },
                        onEdit = { showUpiDialog = true }
                    )

                }
            }

            Spacer(modifier = Modifier.height(lDimens.dp24))

            // Sign Out Button
            Button(
                onClick = { showSignOutConfirmation = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.error,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Sign Out", style = MaterialTheme.typography.titleMedium)
            }
        }

        // UPI Edit Dialog
        if (showUpiDialog) {
            AlertDialog(
                onDismissRequest = { showUpiDialog = false },
                title = { Text("Update UPI ID") },
                text = {
                    OutlinedTextField(
                        value = upiId,
                        onValueChange = { upiId = it },
                        label = { Text("UPI ID") },
                        placeholder = { Text("username@bank") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = colors.cardBackground,
                            unfocusedContainerColor = colors.cardBackground,
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.border
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                try {
                                    println("DEBUG: Attempting to update UPI ID to: $upiId")
                                    userProfileManager.updateUserUpiId(upiId).onSuccess {
                                        println("DEBUG: UPI ID updated successfully in Firestore")
                                        // Update local user model
                                        userModel = userModel?.copy(upiId = upiId)

                                        // Also update the local state in prefs to ensure persistence
                                        saveFirebaseUser(prefs, userModel!!)
                                        println("DEBUG: Updated local user model and prefs")

                                        showUpiDialog = false
                                        snackbarHostState.showSnackbar("UPI ID updated successfully")
                                    }.onFailure { error ->
                                        println("DEBUG: Failed to update UPI ID: ${error.message}")
                                        snackbarHostState.showSnackbar("Failed to update UPI ID: ${error.message}")
                                    }
                                } catch (e: Exception) {
                                    println("DEBUG: Exception updating UPI ID: ${e.message}")
                                    e.printStackTrace()
                                    snackbarHostState.showSnackbar("An error occurred: ${e.message}")
                                }
                            }
                        }
                    ) {
                        Text("Save", color = colors.primary)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showUpiDialog = false }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Sign Out Confirmation Dialog
        if (showSignOutConfirmation) {
            AlertDialog(
                onDismissRequest = { showSignOutConfirmation = false },
                title = { Text("Sign Out") },
                text = { Text("Are you sure you want to sign out?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showSignOutConfirmation = false
                            onSignOut()
                        }
                    ) {
                        Text("Sign Out", color = colors.error)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showSignOutConfirmation = false }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun ProfileInfoItem(
    icon: ImageVector,
    label: String,
    value: String,
    onCopy: () -> Unit,
    onEdit: (() -> Unit)? = null
) {
    val colors = LocalSplitColors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = lDimens.dp8)
    ) {
        // Label
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = colors.textSecondary
        )

        // Value row with actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(lDimens.dp8))

            // Value
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = colors.textPrimary,
                modifier = Modifier.weight(1f)
            )

            // Copy button
            IconButton(
                onClick = onCopy,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.ThumbUp,
                    contentDescription = "Copy",
                    tint = colors.textSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Edit button (if applicable)
            if (onEdit != null) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}