package tv.trakt.trakt.core.summary.livingposter.ui.painters

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.floor

private const val COLUMNS = 30
private const val ROWS = 45

private fun toCell(
    value: Float,
    cells: Int,
): Int = floor(value * cells).toInt().coerceIn(0, cells - 1)

/** Reads a coarse luminance grid of [bitmap], so stars can be placed on dark sky. */
internal fun sampleLuminance(bitmap: Bitmap): SkyLuminance? {
    val readable = if (bitmap.config == Bitmap.Config.HARDWARE) {
        bitmap.copy(Bitmap.Config.ARGB_8888, false) ?: return null
    } else {
        bitmap
    }
    val pixels = IntArray(COLUMNS * ROWS)
    Bitmap
        .createScaledBitmap(readable, COLUMNS, ROWS, true)
        .getPixels(pixels, 0, COLUMNS, 0, 0, COLUMNS, ROWS)

    val grid = FloatArray(pixels.size) { index ->
        val pixel = pixels[index]
        val luminance = 0.2126F * Color.red(pixel) + 0.7152F * Color.green(pixel) + 0.0722F * Color.blue(pixel)

        luminance / 255F * (Color.alpha(pixel) / 255F)
    }

    return SkyLuminance { x, y -> grid[toCell(y, ROWS) * COLUMNS + toCell(x, COLUMNS)] }
}
