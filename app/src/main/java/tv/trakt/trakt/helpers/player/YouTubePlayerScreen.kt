@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package tv.trakt.trakt.helpers.player

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.children
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.FullscreenListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.loadOrCueVideo
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.LocalBottomBarVisibility
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

@SuppressLint("SourceLockedOrientationActivity")
@Composable
internal fun YouTubePlayerScreen(
    modifier: Modifier = Modifier,
    viewModel: YouTubePlayerViewModel = koinViewModel(),
    onNavigateBack: () -> Unit,
) {
    val activity = LocalActivity.current
    val uriHandler = LocalUriHandler.current
    val bottomBarVisibility = LocalBottomBarVisibility.current

    val state by viewModel.state.collectAsStateWithLifecycle()

    YouTubePlayerScreen(
        state = state,
        onError = {
            // Fall back to opening the video outside.
            state.videoUrl?.let {
                uriHandler.openUri(it)
                onNavigateBack()
            }
        },
        onBackClick = onNavigateBack,
        modifier = modifier,
    )

    DisposableEffect(Unit) {
        bottomBarVisibility.value = false
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

        onDispose {
            bottomBarVisibility.value = true
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }
}

@Composable
internal fun YouTubePlayerScreen(
    state: YouTubePlayerState,
    onError: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentPadding = PaddingValues(
        top = WindowInsets.statusBars.asPaddingValues()
            .calculateTopPadding(),
        bottom = WindowInsets.navigationBars.asPaddingValues()
            .calculateBottomPadding(),
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(contentPadding),
    ) {
        TitleBar(
            modifier = Modifier
                .padding(
                    horizontal = TraktTheme.spacing.mainPageHorizontalSpace + 6.dp,
                    vertical = 8.dp,
                )
                .onClick {
                    onBackClick()
                },
        )

        state.videoId?.let {
            TvVideoPlayer(
                videoId = it,
                onError = onError,
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(Color.Black)
                    .fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun TitleBar(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = spacedBy(12.dp),
            modifier = Modifier
                .height(TraktTheme.size.titleBarHeight)
                .graphicsLayer {
                    translationX = -2.dp.toPx()
                },
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_back_arrow),
                tint = TraktTheme.colors.textPrimary,
                contentDescription = null,
            )
        }
    }
}

@Composable
private fun TvVideoPlayer(
    videoId: String,
    modifier: Modifier = Modifier,
    onError: () -> Unit,
) {
    val activity = LocalActivity.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var player by remember { mutableStateOf<YouTubePlayer?>(null) }
    var playerView by remember { mutableStateOf<YouTubePlayerView?>(null) }
    var fullScreenView by remember { mutableStateOf<View?>(null) }

    // Use LaunchedEffect to react to changes in videoId or the player instance
    LaunchedEffect(player, videoId) {
        player?.loadOrCueVideo(lifecycleOwner.lifecycle, videoId, 0f)
    }

    DisposableEffect(lifecycleOwner, activity) {
        onDispose {
            val decor = activity?.window?.decorView as? ViewGroup
            fullScreenView?.let { decor?.removeView(it) }
            fullScreenView = null
            playerView?.let {
                lifecycleOwner.lifecycle.removeObserver(it)
                it.release()
            }
            playerView = null
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            YouTubePlayerView(ctx).apply {
                enableAutomaticInitialization = false

                lifecycleOwner.lifecycle.addObserver(this)

                val options = IFramePlayerOptions.Builder(ctx)
                    .controls(1)
                    .fullscreen(1)
                    .autoplay(1)
                    .ivLoadPolicy(3)
                    .build()

                val listener = object : AbstractYouTubePlayerListener() {
                    override fun onReady(youTubePlayer: YouTubePlayer) {
                        player = youTubePlayer
                    }

                    override fun onError(
                        youTubePlayer: YouTubePlayer,
                        error: PlayerConstants.PlayerError,
                    ) {
                        onError()
                        super.onError(youTubePlayer, error)
                    }
                }

                initialize(listener, options)

                addFullscreenListener(object : FullscreenListener {
                    override fun onEnterFullscreen(
                        fullscreenView: View,
                        exitFullscreen: () -> Unit,
                    ) {
                        val window = activity?.window ?: return
                        val decor = window.decorView as? ViewGroup ?: return
                        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                        WindowCompat.getInsetsController(window, decor).apply {
                            hide(WindowInsetsCompat.Type.systemBars())
                            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                        }

                        decor.addView(
                            fullscreenView,
                            ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT,
                            ),
                        )
                        fullScreenView = fullscreenView
                    }

                    override fun onExitFullscreen() {
                        val window = activity?.window ?: return
                        val decor = window.decorView as? ViewGroup ?: return
                        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                        WindowCompat.getInsetsController(window, decor)
                            .show(WindowInsetsCompat.Type.systemBars())

                        fullScreenView?.let { decor.removeView(it) }
                        fullScreenView = null
                    }
                })

                playerView = this
                disableWebViewsBackground()
            }
        },
    )
}

// https://github.com/PierfrancescoSoffritti/android-youtube-player/issues/514
private fun ViewGroup.disableWebViewsBackground() {
    children.forEach { child ->
        when (child) {
            is WebView -> child.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            is ViewGroup -> child.disableWebViewsBackground()
        }
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun PreviewScreen() {
    TraktTheme {
        YouTubePlayerScreen(
            state = YouTubePlayerState(
                videoId = "dQw4w9WgXcQ",
            ),
            onError = {},
            onBackClick = {},
        )
    }
}
