package tv.trakt.trakt.app.core.player.plex

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.retain.RetainedEffect
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush.Companion.horizontalGradient
import androidx.compose.ui.graphics.Brush.Companion.verticalGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.common.text.CueGroup
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.CaptionStyleCompat
import androidx.media3.ui.SubtitleView
import androidx.media3.ui.compose.ContentFrame
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import org.koin.compose.koinInject
import timber.log.Timber
import tv.trakt.trakt.app.core.player.plex.audio.TvPlexPlayerAudioDialog
import tv.trakt.trakt.app.core.player.plex.controls.TvPlexPlayerControls
import tv.trakt.trakt.app.core.player.plex.subtitles.TvPlexPlayerSubtitlesDialog
import tv.trakt.trakt.app.core.player.plex.subtitles.model.SubtitleSize
import tv.trakt.trakt.app.core.scrobble.data.work.PostScrobbleStartWorker
import tv.trakt.trakt.app.core.scrobble.data.work.PostScrobbleStopWorker
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import java.lang.System.currentTimeMillis
import kotlin.time.Duration.Companion.seconds

@OptIn(UnstableApi::class)
@Composable
internal fun TvPlexPlayerScreen(
    videoUrl: String,
    secondaryVideoUrls: List<String>,
    videoTitle: String,
    videoSubtitle: String?,
    videoProgress: Float,
    mediaId: TraktId,
    mediaType: MediaType,
) {
    val activity = LocalActivity.current
    val appContext = LocalContext.current.applicationContext
    val analytics = koinInject<Analytics>()

    val player = retain {
        ExoPlayer
            .Builder(activity!!.applicationContext)
            .build()
    }

    var playersCues by retain {
        mutableStateOf(CueGroup.EMPTY_TIME_ZERO)
    }

    val allUrls = remember { listOf(videoUrl) + secondaryVideoUrls }
    val currentUrlIndex = retain { mutableIntStateOf(0) }

    val isPlaying = retain { mutableStateOf(false) }
    val isBuffering = retain { mutableStateOf(true) }
    val isError = retain { mutableStateOf<PlaybackException?>(null) }

    val initialFocus = remember { FocusRequester() }
    val controlsFocus = remember { FocusRequester() }
    val controlsPlayFocus = remember { FocusRequester() }
    val controlsProgressFocus = remember { FocusRequester() }

    var controlsVisible by retain { mutableStateOf(false) }
    var controlsVisibleTimestamp by retain { mutableLongStateOf(currentTimeMillis()) }

    val subtitlesFocus = remember { FocusRequester() }
    var subtitlesControlsVisible by retain { mutableStateOf(false) }
    var subtitlesTracks by remember { mutableStateOf<ImmutableList<Tracks.Group>>(EmptyImmutableList) }
    var selectedSubtitleTrack by remember { mutableStateOf<Tracks.Group?>(null) }
    var selectedSubtitleSize by remember { mutableStateOf(SubtitleSize.DEFAULT) }

    val audioFocus = remember { FocusRequester() }
    var audioControlsVisible by retain { mutableStateOf(false) }
    var audioTracks by remember { mutableStateOf<ImmutableList<Tracks.Group>>(EmptyImmutableList) }
    var selectedAudioTrack by remember { mutableStateOf<Tracks.Group?>(null) }

    BackHandler(
        enabled = controlsVisible || subtitlesControlsVisible || audioControlsVisible,
    ) {
        controlsVisible = false
        subtitlesControlsVisible = false
        audioControlsVisible = false

        controlsPlayFocus.freeFocus()
        controlsProgressFocus.freeFocus()
        controlsFocus.freeFocus()
        subtitlesFocus.freeFocus()
        audioFocus.freeFocus()

        initialFocus.requestFocus()
    }

    LaunchedEffect(Unit) {
        initialFocus.requestFocus()
    }

    RetainedEffect(player) {
        var initialSeekDone = false
        var scrobbleStartScheduled = false

        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlayingNow: Boolean) {
                isPlaying.value = isPlayingNow

                if (isPlayingNow && !scrobbleStartScheduled) {
                    scrobbleStartScheduled = true

                    PostScrobbleStartWorker.scheduleOneTime(
                        appContext = appContext,
                        mediaId = mediaId,
                        mediaType = mediaType,
                        progress = if (player.duration > 0) {
                            (player.currentPosition.toFloat() / player.duration.toFloat() * 100f).coerceIn(0f, 100f)
                        } else {
                            0f
                        },
                    )

                    analytics.playback.logPlaybackStart(mediaType = mediaType.name)
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                isBuffering.value = playbackState == Player.STATE_BUFFERING

                if (!initialSeekDone && playbackState == Player.STATE_READY && videoProgress > 0f) {
                    initialSeekDone = true
                    val seekPositionMs = (videoProgress / 100f * player.duration).toLong()
                    player.seekTo(seekPositionMs)
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                Timber.e(error, "Plex Player Error")

                // If the error is a bad HTTP status, it likely means the URL is invalid or expired.
                // In this case, we want to try secondary urls without showing an error message.
                if (error.errorCode != ERROR_CODE_IO_BAD_HTTP_STATUS) {
                    isError.value = error
                    super.onPlayerError(error)
                    return
                }

                val nextIndex = currentUrlIndex.intValue + 1
                if (nextIndex < allUrls.size) {
                    Timber.d("Primary URL failed, retrying with secondary URL [$nextIndex]: ${allUrls[nextIndex]}")
                    currentUrlIndex.intValue = nextIndex

                    player.setMediaItem(MediaItem.fromUri(allUrls[nextIndex]))
                    player.prepare()
                    player.play()
                } else {
                    isError.value = error
                }

                super.onPlayerError(error)
            }

            override fun onCues(cueGroup: CueGroup) {
                playersCues = cueGroup
            }

            override fun onTracksChanged(tracks: Tracks) {
                val textGroups = tracks.groups
                    .filter { it.type == C.TRACK_TYPE_TEXT }
                    .also {
                        subtitlesTracks = it.toImmutableList()
                    }

                val audioGroups = tracks.groups
                    .filter { it.type == C.TRACK_TYPE_AUDIO }
                    .also {
                        audioTracks = it.toImmutableList()
                    }

                textGroups.forEachIndexed { i, group ->
                    val format = group.getTrackFormat(0)
                    Timber.d("ExoPlayer text[$i]: lang=${format.language} label=${format.label}")
                }

                audioGroups.forEachIndexed { i, group ->
                    val format = group.getTrackFormat(0)
                    Timber.d(
                        "ExoPlayer audio[$i]: lang=${format.language} label=${format.label} channels=${format.channelCount}",
                    )
                }
            }
        }

        player.addListener(listener)
        player.setMediaItem(MediaItem.fromUri(videoUrl))
        player.prepare()
        player.play()

        onRetire {
            player.removeListener(listener)
            player.release()

            PostScrobbleStopWorker.scheduleOneTime(
                appContext = appContext,
                mediaId = mediaId,
                mediaType = mediaType,
                progress = if (player.duration > 0) {
                    (player.currentPosition.toFloat() / player.duration.toFloat() * 100f).coerceIn(0f, 100f)
                } else {
                    0f
                },
            )

            if (isError.value == null) {
                analytics.playback.logPlaybackStop(mediaType = mediaType.name)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusProperties { canFocus = false },
    ) {
        ContentFrame(
            player = player,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .focusProperties { canFocus = false },
        )

        AndroidView(
            factory = { ctx ->
                SubtitleView(ctx).apply {
                    setStyle(
                        CaptionStyleCompat(
                            Color.White.toArgb(),
                            Color.Transparent.toArgb(),
                            Color.Transparent.toArgb(),
                            CaptionStyleCompat.EDGE_TYPE_OUTLINE,
                            Color.Black.toArgb(),
                            null,
                        ),
                    )

                    val subtitlesSize = SubtitleView.DEFAULT_TEXT_SIZE_FRACTION * selectedSubtitleSize.scale
                    setFractionalTextSize(subtitlesSize)
                }
            },
            update = {
                it.setCues(playersCues.cues)

                val subtitlesSize = SubtitleView.DEFAULT_TEXT_SIZE_FRACTION * selectedSubtitleSize.scale
                it.setFractionalTextSize(subtitlesSize)
            },
            modifier = Modifier
                .fillMaxSize()
                .focusProperties { canFocus = false },
        )

        // When controls become visible, start a timer to hide them after a delay. If controls are interacted with again, reset the timer.
        LaunchedEffect(controlsVisibleTimestamp) {
            delay(5.seconds)

            if (controlsVisible && !subtitlesControlsVisible && !audioControlsVisible) {
                controlsVisible = false
                subtitlesControlsVisible = false
                audioControlsVisible = false

                controlsPlayFocus.freeFocus()
                controlsProgressFocus.freeFocus()
                controlsFocus.freeFocus()
                subtitlesFocus.freeFocus()
                audioFocus.freeFocus()

                initialFocus.requestFocus()
            }
        }

        // Invisible focusable Box to capture initial focus.
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(16.dp)
                .focusRequester(initialFocus)
                .focusable()
                .focusProperties {
                    up = initialFocus
                    down = initialFocus
                    left = initialFocus
                    right = initialFocus
                }
                .onKeyEvent {
                    val isKeyUp = it.type == KeyEventType.KeyUp
                    val isHorizontal = it.key == Key.DirectionLeft || it.key == Key.DirectionRight
                    val isVertical = it.key == Key.DirectionUp || it.key == Key.DirectionDown

                    if (isKeyUp && (isHorizontal || isVertical)) {
                        controlsVisible = true
                        controlsFocus.requestFocus()
                        when {
                            isHorizontal -> controlsProgressFocus.requestFocus()
                            else -> controlsPlayFocus.requestFocus()
                        }
                        return@onKeyEvent true
                    }

                    false
                }
                .onClick(throttle = false) {
                    controlsVisible = !controlsVisible
                    if (controlsVisible) {
                        controlsFocus.requestFocus()
                        controlsPlayFocus.requestFocus()
                    }
                },
        )

        // UI Controls

        val animatedAlpha by animateFloatAsState(
            targetValue = when (controlsVisible) {
                true -> 1F
                else -> 0f
            },
            animationSpec = tween(100),
            label = "alpha",
        )

        TvPlexPlayerControls(
            player = player,
            title = videoTitle,
            subtitle = videoSubtitle,
            controlsPlayFocus = controlsPlayFocus,
            controlsProgressFocus = controlsProgressFocus,
            onUserInteraction = {
                controlsVisibleTimestamp = currentTimeMillis()
            },
            onSubtitlesClick = {
                controlsVisible = false
                controlsPlayFocus.freeFocus()
                controlsProgressFocus.freeFocus()
                controlsFocus.freeFocus()
                initialFocus.requestFocus()

                subtitlesControlsVisible = true
            },
            onAudioClick = {
                controlsVisible = false
                controlsPlayFocus.freeFocus()
                controlsProgressFocus.freeFocus()
                controlsFocus.freeFocus()
                initialFocus.requestFocus()

                audioControlsVisible = true
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .alpha(animatedAlpha)
                .background(
                    remember {
                        verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.5F),
                            ),
                        )
                    },
                )
                .padding(
                    top = 48.dp,
                    bottom = 16.dp,
                    start = 32.dp,
                    end = 32.dp,
                )
                .focusRequester(controlsFocus)
                .focusable(controlsVisible),
        )

        SubtitlesControlsOverlay(
            visible = subtitlesControlsVisible,
            tracks = subtitlesTracks,
            selectedTrack = selectedSubtitleTrack,
            selectedSize = selectedSubtitleSize,
            onTrackSelect = {
                controlsVisible = false
                subtitlesControlsVisible = false

                controlsPlayFocus.freeFocus()
                controlsProgressFocus.freeFocus()
                controlsFocus.freeFocus()
                subtitlesFocus.freeFocus()

                initialFocus.requestFocus()

                selectedSubtitleTrack = it
                setSubtitleTrack(
                    track = it,
                    player = player,
                )
            },
            onSizeSelect = {
                selectedSubtitleSize = it
            },
            modifier = Modifier
                .focusRequester(subtitlesFocus),
        )

        AudioControlsOverlay(
            visible = audioControlsVisible,
            tracks = audioTracks,
            selectedTrack = selectedAudioTrack,
            onTrackSelect = {
                controlsVisible = false
                audioControlsVisible = false

                controlsPlayFocus.freeFocus()
                controlsProgressFocus.freeFocus()
                controlsFocus.freeFocus()
                audioFocus.freeFocus()

                initialFocus.requestFocus()

                selectedAudioTrack = it
                setAudioTrack(
                    track = it,
                    player = player,
                )
            },
            modifier = Modifier
                .focusRequester(audioFocus),
        )

        if (isBuffering.value) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
                    .focusProperties { canFocus = false },
            )
        }

        if (isError.value != null) {
            Column(
                verticalArrangement = spacedBy(8.dp),
                modifier = Modifier
                    .padding(32.dp)
                    .align(Alignment.TopStart)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(12.dp)
                    .focusProperties { canFocus = false },
            ) {
                Text(
                    text = "Source URL: $videoUrl",
                    color = Color.White,
                )
                Text(
                    text = "${isError.value?.message}",
                    color = Color.White,
                )
                Text(
                    text = "${isError.value?.cause}",
                    color = Color.White,
                )
            }
        }
    }
}

