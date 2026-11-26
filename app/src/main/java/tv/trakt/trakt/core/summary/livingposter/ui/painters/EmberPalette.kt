package tv.trakt.trakt.core.summary.livingposter.ui.painters

import androidx.compose.ui.graphics.Color
import tv.trakt.trakt.core.summary.livingposter.ui.parseHexColor
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToInt

private const val PALETTE_BINS = 32
private val defaultEmberColor = Color(255, 140, 60)

/** Maps ember heat (0 cold..1 hot) to a colour: black-body tones for warm bases, a tint ramp otherwise. */
internal class EmberPalette(
    hex: String?,
) {
    val colors: List<Color>

    init {
        val base = hex?.let(::parseHexColor)?.copy(alpha = 1F) ?: defaultEmberColor
        val isWarmBase = base.isWarm()
        colors = List(PALETTE_BINS + 1) { index -> binColor(base, isWarmBase, heat = index.toFloat() / PALETTE_BINS) }
    }

    /** Index into [colors] for [heat]. */
    fun bin(heat: Float): Int = (heat.coerceIn(0F, 1F) * PALETTE_BINS).roundToInt()
}

private fun binColor(
    base: Color,
    isWarmBase: Boolean,
    heat: Float,
): Color {
    if (isWarmBase) {
        return kelvinColor(850F + 3150F * heat.pow(1.25F)).scaled(0.3F + 0.7F * heat.pow(0.7F))
    }

    return if (heat < 0.62F) {
        mixRgb(base.scaled(0.28F), base, heat / 0.62F)
    } else {
        mixRgb(base, Color.White, (heat - 0.62F) / 0.38F * 0.85F)
    }
}

private fun Color.isWarm(): Boolean {
    val max = maxOf(red, green, blue)
    val spread = max - minOf(red, green, blue)
    if (max == 0F || spread == 0F) return false

    val sector = when (max) {
        red -> ((green - blue) / spread) % 6F
        green -> (blue - red) / spread + 2F
        else -> (red - green) / spread + 4F
    }
    val hue = (sector * 60F + 360F) % 360F

    return spread / max > 0.25F && (hue < 60F || hue > 330F)
}

private fun kelvinColor(kelvin: Float): Color {
    val t = kelvin / 100F
    val red = if (t <= 66F) 255F else 329.698727446F * (t - 60F).pow(-0.1332047592F)
    val green = if (t <= 66F) {
        99.4708025861F * ln(t) - 161.1195681661F
    } else {
        288.1221695283F * (t - 60F).pow(-0.0755148492F)
    }
    val blue = when {
        t >= 66F -> 255F
        t <= 19F -> 0F
        else -> 138.5177312231F * ln(t - 10F) - 305.0447927307F
    }

    return Color(
        red = red.coerceIn(0F, 255F) / 255F,
        green = green.coerceIn(0F, 255F) / 255F,
        blue = blue.coerceIn(0F, 255F) / 255F,
    )
}

private fun Color.scaled(factor: Float): Color {
    return Color(
        red = (red * factor).coerceIn(0F, 1F),
        green = (green * factor).coerceIn(0F, 1F),
        blue = (blue * factor).coerceIn(0F, 1F),
    )
}

private fun mixRgb(
    from: Color,
    to: Color,
    amount: Float,
): Color {
    return Color(
        red = from.red + (to.red - from.red) * amount,
        green = from.green + (to.green - from.green) * amount,
        blue = from.blue + (to.blue - from.blue) * amount,
    )
}
