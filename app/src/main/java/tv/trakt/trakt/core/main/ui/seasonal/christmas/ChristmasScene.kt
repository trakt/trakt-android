package tv.trakt.trakt.core.main.ui.seasonal.christmas

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.core.main.ui.seasonal.SplashEaseOutQuint
import tv.trakt.trakt.core.main.ui.seasonal.rememberSplashIntro
import tv.trakt.trakt.resources.R

private const val HILLS_ASPECT = 3.75F
private const val SLEIGH_ASPECT = 5.17F
private const val SLEIGH_TOP = 0.14F

private val HillsMinHeight = 112.dp
private val HillsMaxHeight = 240.dp
private val GarlandHeight = 24.dp
private val SleighWidth = 200.dp

private data class SleighFrame(
    val at: Float,
    val x: Float,
    val y: Float,
    val rotation: Float,
)

// Fractions of the loop and of the screen size. The flight takes the first 30% of
// the loop, the sleigh then waits off screen until the next pass.
private val SleighFlight = persistentListOf(
    SleighFrame(at = 0F, x = -0.3F, y = 0.06F, rotation = -6F),
    SleighFrame(at = 0.1F, x = 0.3F, y = -0.02F, rotation = -2F),
    SleighFrame(at = 0.2F, x = 0.7F, y = 0.03F, rotation = -4F),
    SleighFrame(at = 0.3F, x = 1.15F, y = -0.06F, rotation = -8F),
)

@Composable
internal fun ChristmasGarland(
    animate: Boolean,
    modifier: Modifier = Modifier,
) {
    val intro = rememberSplashIntro(animate = animate, delayMillis = 200, durationMillis = 600)

    Image(
        painter = painterResource(R.drawable.img_christmas_garland),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .fillMaxWidth()
            .height(GarlandHeight)
            .graphicsLayer {
                alpha = intro.value
            },
    )
}

@Composable
internal fun ChristmasHills(
    animate: Boolean,
    modifier: Modifier = Modifier,
) {
    val intro = rememberSplashIntro(
        animate = animate,
        delayMillis = 200,
        durationMillis = 900,
        easing = SplashEaseOutQuint,
    )

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val height = (maxWidth / HILLS_ASPECT).coerceIn(HillsMinHeight, HillsMaxHeight)

        Image(
            painter = painterResource(R.drawable.img_christmas_hills),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alignment = Alignment.BottomCenter,
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .graphicsLayer {
                    alpha = intro.value
                    translationY = (1F - intro.value) * size.height * 0.3F
                },
        )
    }
}

@Composable
internal fun SantaSleigh(modifier: Modifier = Modifier) {
    val direction = when (LocalLayoutDirection.current) {
        LayoutDirection.Ltr -> 1F
        LayoutDirection.Rtl -> -1F
    }

    val progress by rememberInfiniteTransition(label = "santaSleigh").animateFloat(
        initialValue = 0F,
        targetValue = 1F,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 34_000, easing = LinearEasing),
            initialStartOffset = StartOffset(1_000),
        ),
        label = "santaSleighFlight",
    )

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight

        Image(
            painter = painterResource(R.drawable.img_santa_sleigh),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(width = SleighWidth, height = SleighWidth / SLEIGH_ASPECT)
                .graphicsLayer {
                    val frame = sleighFrameAt(progress)
                    translationX = direction * frame.x * screenWidth.toPx()
                    translationY = (SLEIGH_TOP + frame.y) * screenHeight.toPx()
                    rotationZ = direction * frame.rotation
                    scaleX = direction
                },
        )
    }
}

private fun sleighFrameAt(progress: Float): SleighFrame {
    val next = SleighFlight.firstOrNull { it.at >= progress } ?: return SleighFlight.last()
    val previous = SleighFlight.lastOrNull { it.at < progress } ?: return next

    val fraction = (progress - previous.at) / (next.at - previous.at)
    return SleighFrame(
        at = progress,
        x = lerp(previous.x, next.x, fraction),
        y = lerp(previous.y, next.y, fraction),
        rotation = lerp(previous.rotation, next.rotation, fraction),
    )
}
