package com.falcon.split.presentation.screens.mainNavigation.AnimationComponents

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import kotlin.math.abs
import kotlin.math.min

/**
 * A header image that flips upward as the user scrolls
 */
@OptIn(ExperimentalResourceApi::class)
@Composable
fun UpwardFlipHeaderImage(
    imageResource: DrawableResource,
    state: Any? = null, // Can be LazyListState or PagerState or null
    headerHeight: Int = 150
) {
    // Calculate scroll offset based on the type of state provided
    val scrollOffset = when (state) {
        is LazyListState -> {
            // For LazyColumn
            try {
                if (state.firstVisibleItemIndex == 0) {
                    min(state.firstVisibleItemScrollOffset.toFloat(), headerHeight.toFloat())
                } else headerHeight.toFloat()
            } catch (e: Exception) {
                // Fallback if properties aren't available
                0f
            }
        }
        is PagerState -> {
            // For HorizontalPager - use current page offset
            try {
                min(abs(state.currentPageOffsetFraction) * 100, headerHeight.toFloat())
            } catch (e: Exception) {
                // Fallback if properties aren't available
                0f
            }
        }
        else -> {
            // Default case - no animation
            0f
        }
    }

    // Calculate animation values
    val rotationAngle by animateFloatAsState(
        targetValue = (scrollOffset / headerHeight) * 15, // Max 15 degrees rotation
        label = "rotation"
    )

    val scaleValue by animateFloatAsState(
        targetValue = 1f - (scrollOffset / headerHeight) * 0.1f, // Scale down to 90%
        label = "scale"
    )

    // Apply transformations through graphics layer
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(headerHeight.dp)
            .padding(0.dp)
            .graphicsLayer {
                // Apply rotation and scaling
                rotationX = rotationAngle
                scaleX = scaleValue
                scaleY = scaleValue

                // Keep the bottom of the card anchored during rotation
                // Use built-in TransformOrigin
                transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 1f)
            },
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Image(
            painter = painterResource(imageResource),
            contentDescription = "Header Image",
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.Crop
        )
    }
}