package tv.trakt.app.tv.core.splash

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush.Companion.verticalGradient
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import kotlinx.coroutines.delay
import tv.trakt.app.tv.R
import tv.trakt.app.tv.ui.theme.TraktTheme

@Composable
internal fun SplashScreen(onDismiss: () -> Unit) {
    SplashScreenContent(
        onDismiss = onDismiss,
    )
}

@Composable
private fun SplashScreenContent(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
) {
    val background = TraktTheme.colors.backgroundPrimary
    val gradient = remember {
        verticalGradient(
            listOf(
                background.copy(alpha = 0.48F),
                background.copy(alpha = 1F),
            ),
        )
    }

    val enterTransition = remember { mutableStateOf(false) }
    val exitTransition = remember { mutableStateOf(false) }

    val currentOnDismiss by rememberUpdatedState(onDismiss)

    LaunchedEffect(Unit) {
        delay(1000)
        enterTransition.value = true

        delay(3500)
        exitTransition.value = true

        delay(1000)
        currentOnDismiss()
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary),
    ) {
        val animatedAlpha: Float by animateFloatAsState(
            if (exitTransition.value) 0f else 1.0f,
            animationSpec = tween(750),
            label = "alpha",
        )

        val animatedImageAlpha: Float by animateFloatAsState(
            if (exitTransition.value) 0.4f else 0.95f,
            animationSpec = tween(750),
            label = "alphaImage",
        )

        Image(
            painter = painterResource(R.drawable.ic_splash_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            colorFilter = remember {
                ColorFilter.colorMatrix(
                    ColorMatrix().apply {
                        setToSaturation(0F)
                    },
                )
            },
            modifier = Modifier
                .alpha(animatedImageAlpha)
                .fillMaxSize(),
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient),
        )

        Column(
            verticalArrangement = spacedBy(8.dp),
            modifier = Modifier.graphicsLayer { alpha = animatedAlpha },
        ) {
            val painter = painterResource(R.drawable.ic_trakt_logo)
            Image(
                painter = painter,
                contentDescription = "Trakt",
                modifier = Modifier
                    .height(128.dp)
                    .aspectRatio(painter.intrinsicSize.width / painter.intrinsicSize.height),
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = spacedBy(8.dp),
            ) {
                val animatedAlpha1: Float by animateFloatAsState(
                    if (enterTransition.value) 1f else 0.0f,
                    animationSpec = tween(500),
                    label = "alpha1",
                )

                val animatedAlpha2: Float by animateFloatAsState(
                    if (enterTransition.value) 1f else 0.0f,
                    animationSpec = tween(500, delayMillis = 1000),
                    label = "alpha2",
                )

                val animatedAlpha3: Float by animateFloatAsState(
                    if (enterTransition.value) 1f else 0.0f,
                    animationSpec = tween(500, delayMillis = 2000),
                    label = "alpha3",
                )

                Text(
                    text = "discover.",
                    style = TraktTheme.typography.heading4.copy(fontSize = 30.sp),
                    color = TraktTheme.colors.textSecondary,
                    modifier = Modifier.graphicsLayer { alpha = animatedAlpha1 },
                )

                Text(
                    text = "track.",
                    style = TraktTheme.typography.heading4.copy(fontSize = 30.sp),
                    color = TraktTheme.colors.textSecondary,
                    modifier = Modifier.graphicsLayer { alpha = animatedAlpha2 },
                )

                Text(
                    text = "share.",
                    style = TraktTheme.typography.heading4.copy(fontSize = 30.sp),
                    color = TraktTheme.colors.textSecondary,
                    modifier = Modifier.graphicsLayer { alpha = animatedAlpha3 },
                )
            }
        }
    }
}

@Preview(
    device = "id:tv_4k",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        SplashScreenContent(
            onDismiss = { /* no-op */ },
        )
    }
}
