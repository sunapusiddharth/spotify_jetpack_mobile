package com.music.stream.neptune.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay

@Composable
fun StaggeredReveal(
    index: Int,
    modifier: Modifier = Modifier,
    delayStepMs: Long = 45L,
    maxDelaySteps: Int = 8,
    content: @Composable () -> Unit
) {
    var visible by remember(index) { mutableStateOf(false) }

    LaunchedEffect(index) {
        visible = false
        delay((index.coerceAtMost(maxDelaySteps) * delayStepMs))
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(animationSpec = tween(280)) + slideInVertically(
            initialOffsetY = { it / 5 },
            animationSpec = tween(280)
        ),
        exit = fadeOut(animationSpec = tween(160)) + slideOutVertically(
            targetOffsetY = { it / 8 },
            animationSpec = tween(160)
        )
    ) {
        content()
    }
}

@Composable
fun Modifier.pressScale(
    interactionSource: MutableInteractionSource,
    pressedScale: Float = 0.96f
): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "pressScale"
    )

    return this.graphicsLayer(
        scaleX = scale,
        scaleY = scale
    )
}
