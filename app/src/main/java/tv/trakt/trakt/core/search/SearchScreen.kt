@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.search

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.layout.LazyLayoutCacheWindow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.movies.ui.context.sheet.MovieContextSheet
import tv.trakt.trakt.core.search.model.SearchFilter
import tv.trakt.trakt.core.search.model.SearchFilter.MEDIA
import tv.trakt.trakt.core.search.model.SearchFilter.MOVIES
import tv.trakt.trakt.core.search.model.SearchFilter.PEOPLE
import tv.trakt.trakt.core.search.model.SearchFilter.SHOWS
import tv.trakt.trakt.core.search.model.SearchInput
import tv.trakt.trakt.core.search.model.SearchItem
import tv.trakt.trakt.core.search.views.SearchGridItem
import tv.trakt.trakt.core.shows.ui.context.sheet.ShowContextSheet
import tv.trakt.trakt.core.user.UserCollectionState
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.ScrollableBackdropImage
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.mediacards.skeletons.VerticalMediaSkeletonCard
import tv.trakt.trakt.ui.theme.TraktTheme

private val fadeSpec = tween<Float>(200)

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun SearchScreen(
    viewModel: SearchViewModel,
    searchInput: SearchInput,
    onSearchLoading: (Boolean) -> Unit,
    onShowClick: (TraktId) -> Unit,
    onMovieClick: (TraktId) -> Unit,
    onPersonClick: ((TraktId) -> Unit),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val contentGridState = rememberLazyGridState(
        cacheWindow = LazyLayoutCacheWindow(
            aheadFraction = 0.5F,
            behindFraction = 0.5F,
        ),
    )

    var showContextSheet by remember { mutableStateOf<Show?>(null) }
    var movieContextSheet by remember { mutableStateOf<Movie?>(null) }

    LaunchedEffect(state) {
        state.navigateShow?.let {
            viewModel.clearNavigation()
            onShowClick(it.ids.trakt)
        }
        state.navigateMovie?.let {
            viewModel.clearNavigation()
            onMovieClick(it.ids.trakt)
        }
        state.navigatePerson?.let {
            viewModel.clearNavigation()
            onPersonClick(it.ids.trakt)
        }
    }

    LaunchedEffect(searchInput) {
        viewModel.updateSearch(searchInput)

        contentGridState.scrollToItem(0)
    }

    LaunchedEffect(state.searching) {
        onSearchLoading(state.searching)
    }

    SearchScreenContent(
        state = state,
        contentGridState = contentGridState,
        onShowClick = { viewModel.navigateToShow(it) },
        onShowLongClick = {
            if (!state.searching) {
                showContextSheet = it
            }
        },
        onMovieClick = { viewModel.navigateToMovie(it) },
        onMovieLongClick = {
            if (!state.searching) {
                movieContextSheet = it
            }
        },
        onPersonClick = { viewModel.navigateToPerson(it) },
    )

    ShowContextSheet(
        show = showContextSheet,
        onDismiss = { showContextSheet = null },
    )

    MovieContextSheet(
        movie = movieContextSheet,
        onDismiss = { movieContextSheet = null },
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SearchScreenContent(
    state: SearchState,
    contentGridState: LazyGridState,
    modifier: Modifier = Modifier,
    onShowClick: (Show) -> Unit = {},
    onShowLongClick: (Show) -> Unit = {},
    onMovieClick: (Movie) -> Unit = {},
    onMovieLongClick: (Movie) -> Unit = {},
    onPersonClick: (Person) -> Unit = {},
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary),
    ) {
        ScrollableBackdropImage(
            scrollState = contentGridState,
        )

        ContentList(
            listState = contentGridState,
            searching = state.searching,
            query = state.input.query,
            filter = state.input.filter,
            popularItems = state.popularResults?.items ?: EmptyImmutableList,
            resultItems = state.searchResult?.items ?: EmptyImmutableList,
            collection = state.collection,
            onShowClick = onShowClick,
            onShowLongClick = onShowLongClick,
            onMovieClick = onMovieClick,
            onMovieLongClick = onMovieLongClick,
            onPersonClick = onPersonClick,
            error = state.error,
        )
    }
}

