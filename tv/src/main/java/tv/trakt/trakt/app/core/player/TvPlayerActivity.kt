package tv.trakt.trakt.app.core.player

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.SeekBar
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.tv.material3.Icon
import com.pierfrancescosoffritti.androidyoutubeplayer.core.customui.utils.TimeUtilities
import com.pierfrancescosoffritti.androidyoutubeplayer.core.customui.views.YouTubePlayerSeekBar
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.resources.R
import kotlin.time.Duration.Companion.seconds

class TvPlayerActivity : ComponentActivity() {
    companion object {
        fun createIntent(
            context: Context,
            videoUrl: String,
        ): Intent {
            return Intent(context, TvPlayerActivity::class.java).apply {
                putExtra("extra_video_url", videoUrl)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val videoUrl = intent.getStringExtra("extra_video_url")
            ?: throw IllegalArgumentException("Video URL must be provided")

        setContent {
            val uriHandler = LocalUriHandler.current
            TraktTheme {
                TvVideoPlayer(
                    videoUrl = videoUrl,
                    onOpenVideoExternal = {
                        uriHandler.openUri(videoUrl)
                    },
                    onVideoEnd = {
                        finish()
                    },
                )
            }
        }
    }
}

@Composable
private fun TvVideoPlayer(
    videoUrl: String,
    onOpenVideoExternal: (String) -> Unit = {},
    onVideoEnd: () -> Unit = {},
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    var player = remember<YouTubePlayer?> { null }
    val playerTracker = remember { YouTubePlayerTracker() }
    var playerSeekBar = remember<YouTubePlayerSeekBar?> { null }
    var playingState by remember { mutableStateOf(PlayerState.UNKNOWN) }

    val videoId = remember(videoUrl) {
        videoUrl.substringAfterLast("v=")
    }

    var controlsVisible by remember { mutableStateOf(false) }
    var controlsActive by remember { mutableStateOf(false) }

    LaunchedEffect(playingState) {
        if (playingState == PlayerState.ENDED) {
            onVideoEnd()
        }
    }

    LaunchedEffect(controlsVisible) {
        if (controlsVisible) {
            coroutineScope.coroutineContext.cancelChildren()
            coroutineScope.launch {
                delay(2.seconds)
                controlsVisible = false
            }
        } else {
            coroutineScope.coroutineContext.cancelChildren()
        }
    }

    AndroidView(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = null,
                indication = null,
                onClick = {},
            ),
        factory = { context ->
            YouTubePlayerView(context).apply {
                enableAutomaticInitialization = false
                lifecycleOwner.lifecycle.addObserver(this)

                val options = IFramePlayerOptions.Builder()
                    .controls(0)
                    .rel(0)
                    .ivLoadPolicy(3)
                    .ccLoadPolicy(0)
                    .fullscreen(1)
                    .autoplay(1)
                    .build()

                val listener = object : AbstractYouTubePlayerListener() {
                    override fun onReady(youTubePlayer: YouTubePlayer) {
                        player = youTubePlayer

                        playerSeekBar?.let {
                            youTubePlayer.addListener(it)
                        }

                        youTubePlayer.addListener(playerTracker)
                        youTubePlayer.loadVideo(videoId, 0F)

                        controlsActive = true
                    }

                    override fun onStateChange(
                        youTubePlayer: YouTubePlayer,
                        state: PlayerState,
                    ) {
                        playingState = state
                        super.onStateChange(youTubePlayer, state)
                    }

                    override fun onError(
                        youTubePlayer: YouTubePlayer,
                        error: PlayerConstants.PlayerError,
                    ) {
                        onOpenVideoExternal(videoUrl)
                        Timber.e("YouTube Player Error: $error")
                        super.onError(youTubePlayer, error)
                    }
                }

                initialize(listener, options)
            }
        },
    )

    val animatedFade: Float by animateFloatAsState(
        if (controlsVisible) 1f else 0.0f,
        animationSpec = tween(500),
        label = "animatedFade",
    )

    TvVideoPlayerControls(
        onSeekBarReady = {
            playerSeekBar = it
        },
        onSeekTo = { time, fromUser ->
            if (!controlsActive) return@TvVideoPlayerControls

            if (fromUser) {
                player?.seekTo(time)
            }
            playerSeekBar?.videoCurrentTimeTextView?.text = TimeUtilities.formatTime(time)
        },
        onSeekClick = {
            if (!controlsActive) return@TvVideoPlayerControls

            if (playingState == PlayerState.PLAYING || playingState == PlayerState.BUFFERING) {
                player?.pause()
            } else {
                player?.play()
            }
        },
        onYouTubeClick = {
            if (!controlsActive) return@TvVideoPlayerControls
            onOpenVideoExternal(videoUrl)
        },
        onFocused = {
            controlsVisible = true
        },
        modifier = Modifier
            .fillMaxSize()
            .alpha(animatedFade),
    )
}

@Composable
private fun TvVideoPlayerControls(
    modifier: Modifier = Modifier,
    onSeekBarReady: (YouTubePlayerSeekBar) -> Unit = {},
    onSeekTo: (time: Float, fromUser: Boolean) -> Unit = { _, _ -> },
    onSeekClick: () -> Unit = {},
    onYouTubeClick: () -> Unit = {},
    onFocused: () -> Unit = {},
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Box(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 32.dp)
                .padding(bottom = 72.dp)
                .focusProperties { canFocus = false },
        ) {
            val iconFocused = remember { mutableStateOf(false) }
            val animatedIconScale: Float by animateFloatAsState(
                if (iconFocused.value) 1.15F else 1F,
                animationSpec = tween(),
                label = "scale",
            )

            Icon(
                painter = painterResource(R.drawable.ic_youtube_full),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .height(16.dp)
                    .scale(animatedIconScale)
                    .clickable {
                        onYouTubeClick()
                    }
                    .onFocusChanged {
                        iconFocused.value = it.isFocused
                        if (it.isFocused) {
                            onFocused()
                        }
                    }
                    .focusable(),
            )

            AndroidView(
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                factory = {
                    val listener = object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(
                            seekBar: SeekBar?,
                            progress: Int,
                            fromUser: Boolean,
                        ) {
                            onSeekTo(progress.toFloat(), fromUser)
                            if (fromUser) {
                                onFocused()
                            }
                        }

                        override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

                        override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
                    }

                    YouTubePlayerSeekBar(it).apply {
                        seekBar.setOnSeekBarChangeListener(listener)
                        seekBar.setOnClickListener {
                            onSeekClick()
                            onFocused()
                        }
                        onSeekBarReady(this)
                    }
                },
            )
        }
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF131517,
    widthDp = 1920 / 2,
    heightDp = 1080 / 2,
)
@Composable
fun Preview() {
    TraktTheme {
        TvVideoPlayerControls()
    }
}
