package tv.trakt.trakt.core.main.ui.seasonal.halloween

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import tv.trakt.trakt.common.ui.theme.colors.Orange200
import tv.trakt.trakt.common.ui.theme.colors.Orange300
import tv.trakt.trakt.common.ui.theme.colors.Orange400
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

private const val FRAME_NANOS = 16_666_667F
private const val AREA_PER_EMBER = 24_000F
private const val MAX_EMBERS = 20

private val EmberColors = listOf(Orange200, Orange300, Orange400)

private data class HaloLayer(
    val scale: Float,
    val baseAlpha: Float,
    val glowAlpha: Float,
)

private val HaloLayers = listOf(
    HaloLayer(scale = 5F, baseAlpha = 0.04F, glowAlpha = 0.04F),
    HaloLayer(scale = 2.5F, baseAlpha = 0.08F, glowAlpha = 0.1F),
    HaloLayer(scale = 1F, baseAlpha = 0.5F, glowAlpha = 0.5F),
)

private class Ember(
    var x: Float,
    var y: Float,
    val radius: Float,
    val rise: Float,
    val drift: Float,
    var phase: Float,
    val flicker: Float,
    val color: Color,
)

// Mutable on purpose: particles advance every frame inside the draw phase.
private class EmberField {
    val embers = mutableListOf<Ember>()
    var lastFrameNanos = 0L
}

@Composable
internal fun HalloweenEmbers(modifier: Modifier = Modifier) {
    val field = remember { EmberField() }
    val frameNanos = remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos { frameNanos.longValue = it }
        }
    }

    Canvas(modifier = modifier) {
        val now = frameNanos.longValue
        val step = when (field.lastFrameNanos) {
            0L -> 0F
            else -> ((now - field.lastFrameNanos) / FRAME_NANOS).coerceAtMost(4F)
        }
        field.lastFrameNanos = now

        if (field.embers.isEmpty()) {
            val count = (size.width / density * size.height / density / AREA_PER_EMBER)
                .toInt()
                .coerceIn(1, MAX_EMBERS)
            repeat(count) { field.embers += createEmber(initial = true) }
        }

        field.embers.forEachIndexed { index, ember ->
            val alive = ember.advance(step = step, minY = -10 * density)
            val current = if (alive) ember else createEmber(initial = false)
            field.embers[index] = current
            drawEmber(current)
        }
    }
}

private fun DrawScope.createEmber(initial: Boolean): Ember {
    return Ember(
        x = Random.nextFloat() * size.width,
        y = if (initial) Random.nextFloat() * size.height else size.height + 10 * density,
        radius = (Random.nextFloat() * 1.4F + 0.8F) * density,
        rise = (Random.nextFloat() * 0.35F + 0.15F) * density,
        drift = (Random.nextFloat() * 0.6F + 0.2F) * density,
        phase = Random.nextFloat() * 2 * PI.toFloat(),
        flicker = Random.nextFloat() * 0.04F + 0.01F,
        color = EmberColors.random(),
    )
}

private fun Ember.advance(
    step: Float,
    minY: Float,
): Boolean {
    y -= rise * step
    phase += flicker * step
    x += sin(phase) * drift * 0.3F * step
    return y >= minY
}

private fun DrawScope.drawEmber(ember: Ember) {
    val glow = (sin(ember.phase * 3) + 1) / 2
    HaloLayers.forEach { layer ->
        drawCircle(
            color = ember.color,
            radius = ember.radius * layer.scale,
            center = Offset(ember.x, ember.y),
            alpha = (layer.baseAlpha + glow * layer.glowAlpha).coerceIn(0F, 1F),
        )
    }
}
