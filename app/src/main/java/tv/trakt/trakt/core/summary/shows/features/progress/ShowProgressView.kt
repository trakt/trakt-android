@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.summary.shows.features.progress

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.SpaceBetween
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.flow.first
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.rememberPercentFormat
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.ui.theme.colors.Red500
import tv.trakt.trakt.core.summary.shows.features.progress.model.SeasonProgress
import tv.trakt.trakt.core.summary.shows.features.progress.ui.SeasonProgressRow
import tv.trakt.trakt.core.summary.shows.features.progress.ui.WatchProgressLine
import tv.trakt.trakt.core.summary.shows.features.progress.ui.WatchProgressSize
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.buttons.PrimaryButton
import tv.trakt.trakt.ui.components.confirmation.RemoveConfirmationSheet
import tv.trakt.trakt.ui.components.dateselection.DateSelectionSheet
import tv.trakt.trakt.ui.theme.TraktTheme
import tv.trakt.trakt.ui.theme.segmentShape

private const val SKELETON_SEASON_COUNT = 4
private const val HEADER_ITEM_COUNT = 1
private val ButtonHeight = 46.dp
private val SegmentGap = 3.dp

@Composable
internal fun ShowProgressView(
    viewModel: ShowProgressViewModel,
    modifier: Modifier = Modifier,
    onMarkWatchedClick: () -> Unit = {},
    onRemoveWatchedClick: () -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var episodeDateSheet by remember { mutableStateOf<Episode?>(null) }
    var finishDateSheet by remember { mutableStateOf<List<Episode>?>(null) }
    var removeEpisodeSheet by remember { mutableStateOf<Episode?>(null) }

    ShowProgressContent(
        state = state,
        modifier = modifier,
        onSeasonClick = viewModel::toggleSeason,
        onEpisodeClick = { episode, watched ->
            when {
                watched -> removeEpisodeSheet = episode
                else -> episodeDateSheet = episode
            }
        },
        onFinishClick = { finishDateSheet = it },
        onMarkWatchedClick = onMarkWatchedClick,
        onRemoveWatchedClick = onRemoveWatchedClick,
    )

    DateSelectionSheet(
        active = episodeDateSheet != null,
        title = state.show?.title.orEmpty(),
        subtitle = episodeDateSheet?.seasonEpisodeString(),
        onResult = { result ->
            episodeDateSheet?.let {
                viewModel.addToWatched(episode = it, customDate = result)
            }
        },
        onDismiss = { episodeDateSheet = null },
    )

    DateSelectionSheet(
        active = finishDateSheet != null,
        title = state.show?.title.orEmpty(),
        subtitle = finishDateSheet?.let {
            stringResource(R.string.warning_prompt_mark_as_watched_multiple_episodes, it.size)
        },
        onResult = { result ->
            finishDateSheet?.let {
                viewModel.finishSeason(episodes = it, customDate = result)
            }
        },
        onDismiss = { finishDateSheet = null },
    )

    RemoveConfirmationSheet(
        active = removeEpisodeSheet != null,
        onYes = {
            removeEpisodeSheet?.let(viewModel::removeFromWatched)
            removeEpisodeSheet = null
        },
        onNo = { removeEpisodeSheet = null },
        title = stringResource(R.string.button_text_remove_from_history),
        message = stringResource(
            R.string.warning_prompt_remove_from_watched,
            removeEpisodeSheet?.title.orEmpty(),
        ),
    )
}

