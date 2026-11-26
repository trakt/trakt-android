package tv.trakt.trakt.core.summary.livingposter.ui.painters

import android.os.Build
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import tv.trakt.trakt.common.model.livingposter.LivingPosterEffect
import tv.trakt.trakt.core.summary.livingposter.model.EffectPlane
import tv.trakt.trakt.core.summary.livingposter.ui.drawCover
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin

private const val VIEW_WIDTH = 300F
private const val VIEW_HEIGHT = 450F
private const val SPAWN_RATE = 17F
private const val NEAR_SHARE = 0.05F
private const val CURL_STEP = 2F
private const val CURL_STRENGTH = 2300F
private const val LIFT = 44F
private const val WARMUP_SECONDS = 6F
private const val WARMUP_STEP = 1F / 30F
private const val MAX_STEP = 1F / 50F
private const val MAX_FRAME = 1F / 15F
private const val MIN_Y = -30F
private const val HAZE_TOP = 0.6F
private const val HAZE_STRIP = 2.5F
private const val HAZE_OVERLAP = 0.6F
private const val HAZE_AMPLITUDE = 1.6F
private const val RADIANS_TO_DEGREES = 180F / PI.toFloat()

/** The effect box in a 300 x 450 view space, the poster size [EffectScene.unit] is based on. */
private class ViewBox(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
)

/** Mutable, because the simulation steps every ember on every frame and copies would churn the GC. */
private class Ember(
    var x: Float,
    var y: Float,
    val life: Float,
    val size: Float,
    val isNear: Boolean,
    val phase: Float,
    val rate: Float,
) {
    var vx = 0F
    var vy = 0F
    var age = 0F
}

/** Curl of a drifting noise stream: divergence-free, so embers swirl instead of bunching up. */
private class AirField(
    seed: Int,
) {
    private val coarse = Noise2d(seed + 11)
    private val fine = Noise2d(seed + 23)

    private fun stream(
        x: Float,
        y: Float,
        t: Float,
    ): Float {
        return coarse.at(x * 0.011F, y * 0.011F + t * 0.22F) +
            0.45F * fine.at(x * 0.027F - t * 0.13F, y * 0.027F + t * 0.4F)
    }

    fun flow(
        x: Float,
        y: Float,
        t: Float,
    ): Offset {
        return Offset(
            x = (stream(x, y + CURL_STEP, t) - stream(x, y - CURL_STEP, t)) / (2F * CURL_STEP),
            y = -(stream(x + CURL_STEP, y, t) - stream(x - CURL_STEP, y, t)) / (2F * CURL_STEP),
        )
    }
}

private class EmberSimulation(
    effect: LivingPosterEffect,
    seed: Int,
    private val box: ViewBox,
) {
    private val random = SeededRandom(seed)
    private val air = AirField(seed)
    private val rate = SPAWN_RATE * (0.5F + effect.intensity)
    private var pending = 0F
    private var seconds: Float? = null

    private val live = ArrayList<Ember>()

    val embers: List<Ember>
        get() = live

    /** Runs the warm-up ending at time zero, so the poster opens mid-burn. */
    fun prepare() {
        if (seconds != null) return
        warmUp(endingAt = 0F)
    }

    /** Steps the simulation to [now], warming it up first when [prepare] has not run. */
    fun advance(now: Float) {
        val previous = seconds
        if (previous == now) return

        if (previous == null) {
            warmUp(endingAt = now)
            return
        }

        val elapsed = (now - previous).coerceIn(0F, MAX_FRAME)
        val steps = ceil(elapsed / MAX_STEP).toInt()
        (1..steps).forEach { index -> simulate(elapsed / steps, previous + elapsed / steps * index) }
        seconds = now
    }

    private fun warmUp(endingAt: Float) {
        val steps = (WARMUP_SECONDS / WARMUP_STEP).roundToInt()
        repeat(steps) { index -> simulate(WARMUP_STEP, endingAt - WARMUP_SECONDS + index * WARMUP_STEP) }
        seconds = endingAt
    }

    private fun simulate(
        step: Float,
        t: Float,
    ) {
        val due = pending + step * rate
        val spawned = floor(due).toInt()
        pending = due - spawned
        repeat(spawned) { live += spawnEmber() }

        live.forEach { move(it, step, t) }
        live.removeAll { it.age > it.life || it.y < MIN_Y }
    }

    private fun spawnEmber(): Ember {
        val isNear = random.next() < NEAR_SHARE

        return Ember(
            x = box.x + box.width * (0.03F + 0.94F * random.next()),
            y = box.y + box.height * (0.95F + 0.08F * random.next()),
            life = if (isNear) 3F + random.next() else 3.5F + random.next() * 3F,
            size = if (isNear) 2.4F + random.next() * 1.2F else 0.55F + random.next() * 0.6F,
            isNear = isNear,
            phase = random.next() * 2F * PI.toFloat(),
            rate = 6F + random.next() * 10F,
        )
    }

    private fun move(
        ember: Ember,
        step: Float,
        t: Float,
    ) {
        val height = ((box.y + box.height - ember.y) / box.height).coerceIn(0F, 1F)
        val flow = air.flow(ember.x, ember.y, t)
        val lift = 0.45F + 0.7F * height
        val speed = if (ember.isNear) 1.5F else 1F

        ember.vx = flow.x * CURL_STRENGTH * lift * speed
        ember.vy = (flow.y * CURL_STRENGTH * lift - LIFT * (1F - 0.35F * height)) * speed
        ember.x += ember.vx * step
        ember.y += ember.vy * step
        ember.age += step
    }
}