@Composable
private fun ContentList(
    listState: LazyGridState,
    searching: Boolean,
    query: String,
    filter: SearchFilter,
    popularItems: ImmutableList<SearchItem>,
    resultItems: ImmutableList<SearchItem>,
    collection: UserCollectionState,
    onShowClick: (Show) -> Unit = {},
    onMovieClick: (Movie) -> Unit = {},
    onShowLongClick: (Show) -> Unit = {},
    onMovieLongClick: (Movie) -> Unit = {},
    onPersonClick: (Person) -> Unit = {},
    error: Exception? = null,
) {
    val topPadding = WindowInsets.statusBars.asPaddingValues()
        .calculateTopPadding()
        .plus(8.dp)

    val contentPadding = PaddingValues(
        start = TraktTheme.spacing.mainPageHorizontalSpace,
        end = TraktTheme.spacing.mainPageHorizontalSpace,
        bottom = WindowInsets.navigationBars.asPaddingValues()
            .calculateBottomPadding()
            .plus(TraktTheme.size.navigationBarHeight * 2)
            .plus(TraktTheme.spacing.mainPageBottomSpace),
    )

    val isSearching = remember(searching, resultItems) {
        searching || resultItems.isNotEmpty()
    }

    LazyVerticalGrid(
        state = listState,
        columns = GridCells.Fixed(TraktTheme.size.mainGridColumns),
        horizontalArrangement = spacedBy(TraktTheme.spacing.mainGridHorizontalSpace),
        verticalArrangement = spacedBy(TraktTheme.spacing.mainGridVerticalSpace),
        contentPadding = contentPadding,
        overscrollEffect = null,
    ) {
        if (!isSearching && query.isBlank()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                TraktHeader(
                    title = when (filter) {
                        MEDIA, SHOWS, MOVIES -> stringResource(R.string.list_title_most_popular_searches)
                        PEOPLE -> stringResource(R.string.list_title_birthdays_this_month)
                    },
                    modifier = Modifier
                        .padding(top = topPadding)
                        .animateItem(
                            fadeInSpec = fadeSpec,
                            fadeOutSpec = fadeSpec,
                        ),
                )
            }

            if (error == null) {
                items(
                    count = popularItems.size,
                    key = { index -> "${popularItems[index].key}_popular" },
                ) { index ->
                    val item = popularItems[index]
                    SearchGridItem(
                        item = item,
                        filter = filter,
                        watched = collection.isWatched(item.id, item.type),
                        watchlist = collection.isWatchlist(item.id, item.type),
                        onShowClick = onShowClick,
                        onShowLongClick = onShowLongClick,
                        onMovieClick = onMovieClick,
                        onMovieLongClick = onMovieLongClick,
                        onPersonClick = onPersonClick,
                        modifier = Modifier
                            .padding(bottom = 6.dp)
                            .animateItem(
                                fadeInSpec = fadeSpec,
                                fadeOutSpec = fadeSpec,
                            ),
                    )
                }
            }
        }

        if (isSearching) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                TraktHeader(
                    title = stringResource(filter.displayRes),
                    modifier = Modifier
                        .padding(top = topPadding)
                        .animateItem(
                            fadeInSpec = fadeSpec,
                            fadeOutSpec = fadeSpec,
                        ),
                )
            }
            when {
                resultItems.isNotEmpty() -> {
                    items(
                        count = resultItems.size,
                        key = { index -> resultItems[index].key },
                    ) { index ->
                        val item = resultItems[index]
                        SearchGridItem(
                            item = item,
                            filter = filter,
                            watched = collection.isWatched(item.id, item.type),
                            watchlist = collection.isWatchlist(item.id, item.type),
                            onShowClick = onShowClick,
                            onShowLongClick = onShowLongClick,
                            onMovieClick = onMovieClick,
                            onMovieLongClick = onMovieLongClick,
                            onPersonClick = onPersonClick,
                            modifier = Modifier
                                .padding(bottom = 6.dp)
                                .animateItem(
                                    fadeInSpec = fadeSpec,
                                    fadeOutSpec = fadeSpec,
                                ),
                        )
                    }
                }

                else -> {
                    items(count = 12) {
                        VerticalMediaSkeletonCard(
                            chipRatio = 0.66F,
                            chipSpacing = 8.dp,
                            modifier = Modifier
                                .padding(bottom = 9.25.dp)
                                .animateItem(
                                    fadeInSpec = fadeSpec,
                                    fadeOutSpec = fadeSpec,
                                ),
                        )
                    }
                }
            }
        }

        if (error != null) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = "${stringResource(R.string.error_text_unexpected_error_short)}\n\n$error",
                    color = TraktTheme.colors.textSecondary,
                    style = TraktTheme.typography.meta,
                    maxLines = 10,
                )
            }
        }
    }
}

@Preview(
    device = "id:pixel_9",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        SearchScreenContent(
            state = SearchState(),
            contentGridState = rememberLazyGridState(),
        )
    }
}
