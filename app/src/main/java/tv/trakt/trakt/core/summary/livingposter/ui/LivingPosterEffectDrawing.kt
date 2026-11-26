package tv.trakt.trakt.core.summary.livingposter.ui

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isUnspecified
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.isSupported
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.util.lerp
import tv.trakt.trakt.common.model.livingposter.LivingPosterEffect
import tv.trakt.trakt.common.model.livingposter.LivingPosterEffect.Kind
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

internal val easeInOut: Easing = CubicBezierEasing(0.42F, 0F, 0.58F, 1F)
internal val easeOut: Easing = CubicBezierEasing(0F, 0F, 0.58F, 1F)

/** Current tilt (-1..1 per axis) and running time shared by every animated layer. */
internal data class LivingPosterFrame(
    val tiltX: Float,
    val tiltY: Float,
    val seconds: Float,
)

internal fun DrawScope.drawEffect(
    effect: LivingPosterEffect,
    mask: Painter?,
    frame: LivingPosterFrame,
) {
    val boxRect = effect.box.toPixelRect(size)
    val color = effect.color?.let(::parseHexColor) ?: Color.White
    val intensity = effect.intensity

    when (effect.kind) {
        Kind.Haze -> drawHaze(boxRect, color, intensity, frame.seconds)
        Kind.Glow -> drawGlow(boxRect, color, breathe(intensity, frame.seconds, duration = 4.5F))
        Kind.Flicker -> drawGlow(boxRect, color, flicker(intensity, frame.seconds))
        Kind.Bulbs -> drawBulbs(boxRect, effect.points, color, intensity, frame.seconds)
        Kind.Foil -> mask?.let { drawFoil(it, intensity, frame) }
        Kind.Shimmer -> mask?.let { drawShimmer(it, intensity, frame) }
        Kind.Embers, Kind.Rain, Kind.Twinkle, Kind.Snow, Kind.Fireflies, Kind.Dust,
        Kind.Fog, Kind.Lightning, Kind.Flare,
        -> Unit
    }
}

internal fun DrawScope.drawVignette() {
    drawEllipseGradient(
        center = Offset(size.width * 0.5F, size.height * 0.4F),
        radiusX = size.width * 1.2F,
        radiusY = size.height * 0.9F,
        stops = arrayOf(
            0.55F to Color.Black.copy(alpha = 0F),
            1F to Color.Black.copy(alpha = 0.45F),
        ),
        bounds = size.toRect(),
    )
}

internal fun DrawScope.drawGlare(frame: LivingPosterFrame) {
    if (!BlendMode.Softlight.isSupported()) return

    val width = size.width * 1.4F
    val height = size.height * 1.4F
    val center = Offset(
        x = size.width / 2F + frame.tiltX * 0.45F * width,
        y = size.height / 2F + frame.tiltY * 0.45F * height,
    )
    val radius = sqrt((width / 2F) * (width / 2F) + (height / 2F) * (height / 2F))

    drawRect(
        brush = Brush.radialGradient(
            0F to Color.White,
            0.42F to Color.White.copy(alpha = 0F),
            center = center,
            radius = radius,
        ),
        alpha = 0.18F,
        blendMode = BlendMode.Softlight,
    )
}

private fun DrawScope.drawHaze(
    box: Rect,
    color: Color,
    intensity: Float,
    seconds: Float,
) {
    drawRect(
        brush = Brush.verticalGradient(
            0F to color,
            0.55F to color.copy(alpha = 0F),
            startY = box.top,
            endY = box.bottom,
        ),
        topLeft = box.topLeft,
        size = box.size,
        alpha = breathe(intensity, seconds, duration = 7F),
    )
}

private fun DrawScope.drawGlow(
    box: Rect,
    color: Color,
    alpha: Float,
) {
    val glow = Rect(
        left = box.left - box.width * 0.4F,
        top = box.top - box.height * 0.4F,
        right = box.right + box.width * 0.4F,
        bottom = box.bottom + box.height * 0.4F,
    )
    drawEllipseGradient(
        center = glow.center,
        radiusX = glow.width / 2F,
        radiusY = glow.height / 2F,
        stops = arrayOf(0F to color, 1F to color.copy(alpha = 0F)),
        bounds = glow,
        alpha = alpha,
    )
}

