package tv.trakt.trakt.core.summary.livingposter.ui.painters

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import tv.trakt.trakt.common.model.livingposter.LivingPosterEffect
import tv.trakt.trakt.core.summary.livingposter.model.EffectPlane

private const val STREAK_WIDTH = 256
private const val STREAK_HEIGHT = 32
private const val RING_SIZE = 128

private val haloColor = Color(255, 176, 110)
private val glowColor = Color(255, 240, 215)
private val defaultAt = LivingPosterEffect.Point(50F, 30F)

private class FlareGhost(
    val reach: Float,
    val size: Float,
    val color: Color,
    val alpha: Float,
)

private val ghosts = listOf(
    FlareGhost(reach = 0.5F, size = 0.035F, color = Color(120, 170, 255), alpha = 0.1F),
    FlareGhost(reach = 0.85F, size = 0.06F, color = Color(255, 190, 120), alpha = 0.06F),
    FlareGhost(reach = 1.25F, size = 0.022F, color = Color(160, 255, 210), alpha = 0.14F),
    FlareGhost(reach = 1.55F, size = 0.1F, color = Color(170, 140, 255), alpha = 0.05F),
    FlareGhost(reach = 2.05F, size = 0.045F, color = Color(255, 150, 120), alpha = 0.08F),
)

private val streakSprite: ImageBitmap by lazy {
    createSprite(STREAK_WIDTH, STREAK_HEIGHT) {
        val half = STREAK_WIDTH / 2F
        translate(half, STREAK_HEIGHT / 2F)
        scale(1F, STREAK_HEIGHT.toFloat() / STREAK_WIDTH)
        drawRect(
            -half,
            -half,
            half,
            half,
            radialPaint(
                radius = half,
                centerX = 0F,
                centerY = 0F,
                stops = listOf(
                    0F to Color(240, 247, 255),
                    0.06F to Color(175, 208, 255).copy(alpha = 0.75F),
                    0.35F to Color(105, 150, 255).copy(alpha = 0.22F),
                    1F to Color(80, 120, 255).copy(alpha = 0F),
                ),
            ),
        )
    }
}

private val ringSprite: ImageBitmap by lazy {
    createSprite(RING_SIZE, RING_SIZE) {
        val half = RING_SIZE / 2F
        drawRect(
            0F,
            0F,
            RING_SIZE.toFloat(),
            RING_SIZE.toFloat(),
            radialPaint(
                radius = half,
                stops = listOf(
                    0F to Color.White.copy(alpha = 0F),
                    0.68F to Color(120, 160, 255).copy(alpha = 0.05F),
                    0.78F to Color(120, 170, 255).copy(alpha = 0.2F),
                    0.85F to Color(150, 255, 200).copy(alpha = 0.22F),
                    0.91F to Color(255, 210, 130).copy(alpha = 0.24F),
                    0.96F to Color(255, 110, 90).copy(alpha = 0.14F),
                    1F to Color(255, 90, 80).copy(alpha = 0F),
                ),
            ),
        )
    }
}

/** Lens flare that breathes, grows when tilted towards its source and throws ghosts across the frame. */
internal class FlarePainter(
    private val effect: LivingPosterEffect,
    seed: Int,
) : EffectPainter {
    private val breathing = FractalNoise(seed)
    private val haloSprite by lazy { createRadialSprite(haloColor, SpriteProfile.Soft) }
    private val glowSprite by lazy { createRadialSprite(glowColor, SpriteProfile.Soft) }
    private val coreSprite by lazy { createRadialSprite(Color.White, SpriteProfile.Core) }
    private val ghostSprites by lazy { ghosts.map { createRadialSprite(it.color, SpriteProfile.Bokeh) } }

    override fun prepare() {
        (listOf(haloSprite, glowSprite, coreSprite, ringSprite, streakSprite) + ghostSprites)
            .forEach { it.prepareToDraw() }
    }

    override fun DrawScope.paint(
        plane: EffectPlane,
        scene: EffectScene,
    ) {
        when (plane) {
            EffectPlane.Front -> paintFlare(scene)
            EffectPlane.Backdrop, EffectPlane.Back, EffectPlane.Rim -> Unit
        }
    }

    private fun DrawScope.paintFlare(scene: EffectScene) {
        val (tiltX, tiltY, seconds) = scene.frame
        val at = effect.at ?: defaultAt
        val width = size.width
        val source = Offset(at.x / 100F * width, at.y / 100F * size.height)
        val towards = tiltX * (at.x / 50F - 1F) + tiltY * (at.y / 50F - 1F)
        val facing = (1F + 0.3F * towards).coerceIn(0.75F, 1.35F)
        val strength = effect.intensity * 1.6F * (0.9F + 0.1F * breathing.at(seconds * 0.35F)) * facing

        drawSprite(
            sprite = haloSprite,
            center = source,
            width = width * 0.75F,
            alpha = 0.14F * strength,
        )
        drawSprite(
            sprite = glowSprite,
            center = source,
            width = width * 0.26F,
            alpha = 0.5F * strength,
        )
        drawSprite(
            sprite = coreSprite,
            center = source,
            width = width * 0.07F,
            alpha = 0.95F * strength,
        )
        drawSprite(ringSprite, source, width = width * 0.42F, alpha = 0.22F * strength)
        drawSprite(
            sprite = streakSprite,
            center = source,
            width = width * 2.4F * (0.95F + 0.05F * breathing.at(seconds * 0.6F + 3F)),
            height = 7F * scene.unit,
            alpha = 0.45F * strength,
        )
        drawSprite(
            sprite = streakSprite,
            center = source,
            width = width * 1.5F,
            height = 1.8F * scene.unit,
            alpha = 0.8F * strength,
        )

        val focus = Offset(width * (0.5F - tiltX * 0.16F), size.height * (0.5F - tiltY * 0.16F))

        fun along(reach: Float): Offset = source + (focus - source) * reach

        ghosts.forEachIndexed { index, ghost ->
            drawSprite(
                sprite = ghostSprites[index],
                center = along(ghost.reach),
                width = ghost.size * width * 2F,
                alpha = ghost.alpha * strength,
            )
        }
        drawSprite(ringSprite, along(1.4F), width = width * 0.2F, alpha = 0.3F * strength)
    }
}
