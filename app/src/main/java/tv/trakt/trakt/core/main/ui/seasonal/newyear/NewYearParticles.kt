package tv.trakt.trakt.core.main.ui.seasonal.newyear

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotateRad
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import tv.trakt.trakt.common.ui.theme.colors.Amber200
import tv.trakt.trakt.common.ui.theme.colors.Amber300
import tv.trakt.trakt.common.ui.theme.colors.Amber400
import tv.trakt.trakt.common.ui.theme.colors.Shade10
import tv.trakt.trakt.core.main.ui.seasonal.Gold200
import tv.trakt.trakt.core.main.ui.seasonal.SplashParticlePainter
import tv.trakt.trakt.core.main.ui.seasonal.SplashParticles
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

private const val SPARK_COUNT = 36
private const val BURST_LIFE = 90F
private const val GRAVITY = 0.035F
private const val DRAG = 0.975F

private val ConfettiColors = listOf(Amber200, Amber300, Amber400, Shade10)
private val FireworkColors = listOf(Amber200, Amber300, Gold200, Shade10)

private class Confetti(
    var x: Float,
    var y: Float,
    val width: Float,
    val height: Float,
    val fall: Float,
    var angle: Float,
    val spin: Float,
    var flip: Float,
    val flipSpeed: Float,
    val sway: Float,
    val color: Color,
)

private class Spark(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
)

private class Firework(
    val x: Float,
    var y: Float,
    val targetY: Float,
    var speed: Float,
    var delay: Float,
    var sparks: List<Spark>,
    var life: Float,
    val color: Color,
)

private object ConfettiPainter : SplashParticlePainter<Confetti> {
    override val areaPerParticle = 16_000F
    override val maxCount = 72

    override fun DrawScope.create(initial: Boolean): Confetti {
        return Confetti(
            x = Random.nextFloat() * size.width,
            y = if (initial) Random.nextFloat() * size.height else -10 * density,
            width = (Random.nextFloat() * 3 + 3) * density,
            height = (Random.nextFloat() * 2 + 1.5F) * density,
            fall = (Random.nextFloat() * 0.5F + 0.4F) * density,
            angle = Random.nextFloat() * 2 * PI.toFloat(),
            spin = (Random.nextFloat() - 0.5F) * 0.04F,
            flip = Random.nextFloat() * 2 * PI.toFloat(),
            flipSpeed = Random.nextFloat() * 0.06F + 0.02F,
            sway = (Random.nextFloat() * 0.6F + 0.2F) * density,
            color = ConfettiColors.random(),
        )
    }

    override fun DrawScope.advance(
        particle: Confetti,
        step: Float,
    ): Boolean {
        with(particle) {
            y += fall * step
            angle += spin * step
            flip += flipSpeed * step
            x += sin(flip * 0.5F) * sway * step
            return y <= size.height + 10 * density
        }
    }

    override fun DrawScope.draw(particle: Confetti) {
        translate(left = particle.x, top = particle.y) {
            rotateRad(radians = particle.angle, pivot = Offset.Zero) {
                scale(scaleX = cos(particle.flip), scaleY = 1F, pivot = Offset.Zero) {
                    drawRect(
                        color = particle.color,
                        topLeft = Offset(-particle.width / 2, -particle.height / 2),
                        size = Size(particle.width, particle.height),
                        alpha = 0.75F,
                    )
                }
            }
        }
    }
}

private object FireworkPainter : SplashParticlePainter<Firework> {
    override val areaPerParticle = 160_000F
    override val maxCount = 6

    override fun DrawScope.create(initial: Boolean): Firework {
        return Firework(
            x = size.width * (Random.nextFloat() * 0.8F + 0.1F),
            y = size.height,
            targetY = size.height * (Random.nextFloat() * 0.35F + 0.1F),
            speed = (Random.nextFloat() * 2 + 6) * density,
            delay = Random.nextFloat() * if (initial) 160 else 90,
            sparks = emptyList(),
            life = BURST_LIFE,
            color = FireworkColors.random(),
        )
    }

    override fun DrawScope.advance(
        particle: Firework,
        step: Float,
    ): Boolean {
        with(particle) {
            if (delay > 0) {
                delay -= step
                return true
            }

            if (sparks.isEmpty() && y > targetY) {
                y -= speed * step
                speed = (speed * 0.985F.pow(step)).coerceAtLeast(2.4F * density)
                if (y <= targetY) {
                    sparks = burst(x = x, y = y, density = density)
                }
                return true
            }

            val drag = DRAG.pow(step)
            sparks.forEach { spark ->
                spark.vx *= drag
                spark.vy = spark.vy * drag + GRAVITY * density * step
                spark.x += spark.vx * step
                spark.y += spark.vy * step
            }
            life -= step
            return life > 0
        }
    }

    override fun DrawScope.draw(particle: Firework) {
        if (particle.delay > 0) return

        if (particle.sparks.isEmpty()) {
            drawLine(
                color = particle.color,
                start = Offset(particle.x, particle.y),
                end = Offset(particle.x, particle.y + particle.speed * 4),
                strokeWidth = 2 * density,
                cap = StrokeCap.Round,
                alpha = 0.9F,
                blendMode = BlendMode.Plus,
            )
            return
        }

        val fade = (particle.life / BURST_LIFE).coerceIn(0F, 1F)
        particle.sparks.forEach { spark ->
            drawLine(
                color = particle.color,
                start = Offset(spark.x, spark.y),
                end = Offset(spark.x - spark.vx * 3, spark.y - spark.vy * 3),
                strokeWidth = 1.6F * density,
                cap = StrokeCap.Round,
                alpha = fade,
                blendMode = BlendMode.Plus,
            )
        }

        if (fade > 0.85F) {
            drawCircle(
                color = particle.color,
                radius = 18 * density,
                center = Offset(particle.x, particle.y),
                alpha = (fade - 0.85F) / 0.15F,
                blendMode = BlendMode.Plus,
            )
        }
    }
}

private fun burst(
    x: Float,
    y: Float,
    density: Float,
): List<Spark> {
    return List(SPARK_COUNT) { index ->
        val angle = index.toFloat() / SPARK_COUNT * 2 * PI.toFloat()
        val power = (Random.nextFloat() * 1.2F + 2) * density
        Spark(
            x = x,
            y = y,
            vx = cos(angle) * power,
            vy = sin(angle) * power,
        )
    }
}

@Composable
internal fun NewYearParticles(modifier: Modifier = Modifier) {
    SplashParticles(painter = FireworkPainter, modifier = modifier)
    SplashParticles(painter = ConfettiPainter, modifier = modifier)
}
