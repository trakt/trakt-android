package tv.trakt.trakt.core.main.ui.seasonal

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.DrawScope

private const val FRAME_NANOS = 16_666_667F
private const val MAX_STEP = 4F

/**
 * Particle behaviour for [SplashParticles]. Speeds are per 60 fps frame; [step] receives the
 * number of such frames elapsed since the previous draw.
 */
internal interface SplashParticlePainter<T> {
    val areaPerParticle: Float
    val maxCount: Int

    fun DrawScope.create(initial: Boolean): T

    /** Returns false when the particle is finished and must be replaced. */
    fun DrawScope.advance(
        particle: T,
        step: Float,
    ): Boolean

    fun DrawScope.draw(particle: T)
}

// Mutable on purpose: particles advance every frame inside the draw phase.
private class ParticleField<T> {
    val particles = mutableListOf<T>()
    var lastFrameNanos = 0L
}

@Composable
internal fun <T> SplashParticles(
    painter: SplashParticlePainter<T>,
    modifier: Modifier = Modifier,
) {
    val field = remember(painter) { ParticleField<T>() }
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
            else -> ((now - field.lastFrameNanos) / FRAME_NANOS).coerceAtMost(MAX_STEP)
        }
        field.lastFrameNanos = now

        with(painter) {
            if (field.particles.isEmpty()) {
                val count = (size.width / density * size.height / density / areaPerParticle)
                    .toInt()
                    .coerceIn(1, maxCount)
                repeat(count) { field.particles += create(initial = true) }
            }

            field.particles.forEachIndexed { index, particle ->
                val alive = advance(particle = particle, step = step)
                val current = if (alive) particle else create(initial = false)
                field.particles[index] = current
                draw(current)
            }
        }
    }
}
