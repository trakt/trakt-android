package tv.trakt.trakt.core.summary.livingposter.ui.painters

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.util.lerp
import tv.trakt.trakt.common.model.livingposter.LivingPosterEffect
import tv.trakt.trakt.core.summary.livingposter.model.EffectPlane
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin

private const val STAR_COUNT = 28
private const val CANDIDATES = 7
private const val SPIKY_COUNT = 3
private const val BRIGHT_SKY = 0.7F
private const val SAMPLE_WAIT_SECONDS = 1.5F
private const val SPIKE_SIZE = 128
private const val RADIANS_TO_DEGREES = 180F / PI.toFloat()

private val coolTint = Color(200, 215, 255)
private val warmTint = Color(255, 234, 206)

private data class Star(
    val x: Float,
    val y: Float,
    val magnitude: Float,
    val size: Float,
    val phase: Float,
    val scintillation: FractalNoise,
    val rate: Float,
    val turn: Float,
    val tint: Color,
    val isSpiky: Boolean = false,
)

private val spikeSprite: ImageBitmap by lazy {
    createSprite(SPIKE_SIZE, SPIKE_SIZE) {
        val half = SPIKE_SIZE / 2F
        val ray = radialPaint(
            radius = half,
            centerX = 0F,
            centerY = 0F,
            stops = listOf(
                0F to Color.White,
                0.25F to Color.White.copy(alpha = 0.35F),
                1F to Color.White.copy(alpha = 0F),
            ),
        )

        listOf(0F, 90F).forEach { turn ->
            save()
            translate(half, half)
            rotate(turn)
            scale(1F, 0.04F)
            drawRect(-half, -half, half, half, ray)
            restore()
        }
    }
}

/** Stars placed on the darkest sky of the background, twinkling, with a few brighter ones flaring. */
internal class TwinklePainter(
    private val effect: LivingPosterEffect,
    private val seed: Int,
) : EffectPainter {
    private val random = SeededRandom(seed)
    private var stars: List<Star>? = null

    override fun DrawScope.paint(
        plane: EffectPlane,
        scene: EffectScene,
    ) {
        when (plane) {
            EffectPlane.Back -> paintStars(scene)
            EffectPlane.Backdrop, EffectPlane.Rim, EffectPlane.Front -> Unit
        }
    }

    private fun resolveStars(scene: EffectScene): List<Star>? {
        stars?.let { return it }

        val luminance = scene.sources.luminance
        if (luminance == null && scene.frame.seconds < SAMPLE_WAIT_SECONDS) return null

        return placeStars(luminance).also { stars = it }
    }

    private fun DrawScope.paintStars(scene: EffectScene) {
        val placed = resolveStars(scene) ?: return
        val seconds = scene.frame.seconds
        val unit = scene.unit

        placed.forEach { star ->
            val flicker = 0.72F + 0.4F * star.scintillation.at(seconds * star.rate + star.phase)
            val brightness = (0.5F + 0.5F * star.magnitude) * flicker.coerceIn(0F, 1F)
            val center = Offset(star.x * size.width, star.y * size.height)

            drawSprite(
                sprite = radialSprite(star.tint, SpriteProfile.Soft),
                center = center,
                width = star.size * unit * 7F,
                alpha = brightness * 0.3F,
            )
            drawSprite(
                sprite = radialSprite(star.tint, SpriteProfile.Core),
                center = center,
                width = star.size * unit * 3.2F,
                alpha = brightness,
            )
            if (!star.isSpiky) return@forEach

            val pulse = max(0F, sin(seconds * 0.45F + star.phase)).pow(4)
            rotate(degrees = (star.turn + seconds * 0.01F) * RADIANS_TO_DEGREES, pivot = center) {
                drawSprite(
                    sprite = spikeSprite,
                    center = center,
                    width = (10F + star.size * 7F) * unit * (0.5F + 0.7F * pulse),
                    alpha = brightness * (0.3F + 0.5F * pulse),
                )
            }
        }
    }

    private fun placeStars(luminance: SkyLuminance?): List<Star> {
        val box = effect.box
        val count = (STAR_COUNT * intensityScale(effect.intensity)).roundToInt()

        return List(count) { index ->
            val candidates = List(if (luminance != null) CANDIDATES else 1) {
                val x = (box.x + random.next() * box.width) / 100F
                val y = (box.y + random.next() * box.height) / 100F
                Triple(x, y, luminance?.at(x, y) ?: 0F)
            }
            val (x, y, level) = candidates.minBy { (_, _, level) -> level }
            val magnitude = random.next().pow(3)
            val star = Star(
                x = x,
                y = y,
                magnitude = magnitude,
                size = 0.7F + 3.2F * magnitude,
                phase = random.next() * 2F * PI.toFloat(),
                scintillation = FractalNoise(seed + index * 17),
                rate = 2F + random.next() * 3F,
                turn = 0.3F + random.next() * 0.4F,
                tint = mix(Color.White, if (random.next() < 0.5F) coolTint else warmTint, 0.5F),
            )

            star.takeUnless { luminance != null && level > BRIGHT_SKY }
        }
            .filterNotNull()
            .sortedByDescending { it.magnitude }
            .mapIndexed { index, star -> star.copy(isSpiky = index < SPIKY_COUNT) }
    }
}

private fun mix(
    from: Color,
    to: Color,
    amount: Float,
): Color {
    return Color(
        red = lerp(from.red, to.red, amount),
        green = lerp(from.green, to.green, amount),
        blue = lerp(from.blue, to.blue, amount),
    )
}
