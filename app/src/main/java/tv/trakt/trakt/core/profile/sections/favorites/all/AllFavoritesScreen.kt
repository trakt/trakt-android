@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package tv.trakt.trakt.core.profile.sections.favorites.all

import androidx.compose.animation.core.tween
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
import androidx.compose.material3.Icon
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.toLocalDay
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.sorting.SortType
import tv.trakt.trakt.common.model.sorting.SortType.Added
import tv.trakt.trakt.common.model.sorting.SortType.Default
import tv.trakt.trakt.common.model.sorting.SortType.Released
import tv.trakt.trakt.common.model.sorting.SortType.Title
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.core.favorites.model.FavoriteItem
import tv.trakt.trakt.core.favorites.model.FavoriteItem.MovieItem
import tv.trakt.trakt.core.favorites.model.FavoriteItem.ShowItem
import tv.trakt.trakt.core.profile.sections.favorites.all.views.AllFavoritesMovieView
import tv.trakt.trakt.core.profile.sections.favorites.all.views.AllFavoritesShowView
import tv.trakt.trakt.core.profile.sections.favorites.context.movie.FavoriteMovieContextSheet
import tv.trakt.trakt.core.profile.sections.favorites.context.show.FavoriteShowContextSheet
import tv.trakt.trakt.helpers.SimpleScrollConnection
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.MediaModeFilters
import tv.trakt.trakt.ui.components.ScrollableBackdropImage
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.sorting.SortingSplitButton
import tv.trakt.trakt.ui.components.sorting.sheets.SortSelectionSheet
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun AllFavoritesScreen(
    modifier: Modifier = Modifier,
    viewModel: AllFavoritesViewModel,
    onNavigateBack: () -> Unit,
    onShowClick: (TraktId) -> Unit,
    onMovieClick: (TraktId) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var contextMovieSheet by remember { mutableStateOf<Movie?>(null) }
    var contextShowSheet by remember { mutableStateOf<Show?>(null) }
    var sortSheet by remember { mutableStateOf<Sorting?>(null) }

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

    AllFavoritesContent(
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
                is MovieItem -> contextMovieSheet = it.movie
                is ShowItem -> contextShowSheet = it.show
            }
        },
        onFilterClick = { viewModel.setFilter(it) },
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

    FavoriteMovieContextSheet(
        movie = contextMovieSheet,
        onDismiss = { contextMovieSheet = null },
    )

    FavoriteShowContextSheet(
        show = contextShowSheet,
        onDismiss = { contextShowSheet = null },
    )

    SortSelectionSheet(
        active = sortSheet != null,
        selectedSorting = sortSheet,
        typeOptions = remember {
            SortType.entries
                .filter { it != Default }
                .toImmutableList()
        },
        onResult = viewModel::setSorting,
        onDismiss = {
            sortSheet = null
        },
    )
}

@Composable
internal fun AllFavoritesContent(
    state: AllFavoritesState,
    modifier: Modifier = Modifier,
    onClick: (FavoriteItem) -> Unit = {},
    onLongClick: (FavoriteItem) -> Unit = {},
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
            listItems = (state.items ?: emptyList()).toImmutableList(),
            listState = listState,
            listFilter = state.filter ?: MediaMode.MEDIA,
            listSorting = state.sorting,
            listLoading = state.loading,
            contentPadding = contentPadding,
            onFilterClick = onFilterClick,
            onSortTypeClick = onSortTypeClick,
            onSortOrderClick = onSortOrderClick,
            onClick = onClick,
            onLongClick = onLongClick,
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
            title = stringResource(R.string.list_title_favorites),
        )
    }
}

@Composable
private fun ContentList(
    modifier: Modifier = Modifier,
    listState: LazyListState,
    listItems: ImmutableList<FavoriteItem>,
    listFilter: MediaMode,
    listSorting: Sorting,
    listLoading: LoadingState,
    contentPadding: PaddingValues,
    onClick: (FavoriteItem) -> Unit,
    onLongClick: (FavoriteItem) -> Unit,
    onFilterClick: (MediaMode) -> Unit,
    onSortTypeClick: () -> Unit,
    onSortOrderClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    val itemsGroup = remember(listItems) {
        when (listSorting.type) {
            Title -> listItems.groupBy { it.title.firstOrNull()?.uppercaseChar()?.toString() ?: "#" }
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
                modifier = Modifier
                    .onClick { onBackClick() },
            )
        }

        item {
            ContentFilters(
                watchlistFilter = listFilter,
                watchlistSort = listSorting,
                onFilterClick = onFilterClick,
                onSortTypeClick = onSortTypeClick,
                onSortOrderClick = onSortOrderClick,
            )
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
                    is ShowItem -> AllFavoritesShowView(
                        item = item,
                        sorting = listSorting,
                        mediaIcon = listFilter == MediaMode.MEDIA,
                        onClick = { onClick(item) },
                        onLongClick = { onLongClick(item) },
                        modifier = Modifier
                            .padding(bottom = TraktTheme.spacing.mainListVerticalSpace)
                            .animateItem(
                                fadeInSpec = null,
                                fadeOutSpec = null,
                            ),
                    )

                    is MovieItem -> AllFavoritesMovieView(
                        item = item,
                        sorting = listSorting,
                        mediaIcon = listFilter == MediaMode.MEDIA,
                        onClick = { onClick(item) },
                        onLongClick = { onLongClick(item) },
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

        if (listItems.isEmpty() && listLoading.isDone) {
            item {
                ContentEmptyView(
                    modifier = Modifier
                        .animateItem(
                            fadeInSpec = tween(200),
                            fadeOutSpec = tween(200),
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
private fun ContentEmptyView(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.list_placeholder_empty),
        color = TraktTheme.colors.textSecondary,
        style = TraktTheme.typography.heading6,
        modifier = modifier,
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
        AllFavoritesContent(
            state = AllFavoritesState(),
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
        AllFavoritesContent(
            state = AllFavoritesState(),
        )
    }
}
