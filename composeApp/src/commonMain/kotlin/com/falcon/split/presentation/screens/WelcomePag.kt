package com.falcon.split.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.falcon.split.LottieAnimationSpec
import com.falcon.split.LottieAnimationView
import com.falcon.split.presentation.screens.mainNavigation.Routes
import com.falcon.split.presentation.theme.LocalSplitColors
import com.falcon.split.presentation.theme.SplitPrimaryButton
import com.falcon.split.presentation.theme.getSplitTypography
import com.falcon.split.presentation.theme.lDimens
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import split.composeapp.generated.resources.Res
import split.composeapp.generated.resources.Split
import split.composeapp.generated.resources.nunito_bold_1

@OptIn(ExperimentalResourceApi::class)
@Composable
fun WelcomePage(navController: NavHostController) {
    val scope = rememberCoroutineScope()
    val colors = LocalSplitColors.current
    val scrollState = rememberScrollState()

    // Animation states
    var showLogo by remember { mutableStateOf(false) }
    var showAnimation by remember { mutableStateOf(false) }
    var showTitle by remember { mutableStateOf(false) }
    var showFeatures by remember { mutableStateOf(false) }
    var showButton by remember { mutableStateOf(false) }

    // Trigger animations sequentially
    LaunchedEffect(Unit) {
        showLogo = true
        delay(300)
        showAnimation = true
        delay(600)
        showTitle = true
        delay(300)
        showFeatures = true
        delay(300)
        showButton = true
    }

    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundPrimary)
            .verticalScroll(scrollState)
            .padding(bottom = lDimens.dp24)
    ) {
        Spacer(modifier = Modifier.height(lDimens.dp40))

        // Logo
        AnimatedVisibility(
            visible = showLogo,
            enter = fadeIn(animationSpec = tween(800)) +
                    slideInVertically(
                        initialOffsetY = { -100 },
                        animationSpec = tween(800)
                    )
        ) {
            Image(
                painter = painterResource(Res.drawable.Split),
                contentDescription = "Split Logo",
                modifier = Modifier
                    .size(lDimens.dp100)
                    .padding(lDimens.dp8),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.height(lDimens.dp16))

        // Animation
        AnimatedVisibility(
            visible = showAnimation,
            enter = fadeIn(animationSpec = tween(1000))
        ) {
            LottieAnimationView(
                LottieAnimationSpec("welcome.json"),
                modifier = Modifier.size(lDimens.dp230)
            )
        }

        // Title and Tagline
        AnimatedVisibility(
            visible = showTitle,
            enter = fadeIn(animationSpec = tween(800)) +
                    slideInVertically(
                        initialOffsetY = { 50 },
                        animationSpec = tween(800)
                    )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "WELCOME TO SPLIT!",
                    fontSize = 24.sp,
                    fontFamily = FontFamily(org.jetbrains.compose.resources.Font(Res.font.nunito_bold_1, weight = FontWeight.Bold)),
                    style = getSplitTypography().titleMedium,
                    color = colors.textPrimary,
                    modifier = Modifier
                        .padding(lDimens.dp10, lDimens.dp0, lDimens.dp10, lDimens.dp0)
                )

                Text(
                    text = "Split Easy, Stay Friends!",
                    style = getSplitTypography().bodyLarge,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = lDimens.dp24, vertical = lDimens.dp8)
                )
            }
        }

        Spacer(modifier = Modifier.height(lDimens.dp24))

        // Get Started Button
        AnimatedVisibility(
            visible = showButton,
            enter = fadeIn(animationSpec = tween(800)) +
                    slideInVertically(
                        initialOffsetY = { 50 },
                        animationSpec = tween(800)
                    )
        ) {
            SplitPrimaryButton(
                text = "Get Started",
                onClick = {
                    scope.launch {
                        navController.navigate(Routes.SIGN_IN.name)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = lDimens.dp32, vertical = lDimens.dp16)
            )
        }
    }
}