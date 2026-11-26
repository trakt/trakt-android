package tv.trakt.trakt.core.summary.livingposter.ui.painters

import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.toArgb
import tv.trakt.trakt.common.model.livingposter.LivingPosterEffect
import tv.trakt.trakt.core.summary.livingposter.model.EffectPlane
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

private const val DROP_COUNT = 60
private const val SPLASH_EVERY = 0.045F
private const val SPLASH_SECONDS = 0.5F
private const val MAX_CATCH_UP_SECONDS = 1F
private const val SPLASH_HOP = 0.4F
private const val SLANT = 0.1F
private const val STREAK_SPRITE_WIDTH = 4
private const val STREAK_SPRITE_HEIGHT = 64
private val slantDegrees = atan(SLANT) * 180F / PI.toFloat()
private val slantStretch = sqrt(1F + SLANT * SLANT)

private val streakColor = Color(220, 232, 255)
private val streakTail = Color(210, 225, 255, 0)
private val ringColor = Color(215, 228, 255)
private val dropletColor = Color(225, 235, 255)
private val mistColor = Color(200, 215, 240)

private class Drop(
    val x: Float,
    val phase: Float,
    val duration: Float,
    val length: Float,
    val opacity: Float,
)

private class Splash(
    val x: Float,
    val y: Float,
    val at: Float,
    val size: Float,
)

/** Slanted streaks behind the figures; splashes and low mist on the ground in front of them. */
internal class RainPainter(
    effect: LivingPosterEffect,
    seed: Int,
) : EffectPainter {
    private val random = SeededRandom(seed)
    private val drops: List<Drop>
    private val splashEvery: Float
    private var splashes: List<Splash> = emptyList()
    private var lastSplash = 0F
    private val streakSprite by lazy {
        createSprite(STREAK_SPRITE_WIDTH, STREAK_SPRITE_HEIGHT) {
            drawRect(
                0F,
                0F,
                STREAK_SPRITE_WIDTH.toFloat(),
                STREAK_SPRITE_HEIGHT.toFloat(),
                Paint().apply {
                    shader = LinearGradient(
                        0F,
                        0F,
                        0F,
                        STREAK_SPRITE_HEIGHT.toFloat(),
                        streakTail.toArgb(),
                        streakColor.toArgb(),
                        Shader.TileMode.CLAMP,
                    )
                },
            )
        }
    }

    init {
        val scale = intensityScale(effect.intensity)
        drops = List((DROP_COUNT * scale).roundToInt()) {
            Drop(
                x = random.next() * 110F - 5F,
                phase = random.next(),
                duration = 0.6F + random.next() * 0.35F,
                length = 0.05F + random.next() * 0.05F,
                opacity = 0.2F + random.next() * 0.3F,
            )
        }
        splashEvery = SPLASH_EVERY / scale
    }

    override fun prepare() {
        streakSprite.prepareToDraw()
    }

    override fun DrawScope.paint(
        plane: EffectPlane,
        scene: EffectScene,
    ) {
        when (plane) {
            EffectPlane.Back -> paintDrops(scene)
            EffectPlane.Front -> paintGround(scene)
            EffectPlane.Backdrop, EffectPlane.Rim -> Unit
        }
    }

    private fun DrawScope.paintDrops(scene: EffectScene) {
        val seconds = scene.frame.seconds
        val strokeWidth = 0.9F * scene.unit

        drops.forEach { drop ->
            val progress = (seconds / drop.duration + drop.phase) % 1F
            val end = Offset(
                x = drop.x / 100F * size.width - progress * SLANT * size.height,
                y = (progress * 1.25F - 0.15F) * size.height,
            )
            val length = drop.length * size.height * slantStretch

            rotate(degrees = slantDegrees, pivot = end) {
                drawImageIn(
                    image = streakSprite,
                    topLeft = Offset(end.x - strokeWidth / 2F, end.y - length),
                    size = Size(strokeWidth, length),
                    alpha = drop.opacity,
                )
            }
        }
    }

    private fun DrawScope.paintGround(scene: EffectScene) {
        spawnSplashes(scene.frame.seconds)
        splashes.forEach { splash -> drawSplash(scene, splash) }

        val mistTop = size.height * 0.75F
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(mistColor.copy(alpha = 0F), mistColor.copy(alpha = 0.12F)),
                startY = mistTop,
                endY = size.height,
            ),
            topLeft = Offset(0F, mistTop),
            size = Size(size.width, size.height - mistTop),
        )
    }

    private fun spawnSplashes(seconds: Float) {
        lastSplash = max(lastSplash, seconds - MAX_CATCH_UP_SECONDS)
        val spawned = buildList {
            while (lastSplash < seconds) {
                lastSplash += splashEvery
                add(
                    Splash(
                        x = random.next(),
                        y = 0.82F + random.next() * 0.16F,
                        at = lastSplash,
                        size = 0.6F + random.next() * 0.8F,
                    ),
                )
            }
        }

        splashes = (splashes + spawned).filter { seconds - it.at < SPLASH_SECONDS }
    }

    private fun DrawScope.drawSplash(
        scene: EffectScene,
        splash: Splash,
    ) {
        val age = (scene.frame.seconds - splash.at) / SPLASH_SECONDS
        if (age < 0F) return

        val unit = scene.unit
        val x = splash.x * size.width
        val y = splash.y * size.height
        val ring = (3F + age * 10F) * splash.size * unit

        drawOval(
            color = ringColor,
            topLeft = Offset(x - ring, y - ring * 0.3F),
            size = Size(ring * 2F, ring * 0.6F),
            alpha = 0.45F * (1F - age),
            style = Stroke(width = 0.8F * unit),
        )
        if (age >= SPLASH_HOP) return

        val hop = age / SPLASH_HOP
        listOf(-1F, 1F).forEach { side ->
            drawCircle(
                color = dropletColor,
                radius = 0.9F * unit,
                center = Offset(
                    x = x + side * hop * 7F * splash.size * unit,
                    y = y - sin(hop * PI.toFloat()) * 8F * splash.size * unit,
                ),
                alpha = 0.6F * (1F - hop),
            )
        }
    }
}
