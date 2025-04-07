package com.falcon.split.screens.mainNavigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.falcon.split.ClipboardManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import split.composeapp.generated.resources.Res
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import com.falcon.split.presentation.theme.lDimens
import org.jetbrains.compose.resources.DrawableResource
import split.composeapp.generated.resources.google_pay
import split.composeapp.generated.resources.paytm
import split.composeapp.generated.resources.phonepe_icon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    paymentAmount: Int,
    personName: String,
    paymentUpiId: String,
    snackBarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pay Dues") },
                navigationIcon = {
                    IconButton(onClick = {
                        onNavigateBack()
                    } ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ){padding->
        Column(
            modifier = Modifier.padding(padding)
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .padding(18.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    personName,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier
                        .padding(top = lDimens.dp0, bottom = 5.dp, start = lDimens.dp0)
                )
                Text(
                    "Amount to pay : $ $paymentAmount",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Gray,
                    modifier = Modifier
                        .padding(top = lDimens.dp0, bottom = lDimens.dp0)
                )
            }
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                ),
                border = BorderStroke(2.dp,Color.Black),
                modifier = Modifier
                    .padding(lDimens.dp8)
            ) {
                Text(
                    "Pay Using Apps",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Gray,
                    modifier = Modifier
                        .padding(top = lDimens.dp8, start = lDimens.dp15)
                )
                UpiMethod(appName = "Paytm", snackBarHostState = snackBarHostState, drawable = Res.drawable.paytm) {
                    ClipboardManager.copyToClipboard(paymentUpiId)
                    Intents.openPaytm()
                }
                UpiMethod(appName = "G-Pay", snackBarHostState = snackBarHostState, drawable = Res.drawable.google_pay) {
                    ClipboardManager.copyToClipboard(paymentUpiId)
                    Intents.openGooglePay()
                }
                UpiMethod(appName = "PhonePe", snackBarHostState = snackBarHostState, drawable = Res.drawable.phonepe_icon) {
                    ClipboardManager.copyToClipboard(paymentUpiId)
                    Intents.openPhonePe()
                }
            }
        }
    }
}

@Composable
fun UpiMethod(
    appName: String,
    snackBarHostState: SnackbarHostState,
    drawable: DrawableResource,
    openPaymentApp: () -> Unit
) {
    var isCancelled by remember { mutableStateOf(false) }
    Card(
        onClick = {
            val snackBarJob = CoroutineScope(Dispatchers.Main).launch {
                snackBarHostState.showSnackbar(
                    message = "UPI Id Copied to clipboard, Redirecting to $appName in 3 seconds",
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
            containerColor = Color.Transparent
        ),
        modifier = Modifier
            .padding(lDimens.dp4)
    ){
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .padding(bottom = lDimens.dp8)
                .fillMaxWidth()
                .padding(lDimens.dp15)
        ) {
            Card(
                modifier = Modifier
                    .padding(lDimens.dp16)
                    .width(90.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = lDimens.dp4
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(lDimens.dp16),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(drawable),
                        contentDescription = "GPay Icon",
                        modifier = Modifier
                            .size(lDimens.dp48)
                            .padding(bottom = lDimens.dp8)
                    )

                    Text(
                        text = appName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Open",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}