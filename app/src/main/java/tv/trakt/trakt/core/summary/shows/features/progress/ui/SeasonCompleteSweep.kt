package tv.trakt.trakt.core.summary.shows.features.progress.ui

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import tv.trakt.trakt.common.ui.theme.colors.Shade10
import tv.trakt.trakt.common.ui.theme.colors.Yellow300
import tv.trakt.trakt.ui.delight.rememberDelightClock
import tv.trakt.trakt.ui.delight.segment
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private const val SWEEP_DELAY_MS = 180F
private const val SHINE_MS = 700F
private const val SHINE_WIDTH_FRACTION = 0.4F
private const val SHINE_ALPHA = 0.7F

private const val SPARKLE_COUNT = 6
private const val SPARKLE_DELAY_MS = SWEEP_DELAY_MS + 520F
private const val SPARKLE_MS = 700F
private const val SPARKLE_TRAVEL_DP = 24F
private const val SPARKLE_SIZE_DP = 10F
private const val SPARKLE_VIEWBOX = 24F
private const val SPARKLE_PATH = "M12 0C13 8 16 11 24 12C16 13 13 16 12 24C11 16 8 13 0 12C8 11 11 8 12 0Z"
private const val TOTAL_MS = (SPARKLE_DELAY_MS + SPARKLE_MS).toInt()

private val SparkleEasing = CubicBezierEasing(0.2F, 0.8F, 0.3F, 1F)

private data class SeasonCompletion(
    val seasonNumber: Int,
    val complete: Boolean,
    val loading: Boolean,
)

private fun didCompleteSeason(
    previous: SeasonCompletion?,
    next: SeasonCompletion,
): Boolean {
    if (previous == null || previous.loading || next.loading) return false
    if (previous.seasonNumber != next.seasonNumber) return false
    return !previous.complete && next.complete
}

@Composable
internal fun rememberSeasonCompleteCount(
    seasonNumber: Int,
    complete: Boolean,
    loading: Boolean,
): Int {
    var previous by remember { mutableStateOf<SeasonCompletion?>(null) }
    var count by remember { mutableIntStateOf(0) }

    LaunchedEffect(seasonNumber, complete, loading) {
        val next = SeasonCompletion(seasonNumber, complete, loading)
        if (didCompleteSeason(previous, next)) count++
        previous = next
    }

    return count
}

@Composable
internal fun SeasonCompleteSweep(
    completions: Int,
    modifier: Modifier = Modifier,
) {
    if (completions <= 0) return

    val clock = rememberDelightClock(completions, TOTAL_MS)
    val direction = if (LocalLayoutDirection.current == LayoutDirection.Rtl) -1F else 1F
    val star = remember { PathParser().parsePathString(SPARKLE_PATH).toPath() }
    val sparkleAngles = remember { sparkleAngles() }

    Canvas(modifier = modifier.fillMaxSize()) {
        val ms = clock.value
        if (ms >= TOTAL_MS) return@Canvas

        drawShine(ms = ms, direction = direction)
        drawSparkles(ms = ms, direction = direction, star = star, angles = sparkleAngles)
    }
}

private fun sparkleAngles(): List<Float> =
    List(SPARKLE_COUNT) { index ->
        (-PI / 2 + (index - (SPARKLE_COUNT - 1) / 2F) * PI / (SPARKLE_COUNT + 1)).toFloat()
    }

private fun DrawScope.drawShine(
    ms: Float,
    direction: Float,
) {
    val t = segment(ms, SWEEP_DELAY_MS, SHINE_MS)
    if (t <= 0F || t >= 1F) return

    val shineWidth = size.width * SHINE_WIDTH_FRACTION
    val travel = lerp(-1F, 2.5F, t) * shineWidth
    val left = if (direction > 0F) travel else size.width - travel - shineWidth
    val clip = Path().apply {
        addRoundRect(
            RoundRect(
                rect = Rect(Offset.Zero, size),
                cornerRadius = CornerRadius(size.height / 2F),
            ),
        )
    }

    clipPath(clip) {
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(Color.Transparent, Shade10.copy(alpha = SHINE_ALPHA), Color.Transparent),
                startX = left,
                endX = left + shineWidth,
            ),
            topLeft = Offset(left, 0F),
            size = Size(shineWidth, size.height),
        )
    }
}

private fun DrawScope.drawSparkles(
    ms: Float,
    direction: Float,
    star: Path,
    angles: List<Float>,
) {
    val t = segment(ms, SPARKLE_DELAY_MS, SPARKLE_MS)
    if (t <= 0F || t >= 1F) return

    val travel = SPARKLE_TRAVEL_DP.dp.toPx() * SparkleEasing.transform(t)
    val scale = SPARKLE_SIZE_DP.dp.toPx() / SPARKLE_VIEWBOX * lerp(0.3F, 1F, t)
    val alpha = if (t < 0.25F) t / 0.25F else 1F - (t - 0.25F) / 0.75F
    val anchorX = if (direction > 0F) size.width else 0F

    angles.forEach { angle ->
        withTransform({
            translate(
                left = anchorX + direction * cos(angle) * travel,
                top = size.height / 2F + sin(angle) * travel,
            )
            rotate(degrees = 90F * t, pivot = Offset.Zero)
            scale(scale, scale, pivot = Offset.Zero)
            translate(-SPARKLE_VIEWBOX / 2F, -SPARKLE_VIEWBOX / 2F)
        }) {
            drawPath(path = star, color = Yellow300.copy(alpha = alpha))
        }
    }
}
