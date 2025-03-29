package com.falcon.split

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition

actual class LottieAnimationSpec actual constructor(fileName: String) {
    val assetFileName: String = fileName
}

@Composable
actual fun LottieAnimationView(
    spec: LottieAnimationSpec,
    modifier: Modifier
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset(spec.assetFileName))

    LottieAnimation(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        modifier = Modifier
            .size(400.dp)
    )
}