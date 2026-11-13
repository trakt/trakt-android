package tv.trakt.trakt.core.settings.features.avatar

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import kotlin.math.max

internal const val AVATAR_MIN_ZOOM = 1F
internal const val AVATAR_MAX_ZOOM = 4F

/**
 * Square crop area of an image. [left] and [top] are fractions of the image width and height,
 * [size] is a fraction of the shorter image side.
 */
@Immutable
internal data class AvatarCrop(
    val left: Float,
    val top: Float,
    val size: Float,
) {
    companion object {
        fun fromViewport(
            viewport: Float,
            image: Size,
            zoom: Float,
            offset: Offset,
        ): AvatarCrop {
            val shortSide = image.minDimension
            val scale = viewport / shortSide * zoom
            val side = shortSide / zoom
            val centerX = image.width / 2 - offset.x / scale
            val centerY = image.height / 2 - offset.y / scale

            return AvatarCrop(
                left = (centerX - side / 2) / image.width,
                top = (centerY - side / 2) / image.height,
                size = 1F / zoom,
            )
        }

        fun clampOffset(
            viewport: Float,
            image: Size,
            zoom: Float,
            offset: Offset,
        ): Offset {
            val scale = viewport / image.minDimension * zoom
            val maxX = max(0F, (image.width * scale - viewport) / 2)
            val maxY = max(0F, (image.height * scale - viewport) / 2)

            return Offset(
                x = offset.x.coerceIn(-maxX, maxX),
                y = offset.y.coerceIn(-maxY, maxY),
            )
        }
    }
}
