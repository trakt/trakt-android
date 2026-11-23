package tv.trakt.trakt.core.home.sections.streaks

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import tv.trakt.trakt.common.ui.theme.colors.Orange400
import tv.trakt.trakt.common.ui.theme.colors.Yellow300
import tv.trakt.trakt.ui.delight.rememberDelightClock
import tv.trakt.trakt.ui.delight.segment
import kotlin.random.Random

private const val FLARE_MS = 900F
private const val FLARE_PEAK = 0.35F
private const val FLARE_SETTLE = 0.7F

private const val FLIP_MS = 420F

private const val EMBER_COUNT = 12
private const val EMBER_STAGGER_MS = 45F
private const val EMBERS_TOTAL_MS = 1600

private const val PILL_GLOW_DELAY_MS = 260F
private const val PILL_GLOW_MS = 1600F
private const val PILL_GLOW_PEAK = 0.3F
private const val PILL_GLOW_TOTAL_MS = 1900

private val PILL_HALO_SPREAD = 3.dp
private const val PILL_HALO_ALPHA = 0.35F
private val FlipEasing = CubicBezierEasing(0.3F, 1.5F, 0.5F, 1F)

private val EmberRise = 30F..64F
private val EmberDrift = -18F..18F
private val EmberStart = -10F..10F
private val EmberSize = 3F..5F
private val EmberDuration = 700F..1100F

private data class Ember(
    val startX: Float,
    val drift: Float,
    val rise: Float,
    val size: Float,
    val delay: Float,
    val duration: Float,
    val isHot: Boolean,
)

private fun ClosedFloatingPointRange<Float>.random(): Float = start + Random.nextFloat() * (endInclusive - start)

private fun embers(): List<Ember> =
    List(EMBER_COUNT) { index ->
        Ember(
            startX = EmberStart.random(),
            drift = EmberDrift.random(),
            rise = EmberRise.random(),
            size = EmberSize.random(),
            delay = index * EMBER_STAGGER_MS,
            duration = EmberDuration.random(),
            isHot = index % 3 == 0,
        )
    }

@Composable
internal fun Modifier.streakFlare(celebration: Int): Modifier {
    val clock = rememberDelightClock(celebration, FLARE_MS.toInt())
    return graphicsLayer {
        transformOrigin = TransformOrigin(0.5F, 0.9F)
        val t = clock.value / FLARE_MS
        when {
            t < FLARE_PEAK -> {
                val f = t / FLARE_PEAK
                scaleX = lerp(1F, 1.3F, f)
                scaleY = lerp(1F, 1.5F, f)
            }
            t < FLARE_SETTLE -> {
                val f = (t - FLARE_PEAK) / (FLARE_SETTLE - FLARE_PEAK)
                scaleX = lerp(1.3F, 0.96F, f)
                scaleY = lerp(1.5F, 0.96F, f)
            }
            else -> {
                val f = (t - FLARE_SETTLE) / (1F - FLARE_SETTLE)
                scaleX = lerp(0.96F, 1F, f)
                scaleY = lerp(0.96F, 1F, f)
            }
        }
    }
}

@Composable
internal fun Modifier.streakCountFlip(celebration: Int): Modifier {
    val clock = rememberDelightClock(celebration, FLIP_MS.toInt())
    return graphicsLayer {
        rotationX = -90F * (1F - FlipEasing.transform(clock.value / FLIP_MS))
    }
}

@Composable
internal fun StreakEmbers(
    celebration: Int,
    modifier: Modifier = Modifier,
) {
    if (celebration <= 0) return

    val clock = rememberDelightClock(celebration, EMBERS_TOTAL_MS)
    val batch = remember(celebration) { embers() }
    val direction = if (LocalLayoutDirection.current == LayoutDirection.Rtl) -1F else 1F

    Canvas(modifier = modifier.fillMaxSize()) {
        val ms = clock.value
        if (ms >= EMBERS_TOTAL_MS) return@Canvas

        batch.forEach { ember ->
            val t = segment(ms, ember.delay, ember.duration)
            if (t <= 0F || t >= 1F) return@forEach

            val alpha = if (t < 0.1F) t / 0.1F else 1F - (t - 0.1F) / 0.9F
            val x = center.x + direction * (ember.startX + ember.drift * t) * density
            val y = size.height * 0.4F - ember.rise * t * density
            drawCircle(
                color = (if (ember.isHot) Yellow300 else Orange400).copy(alpha = alpha),
                radius = ember.size / 2F * density,
                center = Offset(x, y),
            )
        }
    }
}

@Composable
internal fun Modifier.streakTodayGlow(celebration: Int): Modifier {
    val clock = rememberDelightClock(celebration, PILL_GLOW_TOTAL_MS)
    if (celebration <= 0) return this

    return this
        .graphicsLayer {
            val t = segment(clock.value, PILL_GLOW_DELAY_MS, PILL_GLOW_MS)
            scaleY = when {
                t <= 0F || t >= 1F -> 1F
                t < PILL_GLOW_PEAK -> lerp(1F, 1.2F, t / PILL_GLOW_PEAK)
                else -> lerp(1.2F, 1F, (t - PILL_GLOW_PEAK) / (1F - PILL_GLOW_PEAK))
            }
        }
        .drawBehind {
            val t = segment(clock.value, PILL_GLOW_DELAY_MS, PILL_GLOW_MS)
            if (t <= 0F) return@drawBehind

            val litFraction = (t / PILL_GLOW_PEAK).coerceAtMost(1F)
            val haloAlpha = PILL_HALO_ALPHA * litFraction * (1F - segment(t, PILL_GLOW_PEAK, 1F - PILL_GLOW_PEAK))
            if (haloAlpha > 0F) {
                val spread = PILL_HALO_SPREAD.toPx()
                drawRoundRect(
                    color = Orange400.copy(alpha = haloAlpha),
                    topLeft = Offset(-spread, -spread),
                    size = Size(size.width + spread * 2, size.height + spread * 2),
                    cornerRadius = CornerRadius(size.width / 2F + spread),
                )
            }
            drawRoundRect(
                color = Orange400.copy(alpha = litFraction),
                cornerRadius = CornerRadius(size.width / 2F),
            )
        }
}
