package tv.trakt.trakt.tv.core.details.ui

import android.annotation.SuppressLint
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.CardDefaults
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import tv.trakt.trakt.tv.ui.theme.TraktTheme

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
internal fun BackdropImage(
    imageUrl: String?,
    modifier: Modifier = Modifier,
    blur: Dp = 0.dp,
    saturation: Float = 1f,
    imageAlpha: Float = 0.4F,
    gradientAlpha: Float = 1F,
    crossfade: Boolean = false,
    active: Boolean = true,
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    val background = TraktTheme.colors.backgroundPrimary

    val transition = updateTransition(active)
    val transitionAlpha by transition.animateFloat(transitionSpec = { tween(500) }) { if (it) gradientAlpha else 0F }
    val transitionImageAlpha by transition.animateFloat(transitionSpec = { tween(500) }) { if (it) imageAlpha else 1F }
    val transitionBlur by transition.animateDp(transitionSpec = { tween(500) }) { if (it) blur else 0.dp }
    val transitionSaturation by transition.animateFloat(transitionSpec = { tween(500) }) { if (it) saturation else 1F }

    val grayscaleColorFilter = remember {
        ColorFilter.colorMatrix(
            ColorMatrix().apply {
                setToSaturation(transitionSaturation)
            },
        )
    }

    val radialGradient = remember {
        object : ShaderBrush() {
            override fun createShader(size: Size): Shader {
                return RadialGradientShader(
                    colors = listOf(
                        background.copy(alpha = 0.48F),
                        background.copy(alpha = 1F),
                    ),
                    center = Offset(size.width / 1.05F, -size.height / 0.54F),
                    radius = size.width * 1.6F,
                    colorStops = listOf(0.66F, 1F),
                )
            }
        }
    }

    Box(
        modifier = modifier
            .width(screenWidth)
            .aspectRatio(CardDefaults.HorizontalImageAspectRatio),
    ) {
        Crossfade(
            targetState = imageUrl,
            animationSpec = tween(if (crossfade) 750 else 0),
            label = "Backdrop Fanart",
        ) { url ->
            if (url != null) {
                AsyncImage(
                    model = url,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    colorFilter = grayscaleColorFilter,
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(transitionImageAlpha)
                        .blur(transitionBlur),
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { this.alpha = transitionAlpha }
                .background(radialGradient),
        )
    }
}

@OptIn(ExperimentalCoilApi::class)
@Preview(
    device = "id:tv_4k",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun BackdropImagePreview() {
    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.LightGray.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            BackdropImage(
                imageUrl = "https://trakt.tv/assets/placeholders/thumb/fanart-96d5731216f272365311029c1d1a9388.png",
            )
        }
    }
}
