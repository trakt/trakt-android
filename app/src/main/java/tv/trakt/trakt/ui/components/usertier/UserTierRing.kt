package tv.trakt.trakt.ui.components.usertier

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.model.UserTier
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

private const val RING_WIDTH_RATIO = 0.038F
private const val FREE_RING_WIDTH_RATIO = 0.008F
private const val RING_GAP_RATIO = 0.028F
private const val RING_TURN_MILLIS = 16_000
private const val SWEEP_START_DEGREES = 90F

private val MinRingWidth = 1.5.dp
private val MinFreeRingWidth = 1.dp
private val MinRingGap = 1.5.dp
private val NoGapMaxSize = 32.dp

@Composable
internal fun Modifier.userTierRing(
    tier: UserTier,
    animated: Boolean = false,
    gap: Boolean = true,
    color: Color? = null,
): Modifier {
    val isLight = TraktTheme.colors.isLight
    val brush = remember(tier, isLight, color) {
        color?.let(::SolidColor) ?: tier.ringBrush(isLight)
    }
    val rotation: State<Float>? = when {
        animated && tier != UserTier.Free -> rememberInfiniteTransition(label = "userTierRing")
            .animateFloat(
                initialValue = 0F,
                targetValue = 360F,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = RING_TURN_MILLIS, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
                label = "userTierRingTurn",
            )
        else -> null
    }

    return drawWithContent {
        val side = size.minDimension
        val widthRatio = if (tier == UserTier.Free) FREE_RING_WIDTH_RATIO else RING_WIDTH_RATIO
        val minWidth = if (tier == UserTier.Free) MinFreeRingWidth else MinRingWidth
        val ringWidth = maxOf(minWidth.toPx(), side * widthRatio)
        val ringGap = when {
            !gap || side < NoGapMaxSize.toPx() -> 0F
            else -> maxOf(MinRingGap.toPx(), side * RING_GAP_RATIO)
        }
        val faceScale = ((side - 2 * (ringWidth + ringGap)) / side).coerceAtLeast(0F)

        scale(scale = faceScale, pivot = center) {
            this@drawWithContent.drawContent()
        }

        rotate(degrees = SWEEP_START_DEGREES + (rotation?.value ?: 0F), pivot = center) {
            drawCircle(
                brush = brush,
                radius = (side - ringWidth) / 2,
                center = Offset(size.width / 2, size.height / 2),
                style = Stroke(width = ringWidth),
            )
        }
    }
}

private fun UserTier.ringBrush(isLight: Boolean): Brush {
    if (this == UserTier.Free) return SolidColor(userTierFreeRing(isLight))

    return Brush.sweepGradient(*ringStops(isLight).toTypedArray())
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            UserTier.entries.forEach { tier ->
                Image(
                    painter = painterResource(R.drawable.ic_person_placeholder),
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .userTierRing(tier)
                        .clip(CircleShape),
                )
            }
        }
    }
}
