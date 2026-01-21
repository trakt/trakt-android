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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.IDLE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.fullDayFormat
import tv.trakt.trakt.common.helpers.extensions.nowLocalDay
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.ui.theme.colors.Purple400
import tv.trakt.trakt.core.calendar.ui.CalendarEpisodeItemView
import tv.trakt.trakt.core.home.sections.upcoming.model.HomeUpcomingItem
import tv.trakt.trakt.helpers.SimpleScrollConnection
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.ScrollableBackdropImage
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.mediacards.skeletons.EpisodeSkeletonCard
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.Instant

@Composable
internal fun CalendarScreen(
    viewModel: CalendarViewModel,
    onNavigateBack: () -> Unit,
    onEpisodeClick: (showId: TraktId, episode: Episode) -> Unit,
    onShowClick: (TraktId) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(
        state.navigateShow,
        state.navigateEpisode,
    ) {
        state.navigateShow?.let {
            onShowClick(it)
            viewModel.clearNavigation()
        }
        state.navigateEpisode?.let {
            onEpisodeClick(it.first, it.second)
            viewModel.clearNavigation()
        }
    }

    CalendarScreen(
        state = state,
        onBackClick = onNavigateBack,
        onEpisodeClick = { item ->
            viewModel.navigateToEpisode(item.show, item.episode)
        },
        onShowClick = { item ->
            viewModel.navigateToShow(item.show)
        },
    )
}

@Composable
private fun CalendarScreen(
    state: CalendarState,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onEpisodeClick: (HomeUpcomingItem.EpisodeItem) -> Unit = {},
    onShowClick: (HomeUpcomingItem.EpisodeItem) -> Unit = {},
) {
    val contentPadding = PaddingValues(
        start = TraktTheme.spacing.mainPageHorizontalSpace,
        end = TraktTheme.spacing.mainPageHorizontalSpace,
        top = WindowInsets.statusBars.asPaddingValues()
            .calculateTopPadding()
            .plus(TraktTheme.size.titleBarHeight)
            .plus(24.dp),
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
) {
    when (state.loading) {
        IDLE, LOADING -> {
            ContentLoadingGrid(
                visible = state.loading.isLoading,
                contentPadding = contentPadding,
            )
        }

        DONE -> {
            when {
                state.error != null -> {
                    // TODO
                }
                else -> {
                    ContentItemsGrid(
                        items = state.items ?: emptyMap(),
                        gridState = gridState,
                        contentPadding = contentPadding,
                        onEpisodeClick = onEpisodeClick,
                        onShowClick = onShowClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun ContentItemsGrid(
    items: Map<Instant, ImmutableList<HomeUpcomingItem>?>,
    gridState: LazyGridState,
    contentPadding: PaddingValues,
    onEpisodeClick: (HomeUpcomingItem.EpisodeItem) -> Unit,
    onShowClick: (HomeUpcomingItem.EpisodeItem) -> Unit,
) {
    val sortedDates = remember(items) {
        items.keys.sorted()
    }

    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(2),
        horizontalArrangement = spacedBy(TraktTheme.spacing.mainGridHorizontalSpace),
        verticalArrangement = spacedBy(TraktTheme.spacing.mainGridVerticalSpace),
        contentPadding = contentPadding,
        overscrollEffect = null,
    ) {
        sortedDates.forEachIndexed { index, date ->
            val episodes = items[date] ?: EmptyImmutableList
            if (episodes.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Row(
                        horizontalArrangement = spacedBy(6.dp),
                        verticalAlignment = CenterVertically,
                        modifier = Modifier.padding(
                            top = if (index == 0) 0.dp else 24.dp,
                        ),
                    ) {
                        val isToday = remember {
                            date.toLocal().toLocalDate() == nowLocalDay()
                        }

                        if (isToday) {
                            Icon(
                                painter = painterResource(R.drawable.ic_calendar),
                                tint = Purple400,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
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
                    count = episodes.size,
                    key = { index -> episodes[index].id.value },
                ) { index ->
                    val episode = episodes[index]
                    if (episode is HomeUpcomingItem.EpisodeItem) {
                        CalendarEpisodeItemView(
                            item = episode,
                            onClick = { onEpisodeClick(episode) },
                            onShowClick = { onShowClick(episode) },
                            modifier = Modifier.animateItem(
                                fadeInSpec = null,
                                fadeOutSpec = null,
                            ),
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
        modifier = Modifier
            .fillMaxSize()
            .alpha(if (visible) 1F else 0F),
    ) {
        items(count = 6) {
            EpisodeSkeletonCard()
        }
    }
}
