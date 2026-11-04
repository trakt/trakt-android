package tv.trakt.trakt.core.main.ui.seasonal.newyear

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import tv.trakt.trakt.common.ui.theme.colors.Orange700
import tv.trakt.trakt.core.main.ui.seasonal.Gold400
import tv.trakt.trakt.core.main.ui.seasonal.SplashEaseOutBack
import tv.trakt.trakt.core.main.ui.seasonal.rememberSplashIntro
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

// The medallion artwork includes its fan of rays and glow around the 176 unit emblem.
private const val MEDALLION_SCALE = 300F / 176F
private const val RAYS_SCALE = 3.2F
private const val GLOW_SCALE = 2.8F

@Composable
internal fun NewYearHero(
    animate: Boolean,
    modifier: Modifier = Modifier,
    heroSize: Dp = 176.dp,
) {
    val glowIntro = rememberSplashIntro(
        animate = animate,
        delayMillis = 0,
        durationMillis = 900,
        easing = LinearOutSlowInEasing,
    )
    val raysIntro = rememberSplashIntro(
        animate = animate,
        delayMillis = 0,
        durationMillis = 1_200,
        easing = LinearOutSlowInEasing,
    )
    val medallionIntro = rememberSplashIntro(
        animate = animate,
        delayMillis = 150,
        durationMillis = 800,
        easing = SplashEaseOutBack,
    )

    val transition = rememberInfiniteTransition(label = "newYearHero")
    val pulse by transition.animateFloat(
        initialValue = 0F,
        targetValue = 1F,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2_000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "newYearHeroGlow",
    )
    val spin by transition.animateFloat(
        initialValue = 0F,
        targetValue = 360F,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 60_000, easing = LinearEasing),
        ),
        label = "newYearHeroSpin",
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(heroSize),
    ) {
        Box(
            modifier = Modifier
                .requiredSize(heroSize * GLOW_SCALE)
                .graphicsLayer {
                    val wave = if (animate) pulse else 0F
                    val scale = lerp(0.6F, 1F, glowIntro.value) * (1F + 0.08F * wave)
                    alpha = glowIntro.value * (1F - 0.3F * wave)
                    scaleX = scale
                    scaleY = scale
                }
                .background(
                    Brush.radialGradient(
                        0F to Gold400.copy(alpha = 0.45F),
                        0.35F to Orange700.copy(alpha = 0.2F),
                        0.68F to Color.Transparent,
                    ),
                ),
        )

        Image(
            painter = painterResource(R.drawable.img_new_year_rays),
            contentDescription = null,
            modifier = Modifier
                .requiredSize(heroSize * RAYS_SCALE)
                .graphicsLayer {
                    val scale = lerp(0.4F, 1F, raysIntro.value)
                    alpha = raysIntro.value
                    scaleX = scale
                    scaleY = scale
                    rotationZ = if (animate) spin else 0F
                },
        )

        Image(
            painter = painterResource(R.drawable.img_new_year_medallion),
            contentDescription = null,
            modifier = Modifier
                .requiredSize(heroSize * MEDALLION_SCALE)
                .graphicsLayer {
                    val progress = medallionIntro.value
                    val scale = lerp(0.3F, 1F, progress)
                    alpha = progress.coerceIn(0F, 1F)
                    scaleX = scale
                    scaleY = scale
                    rotationZ = lerp(-8F, 0F, progress)
                },
        )
    }
}

@Preview(widthDp = 360, heightDp = 480)
@Composable
private fun Preview() {
    TraktTheme {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
        ) {
            NewYearHero(animate = false)
        }
    }
}
