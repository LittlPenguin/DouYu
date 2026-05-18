package cn.edu.app.douyu.core.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import cn.edu.app.douyu.ui.theme.DURATION_FAST
import cn.edu.app.douyu.ui.theme.SpringFast
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * Modifier that scales to 0.97 on press with spring animation.
 */
fun Modifier.scaleOnPress(): Modifier = composed {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = SpringFast,
        label = "pressScale"
    )

    this
        .pointerInput(isPressed) {
            coroutineScope {
                launch {
                    while (true) {
                        awaitPointerEventScope {
                            awaitFirstDown(requireUnconsumed = false)
                            isPressed = true
                            waitForUpOrCancellation()
                            isPressed = false
                        }
                    }
                }
            }
        }
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
}

/**
 * Staggered item animation: fade in + slide up with index-based delay.
 */
@Composable
fun StaggeredItemAnimator(
    index: Int,
    delayPerItem: Int = 50,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = 300,
                delayMillis = index * delayPerItem
            )
        ) + slideInVertically(
            animationSpec = tween(
                durationMillis = 300,
                delayMillis = index * delayPerItem
            ),
            initialOffsetY = { 20 }
        )
    ) {
        content()
    }
}

/**
 * Modifier that applies fade-in + slide-up entrance animation.
 */
fun Modifier.fadeSlideIn(
    delayMillis: Int = 0,
    durationMillis: Int = 300
): Modifier = composed {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { visible = true }

    this.graphicsLayer {
        alpha = if (visible) 1f else 0f
        translationY = if (visible) 0f else 20f
    }.animateContentSize()
}
