@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.summary.soundtrack

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.soundtrack.SoundtrackTrack
import tv.trakt.trakt.core.summary.soundtrack.model.SoundtrackSummary
import tv.trakt.trakt.core.summary.soundtrack.ui.SoundtrackPanel
import tv.trakt.trakt.core.summary.soundtrack.ui.SoundtrackSheet
import tv.trakt.trakt.core.summary.soundtrack.ui.SoundtrackTrackList
import tv.trakt.trakt.core.summary.soundtrack.ui.SoundtrackUpsell
import tv.trakt.trakt.core.summary.soundtrack.ui.previewTrack
import tv.trakt.trakt.core.summary.soundtrack.ui.rememberSoundtrackPlayback
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktSectionHeader
import tv.trakt.trakt.ui.theme.TraktTheme

private const val MAX_ROWS = 5
private const val SOURCE_SUMMARY = "soundtrack-summary"
private const val SOURCE_DRAWER = "soundtrack-drawer"

@Composable
internal fun SoundtrackView(
    viewModel: SoundtrackViewModel,
    title: String,
    posterUrl: String?,
    headerPadding: PaddingValues,
    contentPadding: PaddingValues,
    onVipClick: () -> Unit,
    modifier: Modifier = Modifier,
    currentSeason: Int? = null,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var sheetActive by remember { mutableStateOf(false) }

    SoundtrackContent(
        state = state,
        title = title,
        posterUrl = posterUrl,
        headerPadding = headerPadding,
        contentPadding = contentPadding,
        sheetActive = sheetActive,
        onCollapse = viewModel::setCollapsed,
        onPlay = { viewModel.logPlay(it, SOURCE_SUMMARY) },
        onMoreClick = { sheetActive = true },
        onVipClick = onVipClick,
        onVipSeen = viewModel::logUpsellSeen,
        modifier = modifier,
    )

    SoundtrackSheet(
        active = sheetActive,
        tracks = state.tracks ?: persistentListOf(),
        title = title,
        posterUrl = posterUrl,
        currentSeason = currentSeason,
        onPlay = { viewModel.logPlay(it, SOURCE_DRAWER) },
        onDismiss = { sheetActive = false },
    )
}

@Composable
private fun SoundtrackContent(
    state: SoundtrackState,
    title: String,
    posterUrl: String?,
    modifier: Modifier = Modifier,
    headerPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
    sheetActive: Boolean = false,
    onCollapse: (Boolean) -> Unit = {},
    onPlay: (SoundtrackTrack) -> Unit = {},
    onMoreClick: () -> Unit = {},
    onVipClick: () -> Unit = {},
    onVipSeen: () -> Unit = {},
) {
    if (state.loading != LoadingState.Done) return

    val tracks = state.tracks.orEmpty()
    if (state.isVip && tracks.isEmpty()) return

    var animateCollapse by rememberSaveable { mutableStateOf(false) }
    val hasMore = state.isVip && tracks.size > MAX_ROWS

    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.mainRowHeaderSpace),
        modifier = modifier
            .animateContentSize(
                animationSpec = if (animateCollapse) spring() else snap(),
            ),
    ) {
        TraktSectionHeader(
            title = stringResource(R.string.list_title_soundtrack),
            chevron = hasMore,
            collapsed = state.collapsed,
            onCollapseClick = {
                animateCollapse = true
                onCollapse(!state.collapsed)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(headerPadding)
                .onClick(enabled = hasMore, onClick = onMoreClick),
        )

        if (state.collapsed) return@Column

        val contentModifier = Modifier
            .padding(contentPadding)
            .padding(bottom = TraktTheme.spacing.shadowClipSpace)

        if (state.isVip) {
            SoundtrackBoard(
                tracks = tracks,
                title = title,
                posterUrl = posterUrl,
                paused = sheetActive,
                onPlay = onPlay,
                modifier = contentModifier,
            )
        } else {
            SoundtrackUpsell(
                onVipClick = onVipClick,
                onVipSeen = onVipSeen,
                modifier = contentModifier,
            )
        }
    }
}

@Composable
private fun SoundtrackBoard(
    tracks: List<SoundtrackTrack>,
    title: String,
    posterUrl: String?,
    paused: Boolean,
    onPlay: (SoundtrackTrack) -> Unit,
    modifier: Modifier = Modifier,
) {
    val playback = rememberSoundtrackPlayback()
    // The summary describes the whole soundtrack, not only the rows on screen.
    val summary = remember(tracks) { SoundtrackSummary.from(tracks) }
    val visibleTracks = remember(tracks) { tracks.take(MAX_ROWS).toImmutableList() }

    LaunchedEffect(paused) {
        if (paused) playback.stop()
    }

    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.mainRowSpace),
        modifier = modifier,
    ) {
        if (summary.playable > 0) {
            SoundtrackPanel(
                playback = playback,
                summary = summary,
                title = title,
                posterUrl = posterUrl,
            )
        }

        SoundtrackTrackList(
            tracks = visibleTracks,
            playback = playback,
            onPlayClick = {
                playback.play(it)
                onPlay(it)
            },
        )
    }
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        SoundtrackBoard(
            tracks = List(7) { previewTrack(spotifyId = if (it % 3 == 0) null else "abc", position = it) },
            title = "The Social Network",
            posterUrl = null,
            paused = false,
            onPlay = {},
        )
    }
}

@PreviewTraktLightDark
@Composable
private fun PreviewUpsell() {
    TraktThemePreview {
        SoundtrackContent(
            state = SoundtrackState(loading = LoadingState.Done),
            title = "The Social Network",
            posterUrl = null,
        )
    }
}
