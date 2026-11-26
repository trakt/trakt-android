package tv.trakt.trakt.core.summary.livingposter.ui.painters

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import tv.trakt.trakt.common.model.livingposter.LivingPosterEffect
import tv.trakt.trakt.core.summary.livingposter.model.EffectPlane
import kotlin.math.roundToInt
import kotlin.math.sin

private class SnowDepth(
    val count: Int,
    val size: ClosedFloatingPointRange<Float>,
    val duration: ClosedFloatingPointRange<Float>,
    val opacity: Float,
)

private val farDepth = SnowDepth(count = 70, size = 0.8F..1.6F, duration = 12F..18F, opacity = 0.6F)
private val midDepth = SnowDepth(count = 36, size = 2F..3.5F, duration = 8F..12F, opacity = 0.85F)
private val nearDepth = SnowDepth(count = 9, size = 7F..13F, duration = 5F..8F, opacity = 0.45F)

private class Flake(
    val x: Float,
    val phase: Float,
    val size: Float,
    val duration: Float,
    val sway: Float,
    val swaySpeed: Float,
    val opacity: Float,
)

private class PlacedFlake(
    val progress: Float,
    val center: Offset,
    val radius: Float,
)

/** Three depth layers of flakes; the near, out-of-focus ones fall in front of the figures. */
internal class SnowPainter(
    effect: LivingPosterEffect,
    seed: Int,
) : EffectPainter {
    private val far: List<Flake>
    private val mid: List<Flake>
    private val near: List<Flake>

    init {
        val random = SeededRandom(seed)
        val scale = intensityScale(effect.intensity)
        far = createFlakes(farDepth, random, scale)
        mid = createFlakes(midDepth, random, scale)
        near = createFlakes(nearDepth, random, scale)
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
        listOf(far, mid).forEachIndexed { depth, flakes ->
            flakes.forEach { flake ->
                val placed = placeFlake(scene, flake, depth)
                drawCircle(
                    color = Color.White,
                    radius = placed.radius / 2F,
                    center = placed.center,
                    alpha = flake.opacity,
                )
            }
        }
    }

    private fun DrawScope.paintFront(scene: EffectScene) {
        val sprite = radialSprite(Color.White, SpriteProfile.GlowSoft)

        near.forEach { flake ->
            val placed = placeFlake(scene, flake, depth = 2)
            drawSprite(
                sprite = sprite,
                center = placed.center,
                width = placed.radius * 2F,
                alpha = flake.opacity * interpolate(placed.progress, 0F to 0F, 0.15F to 1F, 0.85F to 1F, 1F to 0F),
            )
        }
    }

    private fun DrawScope.placeFlake(
        scene: EffectScene,
        flake: Flake,
        depth: Int,
    ): PlacedFlake {
        val seconds = scene.frame.seconds
        val progress = (seconds / flake.duration + flake.phase) % 1F
        val sway = sin(seconds * flake.swaySpeed + flake.phase * 9F) * flake.sway

        return PlacedFlake(
            progress = progress,
            center = Offset(
                x = (flake.x + sway) / 100F * size.width + scene.frame.tiltX * (depth + 1) * 3F * scene.unit,
                y = (progress * 1.15F - 0.08F) * size.height,
            ),
            radius = flake.size * scene.unit,
        )
    }
}

private fun createFlakes(
    depth: SnowDepth,
    random: SeededRandom,
    scale: Float,
): List<Flake> {
    fun span(range: ClosedFloatingPointRange<Float>): Float {
        return range.start + random.next() * (range.endInclusive - range.start)
    }

    return List((depth.count * scale).roundToInt()) {
        Flake(
            x = random.next() * 100F,
            phase = random.next(),
            size = span(depth.size),
            duration = span(depth.duration),
            sway = 2F + random.next() * 5F,
            swaySpeed = 0.5F + random.next(),
            opacity = depth.opacity,
        )
    }
}
