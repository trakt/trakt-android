package tv.trakt.trakt.core.summary.livingposter.ui

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.isSupported
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import tv.trakt.trakt.common.model.livingposter.LivingPosterEffect
import tv.trakt.trakt.common.model.livingposter.LivingPosterEffect.Kind
import tv.trakt.trakt.core.summary.livingposter.model.LivingPosterParticle
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.sqrt

internal val easeInOut: Easing = CubicBezierEasing(0.42F, 0F, 0.58F, 1F)
internal val easeOut: Easing = CubicBezierEasing(0F, 0F, 0.58F, 1F)

private const val GLOW_RADIUS_RATIO = 2.5F
private const val PARTICLES_ALPHA = 0.9F

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
    val box = effect.box
    val boxRect = Rect(
        left = box.x / 100F * size.width,
        top = box.y / 100F * size.height,
        right = (box.x + box.width) / 100F * size.width,
        bottom = (box.y + box.height) / 100F * size.height,
    )
    val color = effect.color?.let(::parseHexColor) ?: Color.White
    val intensity = effect.intensity

    when (effect.kind) {
        Kind.Fog -> drawFog(boxRect, color, intensity, frame.seconds)
        Kind.Haze -> drawHaze(boxRect, color, intensity, frame.seconds)
        Kind.Glow -> drawGlow(boxRect, color, breathe(intensity, frame.seconds, duration = 4.5F))
        Kind.Flicker -> drawGlow(boxRect, color, flicker(intensity, frame.seconds))
        Kind.Lightning -> drawLightning(boxRect, intensity, frame.seconds)
        Kind.Bulbs -> drawBulbs(boxRect, effect.points, color, intensity, frame.seconds)
        Kind.Flare -> effect.at?.let { drawFlare(boxRect, it, intensity, frame) }
        Kind.Foil -> mask?.let { drawFoil(it, intensity, frame) }
        Kind.Shimmer -> mask?.let { drawShimmer(it, intensity, frame.seconds) }
        Kind.Embers, Kind.Rain, Kind.Twinkle, Kind.Snow, Kind.Fireflies, Kind.Dust -> Unit
    }
}

