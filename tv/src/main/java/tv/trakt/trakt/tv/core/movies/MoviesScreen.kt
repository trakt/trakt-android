package tv.trakt.trakt.tv.core.movies

import GenericErrorView
import InfoChip
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.tv.material3.Text
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.tv.R
import tv.trakt.trakt.tv.common.model.Images.Size
import tv.trakt.trakt.tv.common.ui.PositionFocusLazyRow
import tv.trakt.trakt.tv.common.ui.mediacards.HorizontalMediaCard
import tv.trakt.trakt.tv.common.ui.mediacards.HorizontalMediaSkeletonCard
import tv.trakt.trakt.tv.core.details.ui.BackdropImage
import tv.trakt.trakt.tv.core.movies.model.AnticipatedMovie
import tv.trakt.trakt.tv.core.movies.model.Movie
import tv.trakt.trakt.tv.core.movies.model.TrendingMovie
import tv.trakt.trakt.tv.helpers.extensions.durationFormat
import tv.trakt.trakt.tv.helpers.extensions.emptyFocusListItems
import tv.trakt.trakt.tv.helpers.extensions.thousandsFormat
import tv.trakt.trakt.tv.helpers.preview.PreviewData
import tv.trakt.trakt.tv.ui.theme.TraktTheme

private val sections = listOf(
    "initial",
    "content",
    "trending",
    "hot",
    "popular",
    "anticipated",
    "recommended",
)

@Composable
internal fun MoviesScreen(
    viewModel: MoviesViewModel,
    onNavigateToMovie: (TraktId) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    MoviesScreenContent(
        state = state,
        onMovieClick = onNavigateToMovie,
    )
}

