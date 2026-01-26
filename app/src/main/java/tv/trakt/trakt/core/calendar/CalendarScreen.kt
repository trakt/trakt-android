@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package tv.trakt.trakt.core.calendar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.fullDayFormat
import tv.trakt.trakt.common.helpers.extensions.nowLocalDay
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.ui.theme.colors.Purple400
import tv.trakt.trakt.core.calendar.ui.CalendarControlsView
import tv.trakt.trakt.core.calendar.ui.CalendarEpisodeItemView
import tv.trakt.trakt.core.calendar.ui.CalendarMovieItemView
import tv.trakt.trakt.core.home.sections.upcoming.model.HomeUpcomingItem
import tv.trakt.trakt.helpers.SimpleScrollConnection
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.ScrollableBackdropImage
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.mediacards.skeletons.EpisodeSkeletonCard
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.DayOfWeek.MONDAY
import java.time.Instant
import java.time.LocalDate

@Composable
internal fun CalendarScreen(
    viewModel: CalendarViewModel,
    onNavigateBack: () -> Unit,
    onEpisodeClick: (showId: TraktId, episode: Episode) -> Unit,
    onShowClick: (TraktId) -> Unit,
    onMovieClick: (TraktId) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(
        state.navigateShow,
        state.navigateMovie,
        state.navigateEpisode,
    ) {
        state.navigateShow?.let {
            onShowClick(it)
            viewModel.clearNavigation()
        }
        state.navigateMovie?.let {
            onMovieClick(it)
            viewModel.clearNavigation()
        }
        state.navigateEpisode?.let {
            onEpisodeClick(it.first, it.second)
            viewModel.clearNavigation()
        }
    }

    CalendarScreen(
        state = state,
        onTodayClick = viewModel::loadTodayData,
        onNextWeekClick = viewModel::loadNextWeekData,
        onPreviousWeekClick = viewModel::loadPreviousWeekData,
        onShowClick = { item ->
            viewModel.navigateToShow(item.show)
        },
        onMovieClick = { item ->
            viewModel.navigateToMovie(item.movie)
        },
        onEpisodeClick = { item ->
            viewModel.navigateToEpisode(item.show, item.episode)
        },
        onBackClick = onNavigateBack,
    )
}

@Composable
private fun CalendarScreen(
    state: CalendarState,
    modifier: Modifier = Modifier,
    onTodayClick: () -> Unit = {},
    onNextWeekClick: () -> Unit = {},
    onPreviousWeekClick: () -> Unit = {},
    onShowClick: (HomeUpcomingItem.EpisodeItem) -> Unit = {},
    onMovieClick: (HomeUpcomingItem.MovieItem) -> Unit = {},
    onEpisodeClick: (HomeUpcomingItem.EpisodeItem) -> Unit = {},
    onBackClick: () -> Unit = {},
) {
    val contentPadding = PaddingValues(
        start = TraktTheme.spacing.mainPageHorizontalSpace,
        end = TraktTheme.spacing.mainPageHorizontalSpace,
        top = WindowInsets.statusBars.asPaddingValues()
            .calculateTopPadding()
            .plus(TraktTheme.size.titleBarHeight)
            .plus(176.dp),
        bottom = WindowInsets.navigationBars.asPaddingValues()
            .calculateBottomPadding()
            .plus(TraktTheme.size.navigationBarHeight)
            .plus(32.dp),
    )

    val gridState = rememberLazyGridState()
    val listScrollConnection = rememberSaveable(saver = SimpleScrollConnection.Saver) {
        SimpleScrollConnection()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary)
            .nestedScroll(listScrollConnection),
    ) {
        ScrollableBackdropImage(
            translation = listScrollConnection.resultOffset,
        )

        CalendarContent(
            state = state,
            gridState = gridState,
            contentPadding = contentPadding,
            onEpisodeClick = onEpisodeClick,
            onShowClick = onShowClick,
            onMovieClick = onMovieClick,
        )

        TitleBar(
            onBackClick = onBackClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = TraktTheme.spacing.mainPageHorizontalSpace,
                    end = TraktTheme.spacing.mainPageHorizontalSpace,
                    top = WindowInsets.statusBars.asPaddingValues()
                        .calculateTopPadding()
                        .plus(4.dp),
                )
                .graphicsLayer {
                    translationY = listScrollConnection.resultOffset
                },
        )

        CalendarControlsView(
            enabled = !state.loading.isLoading,
            startDate = state.selectedStartDay,
            focusedDate = LocalDate.now(),
            onTodayClick = onTodayClick,
            onNextWeekClick = onNextWeekClick,
            onPreviousWeekClick = onPreviousWeekClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = WindowInsets.statusBars.asPaddingValues()
                        .calculateTopPadding()
                        .plus(TraktTheme.size.titleBarHeight)
                        .plus(8.dp),
                    start = TraktTheme.spacing.mainPageHorizontalSpace,
                    end = TraktTheme.spacing.mainPageHorizontalSpace,
                )
                .graphicsLayer {
                    translationY = listScrollConnection.resultOffset
                },
        )
    }
}