@Composable
private fun SubtitlesControlsOverlay(
    visible: Boolean,
    tracks: ImmutableList<Tracks.Group>,
    selectedTrack: Tracks.Group?,
    selectedSize: SubtitleSize,
    modifier: Modifier = Modifier,
    onTrackSelect: (Tracks.Group?) -> Unit = {},
    onSizeSelect: (SubtitleSize) -> Unit = {},
) {
    AnimatedVisibility(
        visible = visible,
        enter = EnterTransition.None,
        exit = ExitTransition.None,
        modifier = modifier,
    ) {
        if (visible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        horizontalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.9F))),
                    ),
            ) {
                TvPlexPlayerSubtitlesDialog(
                    tracks = tracks,
                    selectedTrack = selectedTrack,
                    selectedSize = selectedSize,
                    onTrackSelect = onTrackSelect,
                    onSizeSelect = onSizeSelect,
                    modifier = Modifier
                        .padding(32.dp)
                        .width(400.dp)
                        .align(Alignment.TopEnd)
                        .animateEnterExit(
                            enter = fadeIn() + slideInHorizontally { it / 10 },
                            exit = ExitTransition.None,
                        ),
                )
            }
        }
    }
}

@Composable
private fun AudioControlsOverlay(
    visible: Boolean,
    tracks: ImmutableList<Tracks.Group>,
    selectedTrack: Tracks.Group?,
    modifier: Modifier = Modifier,
    onTrackSelect: (Tracks.Group?) -> Unit = {},
) {
    AnimatedVisibility(
        visible = visible,
        enter = EnterTransition.None,
        exit = ExitTransition.None,
        modifier = modifier,
    ) {
        if (visible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        horizontalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.9F))),
                    ),
            ) {
                TvPlexPlayerAudioDialog(
                    tracks = tracks,
                    selectedTrack = selectedTrack,
                    onTrackSelect = onTrackSelect,
                    modifier = Modifier
                        .padding(32.dp)
                        .width(400.dp)
                        .align(Alignment.TopEnd)
                        .animateEnterExit(
                            enter = fadeIn() + slideInHorizontally { it / 10 },
                            exit = ExitTransition.None,
                        ),
                )
            }
        }
    }
}

