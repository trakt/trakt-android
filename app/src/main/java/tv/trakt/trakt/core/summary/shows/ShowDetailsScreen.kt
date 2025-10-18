@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package tv.trakt.trakt.core.summary.shows

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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import tv.trakt.trakt.LocalSnackbarState
import tv.trakt.trakt.common.Config.WEB_V3_BASE_URL
import tv.trakt.trakt.common.helpers.extensions.isNowOrBefore
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.ui.theme.colors.Shade500
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem
import tv.trakt.trakt.core.summary.shows.features.actors.ShowActorsView
import tv.trakt.trakt.core.summary.shows.features.comments.ShowCommentsView
import tv.trakt.trakt.core.summary.shows.features.context.history.ShowDetailsHistorySheet
import tv.trakt.trakt.core.summary.shows.features.context.lists.ShowDetailsListsSheet
import tv.trakt.trakt.core.summary.shows.features.context.more.ShowDetailsContextSheet
import tv.trakt.trakt.core.summary.shows.features.extras.ShowExtrasView
import tv.trakt.trakt.core.summary.shows.features.history.ShowHistoryView
import tv.trakt.trakt.core.summary.shows.features.lists.ShowListsView
import tv.trakt.trakt.core.summary.shows.features.related.ShowRelatedView
import tv.trakt.trakt.core.summary.shows.features.seasons.ShowSeasonsView
import tv.trakt.trakt.core.summary.shows.features.sentiment.ShowSentimentView
import tv.trakt.trakt.core.summary.shows.features.streaming.ShowStreamingsView
import tv.trakt.trakt.core.summary.ui.DetailsActions
import tv.trakt.trakt.core.summary.ui.DetailsBackground
import tv.trakt.trakt.core.summary.ui.DetailsHeader
import tv.trakt.trakt.core.summary.ui.DetailsMetaInfo
import tv.trakt.trakt.helpers.SimpleScrollConnection
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.confirmation.ConfirmationSheet
import tv.trakt.trakt.ui.snackbar.SNACK_DURATION_SHORT
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun ShowDetailsScreen(
    modifier: Modifier = Modifier,
    viewModel: ShowDetailsViewModel,
    onShowClick: ((Show) -> Unit),
    onEpisodeClick: (showId: TraktId, episode: Episode) -> Unit,
    onCommentsClick: ((Show) -> Unit),
    onListClick: ((Show, CustomList) -> Unit),
    onPersonClick: ((Show, Person) -> Unit),
    onNavigateBack: () -> Unit,
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val haptic = LocalHapticFeedback.current
    val snack = LocalSnackbarState.current

    val state by viewModel.state.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    var contextSheet by remember { mutableStateOf<Show?>(null) }
    var listsSheet by remember { mutableStateOf<Show?>(null) }
    var historySheet by remember { mutableStateOf<HomeActivityItem.EpisodeItem?>(null) }
    var confirmAddWatchedSheet by remember { mutableStateOf(false) }
    var confirmRemoveWatchlistSheet by remember { mutableStateOf(false) }

    ShowDetailsContent(
        state = state,
        modifier = modifier,
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
            confirmAddWatchedSheet = true
        },
        onShareClick = {
            state.show?.let { shareShow(it, context) }
        },
        onTrailerClick = {
            state.show?.trailer?.let { uriHandler.openUri(it) }
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
        onMoreCommentsClick = {
            state.show?.let {
                onCommentsClick(it)
            }
        },
        onBackClick = onNavigateBack,
    )

    ShowDetailsListsSheet(
        show = listsSheet,
        inWatchlist = state.showProgress?.inWatchlist == true,
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

    ShowDetailsContextSheet(
        show = contextSheet,
        onShareClick = {
            state.show?.let { shareShow(it, context) }
        },
        onTrailerClick = {
            state.show?.trailer?.let { uriHandler.openUri(it) }
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
            viewModel.addToWatched()
        },
        onNo = { confirmAddWatchedSheet = false },
        title = stringResource(R.string.button_text_mark_as_watched),
        message = stringResource(
            R.string.warning_prompt_mark_as_watched_show,
            state.show?.title ?: "",
        ),
    )

    ConfirmationSheet(
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
    onTrailerClick: (() -> Unit)? = null,
    onListsClick: (() -> Unit)? = null,
    onWatchlistClick: (() -> Unit)? = null,
    onHistoryClick: ((HomeActivityItem.EpisodeItem) -> Unit)? = null,
    onMoreClick: (() -> Unit)? = null,
    onMoreCommentsClick: (() -> Unit)? = null,
    onPersonClick: ((Person) -> Unit)? = null,
    onListClick: ((CustomList) -> Unit)? = null,
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
        state.show?.let { show ->
            val isReleased = remember {
                show.released?.isNowOrBefore() ?: false
            }

            DetailsBackground(
                imageUrl = show.images?.getFanartUrl(Images.Size.THUMB),
                color = show.colors?.colors?.second,
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
                        show = show,
                        ratings = state.showRatings,
                        airedCount = state.showProgress?.aired ?: 0,
                        playsCount = state.showProgress?.plays ?: 0,
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
                        inLists = state.showProgress?.inAnyList,
                        onPrimaryClick = onTrackClick,
                        onSecondaryClick = when {
                            state.showProgress?.hasLists == true -> onListsClick
                            else -> onWatchlistClick
                        },
                        onSecondaryLongClick = onListsClick,
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
                        overview = show.overview,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 18.dp)
                            .padding(horizontal = TraktTheme.spacing.mainPageHorizontalSpace),
                    )
                }

                item {
                    DetailsMeta(
                        show = show,
                        showStudios = state.showStudios,
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
                            ShowStreamingsView(
                                viewModel = koinViewModel(
                                    parameters = { parametersOf(show) },
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
                            ShowSentimentView(
                                viewModel = koinViewModel(
                                    parameters = { parametersOf(show) },
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
                        ShowCommentsView(
                            viewModel = koinViewModel(
                                parameters = { parametersOf(show) },
                            ),
                            headerPadding = sectionPadding,
                            contentPadding = sectionPadding,
                            onMoreClick = onMoreCommentsClick,
                            modifier = Modifier
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
                            onEpisodeClick = onEpisodeClick ?: {},
                            modifier = Modifier
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
                            modifier = Modifier
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
                                .padding(top = 32.dp),
                        )
                    }

                    if ((state.showProgress?.plays ?: 0) > 0) {
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
    show: Show,
    showStudios: ImmutableList<String>?,
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
                show = show,
                showStudios = showStudios,
                modifier = Modifier
                    .padding(
                        top = 57.dp,
                        bottom = 8.dp,
                    ),
            )
        }
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
