package tv.trakt.trakt.core.summary.livingposter.ui.painters

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import tv.trakt.trakt.common.model.livingposter.LivingPosterEffect
import tv.trakt.trakt.core.summary.livingposter.model.EffectPlane
import kotlin.math.floor
import kotlin.math.roundToInt

private const val FIREFLY_COUNT = 16
private const val SEED_RANGE = 1_000_000
private val haloColor = Color(0xFFD8FF6A)
private val coreColor = Color(0xFFFBFFD0)

private val blinkFade = Keyframes(0F to 0F, 0.1F to 1F, 0.35F to 0.9F, 0.55F to 0F, 1F to 0F)

private class Firefly(
    val pathX: Noise,
    val pathY: Noise,
    val phase: Float,
    val blink: Float,
    val size: Float,
)

/** Fireflies wander on smooth noise paths and blink on their own rhythm. */
internal class FirefliesPainter(
    effect: LivingPosterEffect,
    seed: Int,
) : EffectPainter {
    private val flies: List<Firefly>
    private val halo by lazy { createRadialSprite(haloColor, SpriteProfile.GlowSoft) }
    private val core by lazy { createRadialSprite(coreColor, SpriteProfile.Glow) }

    init {
        val random = SeededRandom(seed)
        flies = List((FIREFLY_COUNT * intensityScale(effect.intensity)).roundToInt()) {
            Firefly(
                pathX = Noise(floor(random.next() * SEED_RANGE).toInt()),
                pathY = Noise(floor(random.next() * SEED_RANGE).toInt()),
                phase = random.next() * 10F,
                blink = 3F + random.next() * 3F,
                size = 2F + random.next() * 2F,
            )
        }
    }

    override fun prepare() {
        halo.prepareToDraw()
        core.prepareToDraw()
    }

    override fun DrawScope.paint(
        plane: EffectPlane,
        scene: EffectScene,
    ) {
        when (plane) {
            EffectPlane.Back -> paintBack(scene)
            EffectPlane.Backdrop, EffectPlane.Rim, EffectPlane.Front -> Unit
        }
    }

    private fun DrawScope.paintBack(scene: EffectScene) {
        val seconds = scene.frame.seconds

        flies.forEach { fly ->
            val progress = ((seconds + fly.phase) % fly.blink) / fly.blink
            val opacity = blinkFade.at(progress)
            if (opacity < 0.02F) return@forEach

            val center = Offset(
                x = fly.pathX.at(seconds * 0.12F + fly.phase) * size.width * 1.2F - size.width * 0.1F,
                y = fly.pathY.at(seconds * 0.1F + fly.phase * 2F) * size.height * 1.1F - size.height * 0.05F,
            )
            val radius = fly.size * scene.unit

            drawSprite(halo, center, width = radius * 12F, alpha = opacity * 0.6F)
            drawSprite(core, center, width = radius * 3F, alpha = opacity)
        }
    }
}