/**
 * Embers rising through turbulent air and cooling as they climb, a flickering base glow and a
 * heat haze that ripples the bottom of the background. A few large, blurred embers pass in front.
 */
internal class EmbersPainter(
    private val effect: LivingPosterEffect,
    seed: Int,
) : EffectPainter {
    private val box = ViewBox(
        x = effect.box.x / 100F * VIEW_WIDTH,
        y = effect.box.y / 100F * VIEW_HEIGHT,
        width = effect.box.width / 100F * VIEW_WIDTH,
        height = effect.box.height / 100F * VIEW_HEIGHT,
    )
    private val palette = EmberPalette(effect.color)
    private val glow = FractalNoise(seed + 7)
    private val simulation = EmberSimulation(effect, seed, box)
    private val coreSprites by lazy { palette.colors.map { createRadialSprite(it, SpriteProfile.Core) } }
    private val softSprites by lazy { palette.colors.map { createRadialSprite(it, SpriteProfile.Soft) } }
    private val hazeShader by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) HeatHazeShader() else null
    }

    override fun prepare() {
        (coreSprites + softSprites).forEach { it.prepareToDraw() }
        simulation.prepare()
        hazeShader
    }

    override fun DrawScope.paint(
        plane: EffectPlane,
        scene: EffectScene,
    ) {
        when (plane) {
            EffectPlane.Backdrop -> paintHaze(scene)
            EffectPlane.Back -> paintBack(scene)
            EffectPlane.Front -> paintEmbers(scene, isNear = true)
            EffectPlane.Rim -> Unit
        }
    }

    private fun DrawScope.paintBack(scene: EffectScene) {
        paintBaseGlow(scene)
        paintEmbers(scene, isNear = false)
    }

    private fun DrawScope.paintBaseGlow(scene: EffectScene) {
        val unit = scene.unit

        drawSprite(
            sprite = softSprites[palette.bin(0.55F)],
            center = Offset((box.x + box.width / 2F) * unit, (box.y + box.height) * unit),
            width = box.width * 1.25F * unit,
            height = box.height * 0.5F * unit,
            alpha = (0.07F + 0.045F * glow.at(scene.frame.seconds * 1.6F)) * (0.6F + effect.intensity),
        )
    }

    private fun DrawScope.paintEmbers(
        scene: EffectScene,
        isNear: Boolean,
    ) {
        val seconds = scene.frame.seconds
        val unit = scene.unit
        simulation.advance(seconds)

        simulation.embers.forEach { ember ->
            if (ember.isNear != isNear) return@forEach

            val progress = ember.age / ember.life
            val heat = 1F - progress.pow(1.2F) * 0.92F
            val fade = smoothstep(0F, 0.06F, progress) *
                (1F - smoothstep(0.82F, 1F, progress)) *
                (0.72F + 0.28F * sin(seconds * ember.rate + ember.phase))
            if (fade < 0.01F) return@forEach

            val emberSize = ember.size * unit * (2.6F + 2.6F * heat)
            val speed = hypot(ember.vx, ember.vy)
            val center = Offset(ember.x * unit, ember.y * unit)
            val heading = atan2(ember.vy, ember.vx) - PI.toFloat() / 2F

            rotate(degrees = heading * RADIANS_TO_DEGREES, pivot = center) {
                drawSprite(
                    sprite = (if (isNear) softSprites else coreSprites)[palette.bin(heat)],
                    center = center,
                    width = emberSize,
                    height = emberSize * (1F + speed * 0.012F),
                    alpha = fade * if (isNear) 0.5F else 1F,
                )
            }
        }
    }

    /** Ripples the bottom of the background with a travelling wave, strongest at the base. */
    private fun DrawScope.paintHaze(scene: EffectScene) {
        val unit = scene.unit
        val top = (box.y + box.height * HAZE_TOP) * unit
        val bottom = (box.y + box.height) * unit
        val left = box.x * unit
        val shader = hazeShader
        val bitmap = scene.sources.backdropBitmap

        if (shader == null || bitmap == null) {
            paintHazeStrips(scene)
            return
        }

        drawRect(
            brush = shader.brush(
                backdrop = bitmap,
                canvasSize = size,
                unit = unit,
                seconds = scene.frame.seconds,
                top = top,
                bottom = bottom,
                amplitude = HAZE_AMPLITUDE * unit,
            ),
            topLeft = Offset(left, top),
            size = Size(box.width * unit, bottom - top),
        )
    }

    /** Fallback below API 33: redraws the band in thin strips, each nudged by the same wave. */
    private fun DrawScope.paintHazeStrips(scene: EffectScene) {
        val backdrop = scene.sources.backdrop ?: return
        val unit = scene.unit
        val seconds = scene.frame.seconds
        val top = (box.y + box.height * HAZE_TOP) * unit
        val bottom = (box.y + box.height) * unit
        val left = box.x * unit
        val right = left + box.width * unit
        val strip = HAZE_STRIP * unit
        val stripHeight = (HAZE_STRIP + HAZE_OVERLAP) * unit
        val strips = ceil((bottom - top) / strip).toInt()

        repeat(strips) { index ->
            val y = top + index * strip
            val depth = (y - top) / (bottom - top)
            val amplitude = depth * depth * HAZE_AMPLITUDE * unit
            val row = y / unit
            val dx = amplitude * (sin(row * 0.19F - seconds * 6.1F) * 0.6F + sin(row * 0.053F + seconds * 3.7F) * 0.4F)
            val dy = amplitude * 0.5F * sin(row * 0.11F - seconds * 4.3F)

            clipRect(left, y, right, y + stripHeight) {
                translate(left = dx, top = -dy) { drawCover(backdrop) }
            }
        }
    }
}
