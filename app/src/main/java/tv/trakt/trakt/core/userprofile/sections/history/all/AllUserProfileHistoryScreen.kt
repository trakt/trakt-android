@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package tv.trakt.trakt.core.userprofile.sections.history.all

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.layout.LazyLayoutCacheWindow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.capitalize
import tv.trakt.trakt.common.helpers.extensions.isTraktUnknown
import tv.trakt.trakt.common.helpers.extensions.longDateFormat
import tv.trakt.trakt.common.helpers.extensions.longDateTimeFormat
import tv.trakt.trakt.common.helpers.extensions.nowLocalDay
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.relativePastDateString
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.home.sections.activity.features.all.views.AllActivityEpisodeItem
import tv.trakt.trakt.core.home.sections.activity.features.all.views.AllActivityMovieItem
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem.EpisodeItem
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem.MovieItem
import tv.trakt.trakt.helpers.SimpleScrollConnection
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.MediaModeFilters
import tv.trakt.trakt.ui.components.ScrollableBackdropImage
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.mediacards.skeletons.PanelMediaSkeletonCard
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.LocalDate
import java.time.ZoneId

@Composable
internal fun AllUserProfileHistoryScreen(
    modifier: Modifier = Modifier,
    viewModel: AllUserProfileHistoryViewModel = koinViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToEpisode: (showId: TraktId, episode: Episode) -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(
        state.navigateShow,
        state.navigateMovie,
        state.navigateEpisode,
    ) {
        state.navigateShow?.let {
            onNavigateToShow(it)
            viewModel.clearNavigation()
        }
        state.navigateEpisode?.let {
            onNavigateToEpisode(it.first, it.second)
            viewModel.clearNavigation()
        }
        state.navigateMovie?.let {
            onNavigateToMovie(it)
            viewModel.clearNavigation()
        }
    }

    AllUserProfileHistoryContent(
        state = state,
        modifier = modifier,
        onBackClick = onNavigateBack,
        onLoadMore = viewModel::loadMoreData,
        onShowClick = { viewModel.navigateToShow(it.show) },
        onEpisodeClick = { viewModel.navigateToEpisode(it.show, it.episode) },
        onMovieClick = { viewModel.navigateToMovie(it) },
        onModeClick = viewModel::setFilter,
    )
}

@Composable
internal fun AllUserProfileHistoryContent(
    state: AllUserProfileHistoryState,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onLoadMore: () -> Unit = {},
    onShowClick: (EpisodeItem) -> Unit = {},
    onEpisodeClick: (EpisodeItem) -> Unit = {},
    onMovieClick: (Movie) -> Unit = {},
    onModeClick: (MediaMode) -> Unit = {},
) {
    val listState = rememberLazyListState(
        cacheWindow = LazyLayoutCacheWindow(
            aheadFraction = 0.5F,
            behindFraction = 0.5F,
        ),
    )

    val listScrollConnection = rememberSaveable(saver = SimpleScrollConnection.Saver) {
        SimpleScrollConnection()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary)
            .nestedScroll(listScrollConnection),
    ) {
        val contentPadding = PaddingValues(
            start = TraktTheme.spacing.mainPageHorizontalSpace,
            end = TraktTheme.spacing.mainPageHorizontalSpace,
            top = WindowInsets.statusBars.asPaddingValues()
                .calculateTopPadding()
                .plus(3.dp),
            bottom = WindowInsets.navigationBars.asPaddingValues()
                .calculateBottomPadding()
                .plus(TraktTheme.size.navigationBarHeight * 2),
        )

        ScrollableBackdropImage(
            translation = listScrollConnection.resultOffset,
        )

        ContentList(
            listState = listState,
            listItems = remember(state.items) {
                (state.items ?: emptyMap()).toImmutableMap()
            },
            mediaMode = state.filters.mode,
            contentPadding = contentPadding,
            loading = state.loading.isLoading,
            loadingMore = state.loadingMore.isLoading,
            onEndOfList = onLoadMore,
            onBackClick = onBackClick,
            onShowClick = onShowClick,
            onEpisodeClick = onEpisodeClick,
            onMovieClick = onMovieClick,
            onModeClick = onModeClick,
        )
    }
}

@Composable
private fun TitleBar(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = spacedBy(12.dp),
        modifier = modifier
            .height(TraktTheme.size.titleBarHeight)
            .graphicsLayer {
                translationX = -4.dp.toPx()
            },
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_back_arrow),
            tint = TraktTheme.colors.textPrimary,
            contentDescription = null,
        )
        Text(
            text = stringResource(R.string.list_title_history),
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.heading5,
        )
    }
}

