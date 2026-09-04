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
import androidx.compose.material3.Text
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
import kotlinx.collections.immutable.toImmutableMap
import tv.trakt.trakt.common.core.lists.model.WatchlistItem
import tv.trakt.trakt.common.core.lists.model.WatchlistItem.MovieItem
import tv.trakt.trakt.common.core.lists.model.WatchlistItem.ShowItem
import tv.trakt.trakt.common.core.user.UserCollectionState
import tv.trakt.trakt.common.helpers.extensions.DevicePreview
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.toLocalDay
import tv.trakt.trakt.common.model.DateSelectionResult
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.MediaType.Movie
import tv.trakt.trakt.common.model.MediaType.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.sorting.SortType.Added
import tv.trakt.trakt.common.model.sorting.SortType.Released
import tv.trakt.trakt.common.model.sorting.SortType.Title
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.core.filters.GlobalFiltersSheet
import tv.trakt.trakt.core.filters.navigation.GlobalFiltersOptions
import tv.trakt.trakt.core.lists.sections.watchlist.features.all.views.AllWatchlistMovieView
import tv.trakt.trakt.core.lists.sections.watchlist.features.all.views.AllWatchlistShowView
import tv.trakt.trakt.core.lists.sections.watchlist.features.context.movies.sheets.WatchlistMovieSheet
import tv.trakt.trakt.core.lists.sections.watchlist.features.context.shows.sheets.WatchlistShowSheet
import tv.trakt.trakt.helpers.SimpleScrollConnection
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.MediaFilterIcon
import tv.trakt.trakt.ui.components.MediaModeFilters
import tv.trakt.trakt.ui.components.ScrollableBackdropImage
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.dateselection.DateSelectionSheet
import tv.trakt.trakt.ui.components.mediacards.skeletons.PanelMediaSkeletonCard
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
    var sortSheet by remember { mutableStateOf<Sorting?>(null) }
    var filtersSheet by remember { mutableStateOf(false) }

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
        onLoadMoreData = viewModel::loadMoreData,
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
        onModeClick = { mode ->
            state.filter?.let {
                viewModel.setFilter(it.copy(mode = mode))
            }
        },
        onFiltersClick = {
            filtersSheet = true
        },
        onSortTypeClick = {
            sortSheet = state.sorting
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
            state.collection.isWatched(it, Movie, null)
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
        watched = contextShowSheet?.show?.let {
            state.collection.isWatched(it.ids.trakt, Show, it.airedEpisodes)
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
                    is ShowItem -> Unit
                    is MovieItem -> viewModel.addMovieToHistory(
                        movieId = it.id,
                        customDate = date,
                    )
                }
            }
        },
        onDismiss = {
            dateSheet = null
        },
    )

    SortSelectionSheet(
        active = sortSheet != null,
        selectedSorting = sortSheet,
        onResult = viewModel::setSorting,
        onDismiss = {
            sortSheet = null
        },
    )

    GlobalFiltersSheet(
        active = filtersSheet,
        options = GlobalFiltersOptions(
            global = false,
            initial = state.filter,
        ),
        onUpdate = viewModel::setFilter,
        onDismiss = {
            filtersSheet = false
        },
    )
}

@Composable
internal fun AllWatchlistContent(
    state: AllWatchlistState,
    modifier: Modifier = Modifier,
    onClick: (WatchlistItem) -> Unit = {},
    onCheckClick: (WatchlistItem) -> Unit = {},
    onCheckLongClick: (WatchlistItem) -> Unit = {},
    onLongClick: (WatchlistItem) -> Unit = {},
    onModeClick: (MediaMode) -> Unit = {},
    onFiltersClick: () -> Unit = {},
    onSortTypeClick: () -> Unit = {},
    onSortOrderClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onLoadMoreData: () -> Unit = {},
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
            loadingMore = state.loadingMore.isLoading,
            contentPadding = contentPadding,
            onModeClick = onModeClick,
            onFiltersClick = onFiltersClick,
            onSortTypeClick = onSortTypeClick,
            onSortOrderClick = onSortOrderClick,
            onClick = onClick,
            onCheckClick = onCheckClick,
            onCheckLongClick = onCheckLongClick,
            onLongClick = onLongClick,
            onBackClick = onBackClick,
            onEndOfList = onLoadMoreData,
        )
    }
}

@Composable
private fun TitleBar(
    enabled: Boolean,
    filters: GlobalFilter?,
    onFiltersClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .height(TraktTheme.size.titleBarHeight)
            .graphicsLayer {
                translationX = -2.dp.toPx()
                translationY = 2.dp.toPx()
            },
    ) {
        Row(
            horizontalArrangement = spacedBy(12.dp),
            verticalAlignment = CenterVertically,
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

        filters?.let {
            MediaFilterIcon(
                active = it.isActive,
                enabled = enabled,
                onClick = onFiltersClick,
                modifier = Modifier
                    .graphicsLayer {
                        translationX = 2.dp.toPx()
                    },
            )
        }
    }
}

