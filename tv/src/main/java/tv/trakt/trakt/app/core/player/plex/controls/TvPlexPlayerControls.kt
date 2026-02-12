package tv.trakt.trakt.app.core.player.plex.controls

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.SliderColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.material3.buttons.PlayPauseButton
import androidx.media3.ui.compose.material3.indicator.DurationText
import androidx.media3.ui.compose.material3.indicator.PositionText
import androidx.media3.ui.compose.material3.indicator.ProgressSlider
import tv.trakt.trakt.app.common.ui.buttons.IconButton
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.resources.R

@UnstableApi
@Composable
internal fun TvPlexPlayerControls(
    player: ExoPlayer,
    title: String,
    subtitle: String?,
    controlsPlayFocus: FocusRequester,
    controlsProgressFocus: FocusRequester,
    onUserInteraction: () -> Unit,
    onSubtitlesClick: () -> Unit,
    onAudioClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val unfocusedColor = Color.White.copy(alpha = 0.35F)
    val focusedColor = Color.White

    var currentFocusId by remember { mutableStateOf<String?>(null) }
    val focusLock = remember { FocusRequester() }

    Column(
        verticalArrangement = spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        Column(
            verticalArrangement = spacedBy(4.dp),
            modifier = Modifier,
        ) {
            // Position and Duration Labels
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 5.dp)
                    .focusProperties { canFocus = false }
                    .focusable(false),
            ) {
                PositionText(
                    player = player,
                    color = Color.White,
                )
                DurationText(
                    player = player,
                    color = Color.White,
                )
            }

            // Progress Slider
            val maxHeight = 30.dp
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(maxHeight)
                    .focusGroup()
                    .focusProperties {
                        up = focusLock
                    },
            ) {
                ProgressSlider(
                    player = player,
                    colors = SliderColors(
                        thumbColor = when (currentFocusId) {
                            "progressSlider" -> focusedColor
                            else -> unfocusedColor
                        },
                        activeTrackColor = focusedColor,
                        inactiveTrackColor = unfocusedColor,
                        activeTickColor = focusedColor,
                        inactiveTickColor = unfocusedColor,
                        disabledThumbColor = Color.White,
                        disabledActiveTrackColor = Color.White,
                        disabledActiveTickColor = Color.White,
                        disabledInactiveTrackColor = Color.White,
                        disabledInactiveTickColor = Color.White,
                    ),
                    onValueChangeFinished = {
                        onUserInteraction()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(
                            when (currentFocusId) {
                                "progressSlider" -> maxHeight
                                else -> 8.dp
                            },
                        )
                        .focusRequester(controlsProgressFocus)
                        .onFocusChanged {
                            currentFocusId = when {
                                it.isFocused -> "progressSlider"
                                else -> null
                            }
                            if (currentFocusId != null) {
                                onUserInteraction()
                            }
                        },
                )
            }
        }

        // Buttons Row
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 1.dp)
                .focusGroup(),
        ) {
            // Title and Subtitle
            Column(
                verticalArrangement = spacedBy(2.dp),
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .weight(1F)
                    .padding(end = 32.dp)
                    .focusProperties { canFocus = false }
                    .focusable(false),
            ) {
                Text(
                    text = title,
                    style = TraktTheme.typography.heading5.copy(
                        fontSize = 18.sp,
                    ),
                    color = focusedColor,
                    maxLines = 1,
                    overflow = Ellipsis,
                )
                if (!subtitle.isNullOrEmpty()) {
                    Text(
                        text = subtitle,
                        style = TraktTheme.typography.paragraph,
                        color = focusedColor,
                        maxLines = 1,
                        overflow = Ellipsis,
                    )
                }
            }

            PlayPauseButton(
                player = player,
                onClick = {
                    if (player.isPlaying) {
                        player.pause()
                    } else {
                        player.play()
                    }
                    onUserInteraction()
                },
                colors = IconButtonColors(
                    containerColor = when (currentFocusId) {
                        "playButton" -> focusedColor
                        else -> unfocusedColor
                    },
                    contentColor = Color.Black,
                    disabledContainerColor = Color.White.copy(alpha = 0.2f),
                    disabledContentColor = Color.Black.copy(alpha = 0.2f),
                ),
                modifier = Modifier
                    .focusRequester(controlsPlayFocus)
                    .focusProperties {
                        left = focusLock
                        down = focusLock
                    }
                    .onFocusChanged {
                        currentFocusId = when {
                            it.isFocused -> "playButton"
                            else -> null
                        }
                        if (currentFocusId != null) {
                            onUserInteraction()
                        }
                    },
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = spacedBy(8.dp, Alignment.End),
                modifier = Modifier
                    .weight(1F)
                    .padding(end = 2.dp),
            ) {
                IconButton(
                    size = IconButtonDefaults.extraSmallContainerSize().height,
                    icon = painterResource(R.drawable.ic_subtitles),
                    iconSize = 18.dp,
                    contentColor = Color.Black,
                    containerColor = when (currentFocusId) {
                        "subtitlesButton" -> focusedColor
                        else -> unfocusedColor
                    },
                    disabledContentColor = Color.Black,
                    disabledContainerColor = unfocusedColor,
                    onClick = onSubtitlesClick,
                    modifier = Modifier
                        .onFocusChanged {
                            currentFocusId = when {
                                it.isFocused -> "subtitlesButton"
                                else -> null
                            }
                            if (currentFocusId != null) {
                                onUserInteraction()
                            }
                        },
                )

                IconButton(
                    size = IconButtonDefaults.extraSmallContainerSize().height,
                    icon = painterResource(R.drawable.ic_audio),
                    iconSize = 18.dp,
                    contentColor = Color.Black,
                    containerColor = when (currentFocusId) {
                        "audioButton" -> focusedColor
                        else -> unfocusedColor
                    },
                    disabledContentColor = Color.Black,
                    disabledContainerColor = unfocusedColor,
                    onClick = onAudioClick,
                    modifier = Modifier
                        .onFocusChanged {
                            currentFocusId = when {
                                it.isFocused -> "audioButton"
                                else -> null
                            }
                            if (currentFocusId != null) {
                                onUserInteraction()
                            }
                        },
                )
            }
        }
    }
}
