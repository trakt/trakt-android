package tv.trakt.trakt.core.summary.livingposter.ui.painters

import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Shader
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.toArgb
import tv.trakt.trakt.common.model.livingposter.LivingPosterEffect
import tv.trakt.trakt.core.summary.livingposter.model.EffectPlane
import tv.trakt.trakt.core.summary.livingposter.ui.drawCover
import tv.trakt.trakt.core.summary.livingposter.ui.withLayer
import kotlin.math.exp
import kotlin.math.floor

private const val STRIKES = 36
private const val FIRST_STRIKE = 1.2F
private const val STRIKE_SECONDS = 1.4F
private const val RISE_SECONDS = 0.012F
private const val DECAY_SECONDS = 0.065F
private const val AFTERGLOW = 0.12F
private const val AFTERGLOW_SECONDS = 0.45F
private const val FAR_LEVEL = 0.35F
private const val BOLT_SECONDS = 0.12F
private const val DARK = 0.003F
private const val SKY_SIZE = 128
private const val RIM_WIDTH = 300F
private const val RIM_HEIGHT = 450F

private val skyColor = Color(200, 214, 255)
private val cloudColor = Color(210, 220, 255)
private val washColor = Color(190, 205, 255)
private val rimColor = Color(214, 224, 255)
private val frontColor = Color(205, 218, 255)

private class BoltStroke(
    val width: Float,
    val alpha: Float,
    val color: Color,
)

private val boltStrokes = listOf(
    BoltStroke(width = 6F, alpha = 0.07F, color = Color(200, 215, 255)),
    BoltStroke(width = 2.4F, alpha = 0.22F, color = Color(225, 233, 255)),
    BoltStroke(width = 0.9F, alpha = 0.95F, color = Color.White),
)

/** One lightning strike: a burst of 1-4 return strokes, optionally with a visible bolt in poster fractions. */
private class Strike(
    val at: Float,
    val isFar: Boolean,
    val offsets: List<Float>,
    val amplitudes: List<Float>,
    val centerX: Float,
    val bolt: List<List<Offset>>?,
)

private class Flash(
    val strike: Strike,
    val age: Float,
    val level: Float,
    val lit: Float,
)

private class StrikePlan(
    val strikes: List<Strike>,
    val period: Float,
)

/**
 * Lightning in bursts of flashes on an irregular schedule: the sky and clouds light up behind the
 * figures, near strikes show a branching bolt and rim the figures' top edges, and the whole
 * poster brightens briefly.
 */
internal class LightningPainter(
    effect: LivingPosterEffect,
    private val seed: Int,
) : EffectPainter {
    private val plan = planStrikes(SeededRandom(seed))
    private val strength = effect.intensity * 2F
    private val sky: ImageBitmap by lazy { createSkyTexture(seed) }
    private val glowSprite by lazy { createRadialSprite(skyColor, SpriteProfile.Soft) }

    override fun prepare() {
        sky.prepareToDraw()
        glowSprite.prepareToDraw()
    }

    override fun hasContent(
        plane: EffectPlane,
        seconds: Float,
    ): Boolean {
        val flash = flashAt(seconds) ?: return false

        return when (plane) {
            EffectPlane.Back, EffectPlane.Front -> true
            EffectPlane.Rim -> !flash.strike.isFar
            EffectPlane.Backdrop -> false
        }
    }

    override fun DrawScope.paint(
        plane: EffectPlane,
        scene: EffectScene,
    ) {
        val flash = flashAt(scene.frame.seconds) ?: return

        when (plane) {
            EffectPlane.Back -> paintSky(scene, flash)
            EffectPlane.Rim -> paintRim(scene, flash)
            EffectPlane.Front -> drawRect(frontColor, alpha = (flash.lit * 0.05F).coerceAtMost(1F))
            EffectPlane.Backdrop -> Unit
        }
    }

    override fun planeFilter(seconds: Float): ColorFilter? {
        val flash = flashAt(seconds) ?: return null
        val brightness = 1F + flash.lit * 0.6F
        val matrix = ColorMatrix().apply {
            setToSaturation(1F - flash.level * 0.3F)
            timesAssign(ColorMatrix().apply { setToScale(brightness, brightness, brightness, 1F) })
        }

        return ColorFilter.colorMatrix(matrix)
    }

    private fun flashAt(seconds: Float): Flash? {
        val local = seconds % plan.period
        val strike = plan.strikes.firstOrNull { local >= it.at && local < it.at + STRIKE_SECONDS } ?: return null
        val age = local - strike.at
        val level = strike.level(age)
        val lit = level * strength
        if (lit < DARK) return null

        return Flash(strike = strike, age = age, level = level, lit = lit)
    }

    private fun DrawScope.paintSky(
        scene: EffectScene,
        flash: Flash,
    ) {
        val strike = flash.strike
        val lit = flash.lit

        drawImageIn(sky, Offset.Zero, Size(size.width, size.height * 0.7F), alpha = lit * 0.55F)
        drawSprite(
            sprite = glowSprite,
            center = Offset(strike.centerX * size.width, (if (strike.isFar) 0.3F else 0.1F) * size.height),
            width = size.width * 1.6F,
            height = size.height * 1.1F,
            alpha = lit * 0.7F,
        )
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(washColor.copy(alpha = 0.35F), washColor.copy(alpha = 0F)),
                startY = 0F,
                endY = size.height * 0.75F,
            ),
            size = Size(size.width, size.height * 0.75F),
            alpha = lit.coerceAtMost(1F),
        )

        val bolt = strike.bolt ?: return
        if (flash.age > strike.offsets.last() + BOLT_SECONDS) return

        drawBolt(bolt, lit, scene.unit)
    }

    private fun DrawScope.drawBolt(
        bolt: List<List<Offset>>,
        lit: Float,
        unit: Float,
    ) {
        val paths = bolt.map { line ->
            Path().apply {
                line.forEachIndexed { index, point ->
                    val x = point.x * size.width
                    val y = point.y * size.height
                    if (index == 0) moveTo(x, y) else lineTo(x, y)
                }
            }
        }

        boltStrokes.forEach { stroke ->
            paths.forEachIndexed { index, path ->
                drawPath(
                    path = path,
                    color = stroke.color,
                    alpha = (lit * stroke.alpha).coerceAtMost(1F),
                    style = Stroke(
                        width = stroke.width * unit * if (index == 0) 1F else 0.6F,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round,
                    ),
                )
            }
        }
    }

    /** Lights the figures' upper edges: the cutout tinted, minus itself nudged down and right. */
    private fun DrawScope.paintRim(
        scene: EffectScene,
        flash: Flash,
    ) {
        val figures = scene.sources.figures ?: return
        if (flash.strike.isFar) return

        withLayer(alpha = (flash.lit * 0.95F).coerceAtMost(1F)) {
            drawCover(figures, colorFilter = ColorFilter.tint(rimColor, BlendMode.SrcIn))
            withLayer(blendMode = BlendMode.DstOut) {
                translate(left = 1.2F * size.width / RIM_WIDTH, top = 3.4F * size.height / RIM_HEIGHT) {
                    drawCover(figures)
                }
            }
        }
    }
}

