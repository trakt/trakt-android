package tv.trakt.trakt.core.summary.livingposter.model

import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.sin

private const val MAX_SUBSTEP_SECONDS = 1F / 120F

internal data class Spring(
    val value: Float = 0F,
    val velocity: Float = 0F,
) {
    fun step(
        target: Float,
        seconds: Float,
        stiffness: Float,
        damping: Float,
    ): Spring {
        val steps = max(1, ceil(seconds / MAX_SUBSTEP_SECONDS).toInt())
        val delta = seconds / steps

        return (0 until steps).fold(this) { spring, _ ->
            val acceleration = stiffness * (target - spring.value) - damping * spring.velocity
            val nextVelocity = spring.velocity + acceleration * delta

            Spring(
                value = spring.value + nextVelocity * delta,
                velocity = nextVelocity,
            )
        }
    }
}

internal data class Tilt(
    val x: Float,
    val y: Float,
) {
    companion object {
        /** Slow drift used instead of a pointer on touch screens. */
        fun idle(
            milliseconds: Long,
            phase: Float,
        ): Tilt {
            return Tilt(
                x = sin(milliseconds / 2300F + phase) * 0.45F,
                y = sin(milliseconds / 3100F + phase * 1.7F) * 0.3F,
            )
        }
    }
}
