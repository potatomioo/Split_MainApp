package com.falcon.split.presentation.theme

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Theme Switcher component for toggling between light and dark themes
 */
@Composable
fun ThemeSwitcher(
    darkTheme: Boolean = true,
    size: Dp = lDimens.dp50,
    padding: Dp = lDimens.dp5,
    borderWidth: Dp = lDimens.dp1,
    parentShape: Shape = CircleShape,
    toggleShape: Shape = CircleShape,
    animationSpec: AnimationSpec<Dp> = tween(durationMillis = 300),
    onClick: () -> Unit
) {
    val offset by animateDpAsState(
        targetValue = if (darkTheme) lDimens.dp0 else size,
        animationSpec = animationSpec
    )
    val colors = LocalSplitColors.current

    Box(modifier = Modifier
        .width(size * 2)
        .height(size)
        .clip(shape = parentShape)
        .clickable { onClick() }
        .background(if (darkTheme) colors.cardBackground else colors.backgroundSecondary)
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .offset(x = offset)
                .padding(all = padding)
                .clip(shape = toggleShape)
                .background(colors.primary)
        ) {}
        Row(
            modifier = Modifier
                .border(
                    border = BorderStroke(
                        width = borderWidth,
                        color = colors.primary
                    ),
                    shape = parentShape
                )
        ) {
            Box(
                modifier = Modifier.size(size),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🌙",
                    fontSize = 20.sp
                )
            }
            Box(
                modifier = Modifier.size(size),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "☀️",
                    fontSize = 20.sp
                )
            }
        }
    }
}

/**
 * Primary Button component with consistent styling
 */
@Composable
fun SplitPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null
) {
    val colors = LocalSplitColors.current

    Button(
        onClick = onClick,
        modifier = modifier.height(lDimens.dp48),
        enabled = enabled,
        shape = RoundedCornerShape(lDimens.dp24),
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.primary,
            contentColor = Color.White,
            disabledContainerColor = colors.primary.copy(alpha = 0.5f),
            disabledContentColor = Color.White.copy(alpha = 0.5f)
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier
                        .size(lDimens.dp18)
                        .padding(end = lDimens.dp8)
                )
            }
            Text(
                text = text,
                style = getSplitTypography().labelLarge.copy(fontWeight = FontWeight.SemiBold)
            )
        }
    }
}

/**
 * Secondary Button component with consistent styling
 */
@Composable
fun SplitSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null
) {
    val colors = LocalSplitColors.current

    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(lDimens.dp48),
        enabled = enabled,
        shape = RoundedCornerShape(lDimens.dp24),
        border = BorderStroke(lDimens.dp1, colors.primary)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier
                        .size(lDimens.dp18)
                        .padding(lDimens.dp8),
                    tint = colors.primary
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = colors.primary
            )
        }
    }
}

/**
 * Split Card component with consistent styling
 */
@Composable
fun SplitCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val colors = LocalSplitColors.current
    val isDarkTheme = isSystemInDarkTheme()

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = colors.cardBackground
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isDarkTheme) lDimens.dp2 else lDimens.dp4
        ),
        shape = RoundedCornerShape(lDimens.dp12)
    ) {
        content()
    }
}

/**
 * Split Outlined TextField component with consistent styling
 */
@Composable
fun SplitTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    singleLine: Boolean = false
) {
    val colors = LocalSplitColors.current

    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier.fillMaxWidth(),
            label = label?.let { { Text(it) } },
            placeholder = placeholder?.let { { Text(it) } },
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            isError = isError,
            singleLine = singleLine,
            shape = RoundedCornerShape(lDimens.dp12),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = colors.cardBackground,
                unfocusedContainerColor = colors.cardBackground,
                disabledContainerColor = colors.backgroundPrimary,
                focusedBorderColor = colors.primary,
                unfocusedBorderColor = colors.border,
                focusedTextColor = colors.textPrimary,
                unfocusedTextColor = colors.textPrimary
            )
        )

        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = colors.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = lDimens.dp16, top = lDimens.dp4)
            )
        }
    }
}

/**
 * Split Surface component with consistent styling
 */
@Composable
fun SplitSurface(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val colors = LocalSplitColors.current

    Surface(
        modifier = modifier,
        color = colors.backgroundPrimary,
        contentColor = colors.textPrimary
    ) {
        content()
    }
}

/**
 * Currency Display component for showing amount values consistently
 */
@Composable
fun CurrencyDisplay(
    amount: Double,
    isIncome: Boolean = true,
    large: Boolean = false,
    currencySymbol: String = "₹"
) {
    val colors = LocalSplitColors.current

    val textColor = when {
        amount == 0.0 -> colors.textSecondary
        isIncome -> colors.success
        else -> colors.error
    }

    val style = if (large) {
        MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
    } else {
        MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
    }

    val prefix = if (!isIncome && amount != 0.0) "-" else ""
    val absAmount = kotlin.math.abs(amount)
    val displayValue = (absAmount * 100).toInt() / 100.0
    Text(
        text = "$prefix$currencySymbol$displayValue",
        style = style,
        color = textColor
    )
}