@Composable
private fun ContentList(
    modifier: Modifier = Modifier,
    listItems: ImmutableMap<LocalDate, ImmutableList<HomeActivityItem>>,
    mediaMode: MediaMode,
    listState: LazyListState,
    contentPadding: PaddingValues,
    loading: Boolean,
    loadingMore: Boolean,
    onEndOfList: () -> Unit,
    onBackClick: () -> Unit,
    onShowClick: (EpisodeItem) -> Unit,
    onEpisodeClick: (EpisodeItem) -> Unit,
    onMovieClick: (Movie) -> Unit,
    onModeClick: (MediaMode) -> Unit,
) {
    val isScrolledToBottom by remember(listItems.size) {
        derivedStateOf {
            val info = listState.layoutInfo
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            info.totalItemsCount > 0 && lastVisible >= info.totalItemsCount - 5
        }
    }

    LaunchedEffect(isScrolledToBottom) {
        if (isScrolledToBottom) {
            onEndOfList()
        }
    }

    LazyColumn(
        state = listState,
        verticalArrangement = spacedBy(0.dp),
        contentPadding = contentPadding,
        overscrollEffect = null,
        modifier = modifier,
    ) {
        item {
            TitleBar(
                modifier = Modifier
                    .padding(bottom = 2.dp)
                    .onClick { onBackClick() },
            )
        }

        item {
            MediaModeFilters(
                selected = mediaMode,
                height = 32.dp,
                onClick = onModeClick,
                modifier = Modifier.padding(bottom = 20.dp),
            )
        }

        val today = nowLocalDay()
        listItems.keys.forEachIndexed { index, date ->
            val itemsForDate = listItems[date] ?: EmptyImmutableList

            item(key = "header-$date") {
                TraktHeader(
                    title = when {
                        today == date || today.minusDays(1) == date -> {
                            date.atStartOfDay(ZoneId.systemDefault()).relativePastDateString()
                        }
                        date.isTraktUnknown() -> {
                            stringResource(R.string.button_text_mark_as_watched_unknown_date)
                        }
                        else -> {
                            date.format(longDateFormat()).capitalize()
                        }
                    },
                    modifier = Modifier.padding(
                        top = when (index) {
                            0 -> 0.dp
                            else -> 16.dp
                        },
                        bottom = 12.dp,
                    ),
                )
            }

            items(
                items = itemsForDate,
                key = { it.id },
            ) { item ->
                val dateFormat = longDateTimeFormat()
                when (item) {
                    is MovieItem -> {
                        AllActivityMovieItem(
                            item = item,
                            enabled = !loading,
                            itemRating = null,
                            onClick = { onMovieClick(item.movie) },
                            onLongClick = {},
                            moreButton = false,
                            dateFormat = dateFormat,
                            modifier = Modifier
                                .padding(bottom = TraktTheme.spacing.mainListVerticalSpace)
                                .animateItem(fadeInSpec = null, fadeOutSpec = null),
                        )
                    }

                    is EpisodeItem -> {
                        AllActivityEpisodeItem(
                            item = item,
                            enabled = !loading,
                            itemRating = null,
                            onClick = { onEpisodeClick(item) },
                            onShowClick = { onShowClick(item) },
                            onLongClick = {},
                            moreButton = false,
                            dateFormat = dateFormat,
                            modifier = Modifier
                                .padding(bottom = TraktTheme.spacing.mainListVerticalSpace)
                                .animateItem(fadeInSpec = null, fadeOutSpec = null),
                        )
                    }
                }
            }
        }

        if (loading && listItems.isEmpty()) {
            items(5) {
                PanelMediaSkeletonCard(
                    modifier = Modifier
                        .padding(bottom = TraktTheme.spacing.mainListVerticalSpace)
                        .animateItem(fadeInSpec = null, fadeOutSpec = null),
                )
            }
        } else if (loadingMore) {
            item {
                PanelMediaSkeletonCard(
                    modifier = Modifier
                        .padding(bottom = TraktTheme.spacing.mainListVerticalSpace)
                        .animateItem(fadeInSpec = null, fadeOutSpec = null),
                )
            }
        }

        if (listItems.isEmpty() && !loading) {
            item {
                Text(
                    text = stringResource(R.string.list_placeholder_empty),
                    color = TraktTheme.colors.textSecondary,
                    style = TraktTheme.typography.heading6,
                    modifier = Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null),
                )
            }
        }
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        AllUserProfileHistoryContent(
            state = AllUserProfileHistoryState(
                loading = LoadingState.Done,
            ),
        )
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun PreviewLoading() {
    TraktTheme {
        AllUserProfileHistoryContent(
            state = AllUserProfileHistoryState(
                loading = LoadingState.Loading,
            ),
        )
    }
}
