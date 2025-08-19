package tv.trakt.trakt.app.core.search

import TraktTextField
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.input.rememberTextFieldState
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
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import timber.log.Timber
import tv.trakt.trakt.app.core.details.ui.BackdropImage
import tv.trakt.trakt.app.core.search.SearchState.State
import tv.trakt.trakt.app.core.search.views.SearchLoadingView
import tv.trakt.trakt.app.core.search.views.SearchMoviesView
import tv.trakt.trakt.app.core.search.views.SearchShowsView
import tv.trakt.trakt.app.helpers.extensions.requestSafeFocus
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.model.Images.Size
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.resources.R

private val sections = listOf(
    "input",
    "shows",
    "movies",
)

@Composable
internal fun SearchScreen(
    viewModel: SearchViewModel,
    onShowClick: (TraktId) -> Unit,
    onMovieClick: (TraktId) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state) {
        state.navigateShow?.let {
            viewModel.clearNavigation()
            onShowClick(it.ids.trakt)
        }
        state.navigateMovie?.let {
            viewModel.clearNavigation()
            onMovieClick(it.ids.trakt)
        }
    }

    SearchScreenContent(
        state = state,
        onSearchQuery = viewModel::searchQuery,
        onShowClick = viewModel::navigateToShow,
        onMovieClick = viewModel::navigateToMovie,
    )
}

@Composable
private fun SearchScreenContent(
    state: SearchState,
    modifier: Modifier = Modifier,
    onSearchQuery: (String) -> Unit = {},
    onShowClick: (Show) -> Unit = {},
    onMovieClick: (Movie) -> Unit = {},
) {
    val searchInputState = rememberTextFieldState("")

    var focusedSection by rememberSaveable { mutableStateOf<String?>(null) }
    var focusedImageUrl by remember { mutableStateOf<String?>(null) }

    val focusRequesters = remember {
        sections.associateBy(
            keySelector = { it },
            valueTransform = { FocusRequester() },
        )
    }

    LaunchedEffect(Unit) {
        Timber.d("Requesting focus for input")
        focusRequesters["input"]?.requestSafeFocus()
    }

    LaunchedEffect(searchInputState.text) {
        onSearchQuery(searchInputState.text.toString())
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary),
    ) {
        BackdropImage(
            imageUrl = focusedImageUrl ?: state.backgroundUrl,
            saturation = 0F,
            crossfade = true,
        )

        Column(
            verticalArrangement = spacedBy(TraktTheme.spacing.mainRowVerticalSpace),
            modifier = Modifier
                .focusProperties {
                    onEnter = {
                        focusRequesters[focusedSection]?.requestSafeFocus()
                    }
                }
                .focusRestorer()
                .focusGroup(),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(
                        top = TraktTheme.spacing.mainContentVerticalSpace,
                    )
                    .fillMaxWidth(),
            ) {
                TraktTextField(
                    state = searchInputState,
                    placeholder = stringResource(R.string.info_search_placeholder),
                    icon = painterResource(R.drawable.ic_search),
                    loading = state.searching,
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .width(400.dp)
                        .onFocusChanged {
                            if (it.isFocused) {
                                focusedSection = "input"
                            }
                        }
                        .focusRequester(focusRequesters.getValue("input")),
                )
            }

            when {
                state.searching && (state.state == State.RECENTS || state.state == State.TRENDING) -> {
                    SearchLoadingView(
                        header = stringResource(R.string.shows),
                        focusRequesters = focusRequesters,
                    )
                    SearchLoadingView(
                        header = stringResource(R.string.movies),
                        focusRequesters = focusRequesters,
                    )
                }
                state.state == State.RECENTS && state.recentsResult != null -> {
                    with(state.recentsResult) {
                        if (!shows.isNullOrEmpty()) {
                            SearchShowsView(
                                header = stringResource(R.string.header_search_shows_recents),
                                items = shows,
                                focusRequesters = focusRequesters,
                                onFocused = {
                                    focusedSection = "shows"
                                    focusedImageUrl = it?.images?.getFanartUrl(Size.FULL)
                                },
                                onClick = onShowClick,
                            )
                        }

                        if (!movies.isNullOrEmpty()) {
                            SearchMoviesView(
                                header = stringResource(R.string.header_search_movies_recents),
                                items = movies,
                                focusRequesters = focusRequesters,
                                onFocused = {
                                    focusedSection = "movies"
                                    focusedImageUrl = it?.images?.getFanartUrl(Size.FULL)
                                },
                                onClick = onMovieClick,
                            )
                        }
                    }
                }
                state.state == State.TRENDING && state.trendingResult != null -> {
                    with(state.trendingResult) {
                        if (!shows.isNullOrEmpty()) {
                            SearchShowsView(
                                header = stringResource(R.string.header_trending_shows),
                                items = shows,
                                focusRequesters = focusRequesters,
                                onFocused = {
                                    focusedSection = "shows"
                                    focusedImageUrl = it?.images?.getFanartUrl(Size.FULL)
                                },
                                onClick = onShowClick,
                            )
                        }

                        if (!movies.isNullOrEmpty()) {
                            SearchMoviesView(
                                header = stringResource(R.string.header_trending_movies),
                                items = movies,
                                focusRequesters = focusRequesters,
                                onFocused = {
                                    focusedSection = "movies"
                                    focusedImageUrl = it?.images?.getFanartUrl(Size.FULL)
                                },
                                onClick = onMovieClick,
                            )
                        }
                    }
                }
            }

            with(state.searchResult) {
                if (!this?.shows.isNullOrEmpty()) {
                    SearchShowsView(
                        header = stringResource(R.string.shows),
                        items = shows,
                        focusRequesters = focusRequesters,
                        onFocused = {
                            focusedSection = "shows"
                            focusedImageUrl = it?.images?.getFanartUrl(Size.FULL)
                        },
                        onClick = onShowClick,
                    )
                }

                if (!this?.movies.isNullOrEmpty()) {
                    SearchMoviesView(
                        header = stringResource(R.string.movies),
                        items = movies,
                        focusRequesters = focusRequesters,
                        onFocused = {
                            focusedSection = "movies"
                            focusedImageUrl = it?.images?.getFanartUrl(Size.FULL)
                        },
                        onClick = onMovieClick,
                    )
                }
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
        SearchScreenContent(
            state = SearchState(),
        )
    }
}
