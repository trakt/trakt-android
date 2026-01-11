package tv.trakt.trakt.ui.components

import android.annotation.SuppressLint
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import tv.trakt.trakt.MainActivity
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.MOBILE_BACKGROUND_IMAGE_URL
import tv.trakt.trakt.ui.theme.HorizontalImageAspectRatio
import tv.trakt.trakt.ui.theme.TraktTheme

private const val PARALLAX_RATIO = 0.75F

@Composable
internal fun ScrollableBackdropImage(
    modifier: Modifier = Modifier,
    imageUrl: String? = null,
    imageAlpha: Float = 0.375F,
    translation: Float,
) {
    BackdropImage(
        imageUrl = imageUrl,
        imageAlpha = imageAlpha,
        modifier = modifier.graphicsLayer {
            translationY = translation * PARALLAX_RATIO
        },
    )
}

@Composable
internal fun ScrollableBackdropImage(
    scrollState: LazyListState,
    modifier: Modifier = Modifier,
    imageUrl: String? = null,
) {
    val firstItemVisible by remember {
        derivedStateOf { scrollState.firstVisibleItemIndex == 0 }
    }

    BackdropImage(
        imageUrl = imageUrl,
        imageAlpha = 0.375F,
        modifier = modifier.graphicsLayer {
            if (firstItemVisible) {
                translationY = (-PARALLAX_RATIO * scrollState.firstVisibleItemScrollOffset)
            } else {
                alpha = 0F
            }
        },
    )
}

@Composable
internal fun ScrollableBackdropImage(
    scrollState: LazyGridState,
    modifier: Modifier = Modifier,
    imageUrl: String? = null,
) {
    val firstItemVisible by remember {
        derivedStateOf { scrollState.firstVisibleItemIndex == 0 }
    }

    BackdropImage(
        imageUrl = imageUrl,
        imageAlpha = 0.375F,
        modifier = modifier.graphicsLayer {
            if (firstItemVisible) {
                translationY = (-PARALLAX_RATIO * scrollState.firstVisibleItemScrollOffset)
            } else {
                alpha = 0F
            }
        },
    )
}

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
private fun BackdropImage(
    imageUrl: String?,
    modifier: Modifier = Modifier,
    imageAlpha: Float = 0.375F,
) {
    val configuration = LocalConfiguration.current
    val activity = LocalActivity.current
    val inspection = LocalInspectionMode.current

    val screenWidth = configuration.screenWidthDp.dp
    val background = TraktTheme.colors.backgroundPrimary

    val imageUrl = remember(imageUrl) {
        if (imageUrl.isNullOrBlank() && !inspection) {
            val config = (activity as? MainActivity)?.customThemeConfig

            val customThemeEnabled = config?.enabled == true
            val customThemeBackground = config?.theme?.backgroundImageUrl

            when {
                customThemeEnabled && !customThemeBackground.isNullOrBlank() -> customThemeBackground
                else -> Firebase.remoteConfig.getString(MOBILE_BACKGROUND_IMAGE_URL).ifBlank { null }
            }
        } else {
            imageUrl
        }
    }

    val grayscaleColorFilter = remember {
        ColorFilter.colorMatrix(
            ColorMatrix().apply {
                setToSaturation(0F)
            },
        )
    }

    val linearGradient = remember {
        Brush.verticalGradient(
            colors = listOf(
                background.copy(alpha = 0.5F),
                background,
            ),
            startY = 0.0f,
        )
    }

    Box(
        modifier = modifier
            .width(screenWidth)
            .aspectRatio(HorizontalImageAspectRatio),
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            colorFilter = grayscaleColorFilter,
            modifier = Modifier
                .fillMaxSize()
                .alpha(imageAlpha),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(linearGradient),
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .height(3.dp)
                .fillMaxWidth()
                .background(background)
                .graphicsLayer {
                    translationY = 1.dp.toPx()
                },
        )
    }
}

@OptIn(ExperimentalCoilApi::class)
@Preview(
    device = "id:tv_4k",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun BackdropImagePreview() {
    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.LightGray.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            BackdropImage(
                imageUrl = "https://trakt.tv/assets/placeholders/thumb/fanart-96d5731216f272365311029c1d1a9388.png",
            )
        }
    }
}
