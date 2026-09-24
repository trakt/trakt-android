package tv.trakt.trakt.app.core.comments.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.model.CommentGif

private const val FALLBACK_ASPECT_RATIO = 1F
private val SpoilerBlurRadius = 24.dp

internal val SideGifMaxSize = DpSize(width = 120.dp, height = 60.dp)
internal val GifShape = RoundedCornerShape(8.dp)

@Composable
internal fun CommentGifView(
    gif: CommentGif,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = GifShape,
    blurred: Boolean = false,
) {
    val (width, height) = gif.size
    val hasSize = width > 0 && height > 0

    val sizeModifier = when {
        hasSize -> {
            Modifier
                .sizeIn(maxWidth = width.dp, maxHeight = height.dp)
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
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (blurred) Modifier.blur(SpoilerBlurRadius) else Modifier,
                ),
        )
    }
}
