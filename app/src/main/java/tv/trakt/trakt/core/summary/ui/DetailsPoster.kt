package tv.trakt.trakt.core.summary.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import coil3.request.ImageRequest
import coil3.request.crossfade
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme
import tv.trakt.trakt.ui.theme.VerticalImageAspectRatio

private val cardShape = RoundedCornerShape(20.dp)

@Composable
internal fun DetailsPoster(
    imageUrl: String?,
    modifier: Modifier = Modifier,
    color: Color? = null,
) {
    var isError by remember(imageUrl) { mutableStateOf(false) }

    Box(
        modifier = modifier
            .aspectRatio(VerticalImageAspectRatio),
    ) {
        if (imageUrl != null && !isError) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                onError = { isError = true },
                modifier = Modifier
                    .dropShadow(
                        shape = cardShape,
                        shadow = Shadow(
                            radius = 46.dp,
                            spread = 8.dp,
                            offset = DpOffset(0.dp, 12.dp),
                            color = color?.copy(alpha = 0.25F) ?: Color.Transparent,
                        ),
                    )
                    .fillMaxSize()
                    .clip(cardShape),
            )
        } else {
            Box(
                modifier = Modifier
                    .shadow(
                        elevation = 6.dp,
                        shape = cardShape,
                    )
                    .fillMaxSize()
                    .clip(cardShape)
                    .background(color = TraktTheme.colors.placeholderContainer),
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_placeholder_vertical_border),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(TraktTheme.colors.placeholderContent),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                        .align(Alignment.Center),
                )
                Icon(
                    painter = painterResource(R.drawable.ic_placeholder_trakt),
                    contentDescription = null,
                    tint = TraktTheme.colors.placeholderContent,
                    modifier = Modifier
                        .size(184.dp)
                        .align(Alignment.TopEnd)
                        .graphicsLayer {
                            translationX = 0.dp.toPx()
                            translationY = -0.dp.toPx()
                        },
                )
                Icon(
                    painter = painterResource(R.drawable.ic_trakt_logo),
                    contentDescription = null,
                    tint = TraktTheme.colors.placeholderContent,
                    modifier = Modifier
                        .size(112.dp)
                        .align(Alignment.Center)
                        .graphicsLayer {
                            translationY = 24.dp.toPx()
                        },
                )
            }
        }
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
            Box(
                modifier = Modifier.padding(64.dp),
            ) {
                DetailsPoster(
                    imageUrl = "https://trakt.tv/assets/placeholders/thumb/fanart-96d5731216f272365311029c1d1a9388.png",
                    color = Color.White,
                )
            }
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
)
@Composable
private fun Preview2() {
    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.Blue.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            Box(
                modifier = Modifier.padding(64.dp),
            ) {
                DetailsPoster(
                    imageUrl = null,
                )
            }
        }
    }
}