private fun DrawScope.drawBulbs(
    box: Rect,
    points: List<LivingPosterEffect.Point>,
    color: Color,
    intensity: Float,
    seconds: Float,
) {
    val bulbSize = box.width * if (points.size > 8) 0.04F else 0.07F

    points.forEachIndexed { index, point ->
        val delay = (index * 1.37F) % 4F
        if (seconds < delay) return@forEachIndexed

        val progress = cycle(seconds - delay, duration = 2.6F + (index * 0.53F) % 1.8F)
        val alpha = keyframes(
            progress,
            easeInOut,
            0F to 0F,
            0.2F to intensity,
            0.32F to intensity * 0.35F,
            0.42F to intensity,
            0.7F to 0F,
            1F to 0F,
        )
        if (alpha <= 0F) return@forEachIndexed

        val scale = keyframes(progress, easeInOut, 0F to 0.7F, 0.2F to 1F, 0.7F to 0.8F, 1F to 0.7F)
        val radius = bulbSize / 2F * scale
        val center = box.at(point.x / 100F, point.y / 100F)

        drawCircle(
            brush = Brush.radialGradient(
                0F to color,
                0.4F to color.copy(alpha = color.alpha * 0.35F),
                1F to color.copy(alpha = 0F),
                center = center,
                radius = radius,
            ),
            radius = radius,
            center = center,
            alpha = alpha,
        )
    }
}

private val foilStops = arrayOf(
    0.20F to Color(255, 120, 180).copy(alpha = 0F),
    0.34F to Color(255, 120, 180).copy(alpha = 0.55F),
    0.44F to Color(120, 220, 255).copy(alpha = 0.6F),
    0.54F to Color(255, 240, 140).copy(alpha = 0.6F),
    0.64F to Color(160, 120, 255).copy(alpha = 0.5F),
    0.78F to Color(160, 120, 255).copy(alpha = 0F),
)

private fun DrawScope.drawFoil(
    mask: Painter,
    intensity: Float,
    frame: LivingPosterFrame,
) {
    val sheetWidth = size.width * 2.2F
    val sheet = Rect(
        offset = Offset((size.width - sheetWidth) * (0.5F + frame.tiltX * 0.6F), 0F),
        size = Size(sheetWidth, size.height),
    )

    drawMasked(mask, alpha = intensity * 0.35F) {
        drawRect(brush = cssLinearGradient(110F + frame.tiltX * 40F, sheet, foilStops))
    }
}

/** A soft gloss that follows the tilt and a warm band sweeping across every few seconds. */
private fun DrawScope.drawShimmer(
    mask: Painter,
    intensity: Float,
    frame: LivingPosterFrame,
) {
    val sweep = keyframes(cycle(frame.seconds, duration = 8.5F), easeInOut, 0F to -0.9F, 0.21F to 0.9F, 1F to 0.9F)
    val blendMode = if (BlendMode.Overlay.isSupported()) BlendMode.Overlay else BlendMode.SrcOver
    val bounds = size.toRect()
    val band = Color(255, 247, 228)

    drawMasked(mask, alpha = intensity * 0.9F, blendMode = blendMode) {
        drawEllipseGradient(
            center = Offset(size.width * (0.5F - frame.tiltX * 0.45F), size.height * (0.35F - frame.tiltY * 0.45F)),
            radiusX = size.width * 0.7F,
            radiusY = size.height * 0.45F,
            stops = arrayOf(0F to Color.White.copy(alpha = 0.1F), 0.7F to Color.White.copy(alpha = 0F)),
            bounds = bounds,
        )
        translate(left = sweep * size.width) {
            drawRect(
                brush = cssLinearGradient(
                    angleDegrees = 108F,
                    rect = bounds,
                    stops = arrayOf(
                        0.37F to band.copy(alpha = 0F),
                        0.5F to band.copy(alpha = 0.8F),
                        0.63F to band.copy(alpha = 0F),
                    ),
                ),
            )
        }
    }
}

private fun DrawScope.drawMasked(
    mask: Painter,
    alpha: Float,
    blendMode: BlendMode = BlendMode.SrcOver,
    content: DrawScope.() -> Unit,
) {
    withLayer(alpha = alpha, blendMode = blendMode) {
        content()
        withLayer(blendMode = BlendMode.DstIn) {
            with(mask) { draw(size) }
        }
    }
}

/** Draws [content] into an offscreen layer composited with [alpha], [blendMode] and [colorFilter]. */
internal fun DrawScope.withLayer(
    bounds: Rect = size.toRect(),
    alpha: Float = 1F,
    blendMode: BlendMode = BlendMode.SrcOver,
    colorFilter: ColorFilter? = null,
    content: DrawScope.() -> Unit,
) {
    val paint = Paint().apply {
        this.alpha = alpha
        this.blendMode = blendMode
        this.colorFilter = colorFilter
    }

    drawIntoCanvas { canvas ->
        canvas.saveLayer(bounds, paint)
        content()
        canvas.restore()
    }
}