@Composable
private fun MoviesScreenContent(
    state: MoviesState,
    modifier: Modifier = Modifier,
    onMovieClick: (TraktId) -> Unit,
) {
    var focusedMovie by remember { mutableStateOf<Movie?>(null) }
    var focusedSection by rememberSaveable { mutableStateOf<String?>(null) }

    val focusRequesters = remember {
        sections.associateBy(
            keySelector = { it },
            valueTransform = { FocusRequester() },
        )
    }

    LaunchedEffect(state.isLoading, state.trendingMovies?.size) {
        if (!state.isLoading && state.trendingMovies != null) {
            focusRequesters.getValue("content").requestFocus()
        } else {
            focusRequesters.getValue("initial").requestFocus()
        }
    }

    Box(
        contentAlignment = Alignment.TopStart,
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary)
            .focusProperties {
                onEnter = {
                    focusRequesters[focusedSection]?.requestFocus()
                }
            },
    ) {
        BackdropImage(
            imageUrl = focusedMovie?.images?.getFanartUrl(Size.FULL),
            saturation = 0F,
            crossfade = true,
        )

        if (state.error == null) {
            LazyColumn(
                verticalArrangement = spacedBy(TraktTheme.spacing.mainRowVerticalSpace),
                contentPadding = PaddingValues(
                    top = TraktTheme.spacing.mainContentVerticalSpace + 8.dp,
                    bottom = TraktTheme.spacing.mainContentVerticalSpace,
                ),
                modifier = Modifier
                    .focusGroup()
                    .focusRequester(focusRequesters.getValue("content")),
            ) {
                item {
                    TrendingMoviesList(
                        header = stringResource(R.string.header_trending),
                        movies = state.trendingMovies,
                        isLoading = state.isLoading,
                        onMovieClick = onMovieClick,
                        onMovieFocus = {
                            focusedMovie = it
                            focusedSection = "trending"
                        },
                        focusRequesters = focusRequesters,
                        modifier = Modifier
                            .focusGroup()
                            .focusRequester(focusRequesters.getValue("trending")),
                    )
                }

                item {
                    HotMoviesList(
                        header = stringResource(R.string.header_hot_month),
                        movies = state.hotMovies,
                        isLoading = state.isLoading,
                        onMovieFocus = {
                            focusedMovie = it
                            focusedSection = "hot"
                        },
                        onMovieClick = onMovieClick,
                        modifier = Modifier
                            .focusGroup()
                            .focusRequester(focusRequesters.getValue("hot")),
                    )
                }

                item {
                    AnticipatedMoviesList(
                        header = stringResource(R.string.header_most_anticipated),
                        movies = state.anticipatedMovies,
                        isLoading = state.isLoading,
                        onMovieFocus = {
                            focusedMovie = it
                            focusedSection = "anticipated"
                        },
                        onMovieClick = onMovieClick,
                        modifier = Modifier
                            .focusGroup()
                            .focusRequester(focusRequesters.getValue("anticipated")),
                    )
                }

                item {
                    StandardMoviesList(
                        header = stringResource(R.string.header_most_popular),
                        movies = state.popularMovies,
                        isLoading = state.isLoading,
                        onMovieFocus = {
                            focusedMovie = it
                            focusedSection = "popular"
                        },
                        onMovieClick = onMovieClick,
                        modifier = Modifier
                            .focusGroup()
                            .focusRequester(focusRequesters.getValue("popular")),
                    )
                }

                if (state.recommendedMovies != null) {
                    item {
                        StandardMoviesList(
                            header = stringResource(R.string.header_recommended),
                            movies = state.recommendedMovies,
                            isLoading = state.isLoading,
                            onMovieFocus = {
                                focusedMovie = it
                                focusedSection = "recommended"
                            },
                            onMovieClick = onMovieClick,
                            modifier = Modifier
                                .focusGroup()
                                .focusRequester(focusRequesters.getValue("recommended")),
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

        Text(
            text = "",
            modifier = modifier
                .align(Alignment.Center)
                .focusRequester(focusRequesters.getValue("initial"))
                .focusable(state.isLoading),
        )
    }
}

@Composable
private fun TrendingMoviesList(
    header: String,
    movies: List<TrendingMovie>?,
    isLoading: Boolean,
    onMovieFocus: (Movie) -> Unit,
    onMovieClick: (TraktId) -> Unit,
    focusRequesters: Map<String, FocusRequester>,
    modifier: Modifier = Modifier,
) {
    var isFocusable by rememberSaveable { mutableStateOf(true) }

    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.mainRowHeaderSpace),
        modifier = modifier,
    ) {
        Text(
            text = header,
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.heading5,
            modifier = Modifier
                .padding(start = TraktTheme.spacing.mainContentStartSpace)
                .focusRequester(focusRequesters.getValue("initial"))
                .focusable(isFocusable)
                .onFocusChanged { isFocusable = false },
        )

        PositionFocusLazyRow(
            contentPadding = PaddingValues(
                start = TraktTheme.spacing.mainContentStartSpace,
                end = 32.dp,
            ),
        ) {
            if (isLoading) {
                items(count = 10) {
                    HorizontalMediaSkeletonCard()
                }
            } else if (!movies.isNullOrEmpty()) {
                items(
                    items = movies,
                    key = { item -> item.movie.ids.trakt.value },
                ) { (watchers, movie) ->
                    HorizontalMediaCard(
                        title = movie.title,
                        onClick = { onMovieClick(movie.ids.trakt) },
                        containerImageUrl = movie.images?.getFanartUrl(),
                        contentImageUrl = movie.images?.getLogoUrl(),
                        paletteColor = movie.colors?.colors?.second,
                        footerContent = {
                            InfoChip(
                                text = stringResource(R.string.people_watching, watchers.thousandsFormat()),
                            )
                        },
                        modifier = Modifier.onFocusChanged {
                            if (it.hasFocus) {
                                onMovieFocus(movie)
                            }
                        },
                    )
                }

                emptyFocusListItems()
            }
        }
    }
}

@Composable
private fun HotMoviesList(
    header: String,
    movies: List<TrendingMovie>?,
    isLoading: Boolean,
    onMovieFocus: (Movie) -> Unit,
    onMovieClick: (TraktId) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.mainRowHeaderSpace),
        modifier = modifier,
    ) {
        Text(
            text = header,
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.heading5,
            modifier = Modifier.padding(start = TraktTheme.spacing.mainContentStartSpace),
        )

        PositionFocusLazyRow(
            contentPadding = PaddingValues(
                start = TraktTheme.spacing.mainContentStartSpace,
                end = 32.dp,
            ),
        ) {
            if (isLoading) {
                items(count = 10) {
                    HorizontalMediaSkeletonCard(
                        modifier = Modifier
                            .focusProperties { canFocus = false },
                    )
                }
            } else if (!movies.isNullOrEmpty()) {
                items(
                    items = movies,
                    key = { item -> item.movie.ids.trakt.value },
                ) { (watchers, movie) ->
                    HorizontalMediaCard(
                        title = movie.title,
                        onClick = { onMovieClick(movie.ids.trakt) },
                        containerImageUrl = movie.images?.getFanartUrl(),
                        contentImageUrl = movie.images?.getLogoUrl(),
                        paletteColor = movie.colors?.colors?.second,
                        footerContent = {
                            InfoChip(
                                text = stringResource(R.string.people_eager, watchers.thousandsFormat()),
                            )
                        },
                        modifier = Modifier.onFocusChanged {
                            if (it.hasFocus) {
                                onMovieFocus(movie)
                            }
                        },
                    )
                }

                emptyFocusListItems()
            }
        }
    }
}

