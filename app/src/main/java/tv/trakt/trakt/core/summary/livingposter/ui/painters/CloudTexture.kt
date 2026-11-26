package tv.trakt.trakt.core.summary.livingposter.ui.painters

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.roundToInt

private const val CLOUD_SIZE = 128
private const val CLOUD_OCTAVES = 5

private class Octave(
    val period: Int,
    val grid: FloatArray,
)

private fun smooth(value: Float): Float = value * value * (3F - 2F * value)

/** A tileable cloud texture: [color] with alpha ramping in where the noise runs from [from] to [to]. */
internal fun cloudTexture(
    seed: Int,
    color: Color,
    from: Float,
    to: Float,
): ImageBitmap {
    val random = SeededRandom(seed)
    val octaves = List(CLOUD_OCTAVES) { octave ->
        val period = 4 shl octave
        Octave(period = period, grid = FloatArray(period * period) { random.next() })
    }
    val rgb = color.copy(alpha = 1F).toArgb() and 0x00FFFFFF
    val pixels = IntArray(CLOUD_SIZE * CLOUD_SIZE) { index ->
        val value = sampleOctaves(octaves, column = index % CLOUD_SIZE, row = index / CLOUD_SIZE)
        val ramp = ((value - from) / (to - from)).coerceIn(0F, 1F)
        val alpha = (255F * smooth(ramp)).roundToInt()

        (alpha shl 24) or rgb
    }

    return Bitmap
        .createBitmap(pixels, CLOUD_SIZE, CLOUD_SIZE, Bitmap.Config.ARGB_8888)
        .asImageBitmap()
}

private fun sampleOctaves(
    octaves: List<Octave>,
    column: Int,
    row: Int,
): Float {
    val (sum, total) = octaves.foldIndexed(0F to 0F) { index, (sum, total), octave ->
        val amplitude = 0.5F.pow(index + 1)
        val period = octave.period
        val fx = column.toFloat() / CLOUD_SIZE * period
        val fy = row.toFloat() / CLOUD_SIZE * period
        val x0 = floor(fx).toInt()
        val y0 = floor(fy).toInt()
        val ux = smooth(fx - x0)
        val uy = smooth(fy - y0)

        fun at(
            x: Int,
            y: Int,
        ): Float = octave.grid[(y % period) * period + (x % period)]

        val top = at(x0, y0) + (at(x0 + 1, y0) - at(x0, y0)) * ux
        val bottom = at(x0, y0 + 1) + (at(x0 + 1, y0 + 1) - at(x0, y0 + 1)) * ux

        (sum + amplitude * (top + (bottom - top) * uy)) to (total + amplitude)
    }

    return sum / total
}
