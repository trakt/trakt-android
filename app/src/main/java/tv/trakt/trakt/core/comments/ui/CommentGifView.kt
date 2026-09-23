package tv.trakt.trakt.core.comments.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import tv.trakt.trakt.common.model.CommentGif
import tv.trakt.trakt.ui.theme.DefaultCardShape
import tv.trakt.trakt.ui.theme.TraktTheme

private const val FALLBACK_ASPECT_RATIO = 1F

internal enum class CommentGifLayout {
    Bottom,
    Side,
}

// Side gif has to leave room for header and footer inside the fixed-height horizontal card.
internal val SideGifMaxSize = DpSize(width = 120.dp, height = 60.dp)

@Composable
internal fun CommentGifView(
    gif: CommentGif,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = DefaultCardShape,
) {
    val (width, height) = gif.size
    val hasSize = width > 0 && height > 0

    // Max-only bounds let aspectRatio pick the largest size that fits both the model size
    // and the parent constraints, so the gif shrinks in tight cards instead of overflowing.
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
            modifier = Modifier.fillMaxSize(),
        )
    }
}
