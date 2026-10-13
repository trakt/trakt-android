package tv.trakt.trakt.core.main.ui.seasonal

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalInspectionMode
import kotlinx.coroutines.delay
import tv.trakt.trakt.helpers.extensions.rememberAnimationsDisabled
import kotlin.time.Duration.Companion.milliseconds

internal val SplashEaseOutQuint = CubicBezierEasing(0.22F, 1F, 0.36F, 1F)
internal val SplashEaseOutBack = CubicBezierEasing(0.34F, 1.56F, 0.64F, 1F)

@Composable
internal fun rememberSplashAnimate(): Boolean {
    return !rememberAnimationsDisabled() && !LocalInspectionMode.current
}

/**
 * One-shot 0 to 1 progress for an entrance animation. Starts at 1 when [animate] is false
 * so reduced-motion users and previews see the settled layout.
 */
@Composable
internal fun rememberSplashIntro(
    animate: Boolean,
    delayMillis: Int,
    durationMillis: Int,
    easing: Easing = LinearOutSlowInEasing,
): Animatable<Float, AnimationVector1D> {
    val progress = remember { Animatable(if (animate) 0F else 1F) }

    LaunchedEffect(animate) {
        if (!animate) return@LaunchedEffect
        delay(delayMillis.milliseconds)
        progress.animateTo(
            targetValue = 1F,
            animationSpec = tween(durationMillis = durationMillis, easing = easing),
        )
    }

    return progress
}
