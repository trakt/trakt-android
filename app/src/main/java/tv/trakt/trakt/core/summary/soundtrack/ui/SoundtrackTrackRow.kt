package tv.trakt.trakt.core.summary.soundtrack.ui

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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.soundtrack.SoundtrackTrack
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

private val RowMinHeight = 56.dp
private val ActionSize = 28.dp
private val ActionIconSize = 14.dp
private const val SELECTED_ALPHA = 0.12F

@Composable
internal fun SoundtrackTrackRow(
    track: SoundtrackTrack,
    number: Int,
    selected: Boolean,
    playing: Boolean,
    modifier: Modifier = Modifier,
    onPlayClick: (SoundtrackTrack) -> Unit = {},
) {
    Row(
        horizontalArrangement = spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = RowMinHeight)
            .background(
                if (selected) {
                    TraktTheme.colors.accent.copy(
                        alpha = SELECTED_ALPHA,
                    )
                } else {
                    TraktTheme.colors.panelCardContainer
                },
            )
            .onClick(enabled = track.isPlayable) { onPlayClick(track) }
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            text = number.toString(),
            color = TraktTheme.colors.textSecondary,
            style = TraktTheme.typography.meta,
            textAlign = TextAlign.End,
            modifier = Modifier.widthIn(min = 20.dp),
        )

        Column(
            verticalArrangement = spacedBy(2.dp),
            modifier = Modifier.weight(1F),
        ) {
            Text(
                text = track.title,
                color = if (track.isPlayable) TraktTheme.colors.textPrimary else TraktTheme.colors.textSecondary,
                style = TraktTheme.typography.paragraphSmall.copy(
                    fontWeight = if (track.isPlayable) FontWeight.W600 else FontWeight.Normal,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            track.performer?.let { performer ->
                Text(
                    text = performer,
                    color = TraktTheme.colors.textSecondary,
                    style = TraktTheme.typography.meta,
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
                    .background(TraktTheme.colors.accent, CircleShape),
            ) {
                Icon(
                    painter = painterResource(if (playing) R.drawable.ic_pause else R.drawable.ic_play),
                    contentDescription = null,
                    tint = TraktTheme.colors.textPrimaryOnAccent,
                    modifier = Modifier.size(ActionIconSize),
                )
            }
        } else {
            Text(
                text = "-",
                color = TraktTheme.colors.textSecondary,
                style = TraktTheme.typography.paragraphSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(min = ActionSize),
            )
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
                selected = true,
                playing = true,
            )
            SoundtrackTrackRow(
                track = previewTrack(spotifyId = "abc"),
                number = 2,
                selected = false,
                playing = false,
            )
            SoundtrackTrackRow(
                track = previewTrack(spotifyId = null),
                number = 3,
                selected = false,
                playing = false,
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