@Composable
private fun ContentList(
    modifier: Modifier = Modifier,
    listState: LazyListState,
    listItems: ImmutableList<WatchlistItem>,
    listFilter: GlobalFilter?,
    listSorting: Sorting,
    collection: UserCollectionState,
    loading: Boolean,
    loadingMore: Boolean,
    contentPadding: PaddingValues,
    onClick: (WatchlistItem) -> Unit,
    onCheckClick: (WatchlistItem) -> Unit,
    onCheckLongClick: (WatchlistItem) -> Unit,
    onLongClick: (WatchlistItem) -> Unit,
    onModeClick: (MediaMode) -> Unit,
    onFiltersClick: () -> Unit,
    onSortTypeClick: () -> Unit,
    onSortOrderClick: () -> Unit,
    onBackClick: () -> Unit,
    onEndOfList: () -> Unit,
) {
    val isScrolledToBottom by remember(listItems.size) {
        derivedStateOf {
            listState.firstVisibleItemIndex >= (listItems.size - 5)
        }
    }

    LaunchedEffect(isScrolledToBottom) {
        if (isScrolledToBottom) {
            onEndOfList()
        }
    }

    val itemsGroup = remember(listItems) {
        when (listSorting.type) {
            Title -> listItems.groupBy { it.titleNormalized.firstOrNull()?.uppercaseChar()?.toString() ?: "#" }
            Added -> listItems.groupBy { it.listedAt.toLocalDay().year.toString() }
            Released -> listItems.groupBy { it.released?.toLocalDay()?.year?.toString() ?: "N/A" }
            else -> listItems.groupBy { null }
        }.toImmutableMap()
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
                enabled = !loading,
                filters = listFilter,
                onFiltersClick = onFiltersClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .onClick { onBackClick() },
            )
        }

        if (listFilter != null) {
            item {
                ContentFilters(
                    filters = listFilter,
                    sorting = listSorting,
                    onModeClick = onModeClick,
                    onSortTypeClick = onSortTypeClick,
                    onSortOrderClick = onSortOrderClick,
                )
            }
        }

        itemsGroup.keys.forEachIndexed { index, key ->
            key?.let {
                item(
                    key = "header-$key",
                ) {
                    TraktHeader(
                        title = key,
                        modifier = Modifier
                            .padding(
                                top = if (index == 0) 0.dp else 16.dp,
                                bottom = 12.dp,
                            )
                            .animateItem(
                                fadeInSpec = null,
                                fadeOutSpec = null,
                            ),
                    )
                }
            }

            items(
                items = itemsGroup[key] ?: EmptyImmutableList,
                key = { it.key },
            ) { item ->
                when (item) {
                    is ShowItem -> AllWatchlistShowView(
                        item = item,
                        sorting = listSorting,
                        enabled = !loading,
                        watched = collection.isWatched(item.id, Show, item.airedEpisodes),
                        plays = collection.plays(item.id, Show, item.airedEpisodes),
                        watching = collection.isWatching(item.id, Show, item.airedEpisodes),
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
                        sorting = listSorting,
                        enabled = !loading,
                        watched = collection.isWatched(item.id, Movie, null),
                        plays = collection.plays(item.id, Movie, null),
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

        if (loading && listItems.isEmpty()) {
            items(5) {
                PanelMediaSkeletonCard(
                    modifier = Modifier
                        .padding(bottom = TraktTheme.spacing.mainListVerticalSpace)
                        .animateItem(
                            fadeInSpec = null,
                            fadeOutSpec = null,
                        ),
                )
            }
        } else if (loadingMore && listItems.isNotEmpty()) {
            items(1) {
                PanelMediaSkeletonCard(
                    modifier = Modifier
                        .padding(bottom = TraktTheme.spacing.mainListVerticalSpace)
                        .animateItem(
                            fadeInSpec = null,
                            fadeOutSpec = null,
                        ),
                )
            }
        } else if (listItems.isEmpty()) {
            item {
                ContentEmpty(mode = listFilter?.mode)
            }
        }
    }
}

@Composable
private fun ContentFilters(
    filters: GlobalFilter,
    sorting: Sorting,
    onSortTypeClick: () -> Unit,
    onSortOrderClick: () -> Unit,
    onModeClick: (MediaMode) -> Unit,
) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = 2.dp,
                bottom = 19.dp,
            ),
    ) {
        MediaModeFilters(
            selected = filters.mode,
            onClick = onModeClick,
            height = 32.dp,
            unselectedTextVisible = false,
        )

        SortingSplitButton(
            text = stringResource(sorting.type.displayStringRes),
            order = sorting.order,
            height = 32.dp,
            onLeadingClick = onSortTypeClick,
            onTrailingClick = onSortOrderClick,
        )
    }
}

@Composable
private fun ContentEmpty(mode: MediaMode?) {
    Text(
        text = stringResource(
            when (mode) {
                MediaMode.Movies -> R.string.list_placeholder_personal_list_empty_movies
                MediaMode.Shows -> R.string.list_placeholder_personal_list_empty_shows
                else -> R.string.list_placeholder_empty
            },
        ),
        color = TraktTheme.colors.textSecondary,
        style = TraktTheme.typography.heading6,
    )
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
    locale = "ar",
)
@Composable
private fun Preview() {
    TraktTheme {
        AllWatchlistContent(
            state = AllWatchlistState(),
        )
    }
}

@DevicePreview
@Composable
private fun Preview2() {
    TraktTheme {
        AllWatchlistContent(
            state = AllWatchlistState(),
        )
    }
}
