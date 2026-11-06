package tv.trakt.trakt.core.summary.soundtrack

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.soundtrack.SoundtrackTrack
import tv.trakt.trakt.core.summary.soundtrack.model.SoundtrackSummary
import tv.trakt.trakt.core.summary.soundtrack.ui.SoundtrackPanel
import tv.trakt.trakt.core.summary.soundtrack.ui.SoundtrackSkeleton
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

@Composable
internal fun SoundtrackView(
    viewModel: SoundtrackViewModel,
    title: String,
    posterUrl: String?,
    headerPadding: PaddingValues,
    contentPadding: PaddingValues,
    onVipClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    SoundtrackContent(
        state = state,
        title = title,
        posterUrl = posterUrl,
        headerPadding = headerPadding,
        contentPadding = contentPadding,
        onCollapse = viewModel::setCollapsed,
        onPlay = { viewModel.logPlay(it, SOURCE_SUMMARY) },
        onMoreClick = onMoreClick,
        onVipClick = onVipClick,
        onVipSeen = viewModel::logUpsellSeen,
        modifier = modifier,
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
    onCollapse: (Boolean) -> Unit = {},
    onPlay: (SoundtrackTrack) -> Unit = {},
    onMoreClick: () -> Unit = {},
    onVipClick: () -> Unit = {},
    onVipSeen: () -> Unit = {},
) {
    val tracks = state.tracks.orEmpty()
    if (state.loading.isDone && state.isVip && tracks.isEmpty()) return

    var animateCollapse by rememberSaveable { mutableStateOf(false) }
    val hasMore = state.loading.isDone && state.isVip && tracks.size > MAX_ROWS

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

        Crossfade(
            targetState = state.loading,
            animationSpec = tween(200),
        ) { loading ->
            when (loading) {
                Idle, Loading -> {
                    SoundtrackSkeleton(
                        rows = MAX_ROWS,
                        visible = loading.isLoading,
                        modifier = contentModifier,
                    )
                }

                Done -> {
                    if (state.isVip) {
                        SoundtrackBoard(
                            tracks = tracks,
                            title = title,
                            posterUrl = posterUrl,
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
        }
    }
}

@Composable
private fun SoundtrackBoard(
    tracks: List<SoundtrackTrack>,
    title: String,
    posterUrl: String?,
    onPlay: (SoundtrackTrack) -> Unit,
    modifier: Modifier = Modifier,
) {
    val playback = rememberSoundtrackPlayback()
    val summary = remember(tracks) { SoundtrackSummary.from(tracks) }
    val visibleTracks = remember(tracks) { tracks.take(MAX_ROWS).toImmutableList() }

    Column(
        verticalArrangement = spacedBy(10.dp),
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

@PreviewTraktLightDark
@Composable
private fun PreviewLoading() {
    TraktThemePreview {
        SoundtrackContent(
            state = SoundtrackState(loading = LoadingState.Loading),
            title = "The Social Network",
            posterUrl = null,
        )
    }
}