@Composable
private fun TitleBar(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = spacedBy(12.dp),
            modifier = Modifier
                .height(TraktTheme.size.titleBarHeight)
                .onClick(onClick = onBackClick)
                .graphicsLayer {
                    translationX = -2.dp.toPx()
                },
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_back_arrow),
                tint = TraktTheme.colors.textPrimary,
                contentDescription = null,
            )
            TraktHeader(
                title = stringResource(R.string.page_title_calendar),
            )
        }
    }
}

@Composable
private fun CalendarContent(
    state: CalendarState,
    gridState: LazyGridState,
    contentPadding: PaddingValues,
    onEpisodeClick: (HomeUpcomingItem.EpisodeItem) -> Unit,
    onShowClick: (HomeUpcomingItem.EpisodeItem) -> Unit,
    onMovieClick: (HomeUpcomingItem.MovieItem) -> Unit,
) {
    if (state.items.isNullOrEmpty() && state.loading.isLoading) {
        ContentLoadingGrid(
            visible = state.loading.isLoading,
            contentPadding = contentPadding,
        )
    } else if (!state.items.isNullOrEmpty()) {
        ContentItemsGrid(
            items = state.items,
            gridState = gridState,
            contentPadding = contentPadding,
            onEpisodeClick = onEpisodeClick,
            onShowClick = onShowClick,
            onMovieClick = onMovieClick,
        )
    }
//    when (state.loading) {
//        IDLE, LOADING -> {
//            if (state.items.isNullOrEmpty()) {
//                ContentLoadingGrid(
//                    visible = state.loading.isLoading,
//                    contentPadding = contentPadding,
//                )
//            }
//        }
//
//        DONE -> {
//            when {
//                state.error != null -> {
//                    Text(
//                        text = "${
//                            stringResource(
//                                R.string.error_text_unexpected_error_short,
//                            )
//                        }\n\n${state.error}",
//                        color = TraktTheme.colors.textSecondary,
//                        style = TraktTheme.typography.meta,
//                        maxLines = 20,
//                        modifier = Modifier.padding(contentPadding),
//                    )
//                }
//                else -> {
//                    ContentItemsGrid(
//                        items = state.items ?: emptyMap(),
//                        gridState = gridState,
//                        contentPadding = contentPadding,
//                        onEpisodeClick = onEpisodeClick,
//                        onShowClick = onShowClick,
//                        onMovieClick = onMovieClick,
//                    )
//                }
//            }
//        }
//    }
}

@Composable
private fun ContentItemsGrid(
    items: Map<Instant, ImmutableList<HomeUpcomingItem>?>,
    gridState: LazyGridState,
    contentPadding: PaddingValues,
    onEpisodeClick: (HomeUpcomingItem.EpisodeItem) -> Unit,
    onShowClick: (HomeUpcomingItem.EpisodeItem) -> Unit,
    onMovieClick: (HomeUpcomingItem.MovieItem) -> Unit,
) {
    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(2),
        horizontalArrangement = spacedBy(TraktTheme.spacing.mainGridHorizontalSpace),
        verticalArrangement = spacedBy(TraktTheme.spacing.mainGridVerticalSpace),
        contentPadding = contentPadding,
        overscrollEffect = null,
    ) {
        items.keys.forEachIndexed { index, date ->
            val gridItems = items[date] ?: EmptyImmutableList
            if (gridItems.isNotEmpty()) {
                item(
                    span = { GridItemSpan(maxLineSpan) },
                    key = "header_$date",
                ) {
                    Row(
                        horizontalArrangement = spacedBy(6.dp),
                        verticalAlignment = CenterVertically,
                        modifier = Modifier
                            .padding(top = if (index == 0) 0.dp else 24.dp),
                    ) {
                        val isToday = remember(date) {
                            date.toLocal().toLocalDate() == nowLocalDay()
                        }

                        if (isToday) {
                            Box(
                                modifier = Modifier
                                    .background(color = Purple400, shape = CircleShape)
                                    .size(6.dp)
                                    .graphicsLayer {
                                        translationY = 2.dp.toPx()
                                    },
                            )
                        }
                        TraktHeader(
                            title = remember {
                                date.toLocal().format(fullDayFormat)
                            },
                        )
                    }
                }

                items(
                    count = gridItems.size,
                    key = { index -> gridItems[index].id.value },
                ) { index ->
                    val item = gridItems[index]
                    if (item is HomeUpcomingItem.EpisodeItem) {
                        CalendarEpisodeItemView(
                            item = item,
                            onClick = { onEpisodeClick(item) },
                            onShowClick = { onShowClick(item) },
                        )
                    }

                    if (item is HomeUpcomingItem.MovieItem) {
                        CalendarMovieItemView(
                            item = item,
                            onClick = { onMovieClick(item) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContentLoadingGrid(
    visible: Boolean = true,
    contentPadding: PaddingValues,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = spacedBy(TraktTheme.spacing.mainGridHorizontalSpace),
        verticalArrangement = spacedBy(TraktTheme.spacing.mainGridVerticalSpace),
        contentPadding = contentPadding,
        overscrollEffect = null,
        userScrollEnabled = false,
        modifier = Modifier
            .fillMaxSize()
            .alpha(if (visible) 1F else 0F),
    ) {
        items(count = 12) {
            EpisodeSkeletonCard()
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
        CalendarScreen(
            state = CalendarState(
                selectedStartDay = LocalDate.now().with(MONDAY),
                loading = DONE,
            ),
        )
    }
}
