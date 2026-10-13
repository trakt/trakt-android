package tv.trakt.trakt.core.main.ui.seasonal.halloween

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.delay
import tv.trakt.trakt.common.ui.theme.colors.Orange300
import tv.trakt.trakt.common.ui.theme.colors.Orange400
import tv.trakt.trakt.common.ui.theme.colors.Orange600
import tv.trakt.trakt.core.main.ui.seasonal.SplashEaseOutBack
import tv.trakt.trakt.core.main.ui.seasonal.SplashEaseOutQuint
import tv.trakt.trakt.core.main.ui.seasonal.rememberSplashIntro
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme
import kotlin.time.Duration.Companion.milliseconds

private const val HAT_TILT = -8F

private data class MoonBat(
    val dx: Float,
    val dy: Float,
    val delayMillis: Int,
    val scale: Float,
)

private val MoonBats = persistentListOf(
    MoonBat(dx = -10F, dy = -150F, delayMillis = 1_400, scale = 0.9F),
    MoonBat(dx = 190F, dy = -60F, delayMillis = 1_800, scale = 1.1F),
    MoonBat(dx = -200F, dy = -40F, delayMillis = 2_300, scale = 1F),
    MoonBat(dx = 120F, dy = -170F, delayMillis = 3_100, scale = 0.7F),
    MoonBat(dx = -140F, dy = -130F, delayMillis = 3_600, scale = 0.8F),
    MoonBat(dx = 230F, dy = 30F, delayMillis = 4_400, scale = 0.6F),
)

@Composable
internal fun HalloweenHero(
    animate: Boolean,
    modifier: Modifier = Modifier,
    heroSize: Dp = 112.dp,
) {
    val glowIntro = rememberSplashIntro(
        animate = animate,
        delayMillis = 0,
        durationMillis = 900,
        easing = LinearOutSlowInEasing,
    )
    val logoIntro = rememberSplashIntro(
        animate = animate,
        delayMillis = 100,
        durationMillis = 700,
        easing = SplashEaseOutBack,
    )

    val pulse by rememberInfiniteTransition(label = "heroGlow").animateFloat(
        initialValue = 0F,
        targetValue = 1F,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2_000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "heroGlowPulse",
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(heroSize * 2),
    ) {
        Box(
            modifier = Modifier
                .requiredSize(heroSize * 2.3F)
                .graphicsLayer {
                    val wave = if (animate) pulse else 0F
                    val scale = lerp(0.6F, 1F, glowIntro.value) * (1F + 0.08F * wave)
                    alpha = glowIntro.value * (1F - 0.3F * wave)
                    scaleX = scale
                    scaleY = scale
                }
                .background(
                    Brush.radialGradient(
                        0F to Orange400.copy(alpha = 0.3F),
                        0.35F to Orange600.copy(alpha = 0.12F),
                        0.6F to Color.Transparent,
                    ),
                ),
        )

        HalloweenMoon(
            animate = animate,
            modifier = Modifier.fillMaxSize(),
        )

        Image(
            painter = painterResource(R.drawable.img_trakt_mark_circle),
            contentDescription = null,
            modifier = Modifier
                .size(heroSize)
                .graphicsLayer {
                    val scale = lerp(0.3F, 1F, logoIntro.value)
                    alpha = logoIntro.value.coerceIn(0F, 1F)
                    scaleX = scale
                    scaleY = scale
                }
                .shadow(elevation = 16.dp, shape = CircleShape),
        )

        Image(
            painter = painterResource(R.drawable.img_witch_hat),
            contentDescription = null,
            modifier = Modifier
                .requiredSize(heroSize * 1.2F)
                .offset(x = heroSize * 0.02F, y = -heroSize * 0.68F)
                .graphicsLayer {
                    transformOrigin = TransformOrigin(0.5F, 1F)
                    alpha = logoIntro.value.coerceIn(0F, 1F)
                    rotationZ = HAT_TILT
                },
        )
    }
}

@Composable
private fun HalloweenMoon(
    animate: Boolean,
    modifier: Modifier = Modifier,
) {
    val intro = rememberSplashIntro(
        animate = animate,
        delayMillis = 0,
        durationMillis = 1_100,
        easing = SplashEaseOutQuint,
    )

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val scale = lerp(0.85F, 1F, intro.value)
                    alpha = intro.value
                    translationY = (1F - intro.value) * size.height * 0.2F
                    scaleX = scale
                    scaleY = scale
                },
        ) {
            val radius = size.minDimension / 2
            drawCircle(
                brush = Brush.radialGradient(
                    0F to MoonLight,
                    0.45F to MoonMid,
                    1F to Orange300,
                    center = Offset(size.width * 0.38F, size.height * 0.36F),
                    radius = radius * 1.78F,
                ),
                radius = radius,
            )

            drawCircle(
                color = Orange300.copy(alpha = 0.45F),
                radius = radius * 0.12F,
                center = Offset(size.width * 0.28F, size.height * 0.34F),
            )
            drawCircle(
                color = Orange300.copy(alpha = 0.4F),
                radius = radius * 0.17F,
                center = Offset(size.width * 0.7F, size.height * 0.62F),
            )
            drawCircle(
                color = Orange300.copy(alpha = 0.35F),
                radius = radius * 0.08F,
                center = Offset(size.width * 0.62F, size.height * 0.24F),
            )
        }

        if (animate) {
            MoonBats.forEach { bat ->
                EscapingBat(
                    bat = bat,
                    modifier = Modifier
                        .fillMaxSize(0.12F)
                        .align(Alignment.Center),
                )
            }
        }
    }
}

@Composable
private fun EscapingBat(
    bat: MoonBat,
    modifier: Modifier = Modifier,
) {
    val progress = remember { Animatable(0F) }

    LaunchedEffect(bat) {
        delay(bat.delayMillis.milliseconds)
        while (true) {
            progress.snapTo(0F)
            progress.animateTo(
                targetValue = 1F,
                animationSpec = tween(durationMillis = 5_500, easing = EaseIn),
            )
        }
    }

    val flap by rememberInfiniteTransition(label = "batFlap").animateFloat(
        initialValue = 1F,
        targetValue = 0.45F,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 280, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "batFlapScale",
    )

    Image(
        painter = painterResource(R.drawable.img_bat),
        contentDescription = null,
        modifier = modifier.graphicsLayer {
            val t = progress.value
            val scale = bat.scale * lerp(0.3F, 2.2F, t)
            alpha = when {
                t < 0.1F -> t / 0.1F
                t < 0.7F -> 1F
                else -> (1F - t) / 0.3F
            }
            translationX = bat.dx / 100F * 3F * size.width * t
            translationY = bat.dy / 100F * 3F * size.height * 0.5F * t
            scaleX = scale
            scaleY = scale * flap
        },
    )
}

@Preview(widthDp = 360, heightDp = 400)
@Composable
private fun Preview() {
    TraktTheme {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
        ) {
            HalloweenHero(animate = false)
        }
    }
}