private fun Strike.level(age: Float): Float {
    val strokes = offsets.mapIndexed { index, offset ->
        val since = age - offset
        if (since < 0F) return@mapIndexed 0F

        val shape = if (since < RISE_SECONDS) {
            since / RISE_SECONDS
        } else {
            exp(-(since - RISE_SECONDS) / DECAY_SECONDS)
        }
        shape * amplitudes[index]
    }
    val level = maxOf(AFTERGLOW * exp(-age / AFTERGLOW_SECONDS), strokes.max())

    return if (isFar) level * FAR_LEVEL else level
}

private fun planStrikes(random: SeededRandom): StrikePlan {
    var clock = FIRST_STRIKE

    val strikes = List(STRIKES) {
        val isFar = random.next() < 0.3F
        val strokes = if (isFar) 1 + floor(random.next() * 2F).toInt() else 2 + floor(random.next() * 3F).toInt()
        val offsets = (1 until strokes).runningFold(0F) { last, _ -> last + 0.05F + random.next() * 0.11F }
        val amplitudes = offsets.indices.map { index -> if (index == 0) 1F else 0.55F + random.next() * 0.45F }
        val centerX = 0.15F + random.next() * 0.7F
        val bolt = if (!isFar && random.next() < 0.6F) createBolt(random, centerX) else null

        Strike(
            at = clock,
            isFar = isFar,
            offsets = offsets,
            amplitudes = amplitudes,
            centerX = centerX,
            bolt = bolt,
        ).also { clock += 3.8F + random.next() * 5.5F }
    }

    return StrikePlan(strikes = strikes, period = clock)
}

/** Midpoint displacement: the points after [from] on a jagged line to [to]. */
private fun subdivide(
    random: SeededRandom,
    from: Offset,
    to: Offset,
    spread: Float,
    depth: Int,
): List<Offset> {
    if (depth == 0) return listOf(to)

    val middle = Offset(
        x = (from.x + to.x) / 2F + (random.next() - 0.5F) * spread,
        y = (from.y + to.y) / 2F + (random.next() - 0.5F) * spread * 0.3F,
    )

    return subdivide(random, from, middle, spread / 2F, depth - 1) +
        subdivide(random, middle, to, spread / 2F, depth - 1)
}

private fun createBolt(
    random: SeededRandom,
    centerX: Float,
): List<List<Offset>> {
    val start = Offset(centerX + (random.next() - 0.5F) * 0.1F, -0.02F)
    val end = Offset(centerX + (random.next() - 0.5F) * 0.25F, 0.28F + random.next() * 0.22F)
    val main = listOf(start) + subdivide(random, start, end, spread = 0.14F, depth = 5)
    val branches = List(1 + floor(random.next() * 2F).toInt()) {
        val fork = main[4 + floor(random.next() * (main.size - 10)).toInt()]
        val tip = Offset(fork.x + (random.next() - 0.5F) * 0.3F, fork.y + 0.08F + random.next() * 0.12F)

        listOf(fork) + subdivide(random, fork, tip, spread = 0.07F, depth = 4)
    }

    return listOf(main) + branches
}

private fun createSkyTexture(seed: Int): ImageBitmap {
    val clouds = cloudTexture(seed = seed % 997 + 3, color = cloudColor, from = 0.32F, to = 0.95F)

    return createSprite(SKY_SIZE, SKY_SIZE) {
        drawBitmap(clouds.asAndroidBitmap(), 0F, 0F, null)
        drawRect(
            0F,
            0F,
            SKY_SIZE.toFloat(),
            SKY_SIZE.toFloat(),
            Paint().apply {
                xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
                shader = LinearGradient(
                    0F,
                    0F,
                    0F,
                    SKY_SIZE.toFloat(),
                    intArrayOf(
                        Color.Black.toArgb(),
                        Color.Black.copy(alpha = 0.6F).toArgb(),
                        Color.Black.copy(alpha = 0F).toArgb(),
                    ),
                    floatArrayOf(0F, 0.55F, 1F),
                    Shader.TileMode.CLAMP,
                )
            },
        )
    }
}