@Composable
private fun StandardMoviesList(
    header: String,
    movies: List<Movie>?,
    isLoading: Boolean,
    onMovieFocus: (Movie) -> Unit,
    onMovieClick: (TraktId) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.mainRowHeaderSpace),
        modifier = modifier,
    ) {
        Text(
            text = header,
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.heading5,
            modifier = Modifier.padding(start = TraktTheme.spacing.mainContentStartSpace),
        )

        PositionFocusLazyRow(
            contentPadding = PaddingValues(
                start = TraktTheme.spacing.mainContentStartSpace,
                end = 32.dp,
            ),
        ) {
            if (isLoading) {
                items(count = 10) {
                    HorizontalMediaSkeletonCard(
                        modifier = Modifier
                            .focusProperties { canFocus = false },
                    )
                }
            } else if (!movies.isNullOrEmpty()) {
                items(
                    items = movies,
                    key = { item -> item.ids.trakt.value },
                ) { movie ->
                    HorizontalMediaCard(
                        title = movie.title,
                        onClick = { onMovieClick(movie.ids.trakt) },
                        containerImageUrl = movie.images?.getFanartUrl(),
                        contentImageUrl = movie.images?.getLogoUrl(),
                        paletteColor = movie.colors?.colors?.second,
                        footerContent = {
                            val runtime = movie.runtime?.inWholeMinutes
                            if (runtime != null) {
                                InfoChip(
                                    text = runtime.durationFormat(),
                                )
                            }
                        },
                        modifier = Modifier.onFocusChanged {
                            if (it.hasFocus) {
                                onMovieFocus(movie)
                            }
                        },
                    )
                }

                emptyFocusListItems()
            }
        }
    }
}

@Composable
private fun AnticipatedMoviesList(
    header: String,
    movies: List<AnticipatedMovie>?,
    isLoading: Boolean,
    onMovieFocus: (Movie) -> Unit,
    onMovieClick: (TraktId) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.mainRowHeaderSpace),
        modifier = modifier,
    ) {
        Text(
            text = header,
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.heading5,
            modifier = Modifier.padding(start = TraktTheme.spacing.mainContentStartSpace),
        )

        PositionFocusLazyRow(
            contentPadding = PaddingValues(
                start = TraktTheme.spacing.mainContentStartSpace,
                end = 32.dp,
            ),
        ) {
            if (isLoading) {
                items(count = 10) {
                    HorizontalMediaSkeletonCard(
                        modifier = Modifier
                            .focusProperties { canFocus = false },
                    )
                }
            } else if (!movies.isNullOrEmpty()) {
                items(
                    items = movies,
                    key = { item -> item.movie.ids.trakt.value },
                ) { (listCount, movie) ->
                    HorizontalMediaCard(
                        title = movie.title,
                        onClick = { onMovieClick(movie.ids.trakt) },
                        containerImageUrl = movie.images?.getFanartUrl(),
                        contentImageUrl = movie.images?.getLogoUrl(),
                        paletteColor = movie.colors?.colors?.second,
                        footerContent = {
                            InfoChip(
                                text = stringResource(R.string.people_eager, listCount.thousandsFormat()),
                            )
                        },
                        modifier = Modifier.onFocusChanged {
                            if (it.hasFocus) {
                                onMovieFocus(movie)
                            }
                        },
                    )
                }

                emptyFocusListItems()
            }
        }
    }
}

@Preview(
    device = "id:tv_4k",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        MoviesScreenContent(
            state = MoviesState(
                trendingMovies = listOf(
                    TrendingMovie(
                        watchers = 12341,
                        movie = PreviewData.movie1,
                    ),
                    TrendingMovie(
                        watchers = 872,
                        movie = PreviewData.movie2,
                    ),
                ).toImmutableList(),
            ),
            onMovieClick = {},
        )
    }
}

@Preview(
    device = "id:tv_4k",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview2() {
    TraktTheme {
        MoviesScreenContent(
            state = MoviesState(
                isLoading = true,
                trendingMovies = listOf(
                    TrendingMovie(
                        watchers = 12341,
                        movie = PreviewData.movie1,
                    ),
                    TrendingMovie(
                        watchers = 872,
                        movie = PreviewData.movie2,
                    ),
                ).toImmutableList(),
            ),
            onMovieClick = {},
        )
    }
}
