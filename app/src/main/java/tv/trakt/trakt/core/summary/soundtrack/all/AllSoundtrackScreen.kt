package tv.trakt.trakt.core.summary.soundtrack.all

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.soundtrack.SoundtrackTrack
import tv.trakt.trakt.core.summary.soundtrack.model.SoundtrackSummary
import tv.trakt.trakt.core.summary.soundtrack.model.filterBySeason
import tv.trakt.trakt.core.summary.soundtrack.ui.SoundtrackPanel
import tv.trakt.trakt.core.summary.soundtrack.ui.SoundtrackPanelSize
import tv.trakt.trakt.core.summary.soundtrack.ui.SoundtrackSegmentRow
import tv.trakt.trakt.core.summary.soundtrack.ui.SoundtrackSkeleton
import tv.trakt.trakt.core.summary.soundtrack.ui.previewTrack
import tv.trakt.trakt.core.summary.soundtrack.ui.rememberSoundtrackPlayback
import tv.trakt.trakt.helpers.SimpleScrollConnection
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.EmptyListCard
import tv.trakt.trakt.ui.components.EmptyVerticalPanelHeight
import tv.trakt.trakt.ui.components.ScrollableBackdropImage
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.switch.TraktSwitch
import tv.trakt.trakt.ui.theme.TraktTheme

private const val SKELETON_ROWS = 8
private val PanelSpace = 10.dp

@Composable
internal fun AllSoundtrackScreen(
    viewModel: AllSoundtrackViewModel,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    AllSoundtrackContent(
        state = state,
        onPlay = viewModel::logPlay,
        onBackClick = onNavigateBack,
    )
}

@Composable
private fun AllSoundtrackContent(
    state: AllSoundtrackState,
    modifier: Modifier = Modifier,
    onPlay: (SoundtrackTrack) -> Unit = {},
    onBackClick: () -> Unit = {},
) {
    val listState = rememberLazyListState()
    val listScrollConnection = rememberSaveable(saver = SimpleScrollConnection.Saver) {
        SimpleScrollConnection()
    }

    val playback = rememberSoundtrackPlayback()
    var seasonOnly by rememberSaveable { mutableStateOf(false) }

    val tracks = state.tracks.orEmpty()
    val currentSeason = state.currentSeason
    val hasSeasonTracks = remember(tracks, currentSeason) {
        val seasons = tracks.mapNotNullTo(mutableSetOf()) { it.season }
        currentSeason in seasons && seasons.size > 1
    }
    val shownTracks = remember(tracks, currentSeason, hasSeasonTracks, seasonOnly) {
        tracks.filterBySeason(if (hasSeasonTracks && seasonOnly) currentSeason else null).toImmutableList()
    }
    val summary = remember(shownTracks) { SoundtrackSummary.from(shownTracks) }

    LaunchedEffect(shownTracks) {
        val selected = playback.selected ?: return@LaunchedEffect
        if (shownTracks.none { it.position == selected.position }) {
            playback.stop()
        }
    }

    val contentPadding = PaddingValues(
        start = TraktTheme.spacing.mainPageHorizontalSpace,
        end = TraktTheme.spacing.mainPageHorizontalSpace,
        top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
        bottom = WindowInsets.navigationBars.asPaddingValues()
            .calculateBottomPadding()
            .plus(TraktTheme.spacing.mainPageBottomSpace),
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary)
            .nestedScroll(listScrollConnection),
    ) {
        ScrollableBackdropImage(
            imageUrl = state.backgroundUrl,
            translation = listScrollConnection.resultOffset,
        )

        LazyColumn(
            state = listState,
            verticalArrangement = spacedBy(ListItemDefaults.SegmentedGap),
            contentPadding = contentPadding,
            overscrollEffect = null,
        ) {
            item(key = "header") {
                TitleBar(
                    subtitle = state.mediaTitle,
                    onBackClick = onBackClick,
                    modifier = Modifier.padding(bottom = 2.dp),
                ) {
                    if (state.loading == Done && hasSeasonTracks && currentSeason != null) {
                        SeasonOnlySwitch(
                            season = currentSeason,
                            checked = seasonOnly,
                            onCheckedChange = { seasonOnly = it },
                        )
                    }
                }
            }

            when {
                state.loading != Done -> {
                    item(key = "loading") {
                        SoundtrackSkeleton(
                            rows = SKELETON_ROWS,
                            panelSize = SoundtrackPanelSize.Full,
                        )
                    }
                }

                state.error != null -> {
                    item(key = "error") {
                        ErrorView(error = state.error)
                    }
                }

                shownTracks.isEmpty() -> {
                    item(key = "empty") {
                        EmptyListCard(modifier = Modifier.height(EmptyVerticalPanelHeight))
                    }
                }

                else -> {
                    if (summary.playable > 0) {
                        item(key = "panel") {
                            SoundtrackPanel(
                                playback = playback,
                                summary = summary,
                                title = state.mediaTitle,
                                posterUrl = state.posterUrl,
                                size = SoundtrackPanelSize.Full,
                                modifier = Modifier.padding(bottom = PanelSpace),
                            )
                        }
                    }

                    itemsIndexed(
                        items = shownTracks,
                        key = { _, track -> track.position },
                    ) { index, track ->
                        SoundtrackSegmentRow(
                            track = track,
                            index = index,
                            last = shownTracks.lastIndex,
                            playback = playback,
                            searchTitle = state.mediaTitle,
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
}

@Composable
private fun TitleBar(
    subtitle: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: @Composable () -> Unit = {},
) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(TraktTheme.size.titleBarHeight),
    ) {
        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = spacedBy(12.dp),
            modifier = Modifier
                .weight(1F)
                .graphicsLayer { translationX = -2.dp.toPx() }
                .onClick(onClick = onBackClick),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_back_arrow),
                tint = TraktTheme.colors.textPrimary,
                contentDescription = null,
            )
            TraktHeader(
                title = stringResource(R.string.list_title_soundtrack),
                subtitle = subtitle,
            )
        }
        trailing()
    }
}

@Composable
private fun SeasonOnlySwitch(
    season: Int,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        horizontalArrangement = spacedBy(8.dp),
        verticalAlignment = CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.switch_label_soundtrack_season_only, season),
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.meta,
            maxLines = 1,
        )
        TraktSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
private fun ErrorView(
    error: Exception,
    modifier: Modifier = Modifier,
) {
    Text(
        text = "${stringResource(R.string.error_text_unexpected_error_short)}\n\n$error",
        color = TraktTheme.colors.textSecondary,
        style = TraktTheme.typography.meta,
        maxLines = 10,
        modifier = modifier,
    )
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        AllSoundtrackContent(
            state = AllSoundtrackState(
                mediaTitle = "The Social Network",
                currentSeason = 1,
                tracks = List(8) { previewTrack(spotifyId = if (it % 3 == 0) null else "abc", position = it) }
                    .toImmutableList(),
                loading = Done,
            ),
        )
    }
}

@PreviewTraktLightDark
@Composable
private fun PreviewLoading() {
    TraktThemePreview {
        AllSoundtrackContent(
            state = AllSoundtrackState(
                mediaTitle = "The Social Network",
                loading = LoadingState.Loading,
            ),
        )
    }
}
