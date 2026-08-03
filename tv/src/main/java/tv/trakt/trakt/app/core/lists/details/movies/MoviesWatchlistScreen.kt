package tv.trakt.trakt.app.core.lists.details.movies

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.app.common.ui.GenericErrorView
import tv.trakt.trakt.app.common.ui.mediacards.VerticalMediaCard
import tv.trakt.trakt.app.core.details.ui.BackdropImage
import tv.trakt.trakt.app.core.lists.ListsConfig.LISTS_NEXT_PAGE_OFFSET
import tv.trakt.trakt.app.core.lists.filters.TvListControlsState
import tv.trakt.trakt.app.core.lists.filters.TvListEmptyState
import tv.trakt.trakt.app.core.lists.filters.TvListFilterConfiguration
import tv.trakt.trakt.app.core.lists.filters.TvListHeader
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.helpers.extensions.rememberDurationFormat
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Ids
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.SlugId
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.resources.R

@Composable
internal fun MoviesWatchlistScreen(
    viewModel: MoviesWatchlistViewModel,
    onNavigateToMovie: (TraktId) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    MoviesWatchlistContent(
        state = state,
        onMovieClick = onNavigateToMovie,
        onLoadNextPage = { viewModel.loadNextDataPage() },
        onFilterApplied = viewModel::applyFilter,
        onSortingApplied = viewModel::applySorting,
    )
}

@Composable
private fun MoviesWatchlistContent(
    state: MoviesWatchlistState,
    modifier: Modifier = Modifier,
    onMovieClick: (TraktId) -> Unit,
    onLoadNextPage: () -> Unit,
    onFilterApplied: (GlobalFilter) -> Unit,
    onSortingApplied: (Sorting) -> Unit,
) {
    var focusedMovie by remember { mutableStateOf<Movie?>(null) }
    var focusedMovieId by rememberSaveable { mutableStateOf<Int?>(null) }
    val focusRequesters = remember { mutableMapOf<Int, FocusRequester>() }

    Box(
        contentAlignment = Alignment.TopStart,
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary)
            .focusProperties {
                onEnter = {
                    focusRequesters[focusedMovieId]?.requestFocus()
                }
            },
    ) {
        BackdropImage(
            imageUrl = focusedMovie?.images?.getFanartUrl(Images.Size.FULL),
            saturation = 0F,
            crossfade = true,
        )

        val gridSpace = TraktTheme.spacing.mainGridSpace
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = TraktTheme.size.verticalMediaCardSize),
            horizontalArrangement = Arrangement.spacedBy(gridSpace),
            verticalArrangement = Arrangement.spacedBy(gridSpace * 2),
            contentPadding = PaddingValues(
                start = TraktTheme.spacing.mainContentStartSpace,
                end = TraktTheme.spacing.mainContentEndSpace,
                top = 30.dp,
                bottom = TraktTheme.spacing.mainContentVerticalSpace,
            ),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                TvListHeader(
                    title = stringResource(R.string.list_title_watchlist_movies),
                    controlsState = TvListControlsState(
                        filter = state.filter,
                        sorting = state.sorting,
                        configuration = TvListFilterConfiguration.MoviesWatchlist,
                    ),
                    downFocusRequester = focusRequesters.values.firstOrNull()
                        ?: FocusRequester.Default,
                    onFilterApplied = onFilterApplied,
                    onSortingApplied = onSortingApplied,
                )
            }

            if (state.isLoading && state.movies.isNullOrEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    FilmProgressIndicator(
                        modifier = Modifier.focusable(),
                    )
                }
            } else if (!state.movies.isNullOrEmpty()) {
                items(
                    count = state.movies.size,
                    key = { index -> state.movies[index].ids.trakt.value },
                ) { index ->
                    val movie = state.movies[index]
                    val focusRequester = remember(movie.ids.trakt.value) {
                        focusRequesters.getOrPut(movie.ids.trakt.value) {
                            FocusRequester()
                        }
                    }

                    VerticalMediaCard(
                        title = movie.title,
                        imageUrl = movie.images?.getPosterUrl(),
                        onClick = {
                            if (!state.isLoadingPage) {
                                onMovieClick(movie.ids.trakt)
                            }
                        },
                        chipContent = {
                            Column(
                                verticalArrangement = spacedBy(1.dp),
                            ) {
                                Text(
                                    text = buildString {
                                        append(movie.yearString)
                                        movie.runtime?.inWholeMinutes?.let {
                                            if (isNotEmpty()) append("  •  ")
                                            append(rememberDurationFormat(it))
                                        }
                                    },
                                    style = TraktTheme.typography.cardTitle,
                                    color = TraktTheme.colors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        },
                        modifier = Modifier
                            .focusRequester(focusRequester)
                            .onFocusChanged {
                                if (it.isFocused) {
                                    focusedMovie = movie
                                    focusedMovieId = movie.ids.trakt.value

                                    loadNextPageIfNeeded(
                                        size = state.movies.size,
                                        index = index,
                                        onLoadNextPage = onLoadNextPage,
                                    )
                                }
                            },
                    )
                }
            } else if (!state.isLoading) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    TvListEmptyState(
                        filter = state.filter,
                        modifier = Modifier.focusable(),
                    )
                }
            }

            if (state.isLoadingPage) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    FilmProgressIndicator(
                        modifier = Modifier.focusable(),
                    )
                }
            }
        }
    }

    if (state.error != null) {
        GenericErrorView(
            error = state.error,
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = TraktTheme.spacing.mainContentStartSpace,
                    end = TraktTheme.spacing.mainContentEndSpace,
                ),
        )
    }
}

private fun loadNextPageIfNeeded(
    size: Int,
    index: Int,
    onLoadNextPage: () -> Unit,
) {
    if (index >= (size - LISTS_NEXT_PAGE_OFFSET).coerceAtLeast(0)) {
        onLoadNextPage()
    }
}

@Preview(
    device = "id:tv_4k",
    showBackground = true,
    backgroundColor = 0xFF131517,
    heightDp = 1000,
)
@Composable
private fun Preview() {
    TraktTheme {
        MoviesWatchlistContent(
            state = MoviesWatchlistState(
                movies = (1..20).map {
                    PreviewData.movie1.copy(ids = Ids(TraktId(it), SlugId(it.toString())))
                }.toImmutableList(),
            ),
            onMovieClick = {},
            onLoadNextPage = {},
            onFilterApplied = {},
            onSortingApplied = {},
        )
    }
}
