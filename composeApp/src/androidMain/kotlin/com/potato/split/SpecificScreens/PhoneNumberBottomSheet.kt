package com.potato.split.SpecificScreens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.potato.split.Auth.AuthManager
import com.potato.split.presentation.theme.LocalSplitColors
import com.potato.split.presentation.theme.lDimens
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneNumberBottomSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onPhoneNumberSubmit: (String) -> Unit
) {
    if (isVisible) {
        val keyboardController = LocalSoftwareKeyboardController.current
        var phoneNumber by remember { mutableStateOf("") }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onDismiss() },
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(Color.White)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { /* Prevent click through */ }
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Handle bar
                Box(
                    modifier = Modifier
                        .width(lDimens.dp40)
                        .height(lDimens.dp4)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFFE5E7EB))
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Enter Phone Number",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF8fcb39)
                )

                Spacer(modifier = Modifier.height(lDimens.dp16))

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = {
                        if (it.length <= 10 && it.all { char -> char.isDigit() }) {
                            phoneNumber = it
                            if (it.length == 10) {
                                keyboardController?.hide()
                                onPhoneNumberSubmit(it)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = lDimens.dp8),
                    label = { Text("Phone Number") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun PhoneNumberScreen(
    authManager: AuthManager
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var phoneNumber by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val colors = LocalSplitColors.current
    val keyboardController = LocalSoftwareKeyboardController.current


    BackHandler(enabled = true) {
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(lDimens.dp24),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Enter Your Phone Number",
            style = MaterialTheme.typography.headlineLarge,
            color = colors.textPrimary,
            modifier = Modifier.padding(bottom = lDimens.dp8)
        )
        Text(
            text = "Please enter your phone number to complete the setup.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = colors.textSecondary,
            modifier = Modifier.padding(bottom = lDimens.dp32)
        )
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = {
                if (it.length <= 10 && it.all { char -> char.isDigit() }) {
                    phoneNumber = it
                    if (it.length == 10) {
                        isLoading = true
                        error = null
                        keyboardController?.hide()
                        scope.launch {
                            val success = authManager.completePhoneNumberSetup(phoneNumber.trim())
                            if (!success) {
                                error = "Failed to save phone number. Please try again."
                            }
                            isLoading = false
                        }
                    }
                }
            },
            label = { Text("Phone Number") },
            placeholder = { Text("Type Here", color = colors.textSecondary) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            enabled = !isLoading,
            isError = error != null,
            modifier = Modifier.fillMaxWidth(),
        )
        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = lDimens.dp8)
            )
        }
        Spacer(modifier = Modifier.height(lDimens.dp24))
        Button(
            onClick = {
                if (phoneNumber.isNotBlank()) {
                    isLoading = true
                    error = null
                    scope.launch {
                        val success = authManager.completePhoneNumberSetup(phoneNumber.trim())
                        if (!success) {
                            error = "Failed to save phone number. Please try again."
                        }
                        isLoading = false
                    }
                }
            },
            enabled = !isLoading && phoneNumber.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        strokeWidth = lDimens.dp4,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(lDimens.dp8))
                    Text("Saving...")
                }
            } else {
                Text("Continue")
            }
        }
    }
}