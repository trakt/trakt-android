package tv.trakt.trakt.core.summary.livingposter.ui

import android.view.Surface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isUnspecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.transformations
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.launch
import tv.trakt.trakt.common.model.livingposter.LivingPoster
import tv.trakt.trakt.core.summary.livingposter.model.LivingPosterParticle
import tv.trakt.trakt.core.summary.livingposter.model.LivingPosterStackItem
import tv.trakt.trakt.core.summary.livingposter.model.Spring
import tv.trakt.trakt.core.summary.livingposter.model.Tilt
import tv.trakt.trakt.core.summary.livingposter.model.toStack

private val cardShape = RoundedCornerShape(24.dp)

private const val LAYER_MAX_SIZE_PX = 1000
private const val LAYER_TRAVEL = 0.006F
private const val MAX_LAYER_SHIFT = 1F
private const val PLANE_SCALE = 1F + 2F * LAYER_TRAVEL * MAX_LAYER_SHIFT
private const val TILT_STIFFNESS = 140F
private const val TILT_DAMPING = 15F
private const val MAX_FRAME_SECONDS = 1F / 30F
private const val PARTICLE_BASE_SEED = 42
private const val PARTICLE_SEED_STEP = 101

/** Draws the layered poster once every layer has loaded, reported through [onReady]. */
@Composable
internal fun LivingPosterStage(
    poster: LivingPoster,
    onReady: (Boolean) -> Unit,
    tiltEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val stack = remember(poster) { poster.toStack() }
    val particles = remember(stack) { stack.toParticles() }

    val layerPainters = stack
        .filterIsInstance<LivingPosterStackItem.Layer>()
        .associate { item -> key(item.key) { item.key to rememberLayerPainter(item.url) } }
    val shadowPainters = stack
        .filterIsInstance<LivingPosterStackItem.Layer>()
        .filter { it.isCutout }
        .associate { item -> key(item.key) { item.key to rememberLayerPainter(item.url, shadow = true) } }
    val maskPainters = stack
        .filterIsInstance<LivingPosterStackItem.Effect>()
        .mapNotNull { item -> item.maskUrl?.let { item.key to it } }
        .associate { (itemKey, url) -> key(itemKey) { itemKey to rememberLayerPainter(url) } }

    val layersLoaded = layerPainters.values
        .map { it.state.collectAsState().value }
        .all { it is AsyncImagePainter.State.Success }
    val maskStates = maskPainters.mapValues { (_, painter) -> painter.state.collectAsState().value }
    val masksSettled = maskStates.values.all {
        it is AsyncImagePainter.State.Success || it is AsyncImagePainter.State.Error
    }
    val readyMasks = maskPainters.filterKeys { maskStates[it] is AsyncImagePainter.State.Success }
    val shadowStates = shadowPainters.mapValues { (_, painter) -> painter.state.collectAsState().value }
    val readyShadows = shadowPainters.filterKeys { shadowStates[it] is AsyncImagePainter.State.Success }

    val ready = layersLoaded && masksSettled
    val currentOnReady by rememberUpdatedState(onReady)
    LaunchedEffect(ready) { currentOnReady(ready) }
    DisposableEffect(Unit) { onDispose { currentOnReady(false) } }

    val motion = rememberLivingPosterMotion(tiltEnabled)
    val planeScale = if (tiltEnabled) PLANE_SCALE else 1F

    Canvas(
        modifier = modifier.graphicsLayer {
            shape = cardShape
            clip = true
            compositingStrategy = CompositingStrategy.Offscreen
        },
    ) {
        if (!ready) return@Canvas

        val frame = LivingPosterFrame(
            tiltX = motion.tiltX,
            tiltY = motion.tiltY,
            seconds = motion.seconds,
        )

        drawRect(Color.Black)
        withTransform({ scale(planeScale, planeScale, pivot = center) }) {
            drawStack(
                stack = stack,
                frame = frame,
                layers = layerPainters,
                shadows = readyShadows,
                masks = readyMasks,
                particles = particles,
            )
        }
        drawVignette()
        drawGlare(frame)
    }
}

