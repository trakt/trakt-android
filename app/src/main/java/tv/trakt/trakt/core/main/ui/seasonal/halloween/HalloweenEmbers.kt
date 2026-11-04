package tv.trakt.trakt.core.main.ui.seasonal.halloween

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import tv.trakt.trakt.common.ui.theme.colors.Orange200
import tv.trakt.trakt.common.ui.theme.colors.Orange300
import tv.trakt.trakt.common.ui.theme.colors.Orange400
import tv.trakt.trakt.core.main.ui.seasonal.SplashParticlePainter
import tv.trakt.trakt.core.main.ui.seasonal.SplashParticles
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

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

private object EmberPainter : SplashParticlePainter<Ember> {
    override val areaPerParticle = 24_000F
    override val maxCount = 20

    override fun DrawScope.create(initial: Boolean): Ember {
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

    override fun DrawScope.advance(
        particle: Ember,
        step: Float,
    ): Boolean {
        with(particle) {
            y -= rise * step
            phase += flicker * step
            x += sin(phase) * drift * 0.3F * step
            return y >= -10 * density
        }
    }

    override fun DrawScope.draw(particle: Ember) {
        val glow = (sin(particle.phase * 3) + 1) / 2
        HaloLayers.forEach { layer ->
            drawCircle(
                color = particle.color,
                radius = particle.radius * layer.scale,
                center = Offset(particle.x, particle.y),
                alpha = (layer.baseAlpha + glow * layer.glowAlpha).coerceIn(0F, 1F),
            )
        }
    }
}

@Composable
internal fun HalloweenEmbers(modifier: Modifier = Modifier) {
    SplashParticles(painter = EmberPainter, modifier = modifier)
}
