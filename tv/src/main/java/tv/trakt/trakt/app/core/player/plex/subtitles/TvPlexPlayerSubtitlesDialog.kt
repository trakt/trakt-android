package tv.trakt.trakt.app.core.player.plex.subtitles

import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import tv.trakt.trakt.app.core.player.plex.subtitles.model.SubtitleSize
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.resources.R
import java.util.Locale

@Composable
internal fun TvPlexPlayerSubtitlesDialog(
    tracks: ImmutableList<Tracks.Group>,
    selectedTrack: Tracks.Group?,
    selectedSize: SubtitleSize,
    modifier: Modifier = Modifier,
    onTrackSelect: (Tracks.Group?) -> Unit = {},
    onSizeSelect: (SubtitleSize) -> Unit = {},
) {
    TvPlexPlayerSubtitlesContent(
        tracks = tracks,
        selectedTrack = selectedTrack,
        selectedSize = selectedSize,
        onTrackSelect = onTrackSelect,
        onSizeSelect = onSizeSelect,
        modifier = modifier,
    )
}

@Composable
private fun TvPlexPlayerSubtitlesContent(
    tracks: ImmutableList<Tracks.Group>?,
    selectedTrack: Tracks.Group?,
    selectedSize: SubtitleSize,
    modifier: Modifier = Modifier,
    onTrackSelect: (Tracks.Group?) -> Unit = {},
    onSizeSelect: (SubtitleSize) -> Unit = {},
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
            text = stringResource(R.string.header_player_subtitle),
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.heading4,
            modifier = Modifier
                .padding(bottom = 22.dp)
                .focusProperties { canFocus = false },
        )

        Text(
            text = stringResource(R.string.header_player_subtitle_size).uppercase(),
            color = TraktTheme.colors.textSecondary,
            style = TraktTheme.typography.heading6,
            modifier = Modifier
                .padding(bottom = 8.dp)
                .focusProperties { canFocus = false },
        )

        Row(
            horizontalArrangement = spacedBy(8.dp),
            modifier = Modifier
                .padding(bottom = 24.dp)
                .focusGroup()
                .focusProperties {
                    up = focusLock
                },
        ) {
            val entries = SubtitleSize.entries
            entries.forEachIndexed { index, size ->
                PrimaryButton(
                    text = when (size) {
                        SubtitleSize.DEFAULT -> "1x"
                        else -> "${size.scale}x"
                    },
                    textAllCaps = false,
                    onClick = {
                        onSizeSelect(size)
                    },
                    contentColor = when (selectedSize == size) {
                        true -> Color.Black
                        else -> TraktTheme.colors.textPrimary
                    },
                    containerColor = when (selectedSize == size) {
                        true -> Color.White
                        else -> TraktTheme.colors.commentReplyContainer
                    },
                    borderColor = TraktTheme.colors.accent,
                    modifier = Modifier
                        .weight(1F)
                        .focusProperties {
                            when (index) {
                                0 -> left = focusLock
                                entries.lastIndex -> right = focusLock
                            }
                        },
                )
            }
        }

        Text(
            text = stringResource(R.string.header_player_subtitle_language).uppercase(),
            color = TraktTheme.colors.textSecondary,
            style = TraktTheme.typography.heading6,
            modifier = Modifier
                .focusProperties { canFocus = false },
        )

        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = spacedBy(8.dp),
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .focusGroup()
                .focusProperties {
                    left = focusLock
                    right = focusLock
                },
        ) {
            PrimaryButton(
                text = stringResource(
                    when {
                        tracks.isNullOrEmpty() -> R.string.text_unavailable
                        else -> R.string.button_text_player_subtitles_off
                    },
                ),
                onClick = { onTrackSelect(null) },
                contentColor = when (selectedTrack) {
                    null -> Color.Black
                    else -> TraktTheme.colors.textPrimary
                },
                containerColor = when (selectedTrack) {
                    null -> Color.White
                    else -> TraktTheme.colors.commentReplyContainer
                },
                borderColor = TraktTheme.colors.accent,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .focusProperties {
                        if (tracks.isNullOrEmpty()) {
                            down = focusLock
                        }
                    },
            )

            (tracks ?: EmptyImmutableList)
                .forEachIndexed { index, track ->
                    val format = track.getTrackFormat(0)
                    SubtitleItem(
                        format = format,
                        isSelected = format.id == selectedTrackFormat,
                        onClick = {
                            onTrackSelect(track)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusProperties {
                                if (index == tracks?.lastIndex) {
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
private fun SubtitleItem(
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
fun CommentDetailsPreview() {
    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.LightGray.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            Column(
                verticalArrangement = spacedBy(32.dp),
            ) {
                TvPlexPlayerSubtitlesContent(
                    tracks = persistentListOf(
                        Tracks.Group(
                            TrackGroup(
                                Format.Builder()
                                    .setId(1)
                                    .setLabel("Forced")
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
                                    .setLabel("SDH")
                                    .setLanguage("pl")
                                    .build(),
                            ),
                            false,
                            intArrayOf(1),
                            booleanArrayOf(false),
                        ),
                    ),
                    selectedTrack = null,
                    selectedSize = SubtitleSize.DEFAULT,
                )
            }
        }
    }
}
