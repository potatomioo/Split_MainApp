package com.falcon.split

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// commonMain/src/CommonLottieAnimation.kt

expect class LottieAnimationSpec(fileName: String)

@Composable
expect fun LottieAnimationView(spec: LottieAnimationSpec, modifier: Modifier = Modifier)
