package tv.trakt.trakt.core.klipy.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import tv.trakt.trakt.common.core.klipy.model.Gif
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.ui.theme.TraktTheme

// Smaller than DefaultCardShape - GIF thumbnails sit at roughly a third of the screen width.
private val GifCardShape = RoundedCornerShape(12.dp)
private const val FALLBACK_ASPECT_RATIO = 1F

@Composable
internal fun GifCard(
    gif: Gif,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val preview = gif.preview ?: return
    val aspectRatio = when {
        preview.width > 0 && preview.height > 0 -> preview.width.toFloat() / preview.height
        else -> FALLBACK_ASPECT_RATIO
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(aspectRatio)
            .clip(GifCardShape)
            .background(TraktTheme.colors.skeletonContainer)
            .onClick(onClick = onClick),
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(preview.url)
                .crossfade(true)
                .build(),
            contentDescription = gif.title.ifBlank { null },
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
