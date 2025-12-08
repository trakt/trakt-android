@file:OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class,
    ExperimentalMaterial3ExpressiveApi::class,
)

package tv.trakt.trakt.core.lists.sections.watchlist.features.all

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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.layout.LazyLayoutCacheWindow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType.Companion.Confirm
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.sorting.SortTypeList
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.core.lists.sections.watchlist.features.all.views.AllWatchlistMovieView
import tv.trakt.trakt.core.lists.sections.watchlist.features.all.views.AllWatchlistShowView
import tv.trakt.trakt.core.lists.sections.watchlist.features.context.movies.sheets.WatchlistMovieSheet
import tv.trakt.trakt.core.lists.sections.watchlist.features.context.shows.sheets.WatchlistShowSheet
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem.MovieItem
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem.ShowItem
import tv.trakt.trakt.core.main.model.MediaMode
import tv.trakt.trakt.core.user.UserCollectionState
import tv.trakt.trakt.helpers.SimpleScrollConnection
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.MediaModeFilters
import tv.trakt.trakt.ui.components.ScrollableBackdropImage
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.dateselection.DateSelectionResult
import tv.trakt.trakt.ui.components.dateselection.DateSelectionSheet
import tv.trakt.trakt.ui.components.sorting.SortingSplitButton
import tv.trakt.trakt.ui.components.sorting.sheets.SortSelectionSheet
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun AllWatchlistScreen(
    modifier: Modifier = Modifier,
    viewModel: AllWatchlistViewModel,
    onNavigateBack: () -> Unit,
    onShowClick: (TraktId) -> Unit,
    onMovieClick: (TraktId) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current

    var contextMovieSheet by remember { mutableStateOf<MovieItem?>(null) }
    var contextShowSheet by remember { mutableStateOf<ShowItem?>(null) }
    var dateSheet by remember { mutableStateOf<WatchlistItem?>(null) }
    var sortSheet by remember { mutableStateOf<SortTypeList?>(null) }

    LaunchedEffect(state) {
        state.navigateShow?.let {
            viewModel.clearNavigation()
            onShowClick(it)
        }
        state.navigateMovie?.let {
            viewModel.clearNavigation()
            onMovieClick(it)
        }
    }

    LaunchedEffect(state.info) {
        if (state.info != null) {
            haptic.performHapticFeedback(Confirm)
            viewModel.clearInfo()
        }
    }

    AllWatchlistContent(
        state = state,
        modifier = modifier,
        onClick = {
            when (it) {
                is ShowItem -> viewModel.navigateToShow(it.show)
                is MovieItem -> viewModel.navigateToMovie(it.movie)
            }
        },
        onLongClick = {
            when (it) {
                is MovieItem -> contextMovieSheet = it
                is ShowItem -> contextShowSheet = it
            }
        },
        onCheckClick = {
            if (it is MovieItem) {
                viewModel.addMovieToHistory(it.id)
            }
        },
        onCheckLongClick = {
            if (it is MovieItem) {
                dateSheet = it
            }
        },
        onFilterClick = { viewModel.setFilter(it) },
        onSortTypeClick = {
            sortSheet = state.sorting.type
        },
        onSortOrderClick = {
            val sorting = state.sorting
            viewModel.setSorting(
                sorting.copy(
                    order = sorting.order.toggle(),
                ),
            )
        },
        onBackClick = onNavigateBack,
    )

    WatchlistMovieSheet(
        movie = contextMovieSheet?.movie,
        addLocally = true,
        watched = contextMovieSheet?.movie?.ids?.trakt?.let {
            state.collection.isWatched(it)
        } ?: false,
        onDismiss = { contextMovieSheet = null },
        onRemoveWatchlist = {
            viewModel.removeItem(contextMovieSheet)
        },
        onAddWatched = {
            viewModel.removeItem(contextMovieSheet)
        },
    )

    WatchlistShowSheet(
        show = contextShowSheet?.show,
        addLocally = true,
        watched = contextShowSheet?.show?.ids?.trakt?.let {
            state.collection.isWatched(it)
        } ?: false,
        onDismiss = { contextShowSheet = null },
        onRemoveWatchlist = {
            viewModel.removeItem(contextShowSheet)
        },
        onAddWatched = {
            viewModel.removeItem(contextShowSheet)
        },
    )

    WatchlistDateSelectionSheet(
        item = dateSheet,
        onDateSelected = { date ->
            dateSheet?.let {
                when (it) {
                    is MovieItem -> {
                        viewModel.addMovieToHistory(
                            movieId = it.id,
                            customDate = date,
                        )
                    }

                    is ShowItem -> {
                        Unit
                    }
                }
            }
        },
        onDismiss = {
            dateSheet = null
        },
    )

    SortSelectionSheet(
        active = sortSheet != null,
        selected = sortSheet,
        onResult = {
            viewModel.setSorting(
                state.sorting.copy(
                    type = it,
                ),
            )
        },
        onDismiss = {
            sortSheet = null
        },
    )
}

