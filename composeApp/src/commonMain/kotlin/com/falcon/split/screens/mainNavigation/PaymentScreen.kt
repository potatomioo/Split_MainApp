package com.falcon.split.screens.mainNavigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.falcon.split.ClipboardManager
import com.falcon.split.presentation.group.GroupState
import com.falcon.split.presentation.group.GroupViewModel
import com.falcon.split.presentation.theme.LocalSplitColors
import com.falcon.split.presentation.theme.lDimens
import com.falcon.split.utils.MemberNameResolver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.google_pay
import split.composeapp.generated.resources.paytm
import split.composeapp.generated.resources.phonepe_icon

@OptIn(ExperimentalMaterial3Api::class, ExperimentalResourceApi::class)
@Composable
fun PaymentScreen(
    paymentAmount: Double,
    userId: String,
    groupId: String,
    snackBarHostState: SnackbarHostState,
    viewModel: GroupViewModel,
    contactManager: Any? = null,
    onNavigateBack: () -> Unit
) {
    val colors = LocalSplitColors.current
    val scrollState = rememberScrollState()

    // Get group and user information
    val groupState by viewModel.groupState.collectAsState()

    // Default fallback values in case we can't get the actual data
    var personName by remember { mutableStateOf("User") }
    var paymentUpiId by remember { mutableStateOf("unknown@upi") }

    // Attempt to get the group and member details
    LaunchedEffect(groupId, userId) {
        viewModel.loadGroupDetails(groupId)
    }

    // Extract user details when group is loaded
    LaunchedEffect(groupState) {
        if (groupState is GroupState.GroupDetailSuccess) {
            val group = (groupState as GroupState.GroupDetailSuccess).group
            val member = group.members.find { it.userId == userId }

            if (member != null) {
                val nameResolver = MemberNameResolver(null) // Replace with contactManager if available
                personName = nameResolver.resolveDisplayName(member)

                // Get UPI from user profile if available (this would need to be added to your model)
                // For now using a placeholder
                paymentUpiId = member.upiId ?: ""
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pay Dues") },
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
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Header info - who you're paying and how much
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(lDimens.dp16),
                colors = CardDefaults.cardColors(
                    containerColor = colors.cardBackground
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(lDimens.dp16),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Paying to",
                        fontSize = 16.sp,
                        color = colors.textSecondary
                    )

                    Text(
                        personName,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        modifier = Modifier.padding(vertical = lDimens.dp8)
                    )

                    Spacer(modifier = Modifier.height(lDimens.dp16))

                    Text(
                        "Amount to pay",
                        fontSize = 16.sp,
                        color = colors.textSecondary
                    )

                    Text(
                        "₹ $paymentAmount",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary,
                        modifier = Modifier.padding(vertical = lDimens.dp8)
                    )

                    Spacer(modifier = Modifier.height(lDimens.dp8))

                    if (paymentUpiId != "unknown@upi") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = lDimens.dp8)
                        ) {
                            Text(
                                "UPI ID: $paymentUpiId",
                                fontSize = 14.sp,
                                color = colors.textSecondary
                            )

                            IconButton(
                                onClick = {
                                    ClipboardManager.copyToClipboard(paymentUpiId)
                                    CoroutineScope(Dispatchers.Main).launch {
                                        snackBarHostState.showSnackbar(
                                            message = "UPI ID copied to clipboard",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                },
                                modifier = Modifier.size(lDimens.dp32)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "Copy",
                                    tint = colors.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Payment Options Section
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = colors.cardBackground
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = lDimens.dp16, vertical = lDimens.dp8)
            ) {
                Column(
                    modifier = Modifier.padding(lDimens.dp16)
                ) {
                    Text(
                        "Pay Using Apps",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary,
                        modifier = Modifier.padding(bottom = lDimens.dp16)
                    )

                    UpiMethod(
                        appName = "Paytm",
                        snackBarHostState = snackBarHostState,
                        drawable = Res.drawable.paytm,
                        paymentUpiId = paymentUpiId
                    ) {
                        ClipboardManager.copyToClipboard(paymentUpiId)
                        Intents.openPaytm()
                    }

                    UpiMethod(
                        appName = "G-Pay",
                        snackBarHostState = snackBarHostState,
                        drawable = Res.drawable.google_pay,
                        paymentUpiId = paymentUpiId
                    ) {
                        ClipboardManager.copyToClipboard(paymentUpiId)
                        Intents.openGooglePay()
                    }

                    UpiMethod(
                        appName = "PhonePe",
                        snackBarHostState = snackBarHostState,
                        drawable = Res.drawable.phonepe_icon,
                        paymentUpiId = paymentUpiId
                    ) {
                        ClipboardManager.copyToClipboard(paymentUpiId)
                        Intents.openPhonePe()
                    }
                }
            }

            // Instructions/Note
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = colors.cardBackground
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = lDimens.dp16, vertical = lDimens.dp8)
            ) {
                Column(
                    modifier = Modifier.padding(lDimens.dp16)
                ) {
                    Text(
                        "How it works",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary,
                        modifier = Modifier.padding(bottom = lDimens.dp8)
                    )

                    Text(
                        "1. Choose your preferred payment app\n" +
                                "2. The UPI ID will be copied to your clipboard\n" +
                                "3. Complete the payment in your payment app\n" +
                                "4. Come back to Split app to mark this debt as settled",
                        fontSize = 14.sp,
                        color = colors.textSecondary,
                        lineHeight = 20.sp
                    )

                    Text(
                        "Note: This doesn't automatically settle the debt in Split. After payment, you'll need to mark it as settled.",
                        fontSize = 12.sp,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(top = lDimens.dp16),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun UpiMethod(
    appName: String,
    snackBarHostState: SnackbarHostState,
    drawable: DrawableResource,
    paymentUpiId: String,
    openPaymentApp: () -> Unit
) {
    val colors = LocalSplitColors.current
    var isCancelled by remember { mutableStateOf(false) }

    Card(
        onClick = {
            val snackBarJob = CoroutineScope(Dispatchers.Main).launch {
                snackBarHostState.showSnackbar(
                    message = "UPI ID copied to clipboard, Redirecting to $appName in 3 seconds",
                    actionLabel = "Cancel",
                    duration = SnackbarDuration.Short,
                    withDismissAction = false
                ).let { result ->
                    when (result) {
                        SnackbarResult.ActionPerformed -> {
                            isCancelled = true
                            this.cancel()
                        }
                        else -> {}
                    }
                }
            }

            CoroutineScope(Dispatchers.IO).launch {
                delay(3000)
                if (!isCancelled) {
                    openPaymentApp()
                    snackBarJob.cancel()
                }
            }
        },
        colors = CardDefaults.cardColors(
            containerColor = colors.cardBackground
        ),
        border = BorderStroke(1.dp, colors.divider),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = lDimens.dp8)
    ){
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(lDimens.dp16)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(drawable),
                    contentDescription = "$appName Icon",
                    modifier = Modifier
                        .size(lDimens.dp40)
                )

                Spacer(modifier = Modifier.width(lDimens.dp16))

                Text(
                    text = appName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textPrimary
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Open",
                modifier = Modifier.size(24.dp),
                tint = colors.textSecondary
            )
        }
    }
}