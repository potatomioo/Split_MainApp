package com.falcon.split.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.falcon.split.presentation.AppFontFamily

//Theme color file with all the defined sections.

// Brand Colors
val SplitGreen = Color(0xFF8FCB39)       // Primary brand green
val SplitIndigo = Color(0xFF4E5BF2)      // Secondary color for contrast and accents
val SplitPurple = Color(0xFF6B4EE6)      // Alternative accent color

// Light Theme Colors
val LightBackground = Color(0xFFF8F9FA)  // Main background - light gray with slight warmth
val LightSurface = Color(0xFFFFFFFF)     // Card & surface background - pure white
val LightPrimary = SplitGreen            // Primary actions and highlights - brand green
val LightSecondary = SplitIndigo         // Secondary actions - indigo
val LightTextPrimary = Color(0xFF212529) // Primary text - near black
val LightTextSecondary = Color(0xFF6C757D) // Secondary text - medium gray
val LightBorder = Color(0xFFDEE2E6)      // Border color - light gray
val LightDivider = Color(0xFFE9ECEF)     // Divider color - lighter gray

// Dark Theme Colors
val DarkBackground = Color(0xFF121212)   // Main background - near black
val DarkSurface = Color(0xFF1E1E1E)      // Card & surface background - dark gray
val DarkPrimary = SplitGreen.copy(alpha = 0.9f) // Brand green with slight transparency
val DarkSecondary = SplitIndigo.copy(alpha = 0.9f) // Indigo with slight transparency
val DarkTextPrimary = Color(0xFFF8F9FA)  // Primary text - off-white
val DarkTextSecondary = Color(0xFFADB5BD) // Secondary text - light gray
val DarkBorder = Color(0xFF2D3238)       // Border color - dark gray
val DarkDivider = Color(0xFF343A40)      // Divider color - medium dark gray

// Semantic Colors - Light Theme
val LightSuccess = Color(0xFF28A745)     // Success states - green
val LightError = Color(0xFFDC3545)       // Error states - red
val LightWarning = Color(0xFFFFC107)     // Warning states - amber
val LightInfo = Color(0xFF17A2B8)        // Info states - teal

// Semantic Colors - Dark Theme
val DarkSuccess = Color(0xFF5DD879)      // Success states - lighter green
val DarkError = Color(0xFFE35D6A)        // Error states - lighter red
val DarkWarning = Color(0xFFFFD43B)      // Warning states - brighter amber
val DarkInfo = Color(0xFF4ECBE0)         // Info states - lighter teal


class SplitColors(
    // Background colors
    val backgroundPrimary: Color,        // Main app background
    val backgroundSecondary: Color,      // Secondary background (headers, bottom bars)
    val cardBackground: Color,           // Card and surface elements

    // Brand colors
    val primary: Color,                  // Primary brand color for main actions
    val secondary: Color,                // Secondary color for alternate actions
    val accent: Color,                   // Accent color for highlights

    // Text colors
    val textPrimary: Color,              // Primary text color
    val textSecondary: Color,            // Secondary text color
    val textTertiary: Color,             // Tertiary/hint text color

    // UI element colors
    val border: Color,                   // Border color
    val divider: Color,                  // Divider color

    // Semantic colors
    val success: Color,                  // Success states
    val error: Color,                    // Error states
    val warning: Color,                  // Warning states
    val info: Color                      // Info states
)

// Provide app colors based on theme
val LocalSplitColors = staticCompositionLocalOf {
    lightSplitColors() // Default to light theme colors
}


 //Light theme colors configuration

fun lightSplitColors() = SplitColors(
    backgroundPrimary = LightBackground,
    backgroundSecondary = LightSurface,
    cardBackground = LightSurface,
    primary = LightPrimary,
    secondary = LightSecondary,
    accent = SplitPurple,
    textPrimary = LightTextPrimary,
    textSecondary = LightTextSecondary,
    textTertiary = LightTextSecondary.copy(alpha = 0.7f),
    border = LightBorder,
    divider = LightDivider,
    success = LightSuccess,
    error = LightError,
    warning = LightWarning,
    info = LightInfo
)

 //Dark theme colors configuration

fun darkSplitColors() = SplitColors(
    backgroundPrimary = DarkBackground,
    backgroundSecondary = DarkSurface,
    cardBackground = DarkSurface,
    primary = DarkPrimary,
    secondary = DarkSecondary,
    accent = SplitPurple.copy(alpha = 0.9f),
    textPrimary = DarkTextPrimary,
    textSecondary = DarkTextSecondary,
    textTertiary = DarkTextSecondary.copy(alpha = 0.7f),
    border = DarkBorder,
    divider = DarkDivider,
    success = DarkSuccess,
    error = DarkError,
    warning = DarkWarning,
    info = DarkInfo
)


