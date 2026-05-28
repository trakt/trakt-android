@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package tv.trakt.trakt.core.summary.shows

import android.content.Context
import android.content.Intent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.layout.LazyLayoutCacheWindow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.TopCenter
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.hapticfeedback.HapticFeedbackType.Companion.Confirm
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import tv.trakt.trakt.LocalRatePromptVisibility
import tv.trakt.trakt.LocalSnackbarState
import tv.trakt.trakt.common.Config.WEB_V3_BASE_URL
import tv.trakt.trakt.common.helpers.DynamicStringResource
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.extensions.customAnnotatedString
import tv.trakt.trakt.common.helpers.extensions.ifOrElse
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.ExtraVideo
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.ratings.UserRating
import tv.trakt.trakt.core.comments.model.CommentsFilter
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem
import tv.trakt.trakt.core.ratings.ui.UserRatingBar
import tv.trakt.trakt.core.settings.features.cover.CoverImageSheet
import tv.trakt.trakt.core.summary.shows.features.actors.ShowActorsView
import tv.trakt.trakt.core.summary.shows.features.comments.ShowCommentsView
import tv.trakt.trakt.core.summary.shows.features.context.history.ShowDetailsHistorySheet
import tv.trakt.trakt.core.summary.shows.features.context.lists.ShowDetailsListsSheet
import tv.trakt.trakt.core.summary.shows.features.context.more.ShowDetailsContextSheet
import tv.trakt.trakt.core.summary.shows.features.extras.ShowExtrasView
import tv.trakt.trakt.core.summary.shows.features.history.ShowHistoryView
import tv.trakt.trakt.core.summary.shows.features.info.ShowInfoSheet
import tv.trakt.trakt.core.summary.shows.features.lists.ShowListsView
import tv.trakt.trakt.core.summary.shows.features.related.ShowRelatedView
import tv.trakt.trakt.core.summary.shows.features.seasons.ShowSeasonsView
import tv.trakt.trakt.core.summary.shows.features.sentiment.ShowSentimentView
import tv.trakt.trakt.core.summary.shows.features.streaming.ShowStreamingsView
import tv.trakt.trakt.core.summary.shows.features.trivia.ShowTriviaView
import tv.trakt.trakt.core.summary.ui.DetailsActions
import tv.trakt.trakt.core.summary.ui.DetailsBackground
import tv.trakt.trakt.core.summary.ui.header.DetailsHeader
import tv.trakt.trakt.helpers.SimpleScrollConnection
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.confirmation.ConfirmationSheet
import tv.trakt.trakt.ui.components.confirmation.RemoveConfirmationSheet
import tv.trakt.trakt.ui.components.dateselection.DateSelectionSheet
import tv.trakt.trakt.ui.components.vip.VipBanner
import tv.trakt.trakt.ui.extensions.isAtLeastMedium
import tv.trakt.trakt.ui.snackbar.SNACK_DURATION_SHORT
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun ShowDetailsScreen(
    modifier: Modifier = Modifier,
    viewModel: ShowDetailsViewModel,
    onShowClick: ((Show) -> Unit),
    onEpisodeClick: (showId: TraktId, episode: Episode) -> Unit,
    onCommentsClick: ((Show, CommentsFilter) -> Unit),
    onListClick: ((Show, CustomList) -> Unit),
    onPersonClick: ((Show, Person) -> Unit),
    onTriviaClick: ((Show) -> Unit)? = null,
    onTrailerClick: (String) -> Unit,
    onExtraClick: (ExtraVideo) -> Unit,
    onAllSeasonsClick: (Show, Int?) -> Unit,
    onNavigateVip: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val snack = LocalSnackbarState.current
    val localRateVisibility = LocalRatePromptVisibility.current

    val state by viewModel.state.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    var detailsSheet by remember { mutableStateOf<Show?>(null) }
    var contextSheet by remember { mutableStateOf<Show?>(null) }
    var listsSheet by remember { mutableStateOf<Show?>(null) }
    var historySheet by remember { mutableStateOf<HomeActivityItem.EpisodeItem?>(null) }
    var confirmAddWatchedSheet by remember { mutableStateOf(false) }
    var confirmRemoveWatchedSheet by remember { mutableStateOf(false) }
    var confirmRemoveWatchlistSheet by remember { mutableStateOf(false) }
    var dateSheet by remember { mutableStateOf(false) }
    var coverImageSheet by remember { mutableStateOf<Show?>(null) }

    DisposableEffect(Unit) {
        localRateVisibility.value = false

        onDispose {
            localRateVisibility.value = true
        }
    }

    ShowDetailsContent(
        state = state,
        modifier = modifier,
        onInfoClick = {
            state.show?.let {
                detailsSheet = it
            }
        },
        onPersonClick = {
            state.show?.let { show ->
                onPersonClick(show, it)
            }
        },
        onShowClick = onShowClick,
        onEpisodeClick = {
            state.show?.let { show ->
                viewModel.navigateToEpisode(show, it)
            }
        },
        onListClick = {
            state.show?.let { show ->
                onListClick(show, it)
            }
        },
        onTrackClick = {
            if (state.showProgress?.isWatched == true) {
                confirmRemoveWatchedSheet = true
            } else {
                confirmAddWatchedSheet = true
            }
        },
        onShareClick = {
            state.show?.let { shareShow(it, context) }
        },
        onTrailerClick = {
            state.show?.trailer?.let { url -> onTrailerClick(url) }
        },
        onExtraClick = { extra ->
            onExtraClick(extra)
        },
        onWatchlistClick = {
            if (state.showProgress?.inWatchlist == true) {
                confirmRemoveWatchlistSheet = true
            } else {
                viewModel.toggleWatchlist()
            }
        },
        onListsClick = {
            listsSheet = state.show
        },
        onHistoryClick = {
            historySheet = it
        },
        onMoreClick = {
            contextSheet = state.show
        },
        onMoreCommentsClick = { filter ->
            state.show?.let {
                onCommentsClick(it, filter)
            }
        },
        onRatingClick = {
            viewModel.addRating(it)
            haptic.performHapticFeedback(Confirm)
        },
        onRatingRemoveClick = {
            viewModel.removeRating()
            haptic.performHapticFeedback(Confirm)
        },
        onFavoriteClick = {
            viewModel.toggleFavorite(
                state.showUserRating?.rating?.favorite != true,
            )
        },
        onTriviaClick = {
            state.show?.let { show ->
                onTriviaClick?.invoke(show)
            }
        },
        onAllSeasonsClick = onAllSeasonsClick,
        onVipClick = onNavigateVip,
        onBackClick = onNavigateBack,
    )

    ShowInfoSheet(
        show = detailsSheet,
        onPersonClick = {
            detailsSheet = null
            state.show?.let { show ->
                onPersonClick(show, it)
            }
        },
        onDismiss = {
            detailsSheet = null
        },
    )

    ShowDetailsListsSheet(
        show = listsSheet,
        inWatchlist = state.showProgress?.inWatchlist == true,
        onWatchlistClick = {
            viewModel.toggleWatchlist()
        },
        onAddListClick = { listId, ownerId ->
            viewModel.toggleList(listId, ownerId, true)
        },
        onRemoveListClick = { listId, ownerId ->
            viewModel.toggleList(listId, ownerId, false)
        },
        onDismiss = {
            listsSheet = null
        },
    )

    ShowDetailsContextSheet(
        show = contextSheet,
        watched = state.showProgress?.isWatched == true,
        lists = state.showProgress?.inLists == true,
        onShareClick = {
            state.show?.let { shareShow(it, context) }
        },
        onCheckClick = {
            confirmAddWatchedSheet = true
        },
        onRemoveClick = {
            confirmRemoveWatchedSheet = true
        },
        onListsClick = {
            listsSheet = state.show
        },
        onCoverClick = {
            coverImageSheet = state.show
        },
        onDismiss = {
            contextSheet = null
        },
    )

    ShowDetailsHistorySheet(
        sheetItem = historySheet,
        onRemovePlay = {
            viewModel.removeFromWatched(playId = it.id)
        },
        onDismiss = {
            historySheet = null
        },
    )

    ConfirmationSheet(
        active = confirmAddWatchedSheet,
        onYes = {
            confirmAddWatchedSheet = false
            dateSheet = true
        },
        onNo = { confirmAddWatchedSheet = false },
        title = stringResource(R.string.button_text_mark_as_watched),
        annotatedMessage = customAnnotatedString(
            stringResource(
                R.string.warning_prompt_mark_as_watched_show_android,
                state.show?.airedEpisodes ?: 0,
                state.show?.title ?: "",
            ),
            style = TraktTheme.typography.paragraph.toSpanStyle()
                .copy(
                    fontWeight = FontWeight.W600,
                    textDecoration = TextDecoration.Underline,
                ),
        ),
        holdToYes = true,
        yesText = stringResource(R.string.button_text_hold_to_confirm),
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
            state.show?.title ?: "",
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
            state.show?.title ?: "",
        ),
    )

    DateSelectionSheet(
        active = dateSheet,
        title = state.show?.title ?: "",
        onResult = viewModel::addToWatched,
        onDismiss = {
            dateSheet = false
        },
    )

    CoverImageSheet(
        mediaId = coverImageSheet?.ids?.trakt,
        mediaTitle = coverImageSheet?.title ?: "",
        mediaType = MediaType.SHOW,
        mediaImage = state.show?.images?.getFanartUrl(),
        onImageSet = {
            scope.launch {
                val info = DynamicStringResource(R.string.text_info_cover_set)
                snack.showSnackbar(info.get(context))
            }
            coverImageSheet = null
        },
        onVipClick = {
            onNavigateVip()
            coverImageSheet = null
        },
        onDismiss = {
            coverImageSheet = null
        },
    )

    LaunchedEffect(state.navigateEpisode) {
        state.navigateEpisode?.let {
            onEpisodeClick(it.first, it.second)
            viewModel.clearNavigation()
        }
    }

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
internal fun ShowDetailsContent(
    state: ShowDetailsState,
    modifier: Modifier = Modifier,
    onShowClick: ((Show) -> Unit)? = null,
    onEpisodeClick: ((Episode) -> Unit)? = null,
    onTrackClick: (() -> Unit)? = null,
    onShareClick: (() -> Unit)? = null,
    onInfoClick: (() -> Unit)? = null,
    onTrailerClick: (() -> Unit)? = null,
    onExtraClick: ((ExtraVideo) -> Unit)? = null,
    onListsClick: (() -> Unit)? = null,
    onWatchlistClick: (() -> Unit)? = null,
    onHistoryClick: ((HomeActivityItem.EpisodeItem) -> Unit)? = null,
    onMoreClick: (() -> Unit)? = null,
    onMoreCommentsClick: ((CommentsFilter) -> Unit)? = null,
    onPersonClick: ((Person) -> Unit)? = null,
    onListClick: ((CustomList) -> Unit)? = null,
    onRatingClick: ((Int) -> Unit)? = null,
    onRatingRemoveClick: (() -> Unit)? = null,
    onFavoriteClick: (() -> Unit)? = null,
    onTriviaClick: (() -> Unit)? = null,
    onAllSeasonsClick: ((Show, Int?) -> Unit)? = null,
    onVipClick: (() -> Unit)? = null,
    onBackClick: (() -> Unit)? = null,
) {
    val previewMode = LocalInspectionMode.current
    val windowClass = currentWindowAdaptiveInfo().windowSizeClass

    val contentPadding = PaddingValues(
        top = WindowInsets.statusBars.asPaddingValues()
            .calculateTopPadding()
            .plus(16.dp),
        bottom = WindowInsets.navigationBars.asPaddingValues()
            .calculateBottomPadding()
            .plus(TraktTheme.size.navigationBarHeight)
            .plus(TraktTheme.spacing.mainPageBottomSpace),
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

    var ratingAlphaMaskActive by remember { mutableStateOf(false) }
    val ratingAlphaMask: Float by animateFloatAsState(
        targetValue = if (ratingAlphaMaskActive) 0.1F else 1F,
        animationSpec = tween(150),
        label = "alpha",
    )

    Box(
        contentAlignment = TopCenter,
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary)
            .nestedScroll(listScrollConnection),
    ) {
        state.show?.let { show ->
            val isReleased = show.rememberReleased()
            val isWatched = remember(state.showProgress?.plays) {
                state.showProgress?.isWatched == true
            }

            DetailsBackground(
                imageUrl = show.images?.getFanartUrl(Images.Size.THUMB),
                color = show.colors?.colors?.second,
                translation = listScrollConnection.resultOffset,
                modifier = Modifier.alpha(ratingAlphaMask),
            )

            LazyColumn(
                state = listState,
                horizontalAlignment = CenterHorizontally,
                verticalArrangement = spacedBy(0.dp),
                contentPadding = contentPadding,
                overscrollEffect = null,
            ) {
                item {
                    DetailsHeader(
                        show = show,
                        showTranslation = state.showTranslation,
                        ratings = state.showRatings,
                        creator = state.showCreator,
                        airedCount = state.showProgress?.aired ?: 0,
                        playsCount = state.showProgress?.plays ?: 0,
                        loading = state.loading.isLoading ||
                            state.loadingProgress.isLoading,
                        onCreatorClick = onPersonClick ?: {},
                        onBackClick = onBackClick ?: {},
                        onShareClick = onShareClick ?: {},
                        onInfoClick = onInfoClick ?: {},
                        modifier = Modifier
                            .align(Alignment.Center)
                            .alpha(ratingAlphaMask),
                    )
                }

                item {
                    DetailsActions(
                        primaryEnabled = isReleased,
                        primaryIcon = when {
                            isWatched -> R.drawable.ic_check_double
                            else -> R.drawable.ic_check_2
                        },
                        enabled = state.user != null &&
                            !state.loadingProgress.isLoading &&
                            !state.loadingLists.isLoading,
                        loading = state.loadingProgress.isLoading ||
                            state.loadingLists.isLoading,
                        watched = isWatched,
                        watchlist = state.showProgress?.inWatchlist,
                        trailer = !show.trailer.isNullOrBlank(),
                        onPrimaryClick = onTrackClick,
                        onSecondaryClick = onWatchlistClick,
                        onSecondaryLongClick = onListsClick,
                        onTrailerClick = onTrailerClick,
                        onMoreClick = onMoreClick,
                        modifier = Modifier
                            .alpha(
                                when {
                                    ratingAlphaMaskActive -> ratingAlphaMask * 0.3F
                                    else -> ratingAlphaMask
                                },
                            )
                            .padding(top = 16.dp)
                            .ifOrElse(
                                windowClass.isAtLeastMedium(),
                                isTrue = Modifier.width(TraktTheme.size.detailsActionButtonsSize),
                                isFalse = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = TraktTheme.spacing.detailsActionsHorizontalSpace,
                                    ),
                            ),
                    )
                }

                item {
                    val isLoaded = state.showUserRating?.loading == LoadingState.Done
                    DetailsRating(
                        visible = isWatched && isLoaded,
                        rating = state.showUserRating?.rating,
                        loading = state.loadingFavorite.isLoading,
                        onRatingDrag = {
                            ratingAlphaMaskActive = it
                        },
                        onRatingClick = onRatingClick ?: {},
                        onRatingRemoveClick = onRatingRemoveClick ?: {},
                        onFavoriteClick = onFavoriteClick ?: {},
                    )
                }

                item {
                    DetailsOverview(
                        overview = show.overview,
                        overviewTranslation = state.showTranslation?.overview,
                        modifier = Modifier
                            .alpha(ratingAlphaMask)
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
                                .alpha(ratingAlphaMask)
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
                            ShowStreamingsView(
                                viewModel = koinViewModel(
                                    parameters = { parametersOf(show) },
                                ),
                                headerPadding = sectionPadding,
                                contentPadding = sectionPadding,
                                modifier = Modifier
                                    .alpha(ratingAlphaMask)
                                    .padding(top = 24.dp),
                            )
                        }
                    }

                    if (isReleased) {
                        item {
                            ShowSentimentView(
                                viewModel = koinViewModel(
                                    parameters = { parametersOf(show) },
                                ),
                                headerPadding = sectionPadding,
                                contentPadding = sectionPadding,
                                modifier = Modifier
                                    .alpha(ratingAlphaMask)
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
                        ShowCommentsView(
                            viewModel = koinViewModel(
                                parameters = { parametersOf(show) },
                            ),
                            headerPadding = sectionPadding,
                            contentPadding = sectionPadding,
                            onMoreClick = onMoreCommentsClick,
                            modifier = Modifier
                                .alpha(ratingAlphaMask)
                                .padding(top = 32.dp),
                        )
                    }

                    item {
                        ShowActorsView(
                            viewModel = koinViewModel(
                                parameters = { parametersOf(show) },
                            ),
                            headerPadding = sectionPadding,
                            contentPadding = sectionPadding,
                            onPersonClick = onPersonClick ?: {},
                            modifier = Modifier
                                .alpha(ratingAlphaMask)
                                .padding(top = 32.dp),
                        )
                    }

                    item {
                        ShowSeasonsView(
                            viewModel = koinViewModel(
                                parameters = { parametersOf(show) },
                            ),
                            headerPadding = sectionPadding,
                            contentPadding = sectionPadding,
                            user = state.user,
                            onEpisodeClick = onEpisodeClick ?: {},
                            onAllSeasonsClick = { onAllSeasonsClick?.invoke(show, it) },
                            modifier = Modifier
                                .alpha(ratingAlphaMask)
                                .padding(top = 32.dp),
                        )
                    }

                    item {
                        ShowExtrasView(
                            viewModel = koinViewModel(
                                parameters = { parametersOf(show) },
                            ),
                            headerPadding = sectionPadding,
                            contentPadding = sectionPadding,
                            onVideoClick = onExtraClick ?: {},
                            modifier = Modifier
                                .alpha(ratingAlphaMask)
                                .padding(top = 32.dp),
                        )
                    }

                    item {
                        ShowRelatedView(
                            viewModel = koinViewModel(
                                parameters = { parametersOf(show) },
                            ),
                            headerPadding = sectionPadding,
                            contentPadding = sectionPadding,
                            onClick = onShowClick,
                            modifier = Modifier
                                .alpha(ratingAlphaMask)
                                .padding(top = 32.dp),
                        )
                    }

                    item {
                        ShowListsView(
                            viewModel = koinViewModel(
                                parameters = { parametersOf(show) },
                            ),
                            headerPadding = sectionPadding,
                            contentPadding = sectionPadding,
                            onClick = onListClick ?: {},
                            modifier = Modifier
                                .alpha(ratingAlphaMask)
                                .padding(top = 32.dp),
                        )
                    }

                    if (isWatched) {
                        item {
                            ShowHistoryView(
                                viewModel = koinViewModel(
                                    parameters = { parametersOf(show) },
                                ),
                                headerPadding = sectionPadding,
                                contentPadding = sectionPadding,
                                loading = state.loadingProgress.isLoading,
                                onClick = onHistoryClick,
                                modifier = Modifier
                                    .alpha(ratingAlphaMask)
                                    .padding(top = 32.dp),
                            )
                        }
                    }

                    item {
                        ShowTriviaView(
                            viewModel = koinViewModel(
                                parameters = { parametersOf(show) },
                            ),
                            headerPadding = sectionPadding,
                            contentPadding = sectionPadding,
                            onVipClick = onVipClick ?: {},
                            onTriviaClick = onTriviaClick ?: {},
                            modifier = Modifier
                                .alpha(ratingAlphaMask)
                                .padding(top = 32.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetailsRating(
    modifier: Modifier = Modifier,
    visible: Boolean,
    rating: UserRating?,
    loading: Boolean,
    onRatingDrag: (Boolean) -> Unit,
    onRatingClick: (Int) -> Unit,
    onRatingRemoveClick: () -> Unit,
    onFavoriteClick: () -> Unit,
) {
    var animated by remember { mutableStateOf(false) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .ifOrElse(
                condition = animated,
                isTrue = Modifier,
                isFalse = Modifier.animateContentSize(
                    animationSpec = tween(200, delayMillis = 250),
                ),
            ),
    ) {
        if (visible) {
            UserRatingBar(
                rating = rating?.rating,
                favoriteLoading = loading,
                favoriteVisible = true,
                favorite = rating?.favorite == true,
                onRatingDrag = {
                    animated = it
                    onRatingDrag(it)
                },
                onRatingClick = onRatingClick,
                onRatingRemoveClick = onRatingRemoveClick,
                onFavoriteClick = onFavoriteClick,
                modifier = Modifier
                    .padding(horizontal = TraktTheme.spacing.mainPageHorizontalSpace),
            )
        }
    }
}

@Composable
private fun DetailsOverview(
    modifier: Modifier = Modifier,
    overview: String? = null,
    overviewTranslation: String? = null,
) {
    var isCollapsed by remember { mutableStateOf(true) }

    Crossfade(
        targetState = overviewTranslation,
        animationSpec = tween(250),
        modifier = modifier
            .fillMaxWidth()
            .onClick { isCollapsed = !isCollapsed },
    ) { translation ->
        Text(
            text = when {
                !translation.isNullOrBlank() -> translation
                overview.isNullOrBlank() -> stringResource(R.string.text_overview_placeholder)
                else -> overview
            },
            style = TraktTheme.typography.paragraphSmall,
            color = TraktTheme.colors.textSecondary,
            maxLines = if (isCollapsed) 6 else Int.MAX_VALUE,
            textAlign = TextAlign.Start,
            overflow = Ellipsis,
        )
    }
}

private fun shareShow(
    show: Show,
    context: Context,
) {
    val shareText = "${WEB_V3_BASE_URL}shows/${show.ids.slug.value}"
    val intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareText)
        type = "text/plain"
    }

    context.startActivity(Intent.createChooser(intent, show.title))
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        ShowDetailsContent(
            state = ShowDetailsState(
                show = PreviewData.show1,
            ),
        )
    }
}
