package tv.trakt.trakt.core.main.ui.seasonal.christmas

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import tv.trakt.trakt.common.ui.theme.colors.Shade10
import tv.trakt.trakt.core.main.ui.seasonal.SplashParticlePainter
import tv.trakt.trakt.core.main.ui.seasonal.SplashParticles
import kotlin.math.PI
import kotlin.math.cos
import kotlin.random.Random

private class Snowflake(
    var x: Float,
    var y: Float,
    val radius: Float,
    val speed: Float,
    var phase: Float,
    val phaseStep: Float,
    val sway: Float,
    val alpha: Float,
)

private object SnowPainter : SplashParticlePainter<Snowflake> {
    override val areaPerParticle = 6_000F
    override val maxCount = 200

    override fun DrawScope.create(initial: Boolean): Snowflake {
        val fast = Random.nextFloat() > 0.6F
        return Snowflake(
            x = Random.nextFloat() * size.width,
            y = if (initial) Random.nextFloat() * size.height else -10 * density,
            radius = (if (fast) 1F else Random.nextFloat() * 1.5F + 0.5F) * density,
            speed = (if (fast) Random.nextFloat() + 1.5F else Random.nextFloat() + 0.5F) * density,
            phase = Random.nextFloat() * 2 * PI.toFloat(),
            phaseStep = Random.nextFloat() / 30F,
            sway = (if (fast) 2F else 1.2F) * density,
            alpha = Random.nextFloat() * 0.5F + 0.3F,
        )
    }

    override fun DrawScope.advance(
        particle: Snowflake,
        step: Float,
    ): Boolean {
        val margin = 20 * density
        with(particle) {
            y += speed * step
            phase += phaseStep * step
            x += cos(phase) * sway * step

            if (x > size.width + margin) x = -margin / 2
            if (x < -margin) x = size.width + margin / 2

            return y <= size.height
        }
    }

    override fun DrawScope.draw(particle: Snowflake) {
        drawCircle(
            color = Shade10,
            radius = particle.radius,
            center = Offset(particle.x, particle.y),
            alpha = particle.alpha,
        )
    }
}

@Composable
internal fun ChristmasSnow(modifier: Modifier = Modifier) {
    SplashParticles(painter = SnowPainter, modifier = modifier)
}
