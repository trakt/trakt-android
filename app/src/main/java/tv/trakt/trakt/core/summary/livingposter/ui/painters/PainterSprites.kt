package tv.trakt.trakt.core.summary.livingposter.ui.painters

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.toArgb

private const val SPRITE_SIZE = 64

/** Alpha falloff of a round sprite, as (radius fraction, alpha) stops. */
internal enum class SpriteProfile(
    val stops: List<Pair<Float, Float>>,
) {
    Glow(listOf(0F to 1F, 0.2F to 1F, 0.45F to 0.3F, 1F to 0F)),
    GlowSoft(listOf(0F to 1F, 0.35F to 0.35F, 1F to 0F)),
    Core(listOf(0F to 1F, 0.16F to 0.95F, 0.4F to 0.26F, 0.7F to 0.05F, 1F to 0F)),
    Soft(listOf(0F to 1F, 0.28F to 0.45F, 0.62F to 0.11F, 1F to 0F)),
    Bokeh(listOf(0F to 0.5F, 0.6F to 0.56F, 0.8F to 0.5F, 0.92F to 0.16F, 1F to 0F)),
}

/** A small glow bitmap, so particles draw an image instead of building a gradient every frame. */
internal fun createRadialSprite(
    color: Color,
    profile: SpriteProfile,
): ImageBitmap {
    return createSprite(SPRITE_SIZE, SPRITE_SIZE) {
        val half = SPRITE_SIZE / 2F
        drawCircle(
            half,
            half,
            half,
            radialPaint(
                radius = half,
                stops = profile.stops.map { (at, alpha) -> at to color.copy(alpha = alpha) },
            ),
        )
    }
}

internal fun createSprite(
    width: Int,
    height: Int,
    paint: Canvas.() -> Unit,
): ImageBitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    Canvas(bitmap).paint()
    return bitmap.asImageBitmap()
}

internal fun radialPaint(
    radius: Float,
    stops: List<Pair<Float, Color>>,
    centerX: Float = radius,
    centerY: Float = radius,
): Paint {
    return Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = RadialGradient(
            centerX,
            centerY,
            radius,
            stops.map { (_, color) -> color.toArgb() }.toIntArray(),
            stops.map { (at, _) -> at }.toFloatArray(),
            Shader.TileMode.CLAMP,
        )
    }
}

/** Draws [image] stretched into the rect at [topLeft], keeping sub-pixel positions. */
internal fun DrawScope.drawImageIn(
    image: ImageBitmap,
    topLeft: Offset,
    size: Size,
    alpha: Float,
) {
    val clamped = alpha.coerceIn(0F, 1F)
    if (clamped <= 0F || size.width <= 0F || size.height <= 0F) return

    withTransform({
        translate(topLeft.x, topLeft.y)
        scale(size.width / image.width, size.height / image.height, pivot = Offset.Zero)
    }) {
        drawImage(image, alpha = clamped)
    }
}

internal fun DrawScope.drawSprite(
    sprite: ImageBitmap,
    center: Offset,
    width: Float,
    alpha: Float,
    height: Float = width,
) {
    drawImageIn(
        image = sprite,
        topLeft = Offset(center.x - width / 2F, center.y - height / 2F),
        size = Size(width, height),
        alpha = alpha,
    )
}
