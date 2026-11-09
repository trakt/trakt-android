package tv.trakt.trakt.core.userprofile.sections.match.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import tv.trakt.trakt.helpers.extensions.rememberAnimationsDisabled
import kotlin.time.Duration

internal val MatchEaseOut = CubicBezierEasing(0.2F, 0.8F, 0.2F, 1F)

/**
 * Progress from 0 to 1 for a one-shot entrance animation. Starts at 1 when animations are disabled.
 */
@Composable
internal fun rememberMatchEntrance(
    delay: Duration,
    duration: Duration,
    easing: Easing = MatchEaseOut,
): State<Float> {
    val animationsDisabled = rememberAnimationsDisabled()
    val progress = remember { Animatable(if (animationsDisabled) 1F else 0F) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1F,
            animationSpec = tween(
                durationMillis = duration.inWholeMilliseconds.toInt(),
                delayMillis = delay.inWholeMilliseconds.toInt(),
                easing = easing,
            ),
        )
    }

    return progress.asState()
}
