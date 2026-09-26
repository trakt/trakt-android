package tv.trakt.trakt.core.ratings.ui

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.util.lerp
import tv.trakt.trakt.common.ui.theme.colors.Green500
import tv.trakt.trakt.common.ui.theme.colors.Red500
import tv.trakt.trakt.common.ui.theme.colors.Shade10
import tv.trakt.trakt.common.ui.theme.colors.Yellow500
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private const val FLIGHT_MS = 460f
private const val SPLAT_MS = 1500f
private const val DROP_MS = 520f
private const val TOMATO_TOTAL_MS = FLIGHT_MS + SPLAT_MS

private const val KERNEL_COUNT = 16
private const val KERNEL_MAX_DELAY_MS = 520f
private const val KERNEL_MAX_DURATION_MS = 1500f
private const val POPCORN_TOTAL_MS = KERNEL_MAX_DELAY_MS + KERNEL_MAX_DURATION_MS

private const val TOMATO_LEAF_PATH =
    "M12 3.5l1.6 2.6 3-.6-1.9 2.4 2.6 1.3-3.4.2L12 12l-1.9-2.6-3.4-.2 2.6-1.3-1.9-2.4 3 .6z"

private val DecelerateEasing = CubicBezierEasing(0.2f, 0.7f, 0.4f, 1f)
private val AccelerateEasing = CubicBezierEasing(0.5f, 0f, 0.8f, 0.5f)
private val EaseOut = CubicBezierEasing(0f, 0f, 0.58f, 1f)
private val EaseIn = CubicBezierEasing(0.42f, 0f, 1f, 1f)

private data class Drop(
    val x: Float,
    val y: Float,
)

private data class Kernel(
    val startX: Float,
    val drift: Float,
    val rise: Float,
    val fall: Float,
    val spin: Float,
    val delay: Float,
    val duration: Float,
)

@Composable
internal fun RatingDelightOverlay(
    delight: RatingDelight,
    origin: Offset,
    key: Any,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val animationsOff = remember {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        ) == 0f
    }
    if (animationsOff) return

    val direction = if (LocalLayoutDirection.current == LayoutDirection.Rtl) -1f else 1f
    val totalMs = when (delight) {
        RatingDelight.RottenTomato -> TOMATO_TOTAL_MS
        RatingDelight.Popcorn -> POPCORN_TOTAL_MS
    }

    val elapsed = remember(key) { Animatable(0f) }
    LaunchedEffect(key) {
        elapsed.animateTo(
            targetValue = totalMs,
            animationSpec = tween(totalMs.toInt(), easing = LinearEasing),
        )
    }

    val splat = remember(key) { splatPath() }
    val drops = remember(key) { drops() }
    val kernels = remember(key) { kernels() }
    val leaf = remember { PathParser().parsePathString(TOMATO_LEAF_PATH).toPath() }

    Canvas(modifier = modifier) {
        val ms = elapsed.value
        if (ms >= totalMs) return@Canvas

        when (delight) {
            RatingDelight.RottenTomato -> drawRottenTomato(
                ms = ms,
                origin = origin,
                direction = direction,
                leaf = leaf,
                splat = splat,
                drops = drops,
            )
            RatingDelight.Popcorn -> drawPopcorn(
                ms = ms,
                origin = origin,
                direction = direction,
                kernels = kernels,
            )
        }
    }
}

private fun DrawScope.drawRottenTomato(
    ms: Float,
    origin: Offset,
    direction: Float,
    leaf: Path,
    splat: Path,
    drops: List<Drop>,
) {
    val unit = density
    if (ms < FLIGHT_MS) {
        val t = ms / FLIGHT_MS
        val x = lerp(-110f * direction, 0f, t)
        val y = if (t < 0.35f) {
            lerp(-64f, -86f, EaseOut.transform(t / 0.35f))
        } else {
            lerp(-86f, 0f, EaseIn.transform((t - 0.35f) / 0.65f))
        }

        withTransform({
            translate(origin.x + x * unit, origin.y + y * unit)
            rotate(420f * t * direction, pivot = Offset.Zero)
            scale(unit, unit, pivot = Offset.Zero)
            translate(-12f, -12f)
        }) {
            drawCircle(Red500, radius = 9.5f, center = Offset(12f, 13.5f))
            drawPath(leaf, Green500)
            drawOval(
                color = Color.White.copy(alpha = 0.35f),
                topLeft = Offset(5.6f, 9.6f),
                size = Size(4.8f, 2.8f),
            )
        }
        return
    }

    val splatT = (ms - FLIGHT_MS) / SPLAT_MS
    val alpha = keyframe(splatT, 0f to 0f, 0.01f to 1f, 0.2f to 0.95f, 0.6f to 0.95f, 1f to 0f)
    val scale = keyframe(splatT, 0f to 0.2f, 0.12f to 1.12f, 0.2f to 1f, 1f to 1f)
    val sinkY = keyframe(splatT, 0f to 0f, 0.6f to 0f, 1f to 10f)
    val stretchY = keyframe(splatT, 0f to 1f, 0.6f to 1f, 1f to 1.15f)

    withTransform({
        translate(origin.x, origin.y + sinkY * unit)
        scale(unit * scale, unit * scale * stretchY, pivot = Offset.Zero)
        translate(-24f, -24f)
    }) {
        drawPath(splat, Red500, alpha = alpha)
    }

    val dropT = ((ms - FLIGHT_MS) / DROP_MS).coerceIn(0f, 1f)
    if (dropT >= 1f) return
    val travel = DecelerateEasing.transform(dropT)
    drops.forEach { drop ->
        drawCircle(
            color = Red500,
            alpha = 1f - dropT,
            radius = 3f * unit * lerp(1f, 0.5f, travel),
            center = origin + Offset(drop.x * unit, drop.y * unit) * travel,
        )
    }
}

