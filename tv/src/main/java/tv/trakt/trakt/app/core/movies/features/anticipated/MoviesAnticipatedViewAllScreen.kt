package tv.trakt.trakt.app.core.movies.features.anticipated

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Text
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import tv.trakt.trakt.app.common.ui.GenericErrorView
import tv.trakt.trakt.app.common.ui.InfoChip
import tv.trakt.trakt.app.common.ui.mediacards.VerticalMediaCard
import tv.trakt.trakt.app.core.details.ui.BackdropImage
import tv.trakt.trakt.app.core.movies.MoviesConfig.MOVIES_NEXT_PAGE_OFFSET
import tv.trakt.trakt.app.core.movies.MoviesConfig.MOVIES_PAGE_LIMIT
import tv.trakt.trakt.app.core.movies.model.AnticipatedMovie
import tv.trakt.trakt.app.helpers.extensions.requestSafeFocus
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.helpers.extensions.thousandsFormat
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.resources.R

@Composable
internal fun MoviesAnticipatedScreen(
    viewModel: MoviesAnticipatedViewAllViewModel,
    onNavigateToMovie: (TraktId) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    MoviesAnticipatedContent(
        state = state,
        onMovieClick = onNavigateToMovie,
        onLoadNextPage = { viewModel.loadNextDataPage() },
    )
}

@Composable
private fun MoviesAnticipatedContent(
    state: MoviesAnticipatedViewAllState,
    modifier: Modifier = Modifier,
    onMovieClick: (TraktId) -> Unit,
    onLoadNextPage: () -> Unit,
) {
    var focusedMovie by remember { mutableStateOf<Movie?>(null) }
    var focusedMovieId by rememberSaveable { mutableStateOf<Int?>(null) }
    val focusRequesters = remember { mutableMapOf<Int, FocusRequester>() }

    LaunchedEffect(Unit) {
        delay(250)
        focusRequesters[focusedMovieId]?.requestSafeFocus()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary)
            .focusProperties {
                onEnter = {
                    focusRequesters[focusedMovieId]?.requestSafeFocus()
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
                Text(
                    text = stringResource(R.string.list_title_most_anticipated),
                    color = TraktTheme.colors.textPrimary,
                    style = TraktTheme.typography.heading4,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .focusProperties {
                            down = focusRequesters.values.firstOrNull() ?: FocusRequester.Default
                        }
                        .focusable(),
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
                    key = { index -> state.movies[index].movie.ids.trakt.value },
                ) { index ->
                    val anticipatedMovie = state.movies[index]
                    val focusRequester = focusRequesters.getOrPut(anticipatedMovie.movie.ids.trakt.value) {
                        FocusRequester()
                    }

                    VerticalMediaCard(
                        title = anticipatedMovie.movie.title,
                        imageUrl = anticipatedMovie.movie.images?.getPosterUrl(),
                        onClick = {
                            if (!state.isLoadingPage) {
                                onMovieClick(anticipatedMovie.movie.ids.trakt)
                            }
                        },
                        chipContent = {
                            InfoChip(
                                text = anticipatedMovie.listCount.thousandsFormat(),
                                iconPainter = painterResource(R.drawable.ic_star),
                            )
                        },
                        modifier = Modifier
                            .focusRequester(focusRequester)
                            .onFocusChanged {
                                if (it.isFocused) {
                                    focusedMovie = anticipatedMovie.movie
                                    focusedMovieId = anticipatedMovie.movie.ids.trakt.value

                                    loadNextPageIfNeeded(
                                        size = state.movies.size,
                                        index = index,
                                        onLoadNextPage = onLoadNextPage,
                                    )
                                }
                            },
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
    if (size >= MOVIES_PAGE_LIMIT && index >= size - MOVIES_NEXT_PAGE_OFFSET) {
        onLoadNextPage()
    }
}

@Preview
@Composable
private fun MoviesAnticipatedContentPreview() {
    TraktTheme {
        MoviesAnticipatedContent(
            state = MoviesAnticipatedViewAllState(
                movies = listOf(
                    AnticipatedMovie(
                        listCount = 1234,
                        movie = PreviewData.movie1,
                    ),
                ).toImmutableList(),
            ),
            onMovieClick = {},
            onLoadNextPage = {},
        )
    }
}
