package tv.trakt.trakt.common.helpers.extensions

import androidx.compose.ui.graphics.Color
import kotlin.math.pow

/**
 * WCAG recommends a contrast ratio of at least 3:1 for UI components and graphical objects, and 4.5:1 for normal text.
 * https://www.w3.org/TR/WCAG21/#contrast-minimum
 */
fun getContrastRatio(
    c1: Color,
    c2: Color,
): Float {
    fun Float.linearize() =
        if (this <= 0.04045f) {
            this / 12.92f
        } else {
            ((this + 0.055f) / 1.055f).toDouble().pow(2.4).toFloat()
        }

    fun Color.luminance() = 0.2126f * red.linearize() + 0.7152f * green.linearize() + 0.0722f * blue.linearize()

    val l1 = c1.luminance()
    val l2 = c2.luminance()
    return (maxOf(l1, l2) + 0.05f) / (minOf(l1, l2) + 0.05f)
}
