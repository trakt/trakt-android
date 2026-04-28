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
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import tv.trakt.trakt.MainActivity
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.MOBILE_BACKGROUND_IMAGE_URL
import tv.trakt.trakt.ui.extensions.isAtLeastLarge
import tv.trakt.trakt.ui.theme.HorizontalImageAspectRatio
import tv.trakt.trakt.ui.theme.TraktTheme

private const val PARALLAX_RATIO = 0.75F

@Composable
internal fun ScrollableBackdropImage(
    modifier: Modifier = Modifier,
    imageUrl: String? = null,
    imageAlpha: Float = 0.4F,
    translation: Float,
) {
    val localPreview = LocalInspectionMode.current
    if (!localPreview) {
        BackdropImage(
            imageUrl = imageUrl,
            imageAlpha = imageAlpha,
            modifier = modifier.graphicsLayer {
                translationY = translation * PARALLAX_RATIO
            },
        )
    }
}

@Composable
internal fun ScrollableBackdropImage(
    scrollState: LazyListState,
    modifier: Modifier = Modifier,
    imageUrl: String? = null,
) {
    val localPreview = LocalInspectionMode.current

    val firstItemVisible by remember {
        derivedStateOf { scrollState.firstVisibleItemIndex == 0 }
    }

    if (!localPreview) {
        BackdropImage(
            imageUrl = imageUrl,
            imageAlpha = 0.4F,
            modifier = modifier.graphicsLayer {
                if (firstItemVisible) {
                    translationY = (-PARALLAX_RATIO * scrollState.firstVisibleItemScrollOffset)
                } else {
                    alpha = 0F
                }
            },
        )
    }
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
        imageAlpha = 0.4F,
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
    imageAlpha: Float,
    modifier: Modifier = Modifier,
) {
    val configuration = LocalConfiguration.current
    val activity = LocalActivity.current
    val inspection = LocalInspectionMode.current

    val sessionManager = koinInject<SessionManager>()
    var userImageUrl by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    LaunchedEffect(imageUrl) {
        scope.launch {
            userImageUrl = sessionManager.getProfileImage().orEmpty()
        }
    }

    val imageUrl = remember(imageUrl, userImageUrl) {
        val config = (activity as? MainActivity)?.customThemeConfig

        val customThemeEnabled = config?.enabled == true
        val customThemeBackground = config?.theme?.backgroundImageUrl

        when {
            inspection -> imageUrl // For preview.
            customThemeEnabled && !customThemeBackground.isNullOrBlank() -> customThemeBackground
            !imageUrl.isNullOrBlank() -> imageUrl
            userImageUrl == null -> null // Show nothing while loading the user image.
            !userImageUrl.isNullOrBlank() -> userImageUrl
            else -> Firebase.remoteConfig.getString(MOBILE_BACKGROUND_IMAGE_URL).ifBlank { null }
        }
    }

    val grayscaleColorFilter = remember {
        ColorFilter.colorMatrix(
            ColorMatrix().apply {
                setToSaturation(0F)
            },
        )
    }

    val background = TraktTheme.colors.backgroundPrimary
    val linearGradient = remember {
        Brush.verticalGradient(
            colors = listOf(
                background.copy(alpha = 0.5F),
                background,
            ),
            startY = 0.0f,
        )
    }

    val windowClass = currentWindowAdaptiveInfo().windowSizeClass
    Box(
        modifier = modifier
            .width(configuration.screenWidthDp.dp)
            .aspectRatio(
                when {
                    windowClass.isAtLeastLarge() -> HorizontalImageAspectRatio * 2.5F
                    else -> HorizontalImageAspectRatio
                },
            ),
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
                imageAlpha = 0.4F,
            )
        }
    }
}