@Composable
private fun ShowProgressContent(
    state: ShowProgressState,
    modifier: Modifier = Modifier,
    onSeasonClick: (Int) -> Unit = {},
    onEpisodeClick: (Episode, watched: Boolean) -> Unit = { _, _ -> },
    onFinishClick: (List<Episode>) -> Unit = {},
    onMarkWatchedClick: () -> Unit = {},
    onRemoveWatchedClick: () -> Unit = {},
) {
    Box(
        modifier = modifier.padding(bottom = 16.dp),
    ) {
        Column {
            ProgressList(
                state = state,
                onSeasonClick = onSeasonClick,
                onEpisodeClick = onEpisodeClick,
                onFinishClick = onFinishClick,
                modifier = Modifier.weight(1F, fill = false),
            )

            Spacer(modifier = Modifier.height(ButtonHeight / 2))
        }

        val buttonModifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .padding(horizontal = 24.dp)

        when {
            state.isWatched -> PrimaryButton(
                text = stringResource(R.string.button_text_remove_from_history),
                containerColor = Red500,
                contentColor = Color.White,
                height = ButtonHeight,
                onClick = onRemoveWatchedClick,
                modifier = buttonModifier,
            )

            else -> PrimaryButton(
                text = stringResource(R.string.button_text_mark_as_watched),
                icon = painterResource(R.drawable.ic_check_2),
                enabled = state.totalEpisodes > 0,
                height = ButtonHeight,
                onClick = onMarkWatchedClick,
                modifier = buttonModifier,
            )
        }
    }
}

@Composable
private fun ProgressHero(state: ShowProgressState) {
    val loading = state.seasons == null && state.error == null
    val shimmerColor = rememberSkeletonShimmerColor()
    val textColor: (Color) -> Color = { if (loading) Color.Transparent else it }

    Column(
        verticalArrangement = spacedBy(2.dp),
    ) {
        Row(
            horizontalArrangement = SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = (state.fraction * 100).toInt().rememberPercentFormat(),
                style = TraktTheme.typography.heading1.copy(fontWeight = FontWeight.W200),
                color = textColor(TraktTheme.colors.textPrimary),
                modifier = Modifier
                    .alignBy(LastBaseline)
                    .then(
                        when {
                            loading -> Modifier.background(shimmerColor, RoundedCornerShape(12.dp))
                            else -> Modifier
                        },
                    ),
            )

            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier
                    .alignBy(LastBaseline)
                    .alpha(if (loading) 0F else 1F),
            ) {
                Text(
                    text = stringResource(
                        R.string.text_watch_state_fraction,
                        state.watchedCount,
                        state.totalEpisodes,
                    ),
                    style = TraktTheme.typography.cardTitle,
                    color = TraktTheme.colors.textPrimary,
                )
                Text(
                    text = stringResource(R.string.text_watch_state_episodes),
                    style = TraktTheme.typography.meta,
                    color = TraktTheme.colors.textSecondary,
                )
            }
        }

        when {
            loading -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(WatchProgressSize.Large.height)
                    .background(shimmerColor, RoundedCornerShape(100)),
            )
            else -> WatchProgressLine(
                progress = state.fraction,
                size = WatchProgressSize.Large,
                animate = false,
            )
        }
    }
}