/** Draws [painter] scaled to fill the canvas, cropping the overflow like `ContentScale.Crop`. */
internal fun DrawScope.drawCover(
    painter: Painter,
    alpha: Float = 1F,
    colorFilter: ColorFilter? = null,
) {
    val intrinsic = painter.intrinsicSize
    if (intrinsic.isUnspecified || intrinsic.width <= 0F || intrinsic.height <= 0F) {
        with(painter) { draw(size, alpha, colorFilter) }
        return
    }

    val scale = maxOf(size.width / intrinsic.width, size.height / intrinsic.height)
    val drawSize = Size(intrinsic.width * scale, intrinsic.height * scale)

    translate((size.width - drawSize.width) / 2F, (size.height - drawSize.height) / 2F) {
        with(painter) { draw(drawSize, alpha, colorFilter) }
    }
}

internal fun LivingPosterEffect.Box.toPixelRect(size: Size): Rect {
    return Rect(
        left = x / 100F * size.width,
        top = y / 100F * size.height,
        right = (x + width) / 100F * size.width,
        bottom = (y + height) / 100F * size.height,
    )
}

private fun DrawScope.drawEllipseGradient(
    center: Offset,
    radiusX: Float,
    radiusY: Float,
    stops: Array<Pair<Float, Color>>,
    bounds: Rect,
    alpha: Float = 1F,
) {
    if (radiusX <= 0F || radiusY <= 0F || bounds.isEmpty) return

    val scaleY = radiusY / radiusX
    val top = center.y - (center.y - bounds.top) / scaleY
    val bottom = center.y + (bounds.bottom - center.y) / scaleY

    clipRect(bounds.left, bounds.top, bounds.right, bounds.bottom) {
        withTransform({ scale(scaleX = 1F, scaleY = scaleY, pivot = center) }) {
            drawRect(
                brush = Brush.radialGradient(*stops, center = center, radius = radiusX),
                topLeft = Offset(bounds.left, top),
                size = Size(bounds.width, bottom - top),
                alpha = alpha,
            )
        }
    }
}

private fun cssLinearGradient(
    angleDegrees: Float,
    rect: Rect,
    stops: Array<Pair<Float, Color>>,
): Brush {
    val radians = angleDegrees * PI.toFloat() / 180F
    val directionX = sin(radians)
    val directionY = -cos(radians)
    val half = (abs(rect.width * directionX) + abs(rect.height * directionY)) / 2F
    val center = rect.center

    return Brush.linearGradient(
        *stops,
        start = Offset(center.x - directionX * half, center.y - directionY * half),
        end = Offset(center.x + directionX * half, center.y + directionY * half),
    )
}

private fun breathe(
    intensity: Float,
    seconds: Float,
    duration: Float,
): Float {
    return keyframes(
        cycle(seconds, duration),
        easeInOut,
        0F to intensity * 0.55F,
        0.5F to intensity,
        1F to intensity * 0.55F,
    )
}

private fun flicker(
    intensity: Float,
    seconds: Float,
): Float {
    val progress = cycle(seconds, duration = 3.2F)
    val steps = listOf(
        0F to intensity,
        0.07F to intensity * 0.3F,
        0.09F to intensity,
        0.41F to intensity * 0.75F,
        0.44F to intensity,
        0.70F to intensity * 0.4F,
        0.72F to intensity,
    )

    return steps.last { (at, _) -> at <= progress }.second
}

private fun cycle(
    seconds: Float,
    duration: Float,
): Float {
    return (seconds / duration) % 1F
}

private fun keyframes(
    progress: Float,
    easing: Easing,
    vararg frames: Pair<Float, Float>,
): Float {
    val next = frames.indexOfFirst { (at, _) -> at >= progress }
    if (next <= 0) return frames.first().second

    val (fromAt, fromValue) = frames[next - 1]
    val (toAt, toValue) = frames[next]
    val span = toAt - fromAt
    if (span == 0F) return toValue

    return lerp(fromValue, toValue, easing.transform((progress - fromAt) / span))
}

private fun Rect.at(
    fractionX: Float,
    fractionY: Float,
): Offset {
    return Offset(left + width * fractionX, top + height * fractionY)
}

internal fun parseHexColor(hex: String): Color {
    val digits = hex.removePrefix("#")
    val expanded = when (digits.length) {
        3, 4 -> digits.map { "$it$it" }.joinToString("")
        else -> digits
    }
    val rgba = if (expanded.length == 6) "${expanded}ff" else expanded
    val value = rgba.toLongOrNull(16) ?: return Color.White

    return Color(
        red = ((value shr 24) and 0xFF).toInt(),
        green = ((value shr 16) and 0xFF).toInt(),
        blue = ((value shr 8) and 0xFF).toInt(),
        alpha = (value and 0xFF).toInt(),
    )
}
