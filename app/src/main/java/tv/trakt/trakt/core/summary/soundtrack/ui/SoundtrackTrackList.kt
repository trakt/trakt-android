package tv.trakt.trakt.core.summary.soundtrack.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ListItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.model.soundtrack.SoundtrackTrack
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.ui.theme.TraktTheme

internal val SoundtrackListShape = RoundedCornerShape(16.dp)
internal val SegmentInnerCorner = CornerSize(4.dp)

internal fun segmentShape(
    index: Int,
    last: Int,
    innerCorner: CornerSize,
): Shape =
    when {
        index == 0 && last == 0 -> SoundtrackListShape
        index == 0 -> SoundtrackListShape.copy(bottomStart = innerCorner, bottomEnd = innerCorner)
        index == last -> SoundtrackListShape.copy(topStart = innerCorner, topEnd = innerCorner)
        else -> RoundedCornerShape(innerCorner)
    }

@Composable
internal fun SoundtrackTrackList(
    tracks: ImmutableList<SoundtrackTrack>,
    playback: SoundtrackPlayback,
    modifier: Modifier = Modifier,
    onPlayClick: (SoundtrackTrack) -> Unit = {},
) {
    Column(
        verticalArrangement = spacedBy(ListItemDefaults.SegmentedGap),
        modifier = modifier.fillMaxWidth(),
    ) {
        tracks.forEachIndexed { index, track ->
            SoundtrackSegmentRow(
                track = track,
                index = index,
                last = tracks.lastIndex,
                playback = playback,
                onPlayClick = onPlayClick,
            )
        }
    }
}

@Composable
internal fun SoundtrackSegmentRow(
    track: SoundtrackTrack,
    index: Int,
    last: Int,
    playback: SoundtrackPlayback,
    modifier: Modifier = Modifier,
    onPlayClick: (SoundtrackTrack) -> Unit = {},
) {
    val shape = segmentShape(index = index, last = last, innerCorner = SegmentInnerCorner)
    SoundtrackTrackRow(
        track = track,
        number = index + 1,
        playing = playback.isPlaying(track),
        loading = playback.isLoading(track),
        onPlayClick = onPlayClick,
        modifier = modifier
            .shadow(TraktTheme.colors.shadowSmall, shape)
            .clip(shape)
            .background(TraktTheme.colors.customListContainer)
            .padding(horizontal = 16.dp),
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
