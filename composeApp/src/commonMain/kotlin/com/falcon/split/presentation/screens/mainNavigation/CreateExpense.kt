package com.falcon.split.presentation.screens.mainNavigation

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldColors
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.falcon.split.data.network.models_app.Group
import com.falcon.split.presentation.expense.CreateExpenseState
import com.falcon.split.presentation.expense.CreateExpenseViewModel
import com.falcon.split.presentation.theme.LocalSplitColors
import com.falcon.split.presentation.theme.SplitCard
import com.falcon.split.presentation.theme.lDimens
import com.falcon.split.utils.MemberNameResolver
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateExpense(
    navControllerMain: NavHostController,
    viewModel: CreateExpenseViewModel,
    onNavigateBack: () -> Unit,
    backHandler: Any? = null, // Not using directly, keeping for compatibility
    groupId: String? = null
) {
    val colors = LocalSplitColors.current
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    // Form state
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedGroupId by remember { mutableStateOf<String?>(groupId) }
    var date by remember {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        mutableStateOf(today)
    }

    // UI state
    var isGroupDropdownExpanded by remember { mutableStateOf(false) }

    // Validation state
    var descriptionError by remember { mutableStateOf<String?>(null) }
    var amountError by remember { mutableStateOf<String?>(null) }
    var groupError by remember { mutableStateOf<String?>(null) }

    // ViewModel state
    val state by viewModel.state.collectAsState()
    val selectedGroup by viewModel.selectedGroup.collectAsState()

    // Set selected group if groupId is provided
    LaunchedEffect(groupId) {
        if (groupId != null) {
            selectedGroupId = groupId
            viewModel.selectGroup(groupId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(
                    "Add New Expense",
                    color = colors.textPrimary
                ) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (state) {
                is CreateExpenseState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = colors.primary)
                    }
                }

                is CreateExpenseState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(lDimens.dp16),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            (state as CreateExpenseState.Error).message,
                            color = colors.error,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(lDimens.dp16))

                        Button(
                            onClick = onNavigateBack,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primary
                            )
                        ) {
                            Text("Go Back")
                        }
                    }
                }

                is CreateExpenseState.Success -> {
                    val groups = (state as CreateExpenseState.Success).groups

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(lDimens.dp16),
                        verticalArrangement = Arrangement.spacedBy(lDimens.dp16)
                    ) {
                        // Amount Input Card
                        SplitCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(lDimens.dp16),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "Enter Amount",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = colors.textPrimary
                                )

                                Spacer(modifier = Modifier.height(lDimens.dp8))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        "₹",
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = colors.textPrimary,
                                        modifier = Modifier.padding(end = lDimens.dp8)
                                    )

                                    OutlinedTextField(
                                        value = amount,
                                        onValueChange = {
                                            if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                                                amount = it
                                                amountError = if (it.isEmpty()) "Amount is required" else null
                                            }
                                        },
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Decimal,
                                            imeAction = ImeAction.Next
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                                        ),
                                        placeholder = { Text(
                                            "0.00",
                                             color = colors.textSecondary
                                        ) },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        isError = amountError != null,
                                        supportingText = amountError?.let { { Text(it) } },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = colors.cardBackground,
                                            unfocusedContainerColor = colors.cardBackground,
                                            focusedBorderColor = colors.primary,
                                            unfocusedBorderColor = colors.border,
                                            errorBorderColor = colors.error
                                        )
                                    )
                                }
                            }
                        }

                        // Description Input
                        OutlinedTextField(
                            value = description,
                            onValueChange = {
                                description = it
                                descriptionError = if (it.isEmpty()) "Description is required" else null
                            },
                            label = { Text(
                                "Description",
                                color = colors.textSecondary
                            ) },
                            placeholder = { Text("What's this expense for?") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.ThumbUp,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            singleLine = true,
                            isError = descriptionError != null,
                            supportingText = descriptionError?.let { { Text(it) } },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = colors.cardBackground,
                                unfocusedContainerColor = colors.cardBackground,
                                focusedBorderColor = colors.primary,
                                unfocusedBorderColor = colors.border,
                                errorBorderColor = colors.error
                            )
                        )

                        // Date Picker
                        OutlinedTextField(
                            value = formatDate(date),
                            onValueChange = { /* Date is selected via dialog */ },
                            label = { Text("Date") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.ThumbUp,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = colors.cardBackground,
                                unfocusedContainerColor = colors.cardBackground,
                                focusedBorderColor = colors.primary,
                                unfocusedBorderColor = colors.border
                            )
                        )

                        // Group Selection
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = selectedGroup?.name ?: "",
                                onValueChange = { /* Group is selected via dropdown */ },
                                label = { Text(
                                    "Group",
                                    color = colors.textSecondary
                                ) },
                                placeholder = { Text(
                                    "Select a group",
                                    color = colors.textSecondary
                                ) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.ThumbUp,
                                        contentDescription = null,
                                        tint = colors.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                trailingIcon = {
                                    IconButton(onClick = { isGroupDropdownExpanded = !isGroupDropdownExpanded }) {
                                        Icon(
                                            if (isGroupDropdownExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Toggle dropdown"
                                        )
                                    }
                                },
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth(),
                                isError = groupError != null,
                                supportingText = groupError?.let { { Text(it) } },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = colors.cardBackground,
                                    unfocusedContainerColor = colors.cardBackground,
                                    focusedBorderColor = colors.primary,
                                    unfocusedBorderColor = colors.border,
                                    errorBorderColor = colors.error
                                )
                            )

                            // Group Dropdown Menu
                            DropdownMenu(
                                expanded = isGroupDropdownExpanded,
                                onDismissRequest = { isGroupDropdownExpanded = false },
                                modifier = Modifier
                                    .width(300.dp)
                                    .align(Alignment.TopEnd)
                            ) {
                                groups.forEach { group ->
                                    DropdownMenuItem(
                                        text = { Text(
                                            group.name,
                                            color = colors.textSecondary
                                        ) },
                                        onClick = {
                                            selectedGroupId = group.id
                                            viewModel.selectGroup(group.id)
                                            isGroupDropdownExpanded = false
                                            groupError = null
                                        },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.ThumbUp,
                                                contentDescription = null,
                                                tint = colors.primary
                                            )
                                        }
                                    )
                                }
                            }
                        }

                        // Paid By Section
                        SplitCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(lDimens.dp16)
                            ) {
                                Text(
                                    "Paid By",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = colors.textPrimary
                                )

                                Spacer(modifier = Modifier.height(lDimens.dp8))

                                // Default to current user
                                Surface(
                                    color = colors.primary.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(lDimens.dp12),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Person,
                                            contentDescription = null,
                                            tint = colors.primary,
                                            modifier = Modifier.size(24.dp)
                                        )

                                        Spacer(modifier = Modifier.width(lDimens.dp12))

                                        Text(
                                            "You",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = colors.textPrimary
                                        )

                                        Spacer(modifier = Modifier.weight(1f))

                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = colors.primary
                                        )
                                    }
                                }
                            }
                        }

                        // Split Section
                        if (selectedGroup != null) {
                            SplitCard(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(lDimens.dp16)
                                ) {
                                    Text(
                                        "Split Equally Between",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = colors.textPrimary
                                    )

                                    Spacer(modifier = Modifier.height(lDimens.dp8))

                                    val memberCount = selectedGroup!!.members.size
                                    val amountValue = amount.toDoubleOrNull() ?: 0.0
                                    val splitAmount = if (memberCount > 0) amountValue / memberCount else 0.0

                                    selectedGroup!!.members.forEach { member ->
                                        val memberName = if (member.name.isNullOrBlank())
                                            "User ${member.phoneNumber.takeLast(4)}"
                                        else
                                            member.name

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                memberName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = colors.textPrimary
                                            )

                                            Text(
                                                "₹${formatAmount(splitAmount)}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = colors.textSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Add Expense Button
                        Button(
                            onClick = {
                                // Validate inputs
                                var hasError = false

                                if (description.isEmpty()) {
                                    descriptionError = "Description is required"
                                    hasError = true
                                }

                                if (amount.isEmpty()) {
                                    amountError = "Amount is required"
                                    hasError = true
                                }

                                if (selectedGroupId == null) {
                                    groupError = "Group is required"
                                    hasError = true
                                }

                                if (!hasError) {
                                    viewModel.createExpense(
                                        description = description,
                                        amount = amount.toDoubleOrNull() ?: 0.0,
                                        selectedGroupId = selectedGroupId!!
                                    )
                                    onNavigateBack()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primary,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                "Add Expense",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        // Bottom spacer
                        Spacer(modifier = Modifier.height(lDimens.dp24))
                    }
                }
            }
        }
    }
}

private fun formatDate(date: LocalDate): String {
    return "${date.dayOfMonth} ${date.month.name.take(3)} ${date.year}"
}

private fun formatAmount(amount: Double): String {
    return ((amount * 100).toInt() / 100.0).toString()
}