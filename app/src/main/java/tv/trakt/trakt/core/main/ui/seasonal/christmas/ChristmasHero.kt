package tv.trakt.trakt.core.main.ui.seasonal.christmas

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import tv.trakt.trakt.common.ui.theme.colors.Red400
import tv.trakt.trakt.core.main.ui.seasonal.Gold600
import tv.trakt.trakt.core.main.ui.seasonal.SplashEaseOutBack
import tv.trakt.trakt.core.main.ui.seasonal.rememberSplashIntro
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

// Bauble artwork geometry in its source units. The hero box spans 240 units and the
// sphere sits at its lower part, with the cord rising above the box.
private const val HERO_UNITS = 240F
private const val BAUBLE_WIDTH = 195.75F
private const val BAUBLE_HEIGHT = 390.25F
private const val BAUBLE_CENTER_Y = 44.1F
private const val BAUBLE_PIVOT_Y = 0.141F
private const val BAUBLE_SWING = 3F

@Composable
internal fun ChristmasHero(
    animate: Boolean,
    modifier: Modifier = Modifier,
    heroSize: Dp = 160.dp,
) {
    val glowIntro = rememberSplashIntro(
        animate = animate,
        delayMillis = 0,
        durationMillis = 900,
        easing = LinearOutSlowInEasing,
    )
    val flakeIntro = rememberSplashIntro(
        animate = animate,
        delayMillis = 0,
        durationMillis = 1_200,
        easing = LinearOutSlowInEasing,
    )
    val baubleIntro = rememberSplashIntro(
        animate = animate,
        delayMillis = 150,
        durationMillis = 800,
        easing = SplashEaseOutBack,
    )

    val transition = rememberInfiniteTransition(label = "christmasHero")
    val pulse by transition.animateFloat(
        initialValue = 0F,
        targetValue = 1F,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2_000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "christmasHeroGlow",
    )
    val spin by transition.animateFloat(
        initialValue = 0F,
        targetValue = 360F,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 80_000, easing = LinearEasing),
        ),
        label = "christmasHeroSpin",
    )
    val swing by transition.animateFloat(
        initialValue = -BAUBLE_SWING,
        targetValue = BAUBLE_SWING,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2_500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "christmasHeroSwing",
    )

    val unit = heroSize / HERO_UNITS

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(heroSize),
    ) {
        Box(
            modifier = Modifier
                .requiredSize(heroSize * 2.8F)
                .graphicsLayer {
                    val wave = if (animate) pulse else 0F
                    val scale = lerp(0.6F, 1F, glowIntro.value) * (1F + 0.08F * wave)
                    alpha = glowIntro.value * (1F - 0.3F * wave)
                    scaleX = scale
                    scaleY = scale
                }
                .background(
                    Brush.radialGradient(
                        0F to Red400.copy(alpha = 0.45F),
                        0.35F to Gold600.copy(alpha = 0.2F),
                        0.68F to Color.Transparent,
                    ),
                ),
        )

        Image(
            painter = painterResource(R.drawable.img_christmas_snowflake),
            contentDescription = null,
            modifier = Modifier
                .requiredSize(heroSize * 2.4F)
                .graphicsLayer {
                    val progress = flakeIntro.value
                    val scale = lerp(0.5F, 1F, progress)
                    alpha = progress
                    scaleX = scale
                    scaleY = scale
                    rotationZ = lerp(-40F, 0F, progress) + if (animate) spin else 0F
                },
        )

        Image(
            painter = painterResource(R.drawable.img_christmas_bauble),
            contentDescription = null,
            modifier = Modifier
                .offset(y = unit * (BAUBLE_CENTER_Y - HERO_UNITS / 2))
                .requiredSize(
                    width = unit * BAUBLE_WIDTH,
                    height = unit * BAUBLE_HEIGHT,
                )
                .graphicsLayer {
                    val progress = baubleIntro.value
                    val scale = lerp(0.3F, 1F, progress)
                    transformOrigin = TransformOrigin(0.5F, BAUBLE_PIVOT_Y)
                    alpha = progress.coerceIn(0F, 1F)
                    scaleX = scale
                    scaleY = scale
                    rotationZ = lerp(-8F, 0F, progress) + if (animate) swing else 0F
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
            ChristmasHero(animate = false)
        }
    }
}
