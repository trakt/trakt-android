@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package tv.trakt.trakt.core.summary.episodes

import android.content.Context
import android.content.Intent
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
import androidx.compose.ui.Alignment.Companion.Center
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
import androidx.compose.ui.text.style.TextAlign
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
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.extensions.ifOrElse
import tv.trakt.trakt.common.helpers.extensions.isNowOrBefore
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Images.Size
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.ratings.UserRating
import tv.trakt.trakt.core.comments.model.CommentsFilter
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem
import tv.trakt.trakt.core.ratings.ui.UserRatingBar
import tv.trakt.trakt.core.settings.features.cover.CoverImageSheet
import tv.trakt.trakt.core.summary.episodes.features.actors.EpisodeActorsView
import tv.trakt.trakt.core.summary.episodes.features.comments.EpisodeCommentsView
import tv.trakt.trakt.core.summary.episodes.features.context.history.EpisodeDetailsHistorySheet
import tv.trakt.trakt.core.summary.episodes.features.context.more.EpisodeDetailsContextSheet
import tv.trakt.trakt.core.summary.episodes.features.history.EpisodeHistoryView
import tv.trakt.trakt.core.summary.episodes.features.info.EpisodeInfoSheet
import tv.trakt.trakt.core.summary.episodes.features.related.EpisodeRelatedView
import tv.trakt.trakt.core.summary.episodes.features.season.EpisodeSeasonView
import tv.trakt.trakt.core.summary.episodes.features.streaming.EpisodeStreamingsView
import tv.trakt.trakt.core.summary.ui.DetailsActions
import tv.trakt.trakt.core.summary.ui.DetailsBackground
import tv.trakt.trakt.core.summary.ui.header.DetailsHeader
import tv.trakt.trakt.helpers.SimpleScrollConnection
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.confirmation.RemoveConfirmationSheet
import tv.trakt.trakt.ui.components.dateselection.DateSelectionSheet
import tv.trakt.trakt.ui.components.vip.VipBanner
import tv.trakt.trakt.ui.extensions.isAtLeastMedium
import tv.trakt.trakt.ui.snackbar.SNACK_DURATION_SHORT
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun EpisodeDetailsScreen(
    modifier: Modifier = Modifier,
    viewModel: EpisodeDetailsViewModel,
    onShowClick: ((Show) -> Unit),
    onEpisodeClick: ((TraktId, Episode) -> Unit),
    onCommentsClick: ((Show, Episode, CommentsFilter) -> Unit),
    onPersonClick: ((Show, Episode, Person) -> Unit),
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
    var contextSheet by remember { mutableStateOf(false) }
    var historySheet by remember { mutableStateOf<HomeActivityItem.EpisodeItem?>(null) }
    var confirmRemoveWatchedSheet by remember { mutableStateOf(false) }
    var dateSheet by remember { mutableStateOf(false) }
    var detailsSheet by remember { mutableStateOf<Pair<Show, Episode>?>(null) }
    var coverImageSheet by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        localRateVisibility.value = false

        onDispose {
            localRateVisibility.value = true
        }
    }

    LaunchedEffect(state.navigateEpisode) {
        state.navigateEpisode?.let {
            onEpisodeClick(it.first, it.second)
            viewModel.clearNavigation()
        }
    }

    EpisodeDetailsContent(
        state = state,
        modifier = modifier,
        onShowClick = onShowClick,
        onEpisodeClick = viewModel::navigateToEpisode,
        onTrackClick = {
            if (state.episodeProgress?.watched == true) {
                confirmRemoveWatchedSheet = true
            } else {
                dateSheet = true
            }
        },
        onShareClick = {
            shareEpisode(
                show = state.show,
                episode = state.episode,
                context = context,
            )
        },
        onHistoryClick = {
            historySheet = it
        },
        onMoreClick = {
            contextSheet = true
        },
        onMoreCommentsClick = { filter ->
            val show = state.show
            val episode = state.episode
            if (show != null && episode != null) {
                onCommentsClick(show, episode, filter)
            }
        },
        onPersonClick = {
            val show = state.show
            val episode = state.episode
            if (show != null && episode != null) {
                onPersonClick(show, episode, it)
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
        onAllSeasonsClick = onAllSeasonsClick,
        onVipClick = onNavigateVip,
        onBackClick = onNavigateBack,
        onInfoClick = {
            val show = state.show
            val episode = state.episode
            if (show != null && episode != null) {
                detailsSheet = Pair(show, episode)
            }
        },
    )

    EpisodeDetailsContextSheet(
        active = contextSheet,
        show = state.show,
        episode = state.episode,
        watched = state.episodeProgress?.watched == true,
        onCheckClick = {
            dateSheet = true
        },
        onRemoveClick = {
            confirmRemoveWatchedSheet = true
        },
        onShareClick = {
            shareEpisode(
                show = state.show,
                episode = state.episode,
                context = context,
            )
        },
        onCoverClick = {
            coverImageSheet = true
        },
        onDismiss = {
            contextSheet = false
        },
    )

    EpisodeDetailsHistorySheet(
        sheetItem = historySheet,
        onRemovePlay = {
            viewModel.removeFromWatched(playId = it.id)
        },
        onDismiss = {
            historySheet = null
        },
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
            state.episode?.title ?: "",
        ),
    )

    DateSelectionSheet(
        active = dateSheet,
        title = state.show?.title ?: "",
        subtitle = state.episode?.title ?: "",
        nowWatchingVisible = true,
        onCheckIn = viewModel::addToCheckIn,
        onResult = viewModel::addToWatched,
        onDismiss = {
            dateSheet = false
        },
    )

    EpisodeInfoSheet(
        show = detailsSheet?.first,
        episode = detailsSheet?.second,
        onPersonClick = {
            val show = detailsSheet?.first
            val episode = detailsSheet?.second
            detailsSheet = null
            if (show != null && episode != null) {
                onPersonClick(show, episode, it)
            }
        },
        onDismiss = { detailsSheet = null },
    )

    CoverImageSheet(
        mediaId = when {
            coverImageSheet -> state.episode?.ids?.trakt
            else -> null
        },
        mediaTitle = state.show?.title ?: "",
        mediaType = MediaType.EPISODE,
        mediaImage = state.episode?.images?.getScreenshotUrl(),
        onImageSet = {
            scope.launch {
                val info = DynamicStringResource(R.string.text_info_cover_set)
                snack.showSnackbar(info.get(context))
            }
            coverImageSheet = false
        },
        onVipClick = {
            onNavigateVip()
            coverImageSheet = false
        },
        onDismiss = {
            coverImageSheet = false
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
internal fun EpisodeDetailsContent(
    state: EpisodeDetailsState,
    modifier: Modifier = Modifier,
    onShowClick: ((Show) -> Unit)? = null,
    onEpisodeClick: ((Episode) -> Unit)? = null,
    onTrackClick: (() -> Unit)? = null,
    onShareClick: (() -> Unit)? = null,
    onMoreClick: (() -> Unit)? = null,
    onMoreCommentsClick: ((CommentsFilter) -> Unit)? = null,
    onHistoryClick: ((HomeActivityItem.EpisodeItem) -> Unit)? = null,
    onPersonClick: ((Person) -> Unit)? = null,
    onRatingClick: ((Int) -> Unit)? = null,
    onRatingRemoveClick: (() -> Unit)? = null,
    onAllSeasonsClick: ((Show, Int?) -> Unit)? = null,
    onVipClick: (() -> Unit)? = null,
    onBackClick: (() -> Unit)? = null,
    onInfoClick: (() -> Unit)? = null,
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
        if (state.show != null) {
            DetailsBackground(
                imageUrl = state.show.images?.getFanartUrl(Size.THUMB),
                color = state.show.colors?.colors?.second,
                translation = listScrollConnection.resultOffset,
                modifier = Modifier.alpha(ratingAlphaMask),
            )
        }

        if (state.show != null && state.episode != null) {
            val isReleased = remember {
                state.episode.firstAired?.isNowOrBefore() ?: false
            }

            val isWatched = remember(state.episodeProgress?.plays) {
                state.episodeProgress?.watched == true
            }

            LazyColumn(
                state = listState,
                horizontalAlignment = CenterHorizontally,
                verticalArrangement = spacedBy(0.dp),
                contentPadding = contentPadding,
                overscrollEffect = null,
            ) {
                item {
                    DetailsHeader(
                        episode = state.episode,
                        show = state.show,
                        ratings = state.episodeRatings,
                        creator = state.episodeCreator,
                        playsCount = state.episodeProgress?.plays ?: 0,
                        loading = state.loading.isLoading || state.loadingProgress.isLoading,
                        onCreatorClick = onPersonClick ?: {},
                        onShowClick = onShowClick ?: {},
                        onBackClick = onBackClick ?: {},
                        onShareClick = onShareClick ?: {},
                        onInfoClick = onInfoClick ?: {},
                        modifier = Modifier
                            .align(Center)
                            .alpha(ratingAlphaMask),
                    )
                }

                item {
                    DetailsActions(
                        primaryEnabled = isReleased,
                        primaryIcon = when {
                            isWatched -> R.drawable.ic_check_double
                            else -> R.drawable.ic_check
                        },
                        secondaryVisible = false,
                        watched = isWatched,
                        enabled = state.user != null &&
                            !state.loadingProgress.isLoading,
                        loading = state.loadingProgress.isLoading,
                        onPrimaryClick = onTrackClick,
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
                                        horizontal = TraktTheme.spacing.detailsHeaderHorizontalSpace,
                                    ),
                            ),
                    )
                }

                item {
                    val isLoaded = state.episodeUserRating?.loading == Done
                    DetailsRating(
                        visible = isWatched && isLoaded,
                        rating = state.episodeUserRating?.rating,
                        onRatingClick = onRatingClick ?: {},
                        onRatingRemoveClick = onRatingRemoveClick ?: {},
                        onRatingDrag = { ratingAlphaMaskActive = it },
                    )
                }

                item {
                    DetailsOverview(
                        overview = state.episode.overview,
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
                    val streamingsVisible = isReleased && (state.user != null)

                    if (streamingsVisible) {
                        item {
                            EpisodeStreamingsView(
                                viewModel = koinViewModel(
                                    parameters = {
                                        parametersOf(state.show, state.episode)
                                    },
                                ),
                                headerPadding = sectionPadding,
                                contentPadding = sectionPadding,
                                modifier = Modifier
                                    .alpha(ratingAlphaMask)
                                    .padding(top = 24.dp),
                            )
                        }
                    }

                    item {
                        EpisodeCommentsView(
                            viewModel = koinViewModel(
                                parameters = {
                                    parametersOf(state.show, state.episode)
                                },
                            ),
                            headerPadding = sectionPadding,
                            contentPadding = sectionPadding,
                            onMoreClick = onMoreCommentsClick,
                            modifier = Modifier
                                .alpha(ratingAlphaMask)
                                .padding(
                                    top = when {
                                        streamingsVisible -> 32.dp
                                        else -> 24.dp
                                    },
                                ),
                        )
                    }

                    item {
                        EpisodeActorsView(
                            viewModel = koinViewModel(
                                parameters = {
                                    parametersOf(state.show, state.episode)
                                },
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
                        EpisodeSeasonView(
                            viewModel = koinViewModel(
                                parameters = {
                                    parametersOf(state.show, state.episode)
                                },
                            ),
                            headerPadding = sectionPadding,
                            contentPadding = sectionPadding,
                            onEpisodeClick = onEpisodeClick ?: {},
                            onAllSeasonsClick = { season ->
                                onAllSeasonsClick?.invoke(state.show, season)
                            },
                            modifier = Modifier
                                .alpha(ratingAlphaMask)
                                .padding(top = 32.dp),
                        )
                    }

                    item {
                        EpisodeRelatedView(
                            viewModel = koinViewModel(
                                parameters = {
                                    parametersOf(state.show, state.episode)
                                },
                            ),
                            headerPadding = sectionPadding,
                            contentPadding = sectionPadding,
                            onClick = onShowClick,
                            modifier = Modifier
                                .alpha(ratingAlphaMask)
                                .padding(top = 32.dp),
                        )
                    }

                    if (isWatched) {
                        item {
                            EpisodeHistoryView(
                                viewModel = koinViewModel(
                                    parameters = {
                                        parametersOf(state.episode)
                                    },
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
    onRatingDrag: (Boolean) -> Unit,
    onRatingClick: (Int) -> Unit,
    onRatingRemoveClick: () -> Unit,
) {
    var animated by remember { mutableStateOf(false) }

    Box(
        contentAlignment = Center,
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
                favoriteVisible = false,
                onRatingDrag = {
                    animated = it
                    onRatingDrag(it)
                },
                onRatingClick = onRatingClick,
                onRatingRemoveClick = onRatingRemoveClick,
                modifier = Modifier.padding(
                    horizontal = TraktTheme.spacing.mainPageHorizontalSpace,
                ),
            )
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

private fun shareEpisode(
    show: Show?,
    episode: Episode?,
    context: Context,
) {
    if (show == null || episode == null) {
        return
    }

    val shareText = "${WEB_V3_BASE_URL}shows/${show.ids.slug.value}" +
        "/seasons/${episode.season}" +
        "/episodes/${episode.number}"

    val intent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
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
        EpisodeDetailsContent(
            state = EpisodeDetailsState(
                show = PreviewData.show1,
            ),
        )
    }
}
