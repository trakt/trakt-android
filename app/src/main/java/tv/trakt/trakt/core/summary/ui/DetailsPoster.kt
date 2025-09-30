package tv.trakt.trakt.core.summary.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import tv.trakt.trakt.ui.theme.TraktTheme
import tv.trakt.trakt.ui.theme.VerticalImageAspectRatio

private val cardShape = RoundedCornerShape(20.dp)

@Composable
internal fun DetailsPoster(
    imageUrl: String?,
    modifier: Modifier = Modifier,
    color: Color? = null,
) {
    Box(
        modifier = modifier
            .aspectRatio(VerticalImageAspectRatio),
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .dropShadow(
                    shape = cardShape,
                    shadow = Shadow(
                        radius = 48.dp,
                        spread = 8.dp,
                        offset = DpOffset(0.dp, 12.dp),
                        color = color?.copy(alpha = 0.22F) ?: Color.Transparent,
                    ),
                )
                .fillMaxSize()
                .clip(cardShape),
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
