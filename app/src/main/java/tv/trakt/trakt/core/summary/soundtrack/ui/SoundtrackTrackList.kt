package tv.trakt.trakt.core.summary.soundtrack.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.model.soundtrack.SoundtrackTrack
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.ui.theme.TraktTheme

internal val SoundtrackListShape = RoundedCornerShape(16.dp)

/** A short, non-scrolling tracklist card. */
@Composable
internal fun SoundtrackTrackList(
    tracks: ImmutableList<SoundtrackTrack>,
    playback: SoundtrackPlayback,
    modifier: Modifier = Modifier,
    onPlayClick: (SoundtrackTrack) -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = TraktTheme.colors.shadowSmall,
                shape = SoundtrackListShape,
            )
            .clip(SoundtrackListShape)
            .background(TraktTheme.colors.panelCardContainer),
    ) {
        tracks.forEachIndexed { index, track ->
            if (index > 0) {
                SoundtrackRowSeparator()
            }
            SoundtrackTrackRow(
                track = track,
                number = index + 1,
                selected = playback.isSelected(track),
                playing = playback.isPlaying(track),
                onPlayClick = onPlayClick,
            )
        }
    }
}

@Composable
internal fun SoundtrackRowSeparator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(TraktTheme.colors.separator),
    )
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        SoundtrackTrackList(
            tracks = List(4) { previewTrack(spotifyId = if (it == 2) null else "abc", position = it) }
                .toImmutableList(),
            playback = rememberSoundtrackPlayback(),
        )
    }
}
