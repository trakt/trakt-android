package tv.trakt.trakt.core.comments.ui

import android.graphics.drawable.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import coil3.DrawableImage
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.CommentGif
import tv.trakt.trakt.ui.theme.DefaultCardShape
import tv.trakt.trakt.ui.theme.TraktTheme

private const val FALLBACK_ASPECT_RATIO = 1F

internal enum class CommentGifLayout {
    Bottom,
    Side,
}

internal val SideGifMaxSize = DpSize(width = 120.dp, height = 60.dp)
private val SpoilerBlurRadius = 24.dp

@Composable
internal fun CommentGifView(
    gif: CommentGif,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = DefaultCardShape,
    blurred: Boolean = false,
    paused: Boolean = false,
    onRevealSpoiler: () -> Unit = {},
) {
    val (width, height) = gif.size
    val hasSize = width > 0 && height > 0

    var animatable by remember(gif.url) { mutableStateOf<Animatable?>(null) }
    LaunchedEffect(animatable, paused) {
        val drawable = animatable ?: return@LaunchedEffect
        if (paused) drawable.stop() else drawable.start()
    }

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
            .background(TraktTheme.colors.skeletonShimmer)
            .then(
                if (blurred) Modifier.onClick { onRevealSpoiler() } else Modifier,
            ),
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(gif.url)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            onSuccess = { state ->
                animatable = (state.result.image as? DrawableImage)?.drawable as? Animatable
            },
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (blurred) Modifier.blur(SpoilerBlurRadius) else Modifier,
                ),
        )
    }
}
