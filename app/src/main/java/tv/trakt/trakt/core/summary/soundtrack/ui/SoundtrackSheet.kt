@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.summary.soundtrack.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue.Expanded
import androidx.compose.material3.SheetValue.Hidden
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.model.soundtrack.SoundtrackTrack
import tv.trakt.trakt.core.summary.soundtrack.model.SoundtrackSummary
import tv.trakt.trakt.core.summary.soundtrack.model.filterBySeason
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktBottomSheet
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.switch.TraktSwitch
import tv.trakt.trakt.ui.theme.TraktTheme

private const val SHEET_MAX_HEIGHT_FRACTION = 0.85F

@Composable
internal fun SoundtrackSheet(
    active: Boolean,
    tracks: ImmutableList<SoundtrackTrack>,
    title: String,
    posterUrl: String?,
    currentSeason: Int?,
    onPlay: (SoundtrackTrack) -> Unit,
    onDismiss: () -> Unit,
    state: SheetState = rememberBottomSheetState(
        initialValue = Hidden,
        enabledValues = setOf(Hidden, Expanded),
    ),
) {
    if (!active) return

    TraktBottomSheet(
        sheetState = state,
        onDismiss = onDismiss,
    ) {
        SoundtrackSheetContent(
            tracks = tracks,
            title = title,
            posterUrl = posterUrl,
            currentSeason = currentSeason,
            onPlay = onPlay,
            modifier = Modifier
                .heightIn(max = LocalWindowInfo.current.containerDpSize.height * SHEET_MAX_HEIGHT_FRACTION),
        )
    }
}

@Composable
private fun SoundtrackSheetContent(
    tracks: ImmutableList<SoundtrackTrack>,
    title: String,
    posterUrl: String?,
    currentSeason: Int?,
    onPlay: (SoundtrackTrack) -> Unit,
    modifier: Modifier = Modifier,
) {
    val playback = rememberSoundtrackPlayback()
    var seasonOnly by rememberSaveable { mutableStateOf(false) }

    val hasSeasonTracks = remember(tracks, currentSeason) {
        currentSeason != null && tracks.any { it.season == currentSeason }
    }
    val shownTracks = remember(tracks, currentSeason, hasSeasonTracks, seasonOnly) {
        tracks.filterBySeason(if (hasSeasonTracks && seasonOnly) currentSeason else null)
    }
    val summary = remember(shownTracks) { SoundtrackSummary.from(shownTracks) }

    Column(
        verticalArrangement = spacedBy(16.dp),
        modifier = modifier.padding(bottom = 24.dp),
    ) {
        TraktHeader(
            title = stringResource(R.string.list_title_soundtrack),
            subtitle = stringResource(R.string.text_soundtrack_playable_ratio, summary.playable, summary.total),
            modifier = Modifier.padding(horizontal = 24.dp),
        )

        if (hasSeasonTracks && currentSeason != null) {
            Row(
                horizontalArrangement = spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
            ) {
                Text(
                    text = stringResource(R.string.switch_label_soundtrack_season_only, currentSeason),
                    color = TraktTheme.colors.textSecondary,
                    style = TraktTheme.typography.paragraphSmall,
                    modifier = Modifier.weight(1F),
                )
                TraktSwitch(
                    checked = seasonOnly,
                    onCheckedChange = { seasonOnly = it },
                )
            }
        }

        if (summary.playable > 0) {
            SoundtrackPanel(
                playback = playback,
                summary = summary,
                title = title,
                posterUrl = posterUrl,
                modifier = Modifier.padding(horizontal = 24.dp),
            )
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 24.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            itemsIndexed(
                items = shownTracks,
                key = { _, track -> track.position },
            ) { index, track ->
                Column(
                    modifier = Modifier
                        .clip(rowShape(index = index, last = shownTracks.lastIndex))
                        .background(TraktTheme.colors.panelCardContainer),
                ) {
                    if (index > 0) {
                        SoundtrackRowSeparator()
                    }
                    SoundtrackTrackRow(
                        track = track,
                        number = index + 1,
                        selected = playback.isSelected(track),
                        playing = playback.isPlaying(track),
                        onPlayClick = {
                            playback.play(it)
                            onPlay(it)
                        },
                    )
                }
            }
        }
    }
}

private fun rowShape(
    index: Int,
    last: Int,
) = when {
    index == 0 && last == 0 -> SoundtrackListShape
    index == 0 -> SoundtrackListShape.copy(bottomStart = ZeroCorner, bottomEnd = ZeroCorner)
    index == last -> SoundtrackListShape.copy(topStart = ZeroCorner, topEnd = ZeroCorner)
    else -> RectangleShape
}

private val ZeroCorner = CornerSize(0.dp)