private fun DrawScope.drawStack(
    stack: ImmutableList<LivingPosterStackItem>,
    frame: LivingPosterFrame,
    layers: Map<String, Painter>,
    shadows: Map<String, Painter>,
    masks: Map<String, Painter>,
    particles: Map<String, List<LivingPosterParticle>>,
) {
    stack.forEach { item ->
        val shift = item.shift.coerceIn(-MAX_LAYER_SHIFT, MAX_LAYER_SHIFT)
        translate(
            left = frame.tiltX * shift * -LAYER_TRAVEL * size.width,
            top = frame.tiltY * shift * -LAYER_TRAVEL * size.height,
        ) {
            when (item) {
                is LivingPosterStackItem.Layer -> {
                    shadows[item.key]?.let { drawCastShadow(it, frame) }
                    layers[item.key]?.let { drawCover(it) }
                }

                is LivingPosterStackItem.Effect -> {
                    if (item.maskUrl != null && masks[item.key] == null) return@translate
                    drawEffect(item.effect, masks[item.key], frame)
                }

                is LivingPosterStackItem.Particles -> {
                    drawParticles(
                        particles = particles[item.key].orEmpty(),
                        isScreen = item.isScreen,
                        seconds = frame.seconds,
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawCastShadow(
    shadow: Painter,
    frame: LivingPosterFrame,
) {
    translate(
        left = (frame.tiltX * -10F).dp.toPx(),
        top = (8F + frame.tiltY * -6F).dp.toPx(),
    ) {
        drawCover(shadow, alpha = 0.25F)
    }
}

private fun DrawScope.drawCover(
    painter: Painter,
    alpha: Float = 1F,
) {
    val intrinsic = painter.intrinsicSize
    if (intrinsic.isUnspecified || intrinsic.width <= 0F || intrinsic.height <= 0F) {
        with(painter) { draw(size, alpha) }
        return
    }

    val scale = maxOf(size.width / intrinsic.width, size.height / intrinsic.height)
    val drawSize = Size(intrinsic.width * scale, intrinsic.height * scale)
    val offset = Offset((size.width - drawSize.width) / 2F, (size.height - drawSize.height) / 2F)

    translate(offset.x, offset.y) {
        with(painter) { draw(drawSize, alpha) }
    }
}

private fun ImmutableList<LivingPosterStackItem>.toParticles(): Map<String, List<LivingPosterParticle>> {
    return filterIsInstance<LivingPosterStackItem.Particles>().associate { item ->
        item.key to item.effects.flatMapIndexed { index, effect ->
            LivingPosterParticle.create(effect, seed = PARTICLE_BASE_SEED + index * PARTICLE_SEED_STEP)
        }
    }
}

@Composable
private fun rememberLayerPainter(
    url: String,
    shadow: Boolean = false,
): AsyncImagePainter {
    val context = LocalContext.current
    val request = remember(url, shadow) {
        ImageRequest.Builder(context)
            .data(url)
            .size(LAYER_MAX_SIZE_PX)
            .apply { if (shadow) transformations(LivingPosterShadowTransformation()) }
            .build()
    }

    return rememberAsyncImagePainter(model = request)
}

@Stable
private class LivingPosterMotion {
    var tiltX by mutableFloatStateOf(0F)
    var tiltY by mutableFloatStateOf(0F)
    var seconds by mutableFloatStateOf(0F)
}

/** Advances the effect clock and, with [tiltEnabled], follows the gyroscope while the screen is started. */
@Composable
private fun rememberLivingPosterMotion(tiltEnabled: Boolean): LivingPosterMotion {
    val motion = remember { LivingPosterMotion() }
    val context = LocalContext.current
    val displayRotation = LocalView.current.display?.rotation ?: Surface.ROTATION_0
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    LaunchedEffect(motion, lifecycle, displayRotation, tiltEnabled) {
        var elapsedMillis = (motion.seconds * 1000F).toLong()

        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            var target = Tilt()
            var springX = Spring()
            var springY = Spring()

            if (tiltEnabled) {
                launch {
                    var previousNanos: Long? = null
                    context.screenRotationRates(displayRotation).collect { rate ->
                        val seconds = (rate.timestampNanos - (previousNanos ?: rate.timestampNanos)) / 1E9F
                        previousNanos = rate.timestampNanos
                        target = target.integrate(rate, seconds.coerceAtMost(MAX_FRAME_SECONDS))
                    }
                }
            }

            var previousMillis: Long? = null
            while (true) {
                withFrameMillis { millis ->
                    val deltaMillis = millis - (previousMillis ?: millis)
                    previousMillis = millis
                    elapsedMillis += deltaMillis

                    val seconds = minOf(MAX_FRAME_SECONDS, deltaMillis / 1000F)
                    springX = springX.step(target.x, seconds, TILT_STIFFNESS, TILT_DAMPING)
                    springY = springY.step(target.y, seconds, TILT_STIFFNESS, TILT_DAMPING)

                    motion.tiltX = springX.value
                    motion.tiltY = springY.value
                    motion.seconds = elapsedMillis / 1000F
                }
            }
        }
    }

    return motion
}
