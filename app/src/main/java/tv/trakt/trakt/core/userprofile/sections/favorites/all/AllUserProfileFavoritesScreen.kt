@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package tv.trakt.trakt.core.userprofile.sections.favorites.all

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
import kotlinx.collections.immutable.toImmutableMap
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.core.favorites.FavoriteItem
import tv.trakt.trakt.common.core.favorites.FavoriteItem.MovieItem
import tv.trakt.trakt.common.core.favorites.FavoriteItem.ShowItem
import tv.trakt.trakt.common.core.user.UserCollectionState
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.toLocalDay
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.sorting.SortType.Added
import tv.trakt.trakt.common.model.sorting.SortType.Released
import tv.trakt.trakt.common.model.sorting.SortType.Title
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.core.profile.sections.favorites.all.views.AllFavoritesMovieView
import tv.trakt.trakt.core.profile.sections.favorites.all.views.AllFavoritesShowView
import tv.trakt.trakt.helpers.SimpleScrollConnection
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.MediaModeFilters
import tv.trakt.trakt.ui.components.ScrollableBackdropImage
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.mediacards.skeletons.PanelMediaSkeletonCard
import tv.trakt.trakt.ui.components.sorting.SortingSplitButton
import tv.trakt.trakt.ui.components.sorting.sheets.SortSelectionSheet
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun AllUserProfileFavoritesScreen(
    modifier: Modifier = Modifier,
    viewModel: AllUserProfileFavoritesViewModel = koinViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var sortSheet by remember { mutableStateOf<Sorting?>(null) }

    LaunchedEffect(
        state.navigateShow,
        state.navigateMovie,
    ) {
        state.navigateShow?.let {
            onNavigateToShow(it)
            viewModel.clearNavigation()
        }
        state.navigateMovie?.let {
            onNavigateToMovie(it)
            viewModel.clearNavigation()
        }
    }

    AllUserProfileFavoritesContent(
        state = state,
        modifier = modifier,
        onBackClick = onNavigateBack,
        onShowClick = { viewModel.navigateToShow(it) },
        onMovieClick = { viewModel.navigateToMovie(it) },
        onModeClick = viewModel::setFilter,
        onSortTypeClick = { sortSheet = state.sorting },
        onSortOrderClick = {
            val sorting = state.sorting
            viewModel.setSorting(sorting.copy(order = sorting.order.toggle()))
        },
    )

    SortSelectionSheet(
        active = sortSheet != null,
        selectedSorting = sortSheet,
        onResult = {
            viewModel.setSorting(
                state.sorting.copy(
                    type = it.type,
                    order = it.order,
                ),
            )
        },
        onDismiss = { sortSheet = null },
    )
}

@Composable
internal fun AllUserProfileFavoritesContent(
    state: AllUserProfileFavoritesState,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onShowClick: (Show) -> Unit = {},
    onMovieClick: (Movie) -> Unit = {},
    onModeClick: (MediaMode) -> Unit = {},
    onSortTypeClick: () -> Unit = {},
    onSortOrderClick: () -> Unit = {},
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
            listItems = state.items,
            collectionState = state.collection,
            filter = state.filter,
            sorting = state.sorting,
            contentPadding = contentPadding,
            loading = state.loading.isLoading,
            onBackClick = onBackClick,
            onShowClick = onShowClick,
            onMovieClick = onMovieClick,
            onModeClick = onModeClick,
            onSortTypeClick = onSortTypeClick,
            onSortOrderClick = onSortOrderClick,
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
        TraktHeader(
            title = stringResource(R.string.list_title_favorites),
        )
    }
}

@Composable
private fun ContentList(
    modifier: Modifier = Modifier,
    listState: LazyListState,
    listItems: ImmutableList<FavoriteItem>?,
    collectionState: UserCollectionState,
    filter: MediaMode,
    sorting: Sorting,
    contentPadding: PaddingValues,
    loading: Boolean,
    onBackClick: () -> Unit,
    onShowClick: (Show) -> Unit,
    onMovieClick: (Movie) -> Unit,
    onModeClick: (MediaMode) -> Unit,
    onSortTypeClick: () -> Unit,
    onSortOrderClick: () -> Unit,
) {
    val itemsGroup = remember(listItems) {
        when (sorting.type) {
            Title -> listItems?.groupBy { it.title.first().uppercaseChar().toString() }
            Added -> listItems?.groupBy { it.listedAt.toLocalDay().year.toString() }
            Released -> listItems?.groupBy { it.released?.toLocalDay()?.year.toString().ifEmpty { "N/A" } }
            else -> listItems?.groupBy { null }
        }?.toImmutableMap()
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
            ContentFilters(
                filter = filter,
                sorting = sorting,
                onFilterClick = onModeClick,
                onSortTypeClick = onSortTypeClick,
                onSortOrderClick = onSortOrderClick,
            )
        }

        itemsGroup?.keys?.forEachIndexed { index, key ->
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
                        sorting = sorting,
                        loading = loading,
                        mediaIcon = filter == MediaMode.Media,
                        watched = collectionState.isWatched(item.id, item.type, item.airedEpisodes),
                        plays = collectionState.plays(item.id, item.type, item.airedEpisodes),
                        watching = collectionState.isWatching(item.id, item.type, item.airedEpisodes),
                        watchlist = collectionState.isWatchlist(item.id, item.type),
                        onClick = { onShowClick(item.show) },
                        onLongClick = {},
                        modifier = Modifier
                            .padding(bottom = TraktTheme.spacing.mainListVerticalSpace)
                            .animateItem(fadeInSpec = null, fadeOutSpec = null),
                    )

                    is MovieItem -> AllFavoritesMovieView(
                        item = item,
                        sorting = sorting,
                        loading = loading,
                        mediaIcon = filter == MediaMode.Media,
                        watched = collectionState.isWatched(item.id, item.type, item.airedEpisodes),
                        plays = collectionState.plays(item.id, item.type, item.airedEpisodes),
                        watchlist = collectionState.isWatchlist(item.id, item.type),
                        onClick = { onMovieClick(item.movie) },
                        onLongClick = {},
                        modifier = Modifier
                            .padding(bottom = TraktTheme.spacing.mainListVerticalSpace)
                            .animateItem(fadeInSpec = null, fadeOutSpec = null),
                    )
                }
            }
        }

        if (loading && listItems.isNullOrEmpty()) {
            items(5) {
                PanelMediaSkeletonCard(
                    modifier = Modifier
                        .padding(bottom = TraktTheme.spacing.mainListVerticalSpace)
                        .animateItem(fadeInSpec = null, fadeOutSpec = null),
                )
            }
        }

        if (listItems?.isEmpty() == true && !loading) {
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

@Composable
private fun ContentFilters(
    filter: MediaMode,
    sorting: Sorting,
    onFilterClick: (MediaMode) -> Unit,
    onSortTypeClick: () -> Unit,
    onSortOrderClick: () -> Unit,
) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 19.dp),
    ) {
        MediaModeFilters(
            selected = filter,
            height = 32.dp,
            onClick = onFilterClick,
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

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        AllUserProfileFavoritesContent(
            state = AllUserProfileFavoritesState(
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
        AllUserProfileFavoritesContent(
            state = AllUserProfileFavoritesState(
                loading = LoadingState.Loading,
            ),
        )
    }
}