internal fun DrawScope.drawParticles(
    particles: List<LivingPosterParticle>,
    isScreen: Boolean,
    seconds: Float,
) {
    val bounds = size.toRect()
    drawIntoCanvas { canvas ->
        canvas.saveLayer(
            bounds,
            Paint().apply {
                alpha = PARTICLES_ALPHA
                if (isScreen) blendMode = BlendMode.Screen
            },
        )
        particles.forEach { particle -> drawParticle(particle, seconds) }
        canvas.restore()
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

private fun DrawScope.drawParticle(
    particle: LivingPosterParticle,
    seconds: Float,
) {
    val frame = particle.frame(seconds)
    if (frame.opacity <= 0.01F) return

    val box = particle.box
    val x = (box.x + frame.x * box.width / 100F) * size.width / 100F
    val y = (box.y + frame.y * box.height / 100F) * size.height / 100F
    val color = parseHexColor(particle.color)
    val opacity = frame.opacity.coerceIn(0F, 1F)

    when (particle.kind) {
        Kind.Rain -> {
            val length = particle.length.dp.toPx()
            val width = 1.dp.toPx()
            rotate(degrees = frame.rotation, pivot = Offset(x, y + length / 2F)) {
                drawRect(
                    brush = Brush.verticalGradient(
                        0F to color.copy(alpha = 0F),
                        1F to color,
                        startY = y,
                        endY = y + length,
                    ),
                    topLeft = Offset(x, y),
                    size = Size(width, length),
                    alpha = opacity,
                )
            }
        }

        Kind.Snow, Kind.Dust -> {
            drawCircle(
                color = color,
                radius = (particle.size * frame.scale / 2F).dp.toPx(),
                center = Offset(x, y),
                alpha = opacity,
            )
        }

        else -> {
            val radius = (particle.size * frame.scale * GLOW_RADIUS_RATIO).dp.toPx()
            drawCircle(
                brush = Brush.radialGradient(
                    0F to color,
                    0.2F to color,
                    0.45F to color.copy(alpha = 0.3F),
                    1F to color.copy(alpha = 0F),
                    center = Offset(x, y),
                    radius = radius,
                ),
                radius = radius,
                center = Offset(x, y),
                alpha = opacity,
            )
        }
    }
}

private fun DrawScope.drawFog(
    box: Rect,
    color: Color,
    intensity: Float,
    seconds: Float,
) {
    val bank = Rect(
        left = box.left - box.width * 0.3F,
        top = box.top,
        right = box.right + box.width * 0.3F,
        bottom = box.bottom,
    )
    val clear = color.copy(alpha = 0F)

    withLayerAlpha(intensity) {
        val firstShift = sway(seconds, duration = 22F, reverse = false) * bank.width
        translate(left = firstShift) {
            drawEllipseGradient(
                bank.at(0.24F, 0.6F),
                bank.width * 0.4F,
                bank.height * 0.6F,
                arrayOf(
                    0F to color,
                    0.7F to clear,
                ),
                bank,
            )
            drawEllipseGradient(
                bank.at(0.78F, 0.55F),
                bank.width * 0.4F,
                bank.height * 0.6F,
                arrayOf(
                    0F to color,
                    0.7F to clear,
                ),
                bank,
            )
        }

        val secondShift = sway(seconds, duration = 31F, reverse = true) * bank.width
        translate(left = secondShift) {
            drawEllipseGradient(
                bank.at(0.5F, 0.5F),
                bank.width * 0.35F,
                bank.height * 0.55F,
                arrayOf(
                    0F to color,
                    0.7F to clear,
                ),
                bank,
            )
            drawEllipseGradient(
                bank.at(0.95F, 0.6F),
                bank.width * 0.3F,
                bank.height * 0.5F,
                arrayOf(
                    0F to color,
                    0.7F to clear,
                ),
                bank,
            )
        }
    }
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

private fun DrawScope.drawLightning(
    box: Rect,
    intensity: Float,
    seconds: Float,
) {
    val color = Color(220, 230, 255)
    val alpha = keyframes(
        cycle(seconds, duration = 7F),
        LinearEasing,
        0F to 0F,
        0.88F to 0F,
        0.89F to intensity * 0.85F,
        0.90F to intensity * 0.1F,
        0.92F to intensity * 0.7F,
        0.95F to 0F,
        1F to 0F,
    )
    if (alpha <= 0F) return

    drawEllipseGradient(
        center = box.at(0.6F, 0.1F),
        radiusX = box.width * 0.7F,
        radiusY = box.height * 0.5F,
        stops = arrayOf(0F to color.copy(alpha = 0.9F), 0.7F to color.copy(alpha = 0F)),
        bounds = box,
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

private fun DrawScope.drawFlare(
    box: Rect,
    at: LivingPosterEffect.Point,
    intensity: Float,
    frame: LivingPosterFrame,
) {
    val unit = box.width / 100F
    val center = box.at(at.x / 100F, at.y / 100F)
    val haloRadius = 23F * unit

    drawCircle(
        brush = Brush.radialGradient(
            0F to Color(255, 240, 215).copy(alpha = intensity * 0.9F),
            0.45F to Color(255, 180, 110).copy(alpha = intensity * 0.25F),
            1F to Color(255, 180, 110).copy(alpha = 0F),
            center = center,
            radius = haloRadius,
        ),
        radius = haloRadius,
        center = center,
    )

    val streakColor = Color(255, 226, 190)
    val streakWidth = 70F * unit
    val streakHeight = 2.dp.toPx()
    rotate(degrees = frame.tiltX * 6F, pivot = center) {
        drawRect(
            brush = Brush.horizontalGradient(
                0F to streakColor.copy(alpha = 0F),
                0.5F to streakColor.copy(alpha = intensity * 0.8F),
                1F to streakColor.copy(alpha = 0F),
                startX = center.x - streakWidth / 2F,
                endX = center.x + streakWidth / 2F,
            ),
            topLeft = Offset(center.x - streakWidth / 2F, center.y - streakHeight / 2F),
            size = Size(streakWidth, streakHeight),
        )
    }

    val focusX = 50F + frame.tiltX * 20F
    val focusY = 50F + frame.tiltY * 20F
    val unitY = box.height / 100F
    flareGhosts.forEach { ghost ->
        val diameter = ghost.size * unit
        val left = box.left + (at.x + (focusX - at.x) * ghost.k) * unit - 4F * unit
        val top = box.top + (at.y + (focusY - at.y) * ghost.k) * unitY - 4F * unit
        val ghostCenter = Offset(left + diameter / 2F, top + diameter / 2F)

        drawCircle(
            brush = Brush.radialGradient(
                0F to ghost.color,
                1F to ghost.color.copy(alpha = 0F),
                center = ghostCenter,
                radius = diameter / 2F,
            ),
            radius = diameter / 2F,
            center = ghostCenter,
        )
    }
}

private data class FlareGhost(
    val k: Float,
    val size: Float,
    val color: Color,
)

private val flareGhosts = listOf(
    FlareGhost(k = 0.55F, size = 9F, color = Color(255, 170, 120).copy(alpha = 0.22F)),
    FlareGhost(k = 1.15F, size = 5F, color = Color(140, 200, 255).copy(alpha = 0.25F)),
    FlareGhost(k = 1.6F, size = 14F, color = Color(255, 230, 160).copy(alpha = 0.12F)),
)

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

private fun DrawScope.drawShimmer(
    mask: Painter,
    intensity: Float,
    seconds: Float,
) {
    val sweep = keyframes(cycle(seconds, duration = 5.5F), easeInOut, 0F to -1.4F, 0.55F to 1.4F, 1F to 1.4F)
    val white = Color.White

    drawMasked(mask, alpha = intensity) {
        translate(left = sweep * size.width) {
            drawRect(
                brush = cssLinearGradient(
                    angleDegrees = 105F,
                    rect = size.toRect(),
                    stops = arrayOf(
                        0.35F to white.copy(alpha = 0F),
                        0.5F to white.copy(alpha = 0.85F),
                        0.65F to white.copy(alpha = 0F),
                    ),
                ),
            )
        }
    }
}

private fun DrawScope.drawMasked(
    mask: Painter,
    alpha: Float,
    content: DrawScope.() -> Unit,
) {
    val bounds = size.toRect()
    drawIntoCanvas { canvas ->
        canvas.saveLayer(bounds, Paint().apply { this.alpha = alpha })
        content()
        canvas.saveLayer(bounds, Paint().apply { blendMode = BlendMode.DstIn })
        with(mask) { draw(size) }
        canvas.restore()
        canvas.restore()
    }
}

private fun DrawScope.withLayerAlpha(
    alpha: Float,
    content: DrawScope.() -> Unit,
) {
    drawIntoCanvas { canvas ->
        canvas.saveLayer(size.toRect(), Paint().apply { this.alpha = alpha })
        content()
        canvas.restore()
    }
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

/** Horizontal fog drift as a fraction of the bank width, alternating direction each run. */
private fun sway(
    seconds: Float,
    duration: Float,
    reverse: Boolean,
): Float {
    val iteration = floor(seconds / duration).toInt()
    val progress = cycle(seconds, duration)
    val backwards = (iteration % 2 == 1) != reverse
    val directed = if (backwards) 1F - progress else progress

    return lerp(-0.09F, 0.09F, easeInOut.transform(directed))
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
