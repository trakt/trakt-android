package tv.trakt.trakt.core.summary.livingposter.ui

import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Color
import android.graphics.Paint
import androidx.core.graphics.applyCanvas
import androidx.core.graphics.createBitmap
import coil3.size.Size
import coil3.transform.Transformation
import kotlin.math.max

private const val BLUR_RATIO = 0.027F

/** Turns a cutout layer into its soft black silhouette, used as the layer's cast shadow. */
internal class LivingPosterShadowTransformation : Transformation() {
    override val cacheKey = "${this::class.qualifiedName}"

    override suspend fun transform(
        input: Bitmap,
        size: Size,
    ): Bitmap {
        val blurPaint = Paint().apply {
            maskFilter = BlurMaskFilter(max(1F, input.width * BLUR_RATIO), BlurMaskFilter.Blur.NORMAL)
        }
        val offset = IntArray(2)
        val silhouette = input.extractAlpha(blurPaint, offset)
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.BLACK }

        return createBitmap(input.width, input.height).applyCanvas {
            drawBitmap(silhouette, offset[0].toFloat(), offset[1].toFloat(), fillPaint)
            silhouette.recycle()
        }
    }
}
