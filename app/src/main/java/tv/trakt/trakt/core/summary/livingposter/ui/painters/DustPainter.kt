package tv.trakt.trakt.core.summary.livingposter.ui.painters

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import tv.trakt.trakt.common.model.livingposter.LivingPosterEffect
import tv.trakt.trakt.core.summary.livingposter.model.EffectPlane
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

private val dustColor = Color(0xFFF6EAD6)
private const val FAR_COUNT = 60
private const val NEAR_COUNT = 7
private const val NEAR_ALPHA = 0.18F

private class Speck(
    val x: Float,
    val y: Float,
    val phase: Float,
    val size: Float,
)

/** Fine specks drifting behind the figures and a few large, blurred motes in front. */
internal class DustPainter(
    effect: LivingPosterEffect,
    seed: Int,
) : EffectPainter {
    private val far: List<Speck>
    private val near: List<Speck>
    private val farSprite by lazy { createRadialSprite(dustColor, SpriteProfile.Glow) }
    private val nearSprite by lazy { createRadialSprite(dustColor, SpriteProfile.GlowSoft) }

    init {
        val random = SeededRandom(seed)
        val scale = intensityScale(effect.intensity)
        far = createSpecks(random, count = (FAR_COUNT * scale).roundToInt(), sizeFrom = 0.5F, sizeSpan = 1.2F)
        near = createSpecks(random, count = (NEAR_COUNT * scale).roundToInt(), sizeFrom = 6F, sizeSpan = 9F)
    }

    override fun prepare() {
        farSprite.prepareToDraw()
        nearSprite.prepareToDraw()
    }

    override fun DrawScope.paint(
        plane: EffectPlane,
        scene: EffectScene,
    ) {
        when (plane) {
            EffectPlane.Back -> paintBack(scene)
            EffectPlane.Front -> paintFront(scene)
            EffectPlane.Backdrop, EffectPlane.Rim -> Unit
        }
    }

    private fun DrawScope.paintBack(scene: EffectScene) {
        val seconds = scene.frame.seconds

        far.forEach { speck ->
            drawSprite(
                sprite = farSprite,
                center = placeSpeck(speck, seconds, reach = 0.02F, speed = 0.25F),
                width = speck.size * scene.unit * 4F,
                alpha = 0.4F + 0.4F * sin(seconds * 1.5F + speck.phase),
            )
        }
    }

    private fun DrawScope.paintFront(scene: EffectScene) {
        near.forEach { speck ->
            drawSprite(
                sprite = nearSprite,
                center = placeSpeck(speck, scene.frame.seconds, reach = 0.05F, speed = 0.12F),
                width = speck.size * scene.unit * 2F,
                alpha = NEAR_ALPHA,
            )
        }
    }

    private fun DrawScope.placeSpeck(
        speck: Speck,
        seconds: Float,
        reach: Float,
        speed: Float,
    ): Offset {
        val across = (speck.x + sin(seconds * speed + speck.phase) * reach + seconds * 0.004F) % 1F
        val down = speck.y + cos(seconds * speed * 0.8F + speck.phase * 1.3F) * reach

        return Offset(across * size.width, down * size.height)
    }
}

private fun createSpecks(
    random: SeededRandom,
    count: Int,
    sizeFrom: Float,
    sizeSpan: Float,
): List<Speck> {
    return List(count) {
        Speck(
            x = random.next(),
            y = random.next(),
            phase = random.next() * 2F * PI.toFloat(),
            size = sizeFrom + random.next() * sizeSpan,
        )
    }
}