private fun DrawScope.drawPopcorn(
    ms: Float,
    origin: Offset,
    direction: Float,
    kernels: List<Kernel>,
) {
    val unit = density
    kernels.forEach { kernel ->
        val t = (ms - kernel.delay) / kernel.duration
        if (t !in 0f..1f) return@forEach

        val isRising = t < 0.42f
        val q = if (isRising) {
            DecelerateEasing.transform(t / 0.42f)
        } else {
            AccelerateEasing.transform((t - 0.42f) / 0.58f)
        }
        val peakX = kernel.startX + kernel.drift
        val x = if (isRising) lerp(kernel.startX, peakX, q) else lerp(peakX, kernel.startX + kernel.drift * 1.5f, q)
        val y = if (isRising) lerp(0f, -kernel.rise, q) else lerp(-kernel.rise, kernel.fall, q)
        val scale = if (isRising) lerp(0.2f, 1f, q) else lerp(1f, 0.9f, q)
        val spin = if (isRising) lerp(0f, kernel.spin / 2f, q) else lerp(kernel.spin / 2f, kernel.spin, q)
        val alpha = if (t < 0.02f) t / 0.02f else 1f - (t - 0.02f) / 0.98f

        withTransform({
            translate(origin.x + (x * direction) * unit, origin.y + y * unit)
            rotate(spin, pivot = Offset.Zero)
            scale(unit * scale * 0.9f, unit * scale * 0.9f, pivot = Offset.Zero)
            translate(-10f, -10f)
        }) {
            listOf(Offset(6.5f, 11.5f), Offset(13.5f, 11.5f), Offset(10f, 6.5f)).forEach { puff ->
                drawCircle(Shade10, radius = 4.8f, center = puff, alpha = alpha)
                drawCircle(Yellow500, radius = 4.8f, center = puff, alpha = alpha, style = Stroke(1f))
            }
            drawCircle(Yellow500, radius = 2.2f, center = Offset(10f, 14f), alpha = alpha)
        }
    }
}

private fun keyframe(
    t: Float,
    vararg frames: Pair<Float, Float>,
): Float {
    val next = frames.indexOfFirst { it.first >= t }
    if (next <= 0) return frames.first().second
    val (fromT, fromValue) = frames[next - 1]
    val (toT, toValue) = frames[next]
    return lerp(fromValue, toValue, (t - fromT) / (toT - fromT))
}

private fun random(
    min: Float,
    max: Float,
): Float = min + Random.nextFloat() * (max - min)

private fun splatPath(): Path {
    val pointCount = 16
    val points = List(pointCount) { index ->
        val angle = index.toFloat() / pointCount * 2f * PI.toFloat()
        val radius = if (index % 2 == 1) random(9f, 13f) else random(15f, 21f)
        Offset(24f + cos(angle) * radius, 24f + sin(angle) * radius)
    }
    val midpoint = { from: Offset, to: Offset -> (from + to) / 2f }

    return Path().apply {
        val start = midpoint(points.last(), points.first())
        moveTo(start.x, start.y)
        points.forEachIndexed { index, point ->
            val end = midpoint(point, points[(index + 1) % pointCount])
            quadraticTo(point.x, point.y, end.x, end.y)
        }
        close()
    }
}

private fun drops(): List<Drop> {
    return List(8) {
        val angle = random(0f, 2f * PI.toFloat())
        val distance = random(18f, 32f)
        Drop(x = cos(angle) * distance, y = sin(angle) * distance + 12f)
    }
}

private fun kernels(): List<Kernel> {
    return List(KERNEL_COUNT) {
        Kernel(
            startX = random(-150f, 0f),
            drift = random(-26f, 26f),
            rise = random(40f, 88f),
            fall = random(30f, 60f),
            spin = random(-200f, 200f),
            delay = random(0f, KERNEL_MAX_DELAY_MS),
            duration = random(1100f, KERNEL_MAX_DURATION_MS),
        )
    }
}
