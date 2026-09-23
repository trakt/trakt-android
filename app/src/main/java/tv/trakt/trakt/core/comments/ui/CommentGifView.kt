package tv.trakt.trakt.core.comments.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import tv.trakt.trakt.common.model.CommentGif
import tv.trakt.trakt.ui.theme.DefaultCardShape
import tv.trakt.trakt.ui.theme.TraktTheme

private const val FALLBACK_ASPECT_RATIO = 1F

@Composable
internal fun CommentGifView(
    gif: CommentGif,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = DefaultCardShape,
) {
    val (width, height) = gif.size
    val hasSize = width > 0 && height > 0

    // width() clamps to parent max constraints, so an oversized gif shrinks while keeping its ratio.
    val sizeModifier = when {
        hasSize -> {
            Modifier
                .width(width.dp)
                .aspectRatio(width.toFloat() / height)
        }
        else -> {
            Modifier
                .fillMaxWidth()
                .aspectRatio(FALLBACK_ASPECT_RATIO)
        }
    }

    Box(
        modifier = modifier
            .then(sizeModifier)
            .clip(shape)
            .background(TraktTheme.colors.skeletonShimmer),
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(gif.url)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
