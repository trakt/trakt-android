package tv.trakt.trakt.app.core.player.plex.audio

import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.media3.common.Format
import androidx.media3.common.TrackGroup
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.tv.material3.Text
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.app.common.ui.buttons.PrimaryButton
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.resources.R
import java.util.Locale

@Composable
internal fun TvPlexPlayerAudioDialog(
    tracks: ImmutableList<Tracks.Group>,
    selectedTrack: Tracks.Group?,
    modifier: Modifier = Modifier,
    onTrackSelect: (Tracks.Group?) -> Unit = {},
) {
    TvPlexPlayerAudioContent(
        tracks = tracks,
        selectedTrack = selectedTrack,
        onTrackSelect = onTrackSelect,
        modifier = modifier,
    )
}

@Composable
private fun TvPlexPlayerAudioContent(
    tracks: ImmutableList<Tracks.Group>?,
    selectedTrack: Tracks.Group?,
    modifier: Modifier = Modifier,
    onTrackSelect: (Tracks.Group?) -> Unit = {},
) {
    val focusRequester = remember { FocusRequester() }
    val focusLock = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    val selectedTrackFormat = remember(selectedTrack) {
        selectedTrack?.getTrackFormat(0)?.id
    }

    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = spacedBy(0.dp),
        modifier = modifier
            .fillMaxSize()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(24.dp),
            )
            .clip(RoundedCornerShape(24.dp))
            .background(TraktTheme.colors.commentContainer)
            .padding(top = 20.dp)
            .padding(horizontal = 20.dp),
    ) {
        Text(
            text = stringResource(R.string.header_player_audio),
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.heading4,
            modifier = Modifier
                .padding(bottom = 8.dp)
                .focusProperties { canFocus = false },
        )

        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = spacedBy(8.dp),
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(top = 8.dp, bottom = 16.dp)
                .focusGroup()
                .focusRequester(focusRequester)
                .focusProperties {
                    left = focusLock
                    right = focusLock
                },
        ) {
            val allTracks = tracks ?: EmptyImmutableList
            allTracks
                .forEachIndexed { index, track ->
                    val format = track.getTrackFormat(0)
                    AudioItem(
                        format = format,
                        isSelected = format.id == selectedTrackFormat,
                        onClick = {
                            onTrackSelect(track)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusProperties {
                                left = focusLock
                                right = focusLock

                                if (index == 0) {
                                    up = focusLock
                                }

                                if (index == allTracks.lastIndex) {
                                    down = focusLock
                                }
                            },
                    )
                }

            Spacer(
                modifier = Modifier
                    .height(16.dp)
                    .focusProperties { canFocus = false },
            )
        }
    }
}

@Composable
private fun AudioItem(
    format: Format,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = remember(format) {
        val languageDisplayText = format.language
            ?.let { Locale(it, "").displayName }
            ?: "Unknown"

        buildString {
            append(languageDisplayText)
            if (!format.label.isNullOrBlank()) {
                append("  (${format.label})")
            }
        }
    }

    PrimaryButton(
        text = label,
        onClick = onClick,
        contentColor = when (isSelected) {
            true -> Color.Black
            else -> TraktTheme.colors.textPrimary
        },
        containerColor = when (isSelected) {
            true -> Color.White
            else -> TraktTheme.colors.commentReplyContainer
        },
        borderColor = TraktTheme.colors.accent,
        modifier = modifier,
    )
}

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalCoilApi::class)
@Preview(heightDp = 600, widthDp = 380)
@Composable
fun AudioDialogPreview() {
    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.LightGray.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            Column(
                verticalArrangement = spacedBy(32.dp),
            ) {
                TvPlexPlayerAudioContent(
                    tracks = persistentListOf(
                        Tracks.Group(
                            TrackGroup(
                                Format.Builder()
                                    .setId(1)
                                    .setLabel("5.1")
                                    .setLanguage("en")
                                    .build(),
                            ),
                            false,
                            intArrayOf(1),
                            booleanArrayOf(false),
                        ),
                        Tracks.Group(
                            TrackGroup(
                                Format.Builder()
                                    .setId(2)
                                    .setLabel("Stereo")
                                    .setLanguage("pl")
                                    .build(),
                            ),
                            false,
                            intArrayOf(1),
                            booleanArrayOf(false),
                        ),
                    ),
                    selectedTrack = null,
                )
            }
        }
    }
}