private fun setAudioTrack(
    track: Tracks.Group?,
    player: Player,
) {
    if (track != null) {
        player.trackSelectionParameters = player.trackSelectionParameters
            .buildUpon()
            .setTrackTypeDisabled(C.TRACK_TYPE_AUDIO, false)
            .setOverrideForType(TrackSelectionOverride(track.mediaTrackGroup, 0))
            .build()
    } else {
        player.trackSelectionParameters = player.trackSelectionParameters
            .buildUpon()
            .clearOverridesOfType(C.TRACK_TYPE_AUDIO)
            .setTrackTypeDisabled(C.TRACK_TYPE_AUDIO, false)
            .build()
    }
}

private fun setSubtitleTrack(
    track: Tracks.Group?,
    player: Player,
) {
    if (track != null) {
        player.trackSelectionParameters = player.trackSelectionParameters
            .buildUpon()
            .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
            .setOverrideForType(TrackSelectionOverride(track.mediaTrackGroup, 0))
            .build()
    } else {
        player.trackSelectionParameters = player.trackSelectionParameters
            .buildUpon()
            .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
            .build()
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF131517,
    widthDp = 1920 / 2,
    heightDp = 1080 / 2,
    locale = "us",
)
@Composable
fun Preview() {
    TraktTheme {
        TvPlexPlayerScreen(
            videoUrl = "",
            secondaryVideoUrls = emptyList(),
            videoTitle = "The Matrix (2023)",
            videoSubtitle = "S01E01 - Pilot",
            videoProgress = 33f,
            mediaId = TraktId(0),
            mediaType = MediaType.Movie,
        )
    }
}
