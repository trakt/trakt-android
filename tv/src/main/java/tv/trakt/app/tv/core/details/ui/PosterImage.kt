package tv.trakt.app.tv.core.details.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.Icon
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import coil3.request.ImageRequest
import coil3.request.crossfade
import tv.trakt.app.tv.R
import tv.trakt.app.tv.ui.theme.TraktTheme

@Composable
internal fun PosterImage(
    posterUrl: String?,
    modifier: Modifier = Modifier,
    corner: Dp = 24.dp,
    shadow: Dp = 4.dp,
) {
    var isError by remember(posterUrl) { mutableStateOf(false) }
    Box(
        modifier = modifier
            .height(TraktTheme.size.detailsPosterSize)
            .aspectRatio(CardDefaults.VerticalImageAspectRatio)
            .shadow(
                elevation = shadow,
                shape = RoundedCornerShape(corner),
            )
            .clip(RoundedCornerShape(corner))
            .background(TraktTheme.colors.placeholderContainer),
    ) {
        if (posterUrl != null && !isError) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(posterUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                onError = { isError = true },
                modifier = Modifier
                    .fillMaxSize()
                    .align(Center),
            )
        } else {
            Icon(
                painter = painterResource(R.drawable.ic_trakt_placeholder_big),
                contentDescription = null,
                tint = TraktTheme.colors.placeholderContent,
                modifier = Modifier.align(Center),
            )
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Preview
@Composable
private fun PosterPreview() {
    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.Blue.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            PosterImage(
                posterUrl = "https://image.tmdb.org/t/p/w600_and_h900_bestv2/4iWjGghUj2uyHo2Hyw8NFBvsNGm.jpg",
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@Preview
@Composable
private fun PosterPreviewPlaceholder() {
    TraktTheme {
        PosterImage(
            posterUrl = null,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview
@Composable
private fun PosterPreviewError() {
    TraktTheme {
        PosterImage(
            posterUrl = "https://example.com/not-a-poster.jpg",
            modifier = Modifier.padding(16.dp),
        )
    }
}
