package com.falcon.split

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

actual class LottieAnimationSpec actual constructor(fileName: String)

@Composable
actual fun LottieAnimationView(
    spec: LottieAnimationSpec,
    modifier: Modifier
) {
}