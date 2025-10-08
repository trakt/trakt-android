@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package tv.trakt.trakt.core.summary.movies

import android.content.Context
import android.content.Intent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.layout.LazyLayoutCacheWindow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.TopCenter
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType.Companion.Confirm
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import tv.trakt.trakt.LocalSnackbarState
import tv.trakt.trakt.common.Config.WEB_V3_BASE_URL
import tv.trakt.trakt.common.helpers.extensions.isTodayOrBefore
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.ui.theme.colors.Shade500
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem
import tv.trakt.trakt.core.summary.movies.features.actors.MovieActorsView
import tv.trakt.trakt.core.summary.movies.features.comments.MovieCommentsView
import tv.trakt.trakt.core.summary.movies.features.context.history.MovieDetailsHistorySheet
import tv.trakt.trakt.core.summary.movies.features.context.lists.MovieDetailsListsSheet
import tv.trakt.trakt.core.summary.movies.features.context.more.MovieDetailsContextSheet
import tv.trakt.trakt.core.summary.movies.features.extras.MovieExtrasView
import tv.trakt.trakt.core.summary.movies.features.history.MovieHistoryView
import tv.trakt.trakt.core.summary.movies.features.related.MovieRelatedView
import tv.trakt.trakt.core.summary.movies.features.sentiment.MovieSentimentView
import tv.trakt.trakt.core.summary.movies.features.streaming.MovieStreamingsView
import tv.trakt.trakt.core.summary.ui.DetailsActions
import tv.trakt.trakt.core.summary.ui.DetailsBackground
import tv.trakt.trakt.core.summary.ui.DetailsHeader
import tv.trakt.trakt.core.summary.ui.DetailsMetaInfo
import tv.trakt.trakt.helpers.SimpleScrollConnection
import tv.trakt.trakt.helpers.preview.PreviewData
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.snackbar.SNACK_DURATION_SHORT
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun MovieDetailsScreen(
    modifier: Modifier = Modifier,
    viewModel: MovieDetailsViewModel,
    onMovieClick: ((Movie) -> Unit),
    onCommentsClick: ((Movie) -> Unit),
    onNavigateBack: () -> Unit,
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val haptic = LocalHapticFeedback.current
    val snack = LocalSnackbarState.current

    val state by viewModel.state.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    var contextSheet by remember { mutableStateOf<Movie?>(null) }
    var listsSheet by remember { mutableStateOf<Movie?>(null) }
    var historySheet by remember { mutableStateOf<HomeActivityItem.MovieItem?>(null) }

    MovieDetailsContent(
        state = state,
        modifier = modifier,
        onMovieClick = onMovieClick,
        onTrackClick = {
            viewModel.addToWatched()
        },
        onShareClick = {
            state.movie?.let { shareMovie(it, context) }
        },
        onTrailerClick = {
            state.movie?.trailer?.let { uriHandler.openUri(it) }
        },
        onListsClick = {
            listsSheet = state.movie
        },
        onMoreClick = {
            contextSheet = state.movie
        },
        onMoreCommentsClick = {
            state.movie?.let {
                onCommentsClick(it)
            }
        },
        onHistoryClick = {
            historySheet = it
        },
        onBackClick = onNavigateBack,
    )

    MovieDetailsListsSheet(
        movie = listsSheet,
        inWatchlist = state.movieProgress?.inWatchlist == true,
        onWatchlistClick = {
            viewModel.toggleWatchlist()
        },
        onAddListClick = {
            viewModel.toggleList(it, true)
        },
        onRemoveListClick = {
            viewModel.toggleList(it, false)
        },
        onDismiss = {
            listsSheet = null
        },
    )

    MovieDetailsContextSheet(
        movie = contextSheet,
        onShareClick = {
            state.movie?.let { shareMovie(it, context) }
        },
        onTrailerClick = {
            state.movie?.trailer?.let { uriHandler.openUri(it) }
        },
        onDismiss = {
            contextSheet = null
        },
    )

    MovieDetailsHistorySheet(
        sheetItem = historySheet,
        onRemovePlay = {
            viewModel.removeFromWatched(playId = it.id)
        },
        onDismiss = {
            historySheet = null
        },
    )

    LaunchedEffect(state.info) {
        if (state.info == null) {
            return@LaunchedEffect
        }
        haptic.performHapticFeedback(Confirm)
        with(scope) {
            val job = launch {
                state.info?.get(context)?.let {
                    snack.showSnackbar(it)
                }
            }
            delay(SNACK_DURATION_SHORT)
            job.cancel()
        }
        viewModel.clearInfo()
    }
}

