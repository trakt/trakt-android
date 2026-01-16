@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package tv.trakt.trakt.core.summary.movies

import android.content.Context
import android.content.Intent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import tv.trakt.trakt.LocalSnackbarState
import tv.trakt.trakt.common.Config.WEB_V3_BASE_URL
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.extensions.ifOrElse
import tv.trakt.trakt.common.helpers.extensions.isTodayOrBefore
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.ratings.UserRating
import tv.trakt.trakt.core.comments.model.CommentsFilter
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem
import tv.trakt.trakt.core.summary.movies.features.actors.MovieActorsView
import tv.trakt.trakt.core.summary.movies.features.comments.MovieCommentsView
import tv.trakt.trakt.core.summary.movies.features.context.history.MovieDetailsHistorySheet
import tv.trakt.trakt.core.summary.movies.features.context.lists.MovieDetailsListsSheet
import tv.trakt.trakt.core.summary.movies.features.context.more.MovieDetailsContextSheet
import tv.trakt.trakt.core.summary.movies.features.extras.MovieExtrasView
import tv.trakt.trakt.core.summary.movies.features.history.MovieHistoryView
import tv.trakt.trakt.core.summary.movies.features.lists.MovieListsView
import tv.trakt.trakt.core.summary.movies.features.related.MovieRelatedView
import tv.trakt.trakt.core.summary.movies.features.sentiment.MovieSentimentView
import tv.trakt.trakt.core.summary.movies.features.streaming.MovieStreamingsView
import tv.trakt.trakt.core.summary.ui.DetailsActions
import tv.trakt.trakt.core.summary.ui.DetailsBackground
import tv.trakt.trakt.core.summary.ui.DetailsMetaInfo
import tv.trakt.trakt.core.summary.ui.header.DetailsHeader
import tv.trakt.trakt.core.summary.ui.header.POSTER_SPACE_WEIGHT
import tv.trakt.trakt.helpers.SimpleScrollConnection
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktSectionHeader
import tv.trakt.trakt.ui.components.UserRatingBar
import tv.trakt.trakt.ui.components.confirmation.RemoveConfirmationSheet
import tv.trakt.trakt.ui.components.dateselection.DateSelectionSheet
import tv.trakt.trakt.ui.components.vip.VipBanner
import tv.trakt.trakt.ui.extensions.isAtLeastMedium
import tv.trakt.trakt.ui.snackbar.SNACK_DURATION_SHORT
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun MovieDetailsScreen(
    modifier: Modifier = Modifier,
    viewModel: MovieDetailsViewModel,
    onMovieClick: ((Movie) -> Unit),
    onCommentsClick: ((Movie, CommentsFilter) -> Unit),
    onListClick: ((Movie, CustomList) -> Unit),
    onPersonClick: ((Movie, Person) -> Unit),
    onNavigateVip: () -> Unit,
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
    var confirmRemoveWatchedSheet by remember { mutableStateOf(false) }
    var confirmRemoveWatchlistSheet by remember { mutableStateOf(false) }
    var dateSheet by remember { mutableStateOf(false) }

    MovieDetailsContent(
        state = state,
        modifier = modifier,
        onMovieClick = onMovieClick,
        onPersonClick = {
            state.movie?.let { movie ->
                onPersonClick(movie, it)
            }
        },
        onListClick = {
            state.movie?.let { movie ->
                onListClick(movie, it)
            }
        },
        onTrackClick = {
            if ((state.movieProgress?.plays ?: 0) > 0) {
                confirmRemoveWatchedSheet = true
            } else {
                dateSheet = true
            }
        },
        onShareClick = { state.movie?.let { shareMovie(it, context) } },
        onTrailerClick = { state.movie?.trailer?.let { uriHandler.openUri(it) } },
        onWatchlistClick = {
            if (state.movieProgress?.inWatchlist == true) {
                confirmRemoveWatchlistSheet = true
            } else {
                viewModel.toggleWatchlist()
            }
        },
        onListsClick = { listsSheet = state.movie },
        onMoreClick = { contextSheet = state.movie },
        onMoreCommentsClick = { filter ->
            state.movie?.let {
                onCommentsClick(it, filter)
            }
        },
        onHistoryClick = { historySheet = it },
        onRatingClick = {
            viewModel.addRating(it)
            haptic.performHapticFeedback(Confirm)
        },
        onFavoriteClick = {
            viewModel.toggleFavorite(
                state.movieUserRating?.rating?.favorite != true,
            )
        },
        onVipClick = onNavigateVip,
        onBackClick = onNavigateBack,
        onMetaCollapseClick = viewModel::setMetaCollapsed,
    )

    RemoveConfirmationSheet(
        active = confirmRemoveWatchedSheet,
        onYes = {
            confirmRemoveWatchedSheet = false
            viewModel.removeFromWatched()
        },
        onNo = { confirmRemoveWatchedSheet = false },
        title = stringResource(R.string.button_text_remove_from_history),
        message = stringResource(
            R.string.warning_prompt_remove_from_watched,
            state.movie?.title ?: "",
        ),
    )

    RemoveConfirmationSheet(
        active = confirmRemoveWatchlistSheet,
        onYes = {
            confirmRemoveWatchlistSheet = false
            viewModel.toggleWatchlist()
        },
        onNo = { confirmRemoveWatchlistSheet = false },
        title = stringResource(R.string.button_text_watchlist),
        message = stringResource(
            R.string.warning_prompt_remove_from_watchlist,
            state.movie?.title ?: "",
        ),
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
        watched = (state.movieProgress?.plays ?: 0) > 0,
        onShareClick = {
            state.movie?.let { shareMovie(it, context) }
        },
        onCheckClick = {
            dateSheet = true
        },
        onRemoveClick = {
            confirmRemoveWatchedSheet = true
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

    DateSelectionSheet(
        active = dateSheet,
        title = state.movie?.title ?: "",
        onResult = viewModel::addToWatched,
        onDismiss = {
            dateSheet = false
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
    onWatchlistClick: (() -> Unit)? = null,
    onListsClick: (() -> Unit)? = null,
    onHistoryClick: ((HomeActivityItem.MovieItem) -> Unit)? = null,
    onMoreClick: (() -> Unit)? = null,
    onMoreCommentsClick: ((CommentsFilter) -> Unit)? = null,
    onPersonClick: ((Person) -> Unit)? = null,
    onListClick: ((CustomList) -> Unit)? = null,
    onRatingClick: ((Int) -> Unit)? = null,
    onFavoriteClick: (() -> Unit)? = null,
    onVipClick: (() -> Unit)? = null,
    onBackClick: (() -> Unit)? = null,
    onMetaCollapseClick: ((Boolean) -> Unit)? = null,
) {
    val previewMode = LocalInspectionMode.current
    val windowClass = currentWindowAdaptiveInfo().windowSizeClass

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
            aheadFraction = 0.75F,
            behindFraction = 0.75F,
        ),
    )

    val listScrollConnection = rememberSaveable(saver = SimpleScrollConnection.Saver) {
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

            val isWatched = remember(state.movieProgress?.plays) {
                (state.movieProgress?.plays ?: 0) > 0
            }

            DetailsBackground(
                imageUrl = movie.images?.getFanartUrl(Images.Size.THUMB),
                color = movie.colors?.colors?.second,
                translation = listScrollConnection.resultOffset,
            )

            LazyColumn(
                state = listState,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = spacedBy(0.dp),
                contentPadding = contentPadding,
                overscrollEffect = null,
            ) {
                item {
                    DetailsHeader(
                        movie = movie,
                        ratings = state.movieRatings,
                        creator = state.movieCreator,
                        creditsCount = when {
                            isWatched || state.loadingProgress.isLoading -> null
                            else -> movie.credits
                        },
                        playsCount = state.movieProgress?.plays,
                        loading = state.loading.isLoading ||
                            state.loadingProgress.isLoading,
                        onCreatorClick = onPersonClick ?: {},
                        onBackClick = onBackClick ?: {},
                        onShareClick = onShareClick ?: {},
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                item {
                    DetailsActions(
                        primaryEnabled = isReleased,
                        primaryIcon = when {
                            isWatched -> R.drawable.ic_check_double
                            else -> R.drawable.ic_check
                        },
                        enabled = state.user != null &&
                            !state.loadingProgress.isLoading &&
                            !state.loadingLists.isLoading,
                        loading = state.loadingProgress.isLoading ||
                            state.loadingLists.isLoading,
                        inLists = state.movieProgress?.inAnyList,
                        trailer = !movie.trailer.isNullOrBlank(),
                        onTrailerClick = onTrailerClick,
                        onPrimaryClick = onTrackClick,
                        onSecondaryClick = when {
                            state.movieProgress?.hasLists == true -> onListsClick
                            else -> onWatchlistClick
                        },
                        onSecondaryLongClick = onListsClick,
                        onMoreClick = onMoreClick,
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .ifOrElse(
                                windowClass.isAtLeastMedium(),
                                trueModifier = Modifier
                                    .fillMaxWidth(POSTER_SPACE_WEIGHT),
                                falseModifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = TraktTheme.spacing.detailsActionsHorizontalSpace,
                                    ),
                            ),
                    )
                }

                item {
                    val isLoaded = state.movieUserRating?.loading == LoadingState.DONE
                    DetailsRating(
                        visible = isWatched && isLoaded,
                        rating = state.movieUserRating?.rating,
                        loading = state.loadingFavorite.isLoading,
                        onRatingClick = onRatingClick ?: {},
                        onFavoriteClick = onFavoriteClick ?: {},
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

                if (state.user != null && !state.user.isVip) {
                    item {
                        VipBanner(
                            onClick = onVipClick ?: {},
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = TraktTheme.spacing.mainPageHorizontalSpace)
                                .padding(top = 24.dp),
                        )
                    }
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
                            onPersonClick = onPersonClick ?: {},
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

                    item {
                        MovieListsView(
                            viewModel = koinViewModel(
                                parameters = { parametersOf(movie) },
                            ),
                            headerPadding = sectionPadding,
                            contentPadding = sectionPadding,
                            onClick = onListClick ?: {},
                            modifier = Modifier
                                .padding(top = 32.dp),
                        )
                    }

                    if (isWatched) {
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

                    item {
                        DetailsMeta(
                            movie = movie,
                            movieStudios = state.movieStudios,
                            collapsed = state.metaCollapsed ?: false,
                            onCollapseClick = onMetaCollapseClick ?: {},
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 32.dp)
                                .padding(horizontal = TraktTheme.spacing.mainPageHorizontalSpace),
                        )
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
fun DetailsRating(
    modifier: Modifier = Modifier,
    visible: Boolean,
    rating: UserRating?,
    loading: Boolean,
    onRatingClick: (Int) -> Unit,
    onFavoriteClick: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = tween(200, delayMillis = 250),
            ),
    ) {
        if (visible) {
            UserRatingBar(
                rating = rating,
                favoriteLoading = loading,
                favoriteVisible = true,
                favorite = rating?.favorite == true,
                onRatingClick = onRatingClick,
                onFavoriteClick = onFavoriteClick,
                modifier = Modifier
                    .padding(top = 20.dp)
                    .padding(horizontal = TraktTheme.spacing.mainPageHorizontalSpace),
            )
        }
    }
}

@Composable
private fun DetailsMeta(
    modifier: Modifier = Modifier,
    movie: Movie,
    movieStudios: ImmutableList<String>?,
    collapsed: Boolean = false,
    onCollapseClick: (Boolean) -> Unit = {},
) {
    var animateCollapse by rememberSaveable { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = spacedBy(14.dp),
        modifier = modifier
            .animateContentSize(animationSpec = if (animateCollapse) spring() else snap()),
    ) {
        TraktSectionHeader(
            title = stringResource(R.string.header_details),
            chevron = false,
            collapsed = collapsed,
            onCollapseClick = {
                animateCollapse = true
                onCollapseClick(!collapsed)
            },
        )

        if (!collapsed) {
            DetailsMetaInfo(
                movie = movie,
                movieStudios = movieStudios,
            )
        }
    }
}

private fun shareMovie(
    movie: Movie,
    context: Context,
) {
    val shareText = "${WEB_V3_BASE_URL}movies/${movie.ids.slug.value}"
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
