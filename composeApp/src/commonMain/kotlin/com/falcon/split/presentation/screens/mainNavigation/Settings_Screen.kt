package com.falcon.split.presentation.screens.mainNavigation

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.navigation.NavHostController
import com.falcon.split.presentation.theme.LocalSplitColors
import com.falcon.split.presentation.theme.SplitCard
import com.falcon.split.presentation.theme.ThemeSwitcher
import com.falcon.split.presentation.theme.lDimens
import com.falcon.split.toggleDarkTheme
import com.falcon.split.utils.EmailUtils
import com.falcon.split.utils.OpenLink
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    navController: NavHostController,
    onNavigateBack: () -> Unit,
    emailUtils: EmailUtils,
    prefs: DataStore<Preferences>,
    darkTheme: MutableState<Boolean>
) {
    val colors = LocalSplitColors.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Dialog states
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.textPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.backgroundSecondary,
                    titleContentColor = colors.textPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .background(colors.backgroundPrimary)
        ) {
            // General Settings Section
            SettingsSectionHeader("General")


            // Notification Setting
            SettingItem(
                title = "Notifications",
                description = "Manage notification preferences",
                icon = Icons.Default.Notifications,
                onClick = { /* Open notification settings */ }
            )

            // Payment Setting
            SettingItem(
                title = "Payment Account",
                description = "Update your payment methods",
                icon = Icons.Default.ThumbUp,
                onClick = { /* Open payment settings */ }
            )

            // Delete Account Setting
            SettingItem(
                title = "Delete Account",
                description = "Permanently delete your account and data",
                icon = Icons.Default.Delete,
                onClick = { showDeleteDialog = true },
                isDestructive = true
            )

            // Support & About Section
            SettingsSectionHeader("Support & About")

            // Contact Us Setting
            SettingItem(
                title = "Contact Us",
                description = "Get help or send feedback",
                icon = Icons.Default.ThumbUp,
                onClick = {
                    emailUtils.sendEmail(
                        to = "deeptanshushuklaji@gmail.com",
                        subject = "Regarding Split App",
                    )
                }
            )

            // Bug Report Setting
            SettingItem(
                title = "Bug Report",
                description = "Report issues with the app",
                icon = Icons.Default.ThumbUp,
                onClick = {
                    emailUtils.sendEmail(
                        to = "deeptanshushuklaji@gmail.com",
                        subject = "Bug Report For Split App",
                    )
                }
            )

            // Legal Section
            SettingsSectionHeader("Legal")

            // Terms & Conditions
            SettingItem(
                title = "Terms & Conditions",
                description = "Terms and Conditions for using",
                icon = Icons.Default.ThumbUp,
                onClick = {
                    OpenLink.openLink("https://sites.google.com/view/split-app/terms-conditions")
                }
            )

            // Privacy Policy
            SettingItem(
                title = "Privacy Policy",
                description = "All the privacy policies",
                icon = Icons.Default.ThumbUp,
                onClick = {
                    OpenLink.openLink("https://sites.google.com/view/split-app/home")
                }
            )

            // App info
            SplitCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = lDimens.dp16, vertical = lDimens.dp16)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(lDimens.dp16),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Split",
                        style = MaterialTheme.typography.headlineMedium,
                        color = colors.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "Version 1.0.0",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary
                    )

                    Spacer(modifier = Modifier.height(lDimens.dp8))

                    Text(
                        "Split expenses, not friendships",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
            }

            // Bottom spacer
            Spacer(modifier = Modifier.height(lDimens.dp24))
        }

        // Delete Account Confirmation Dialog
        if (showDeleteDialog) {
            DeleteAccountDialog(
                onDismiss = { showDeleteDialog = false },
                onConfirm = { /* Delete account logic */ }
            )
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    val colors = LocalSplitColors.current

    Text(
        title,
        style = MaterialTheme.typography.titleSmall,
        color = colors.primary,
        modifier = Modifier.padding(start = lDimens.dp16, top = lDimens.dp16, end = lDimens.dp16, bottom = lDimens.dp8)
    )
}

@Composable
fun ThemeSettingItem(
    prefs: DataStore<Preferences>,
    darkTheme: MutableState<Boolean>
) {
    val colors = LocalSplitColors.current
    val scope = rememberCoroutineScope()

    SplitCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = lDimens.dp16, vertical = lDimens.dp4)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(lDimens.dp16)
        ) {
            // Icon and text
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(colors.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.ThumbUp,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(lDimens.dp16))

                Column {
                    Text(
                        "Theme",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textPrimary
                    )

                    Text(
                        "Dark theme: ${if (darkTheme.value) "On" else "Off"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary
                    )
                }
            }

            // Theme switcher
            ThemeSwitcher(
                size = 32.dp,
                padding = 4.dp,
                darkTheme = darkTheme.value,
                onClick = {
                    scope.launch {
                        darkTheme.value = !darkTheme.value
                        toggleDarkTheme(prefs)
                    }
                }
            )
        }
    }
}

@Composable
fun SettingItem(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    val colors = LocalSplitColors.current
    val textColor = if (isDestructive) colors.error else colors.textPrimary
    val descriptionColor = if (isDestructive) colors.error.copy(alpha = 0.7f) else colors.textSecondary

    SplitCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = lDimens.dp16, vertical = lDimens.dp4)
            .clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(lDimens.dp16)
        ) {
            // Icon and text
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (isDestructive) colors.error.copy(alpha = 0.1f)
                            else colors.primary.copy(alpha = 0.1f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = if (isDestructive) colors.error else colors.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(lDimens.dp16))

                Column {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        color = textColor
                    )

                    Text(
                        description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = descriptionColor
                    )
                }
            }

            // Arrow icon
            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = "Open",
                tint = if (isDestructive) colors.error else colors.textSecondary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun DeleteAccountDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val colors = LocalSplitColors.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Account") },
        text = {
            Text(
                "Are you sure you want to delete your account? This action cannot be undone and all your data will be permanently removed.",
                color = colors.textPrimary
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                    onDismiss()
                }
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