@Composable
private fun ProgressList(
    state: ShowProgressState,
    onSeasonClick: (Int) -> Unit,
    onEpisodeClick: (Episode, watched: Boolean) -> Unit,
    onFinishClick: (List<Episode>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val seasons = state.seasons
    val listState = rememberLazyListState()
    var hasScrolled by remember { mutableStateOf(false) }
    val currentIndex = seasons?.indexOfFirst { it.number == state.currentSeason } ?: -1

    LaunchedEffect(currentIndex) {
        if (hasScrolled || currentIndex < 0) return@LaunchedEffect
        hasScrolled = true

        val itemIndex = currentIndex + HEADER_ITEM_COUNT
        snapshotFlow { listState.layoutInfo.totalItemsCount }.first { it > itemIndex }

        val visible = listState.layoutInfo.visibleItemsInfo.any { it.index == itemIndex }
        if (!visible) {
            listState.scrollToItem(itemIndex)
        }
    }

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(
            start = 24.dp,
            end = 24.dp,
            top = 8.dp,
            bottom = ButtonHeight / 2 + 16.dp,
        ),
        modifier = modifier,
    ) {
        item(key = "header") {
            Column(
                verticalArrangement = spacedBy(16.dp),
                modifier = Modifier.padding(bottom = 16.dp),
            ) {
                TraktHeader(
                    title = state.show?.title.orEmpty(),
                    subtitle = stringResource(R.string.list_title_progress),
                )

                ProgressHero(state = state)
            }
        }

        if (seasons == null) {
            if (state.error == null) {
                item(key = "skeleton") {
                    ProgressSeasonsSkeleton()
                }
            }
            return@LazyColumn
        }

        itemsIndexed(
            items = seasons,
            key = { _, season -> season.number },
        ) { index, season ->
            SeasonProgressRow(
                season = season,
                expanded = state.isExpanded(season.number),
                loading = season.number in state.loadingSeasons,
                episodes = state.episodes[season.number],
                upNextEpisodeId = state.upNext?.episodeId,
                busyEpisodes = state.busyEpisodes,
                onToggle = { onSeasonClick(season.number) },
                onEpisodeClick = onEpisodeClick,
                onFinishClick = onFinishClick,
                modifier = Modifier
                    .padding(top = if (index > 0) SegmentGap else 0.dp)
                    .segment(
                        index = index,
                        count = seasons.size,
                    ),
            )
        }
    }
}

@Composable
private fun ProgressSeasonsSkeleton(modifier: Modifier = Modifier) {
    val shimmerColor = rememberSkeletonShimmerColor()

    Column(
        verticalArrangement = spacedBy(SegmentGap),
        modifier = modifier,
    ) {
        repeat(SKELETON_SEASON_COUNT) { index ->
            Column(
                verticalArrangement = spacedBy(8.dp),
                modifier = Modifier
                    .segment(index = index, count = SKELETON_SEASON_COUNT)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.3F)
                        .height(14.dp)
                        .background(shimmerColor, RoundedCornerShape(4.dp)),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(shimmerColor, RoundedCornerShape(100)),
                )
            }
        }
    }
}

@Composable
private fun rememberSkeletonShimmerColor(): Color {
    val color by rememberInfiniteTransition(label = "skeletonShimmer")
        .animateColor(
            initialValue = TraktTheme.colors.chipContainer,
            targetValue = TraktTheme.colors.chipContainer.copy(alpha = 0.5F),
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1000),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "skeletonShimmerColor",
        )
    return color
}

@Composable
private fun Modifier.segment(
    index: Int,
    count: Int,
): Modifier {
    val shape = segmentShape(index = index, count = count)
    return fillMaxWidth()
        .shadow(
            elevation = TraktTheme.colors.shadowDynamicSmall,
            shape = shape,
            clip = true,
        )
        .background(TraktTheme.colors.dialogOnContainer)
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    val episodes = (1..10).map {
        PreviewData.episode1.copy(
            ids = PreviewData.episode1.ids.copy(trakt = TraktId(it)),
            number = it,
        )
    }.toImmutableList()

    TraktThemePreview {
        ShowProgressContent(
            state = ShowProgressState(
                show = PreviewData.show1,
                seasons = listOf(
                    SeasonProgress(
                        number = 1,
                        watched = 10,
                        aired = 10,
                        total = 10,
                        watchedEpisodeIds = (1..10).map { TraktId(it) }.toImmutableSet(),
                    ),
                    SeasonProgress(
                        number = 2,
                        watched = 4,
                        aired = 10,
                        total = 10,
                        watchedEpisodeIds = persistentSetOf(TraktId(1), TraktId(2), TraktId(3), TraktId(4)),
                    ),
                ).toImmutableList(),
                episodes = persistentMapOf(2 to episodes),
                upNext = ShowProgressState.UpNext(episodeId = TraktId(5), season = 2),
                watchedEpisodes = 14,
            ),
        )
    }
}

@PreviewTraktLightDark
@Composable
private fun LoadingPreview() {
    TraktThemePreview {
        ShowProgressContent(
            state = ShowProgressState(show = PreviewData.show1),
        )
    }
}