@Composable
fun getSplitTypography(isDarkTheme: Boolean = isSystemInDarkTheme()): Typography {
    val colors = LocalSplitColors.current

    return Typography(
        // Display styles for hero sections and large headings
        displayLarge = TextStyle(
            fontFamily = AppFontFamily.nunitoFamily(),
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            lineHeight = 40.sp,
            letterSpacing = (-0.5).sp,
            color = colors.textPrimary
        ),
        displayMedium = TextStyle(
            fontFamily = AppFontFamily.nunitoFamily(),
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            lineHeight = 36.sp,
            letterSpacing = (-0.5).sp,
            color = colors.textPrimary
        ),
        displaySmall = TextStyle(
            fontFamily = AppFontFamily.nunitoFamily(),
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            lineHeight = 32.sp,
            color = colors.textPrimary
        ),

        // Headline styles for section headers
        headlineLarge = TextStyle(
            fontFamily = AppFontFamily.nunitoFamily(),
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            color = colors.textPrimary
        ),
        headlineMedium = TextStyle(
            fontFamily = AppFontFamily.nunitoFamily(),
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            lineHeight = 26.sp,
            color = colors.textPrimary
        ),
        headlineSmall = TextStyle(
            fontFamily = AppFontFamily.nunitoFamily(),
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            lineHeight = 24.sp,
            color = colors.textPrimary
        ),

        // Title styles for card headers and smaller sections
        titleLarge = TextStyle(
            fontFamily = AppFontFamily.nunitoFamily(),
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            lineHeight = 24.sp,
            color = colors.textPrimary
        ),
        titleMedium = TextStyle(
            fontFamily = AppFontFamily.nunitoFamily(),
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            lineHeight = 22.sp,
            letterSpacing = 0.1.sp,
            color = colors.textPrimary
        ),
        titleSmall = TextStyle(
            fontFamily = AppFontFamily.nunitoFamily(),
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp,
            color = colors.textPrimary
        ),

        // Body styles for main content
        bodyLarge = TextStyle(
            fontFamily = AppFontFamily.nunitoFamily(),
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.15.sp,
            color = colors.textPrimary
        ),
        bodyMedium = TextStyle(
            fontFamily = AppFontFamily.nunitoFamily(),
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.25.sp,
            color = colors.textPrimary
        ),
        bodySmall = TextStyle(
            fontFamily = AppFontFamily.nunitoFamily(),
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.4.sp,
            color = colors.textSecondary
        ),

        // Label styles for buttons, chips, etc.
        labelLarge = TextStyle(
            fontFamily = AppFontFamily.nunitoFamily(),
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp,
            color = colors.textPrimary
        ),
        labelMedium = TextStyle(
            fontFamily = AppFontFamily.nunitoFamily(),
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp,
            color = colors.textPrimary
        ),
        labelSmall = TextStyle(
            fontFamily = AppFontFamily.nunitoFamily(),
            fontWeight = FontWeight.Medium,
            fontSize = 10.sp,
            lineHeight = 14.sp,
            letterSpacing = 0.5.sp,
            color = colors.textSecondary
        )
    )
}


val SplitShapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(12.dp)
)


@Composable
fun SplitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    onThemeUpdated: () -> Unit = {},
    content: @Composable () -> Unit
) {
    // Select colors based on theme
    val splitColors = if (darkTheme) darkSplitColors() else lightSplitColors()

    // Material color scheme for integration with Material components
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = DarkPrimary,
            onPrimary = DarkTextPrimary,
            primaryContainer = DarkPrimary.copy(alpha = 0.12f),
            onPrimaryContainer = DarkPrimary,
            secondary = DarkSecondary,
            onSecondary = DarkTextPrimary,
            secondaryContainer = DarkSecondary.copy(alpha = 0.12f),
            onSecondaryContainer = DarkSecondary,
            tertiary = SplitPurple.copy(alpha = 0.9f),
            background = DarkBackground,
            onBackground = DarkTextPrimary,
            surface = DarkSurface,
            onSurface = DarkTextPrimary,
            surfaceVariant = DarkSurface.copy(alpha = 0.7f),
            error = DarkError,
            onError = DarkTextPrimary
        )
    } else {
        lightColorScheme(
            primary = LightPrimary,
            onPrimary = Color.White,
            primaryContainer = LightPrimary.copy(alpha = 0.12f),
            onPrimaryContainer = LightPrimary,
            secondary = LightSecondary,
            onSecondary = Color.White,
            secondaryContainer = LightSecondary.copy(alpha = 0.12f),
            onSecondaryContainer = LightSecondary,
            tertiary = SplitPurple,
            background = LightBackground,
            onBackground = LightTextPrimary,
            surface = LightSurface,
            onSurface = LightTextPrimary,
            surfaceVariant = LightSurface.copy(alpha = 0.7f),
            error = LightError,
            onError = Color.White
        )
    }

    // Typography system
    val typography = getSplitTypography(darkTheme)

    // Provide theme via CompositionLocal
    CompositionLocalProvider(
        LocalSplitColors provides splitColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            shapes = SplitShapes,
            content = content
        )
    }
}