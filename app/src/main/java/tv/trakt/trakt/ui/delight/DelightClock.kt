package tv.trakt.trakt.ui.delight

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import tv.trakt.trakt.helpers.extensions.rememberAnimationsDisabled

/**
 * Elapsed milliseconds of a one-shot delighter. Rests at [totalMs] (finished) and restarts from zero
 * every time [trigger] changes to a positive value. Read it from draw or graphicsLayer lambdas only.
 */
@Composable
internal fun rememberDelightClock(
    trigger: Int,
    totalMs: Int,
): Animatable<Float, AnimationVector1D> {
    val animationsOff = rememberAnimationsDisabled()
    val clock = remember { Animatable(totalMs.toFloat()) }

    LaunchedEffect(trigger) {
        if (trigger <= 0 || animationsOff) return@LaunchedEffect
        clock.snapTo(0F)
        clock.animateTo(
            targetValue = totalMs.toFloat(),
            animationSpec = tween(totalMs, easing = LinearEasing),
        )
    }

    return clock
}

internal fun segment(
    elapsedMs: Float,
    startMs: Float,
    durationMs: Float,
): Float = ((elapsedMs - startMs) / durationMs).coerceIn(0F, 1F)
