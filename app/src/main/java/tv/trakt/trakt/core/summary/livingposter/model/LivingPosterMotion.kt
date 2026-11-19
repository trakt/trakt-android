package tv.trakt.trakt.core.summary.livingposter.model

import kotlin.math.ceil
import kotlin.math.exp
import kotlin.math.max

private const val MAX_SUBSTEP_SECONDS = 1F / 120F
private const val MAX_TILT_RADIANS = 0.2F
private const val RECENTER_SECONDS = 1.5F

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

/** Rotation rates in rad/s around the screen axes: [x] tips the top edge, [y] turns the side edges. */
internal data class RotationRate(
    val x: Float,
    val y: Float,
    val timestampNanos: Long,
)

/** Tilt per axis in -1..1. */
internal data class Tilt(
    val x: Float = 0F,
    val y: Float = 0F,
) {
    /** Integrates rotation and eases back to center, so only movement tilts, not the holding angle. */
    fun integrate(
        rate: RotationRate,
        seconds: Float,
    ): Tilt {
        val decay = exp(-seconds / RECENTER_SECONDS)

        return Tilt(
            x = (x * decay + rate.y * seconds / MAX_TILT_RADIANS).coerceIn(-1F, 1F),
            y = (y * decay + rate.x * seconds / MAX_TILT_RADIANS).coerceIn(-1F, 1F),
        )
    }
}
