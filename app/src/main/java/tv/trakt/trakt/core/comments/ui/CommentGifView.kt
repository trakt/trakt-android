package tv.trakt.trakt.core.comments.ui

import android.graphics.drawable.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import tv.trakt.trakt.ui.theme.TraktTheme

// Fixed ratio reserves space before the GIF loads, so the layout never jumps.
// Takes the largest box that fits; callers must not force both width and height.
private const val GIF_ASPECT_RATIO = 16F / 9F

internal enum class CommentGifLayout {
    Bottom,
    Side,
}

internal val SideGifMaxSize = DpSize(width = 120.dp, height = 60.dp)
private val SpoilerBlurRadius = 24.dp
private val GifShape = RoundedCornerShape(12.dp)
internal val SideGifShape = RoundedCornerShape(8.dp)

@Composable
internal fun CommentGifView(
    gif: CommentGif,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = GifShape,
    blurred: Boolean = false,
    paused: Boolean = false,
    onRevealSpoiler: () -> Unit = {},
) {
    var animatable by remember(gif.url) { mutableStateOf<Animatable?>(null) }
    LaunchedEffect(animatable, paused) {
        val drawable = animatable ?: return@LaunchedEffect
        if (paused) drawable.stop() else drawable.start()
    }

    var imageRatio by remember(gif.url) { mutableStateOf<Float?>(null) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .aspectRatio(GIF_ASPECT_RATIO)
            .then(
                when (imageRatio) {
                    null -> Modifier.clip(shape).background(TraktTheme.colors.skeletonShimmer)
                    else -> Modifier
                },
            )
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
            contentScale = ContentScale.Fit,
            onSuccess = { state ->
                val image = state.result.image
                if (image.width > 0 && image.height > 0) {
                    imageRatio = image.width.toFloat() / image.height
                }
                animatable = (image as? DrawableImage)?.drawable as? Animatable
            },
            modifier = Modifier
                .gifBounds(imageRatio)
                .clip(shape)
                .then(
                    if (blurred) Modifier.blur(SpoilerBlurRadius) else Modifier,
                ),
        )
    }
}

// Fits the GIF inside the reserved box so rounded corners follow the GIF, not the box.
private fun Modifier.gifBounds(ratio: Float?): Modifier {
    return when (ratio) {
        null -> fillMaxSize()
        else -> aspectRatio(ratio)
    }
}
