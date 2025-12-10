package tv.trakt.trakt.core.summary.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush.Companion.verticalGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import coil3.request.ImageRequest
import coil3.request.crossfade
import tv.trakt.trakt.ui.theme.TraktTheme

private const val PARALLAX_RATIO = 0.7F

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
internal fun DetailsBackground(
    imageUrl: String?,
    modifier: Modifier = Modifier,
    color: Color? = null,
    translation: Float = 0F,
    aspectRatio: Float = TraktTheme.size.detailsBackgroundRatio,
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val background = TraktTheme.colors.backgroundPrimary

    val grayGradient = remember {
        verticalGradient(
            colors = listOf(
                background.copy(alpha = 0.63F),
                background,
            ),
        )
    }

    val colorGradient = remember {
        verticalGradient(
            colors = listOf(
                color?.copy(alpha = 0.63F) ?: Color.Transparent,
                background,
            ),
        )
    }

    Box(
        modifier = modifier
            .width(screenWidth)
            .aspectRatio(aspectRatio)
            .graphicsLayer {
                translationY = translation * PARALLAX_RATIO
            },
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.FillHeight,
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    drawContent()

                    drawRect(grayGradient)
                    if (color != null) {
                        drawRect(colorGradient)
                    }

                    // Mitigate for a thin line at the bottom of the image when using scrolling.
                    drawRect(
                        color = background,
                        topLeft = Offset(0F, size.height - 1.dp.toPx()),
                        size = Size(size.width, 3.dp.toPx()),
                    )
                },
        )
    }
}

@OptIn(ExperimentalCoilApi::class)
@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.Blue.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            DetailsBackground(
                imageUrl = "https://trakt.tv/assets/placeholders/thumb/fanart-96d5731216f272365311029c1d1a9388.png",
            )
        }
    }
}
