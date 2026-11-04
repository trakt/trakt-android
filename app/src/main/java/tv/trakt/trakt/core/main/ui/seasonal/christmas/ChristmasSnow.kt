package tv.trakt.trakt.core.main.ui.seasonal.christmas

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import tv.trakt.trakt.common.ui.theme.colors.Shade10
import kotlin.math.PI
import kotlin.math.cos
import kotlin.random.Random

private const val FRAME_NANOS = 16_666_667F
private const val AREA_PER_FLAKE = 6_000F
private const val MAX_FLAKES = 200

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

// Mutable on purpose: particles advance every frame inside the draw phase.
private class SnowField {
    val flakes = mutableListOf<Snowflake>()
    var lastFrameNanos = 0L
}

@Composable
internal fun ChristmasSnow(modifier: Modifier = Modifier) {
    val field = remember { SnowField() }
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

        if (field.flakes.isEmpty()) {
            val count = (size.width / density * size.height / density / AREA_PER_FLAKE)
                .toInt()
                .coerceIn(1, MAX_FLAKES)
            repeat(count) { field.flakes += createSnowflake(initial = true) }
        }

        field.flakes.forEachIndexed { index, flake ->
            val alive = flake.advance(step = step, scope = this)
            val current = if (alive) flake else createSnowflake(initial = false)
            field.flakes[index] = current
            drawCircle(
                color = Shade10,
                radius = current.radius,
                center = Offset(current.x, current.y),
                alpha = current.alpha,
            )
        }
    }
}

private fun DrawScope.createSnowflake(initial: Boolean): Snowflake {
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

private fun Snowflake.advance(
    step: Float,
    scope: DrawScope,
): Boolean {
    val width = scope.size.width
    val margin = 20 * scope.density

    y += speed * step
    phase += phaseStep * step
    x += cos(phase) * sway * step

    if (x > width + margin) x = -margin / 2
    if (x < -margin) x = width + margin / 2

    return y <= scope.size.height
}
