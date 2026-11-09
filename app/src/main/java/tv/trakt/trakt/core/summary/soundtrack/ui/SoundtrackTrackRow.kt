package tv.trakt.trakt.core.summary.soundtrack.ui

import android.content.ClipData
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import tv.trakt.trakt.LocalSnackbarState
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.soundtrack.SoundtrackTrack
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

internal val RowMinHeight = 56.dp
internal val ActionSize = 28.dp
private val ActionIconSize = 16.dp

@Composable
internal fun SoundtrackTrackRow(
    track: SoundtrackTrack,
    number: Int,
    playing: Boolean,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    onPlayClick: (SoundtrackTrack) -> Unit = {},
) {
    val copyTrack = rememberCopyTrack(track)

    Row(
        horizontalArrangement = spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = RowMinHeight)
            .background(TraktTheme.colors.customListContainer),
    ) {
        Text(
            text = number.toString(),
            color = TraktTheme.colors.textSecondary,
            style = TraktTheme.typography.cardTitle,
            textAlign = TextAlign.End,
        )

        Column(
            verticalArrangement = spacedBy(2.dp),
            modifier = Modifier
                .weight(1F)
                .onClick(onClick = copyTrack)
                .padding(end = 16.dp),
        ) {
            Text(
                text = track.title,
                color = if (track.isPlayable) TraktTheme.colors.textPrimary else TraktTheme.colors.textSecondary,
                style = TraktTheme.typography.cardTitle.copy(
                    fontSize = 13.sp,
                    fontWeight = if (track.isPlayable) FontWeight.W500 else FontWeight.W400,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            track.performer?.let { performer ->
                Text(
                    text = performer,
                    color = TraktTheme.colors.textSecondary,
                    style = TraktTheme.typography.cardSubtitle.copy(
                        fontSize = 11.sp,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        if (track.isPlayable) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(ActionSize)
                    .background(TraktTheme.colors.accent, CircleShape)
                    .onClick(enabled = track.isPlayable) { onPlayClick(track) },
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        color = TraktTheme.colors.textPrimaryOnAccent,
                        strokeWidth = 1.5.dp,
                        modifier = Modifier.size(ActionIconSize / 1.5F),
                    )
                } else if (playing) {
                    Icon(
                        painter = painterResource(R.drawable.ic_pause),
                        contentDescription = null,
                        tint = TraktTheme.colors.textPrimaryOnAccent,
                        modifier = Modifier.size(ActionIconSize),
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.ic_play_round),
                        contentDescription = null,
                        tint = TraktTheme.colors.textPrimaryOnAccent,
                        modifier = Modifier.size(ActionIconSize + 3.dp),
                    )
                }
            }
        } else {
            Text(
                text = "-",
                color = TraktTheme.colors.textSecondary,
                style = TraktTheme.typography.meta,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(min = ActionSize),
            )
        }
    }
}

@Composable
private fun rememberCopyTrack(track: SoundtrackTrack): () -> Unit {
    val clipboard = LocalClipboard.current
    val snackbar = LocalSnackbarState.current
    val scope = rememberCoroutineScope()
    val copiedLabel = stringResource(R.string.button_label_copied)

    return {
        val text = listOfNotNull(track.performer, track.title).joinToString(" - ")
        scope.launch {
            clipboard.setClipEntry(ClipEntry(ClipData.newPlainText(text, text)))
            // Android 13+ shows its own clipboard confirmation overlay.
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                snackbar.showSnackbar(
                    message = copiedLabel,
                    duration = SnackbarDuration.Short,
                )
            }
        }
    }
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        Column {
            SoundtrackTrackRow(
                track = previewTrack(spotifyId = "abc"),
                number = 1,
                playing = true,
            )
            SoundtrackTrackRow(
                track = previewTrack(spotifyId = "abc"),
                number = 2,
                playing = false,
            )
            SoundtrackTrackRow(
                track = previewTrack(spotifyId = null),
                number = 3,
                playing = false,
            )
            SoundtrackTrackRow(
                track = previewTrack(spotifyId = "abc"),
                number = 4,
                playing = false,
                loading = true,
            )
        }
    }
}

internal fun previewTrack(
    spotifyId: String?,
    position: Int = 0,
) = SoundtrackTrack(
    title = "Hurt",
    performer = "Johnny Cash",
    spotifyId = spotifyId,
    matchedOn = "exact",
    position = position,
    season = null,
)
