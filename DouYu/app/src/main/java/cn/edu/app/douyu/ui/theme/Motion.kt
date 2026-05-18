package cn.edu.app.douyu.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

// ── Spring Animations ──

val SpringSpec = spring<Float>(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessMedium
)

val SpringFast = spring<Float>(
    dampingRatio = Spring.DampingRatioLowBouncy,
    stiffness = Spring.StiffnessHigh
)

// ── Tween Animations ──

val SmoothTween = tween<Float>(durationMillis = 200, easing = FastOutSlowInEasing)

// ── Duration Constants ──

const val DURATION_FAST = 150
const val DURATION_NORMAL = 250
const val DURATION_SLOW = 400