@Composable
internal fun MovieDetailsContent(
    state: MovieDetailsState,
    modifier: Modifier = Modifier,
    onMovieClick: ((Movie) -> Unit)? = null,
    onTrackClick: (() -> Unit)? = null,
    onShareClick: (() -> Unit)? = null,
    onTrailerClick: (() -> Unit)? = null,
    onListsClick: (() -> Unit)? = null,
    onHistoryClick: ((HomeActivityItem.MovieItem) -> Unit)? = null,
    onMoreClick: (() -> Unit)? = null,
    onMoreCommentsClick: (() -> Unit)? = null,
    onBackClick: (() -> Unit)? = null,
) {
    val previewMode = LocalInspectionMode.current

    val contentPadding = PaddingValues(
        top = WindowInsets.statusBars.asPaddingValues()
            .calculateTopPadding()
            .plus(16.dp),
        bottom = WindowInsets.navigationBars.asPaddingValues()
            .calculateBottomPadding()
            .plus(TraktTheme.size.navigationBarHeight * 2),
    )

    val sectionPadding = PaddingValues(
        horizontal = TraktTheme.spacing.mainPageHorizontalSpace,
    )

    val listState = rememberLazyListState(
        cacheWindow = LazyLayoutCacheWindow(
            aheadFraction = 0.5F,
            behindFraction = 0.5F,
        ),
    )

    val listScrollConnection = remember {
        SimpleScrollConnection()
    }

    Box(
        contentAlignment = TopCenter,
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary)
            .nestedScroll(listScrollConnection),
    ) {
        state.movie?.let { movie ->
            val isReleased = remember {
                movie.released?.isTodayOrBefore() ?: false
            }

            DetailsBackground(
                imageUrl = movie.images?.getFanartUrl(Images.Size.THUMB),
                color = movie.colors?.colors?.second,
                translation = listScrollConnection.resultOffset,
            )

            LazyColumn(
                state = listState,
                verticalArrangement = spacedBy(0.dp),
                contentPadding = contentPadding,
                overscrollEffect = null,
            ) {
                item {
                    DetailsHeader(
                        movie = movie,
                        ratings = state.movieRatings,
                        creditsCount = when {
                            (state.movieProgress?.plays ?: 0) > 0 || state.loadingProgress.isLoading -> null
                            else -> movie.credits
                        },
                        playsCount = state.movieProgress?.plays,
                        loading = state.loading.isLoading ||
                            state.loadingProgress.isLoading,
                        onBackClick = onBackClick ?: {},
                        onTrailerClick = onTrailerClick ?: {},
                        onShareClick = onShareClick ?: {},
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                item {
                    DetailsActions(
                        primaryEnabled = isReleased,
                        enabled = state.user != null &&
                            !state.loadingProgress.isLoading &&
                            !state.loadingLists.isLoading,
                        loading = state.loadingProgress.isLoading ||
                            state.loadingLists.isLoading,
                        inLists = state.movieProgress?.inAnyList,
                        onPrimaryClick = onTrackClick,
                        onSecondaryClick = onListsClick,
                        onMoreClick = onMoreClick,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                            .padding(horizontal = 42.dp),
                    )
                }

                item {
                    DetailsOverview(
                        overview = movie.overview,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 18.dp)
                            .padding(horizontal = TraktTheme.spacing.mainPageHorizontalSpace),
                    )
                }

                item {
                    DetailsMeta(
                        movie = movie,
                        movieStudios = state.movieStudios,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 22.dp)
                            .padding(horizontal = TraktTheme.spacing.mainPageHorizontalSpace),
                    )
                }

                if (!previewMode) {
                    val showStreamings = (state.user != null) && isReleased

                    if (showStreamings) {
                        item {
                            MovieStreamingsView(
                                viewModel = koinViewModel(
                                    parameters = { parametersOf(movie) },
                                ),
                                headerPadding = sectionPadding,
                                contentPadding = sectionPadding,
                                modifier = Modifier
                                    .padding(top = 24.dp),
                            )
                        }
                    }

                    if (isReleased) {
                        item {
                            MovieSentimentView(
                                viewModel = koinViewModel(
                                    parameters = { parametersOf(movie) },
                                ),
                                headerPadding = sectionPadding,
                                contentPadding = sectionPadding,
                                modifier = Modifier
                                    .padding(
                                        top = when {
                                            showStreamings -> 32.dp
                                            else -> 24.dp
                                        },
                                    ),
                            )
                        }
                    }

                    item {
                        MovieCommentsView(
                            viewModel = koinViewModel(
                                parameters = { parametersOf(movie) },
                            ),
                            headerPadding = sectionPadding,
                            contentPadding = sectionPadding,
                            onMoreClick = onMoreCommentsClick,
                            modifier = Modifier
                                .padding(top = 32.dp),
                        )
                    }

                    item {
                        MovieActorsView(
                            viewModel = koinViewModel(
                                parameters = { parametersOf(movie) },
                            ),
                            headerPadding = sectionPadding,
                            contentPadding = sectionPadding,
                            modifier = Modifier
                                .padding(top = 32.dp),
                        )
                    }

                    item {
                        MovieExtrasView(
                            viewModel = koinViewModel(
                                parameters = { parametersOf(movie) },
                            ),
                            headerPadding = sectionPadding,
                            contentPadding = sectionPadding,
                            modifier = Modifier
                                .padding(top = 32.dp),
                        )
                    }

                    item {
                        MovieRelatedView(
                            viewModel = koinViewModel(
                                parameters = { parametersOf(movie) },
                            ),
                            headerPadding = sectionPadding,
                            contentPadding = sectionPadding,
                            onClick = onMovieClick,
                            modifier = Modifier
                                .padding(top = 32.dp),
                        )
                    }

                    if ((state.movieProgress?.plays ?: 0) > 0) {
                        item {
                            MovieHistoryView(
                                viewModel = koinViewModel(
                                    parameters = { parametersOf(movie) },
                                ),
                                headerPadding = sectionPadding,
                                contentPadding = sectionPadding,
                                onClick = onHistoryClick,
                                modifier = Modifier
                                    .padding(top = 32.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailsOverview(
    modifier: Modifier = Modifier,
    overview: String? = null,
) {
    var isCollapsed by remember { mutableStateOf(true) }
    Text(
        text = overview ?: stringResource(R.string.text_overview_placeholder),
        style = TraktTheme.typography.paragraphSmall,
        color = TraktTheme.colors.textSecondary,
        maxLines = if (isCollapsed) 6 else Int.MAX_VALUE,
        textAlign = TextAlign.Start,
        overflow = Ellipsis,
        modifier = modifier
            .onClick {
                isCollapsed = !isCollapsed
            },
    )
}

@Composable
private fun DetailsMeta(
    modifier: Modifier = Modifier,
    movie: Movie,
    movieStudios: ImmutableList<String>?,
) {
    var isCollapsed by remember { mutableStateOf(true) }
    Box(
        modifier = modifier
            .animateContentSize(),
    ) {
        Text(
            text = when {
                isCollapsed -> stringResource(R.string.button_text_view_details)
                else -> stringResource(R.string.button_text_hide_details)
            }.uppercase(),
            textAlign = TextAlign.Center,
            style = TraktTheme.typography.buttonPrimary
                .copy(fontSize = 14.sp),
            color = TraktTheme.colors.textPrimary,
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = TraktTheme.colors.backgroundPrimary,
                    shape = RoundedCornerShape(100),
                )
                .border(
                    width = (1.5).dp,
                    color = Shade500,
                    shape = RoundedCornerShape(100),
                )
                .padding(vertical = 9.dp)
                .onClick {
                    isCollapsed = !isCollapsed
                },
        )

        if (!isCollapsed) {
            DetailsMetaInfo(
                movie = movie,
                movieStudios = movieStudios,
                modifier = Modifier
                    .padding(
                        top = 57.dp,
                        bottom = 8.dp,
                    ),
            )
        }
    }
}

private fun shareMovie(
    movie: Movie,
    context: Context,
) {
    val shareText = "${context.getString(R.string.text_share_movie, movie.title)} " +
        "${WEB_V3_BASE_URL}movies/${movie.ids.slug.value}"

    val intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareText)
        type = "text/plain"
    }

    context.startActivity(Intent.createChooser(intent, movie.title))
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        MovieDetailsContent(
            state = MovieDetailsState(
                movie = PreviewData.movie1,
            ),
        )
    }
}