@Composable
internal fun AllWatchlistContent(
    state: AllWatchlistState,
    modifier: Modifier = Modifier,
    onTopOfList: () -> Unit = {},
    onClick: (WatchlistItem) -> Unit = {},
    onCheckClick: (WatchlistItem) -> Unit = {},
    onCheckLongClick: (WatchlistItem) -> Unit = {},
    onLongClick: (WatchlistItem) -> Unit = {},
    onFilterClick: (MediaMode) -> Unit = {},
    onSortTypeClick: () -> Unit = {},
    onSortOrderClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
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
                .calculateTopPadding(),
            bottom = WindowInsets.navigationBars.asPaddingValues()
                .calculateBottomPadding()
                .plus(TraktTheme.size.navigationBarHeight * 2),
        )

        ScrollableBackdropImage(
            translation = listScrollConnection.resultOffset,
        )

        ContentList(
            listState = listState,
            listItems = (state.items ?: emptyList()).toImmutableList(),
            listFilter = state.filter,
            listSorting = state.sorting,
            collection = state.collection,
            loading = state.loading.isLoading,
            contentPadding = contentPadding,
            onFilterClick = onFilterClick,
            onSortTypeClick = onSortTypeClick,
            onSortOrderClick = onSortOrderClick,
            onClick = onClick,
            onCheckClick = onCheckClick,
            onCheckLongClick = onCheckLongClick,
            onLongClick = onLongClick,
            onTopOfList = onTopOfList,
            onBackClick = onBackClick,
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
                translationX = -2.dp.toPx()
            },
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_back_arrow),
            tint = TraktTheme.colors.textPrimary,
            contentDescription = null,
        )
        TraktHeader(
            title = stringResource(R.string.page_title_watchlist),
        )
    }
}

@Composable
private fun ContentList(
    modifier: Modifier = Modifier,
    listState: LazyListState,
    listItems: ImmutableList<WatchlistItem>,
    listFilter: MediaMode?,
    listSorting: Sorting?,
    collection: UserCollectionState,
    loading: Boolean,
    contentPadding: PaddingValues,
    onClick: (WatchlistItem) -> Unit,
    onCheckClick: (WatchlistItem) -> Unit,
    onCheckLongClick: (WatchlistItem) -> Unit,
    onLongClick: (WatchlistItem) -> Unit,
    onFilterClick: (MediaMode) -> Unit,
    onSortTypeClick: () -> Unit,
    onSortOrderClick: () -> Unit,
    onTopOfList: () -> Unit,
    onBackClick: () -> Unit,
) {
    val isScrolledToTop by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0 &&
                listState.firstVisibleItemScrollOffset == 0
        }
    }

    LaunchedEffect(isScrolledToTop) {
        if (isScrolledToTop) {
            onTopOfList()
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
                    .onClick { onBackClick() },
            )
        }

        if (listFilter != null && listSorting != null && !loading) {
            item {
                ContentFilters(
                    watchlistFilter = listFilter,
                    watchlistSort = listSorting,
                    onFilterClick = onFilterClick,
                    onSortTypeClick = onSortTypeClick,
                    onSortOrderClick = onSortOrderClick,
                )
            }
        }

        items(
            items = listItems,
            key = { it.key },
        ) { item ->
            when (item) {
                is ShowItem -> AllWatchlistShowView(
                    item = item,
                    watched = collection.isWatched(item.id),
                    onClick = { onClick(item) },
                    onLongClick = { onLongClick(item) },
                    modifier = Modifier
                        .padding(bottom = TraktTheme.spacing.mainListVerticalSpace)
                        .animateItem(
                            fadeInSpec = null,
                            fadeOutSpec = null,
                        ),
                )

                is MovieItem -> AllWatchlistMovieView(
                    item = item,
                    watched = collection.isWatched(item.id),
                    onClick = { onClick(item) },
                    onLongClick = { onLongClick(item) },
                    onCheckClick = { onCheckClick(item) },
                    onCheckLongClick = { onCheckLongClick(item) },
                    modifier = Modifier
                        .padding(bottom = TraktTheme.spacing.mainListVerticalSpace)
                        .animateItem(
                            fadeInSpec = null,
                            fadeOutSpec = null,
                        ),
                )
            }
        }
    }
}

@Composable
private fun ContentFilters(
    watchlistFilter: MediaMode,
    watchlistSort: Sorting,
    onSortTypeClick: () -> Unit,
    onSortOrderClick: () -> Unit,
    onFilterClick: (MediaMode) -> Unit,
) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 19.dp),
    ) {
        MediaModeFilters(
            selected = watchlistFilter,
            onClick = onFilterClick,
            height = 32.dp,
            unselectedTextVisible = false,
            paddingVertical = PaddingValues(
                top = 0.dp,
                bottom = 0.dp,
            ),
        )

        SortingSplitButton(
            text = stringResource(watchlistSort.type.displayStringRes),
            order = watchlistSort.order,
            height = 32.dp,
            onLeadingClick = onSortTypeClick,
            onTrailingClick = onSortOrderClick,
        )
    }
}

@Composable
private fun WatchlistDateSelectionSheet(
    item: WatchlistItem?,
    onDateSelected: (DateSelectionResult?) -> Unit,
    onDismiss: () -> Unit,
) {
    DateSelectionSheet(
        active = item != null,
        title = item?.title.orEmpty(),
        subtitle = when (item) {
            is ShowItem -> item.progress?.nextEpisode?.seasonEpisodeString()
            is MovieItem -> null
            else -> null
        },
        onResult = {
            if (item == null) return@DateSelectionSheet
            onDateSelected(it)
        },
        onDismiss = onDismiss,
    )
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        AllWatchlistContent(
            state = AllWatchlistState(),
        )
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview2() {
    TraktTheme {
        AllWatchlistContent(
            state = AllWatchlistState(),
        )
    }
}
