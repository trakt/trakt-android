@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package tv.trakt.trakt.core.home.sections.watchlist.features.all

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.layout.LazyLayoutCacheWindow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType.Companion.Confirm
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.LocalSnackbarState
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.DateSelectionResult
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.SeasonEpisode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.core.filters.GlobalFiltersSheet
import tv.trakt.trakt.core.filters.navigation.GlobalFiltersOptions
import tv.trakt.trakt.core.home.sections.watchlist.features.all.ui.AllHomeWatchlistEpisodeView
import tv.trakt.trakt.core.lists.sections.watchlist.features.all.views.AllWatchlistMovieView
import tv.trakt.trakt.core.lists.sections.watchlist.features.context.movies.sheets.WatchlistMovieSheet
import tv.trakt.trakt.core.lists.sections.watchlist.features.context.shows.sheets.WatchlistShowSheet
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem.MovieItem
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem.ShowItem
import tv.trakt.trakt.helpers.SimpleScrollConnection
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.MediaFilterIcon
import tv.trakt.trakt.ui.components.MediaModeFilters
import tv.trakt.trakt.ui.components.ScrollableBackdropImage
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.dateselection.DateSelectionSheet
import tv.trakt.trakt.ui.components.mediacards.skeletons.PanelMediaSkeletonCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun AllHomeWatchlistScreen(
    modifier: Modifier = Modifier,
    viewModel: AllHomeWatchlistViewModel,
    onNavigateBack: () -> Unit,
    onShowClick: (TraktId) -> Unit,
    onMovieClick: (TraktId) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val resources = LocalResources.current
    val haptic = LocalHapticFeedback.current
    val snackbar = LocalSnackbarState.current

    var contextMovieSheet by remember { mutableStateOf<MovieItem?>(null) }
    var contextShowSheet by remember { mutableStateOf<ShowItem?>(null) }
    var dateSheet by remember { mutableStateOf<WatchlistItem?>(null) }
    var filtersSheet by remember { mutableStateOf(false) }

    LaunchedEffect(state) {
        state.navigateShow?.let {
            viewModel.clearNavigation()
            onShowClick(it)
        }
        state.navigateMovie?.let {
            viewModel.clearNavigation()
            onMovieClick(it)
        }
    }

    LaunchedEffect(state.info) {
        if (state.info != null) {
            haptic.performHapticFeedback(Confirm)
            snackbar.showSnackbar(
                message = resources.getString(R.string.text_info_history_added),
                duration = SnackbarDuration.Short,
            )
            viewModel.clearInfo()
        }
    }

    AllHomeWatchlistContent(
        state = state,
        modifier = modifier,
        onClick = {
            when (it) {
                is ShowItem -> viewModel.navigateToShow(it.show)
                is MovieItem -> viewModel.navigateToMovie(it.movie)
            }
        },
        onLongClick = {
            when (it) {
                is MovieItem -> contextMovieSheet = it
                is ShowItem -> contextShowSheet = it
            }
        },
        onCheckClick = {
            when (it) {
                is ShowItem -> viewModel.addShowToHistory(showId = it.id)
                is MovieItem -> viewModel.addMovieToHistory(it.id)
            }
        },
        onCheckLongClick = {
            dateSheet = it
        },
        onModeClick = { mode ->
            state.filter?.let {
                viewModel.setFilter(it.copy(mode = mode))
            }
        },
        onFiltersClick = { filtersSheet = true },
        onBackClick = onNavigateBack,
    )

    WatchlistShowSheet(
        addLocally = false,
        watched = false,
        show = contextShowSheet?.show,
        onDismiss = { contextShowSheet = null },
        onAddWatched = {
            dateSheet = contextShowSheet
        },
        onRemoveWatchlist = {
            viewModel.removeItem(contextShowSheet, notify = true)
        },
    )

    WatchlistMovieSheet(
        addLocally = false,
        movie = contextMovieSheet?.movie,
        watched = false,
        onDismiss = { contextMovieSheet = null },
        onAddWatched = {
            dateSheet = contextMovieSheet
        },
        onRemoveWatchlist = {
            viewModel.removeItem(contextMovieSheet, notify = true)
        },
    )

    AllHomeDateSelectionSheet(
        item = dateSheet,
        onDateSelected = { date ->
            dateSheet?.let {
                when (it) {
                    is MovieItem -> {
                        viewModel.addMovieToHistory(
                            movieId = it.id,
                            customDate = date,
                        )
                    }
                    is ShowItem -> {
                        viewModel.addShowToHistory(
                            showId = it.id,
                            customDate = date,
                        )
                    }
                }
            }
        },
        onCheckIn = {
            val item = dateSheet ?: return@AllHomeDateSelectionSheet
            when (item) {
                is MovieItem -> {
                    viewModel.addMovieCheckIn(item.id)
                }
                is ShowItem -> {
                    viewModel.addEpisodeCheckIn(
                        showId = item.id,
                        seasonEpisode = SeasonEpisode(1, 1),
                    )
                }
            }
        },
        onDismiss = {
            dateSheet = null
        },
    )

    GlobalFiltersSheet(
        active = filtersSheet,
        options = GlobalFiltersOptions(
            global = false,
            initial = state.filter,
        ),
        onUpdate = viewModel::setFilter,
        onDismiss = {
            filtersSheet = false
        },
    )
}

@Composable
internal fun AllHomeWatchlistContent(
    state: AllHomeWatchlistState,
    modifier: Modifier = Modifier,
    onClick: (WatchlistItem) -> Unit = {},
    onCheckClick: (WatchlistItem) -> Unit = {},
    onCheckLongClick: (WatchlistItem) -> Unit = {},
    onLongClick: (WatchlistItem) -> Unit = {},
    onModeClick: (MediaMode) -> Unit = {},
    onFiltersClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
) {
    val listState = rememberLazyListState(
        cacheWindow = LazyLayoutCacheWindow(
            aheadFraction = 0.5F,
            behindFraction = 0.5F,
        ),
    )

    val listScrollConnection = rememberSaveable(saver = SimpleScrollConnection.Saver) {
        SimpleScrollConnection()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary)
            .nestedScroll(listScrollConnection),
    ) {
        val contentPadding = PaddingValues(
            start = TraktTheme.spacing.mainPageHorizontalSpace,
            end = TraktTheme.spacing.mainPageHorizontalSpace,
            top = WindowInsets.statusBars.asPaddingValues()
                .calculateTopPadding(),
            bottom = WindowInsets.navigationBars.asPaddingValues()
                .calculateBottomPadding()
                .plus(TraktTheme.size.navigationBarHeight * 2),
        )

        ScrollableBackdropImage(
            translation = listScrollConnection.resultOffset,
        )

        ContentList(
            listItems = (state.items ?: emptyList()).toImmutableList(),
            listState = listState,
            listFilter = state.filter,
            loading = state.loading.isLoading,
            contentPadding = contentPadding,
            onModeClick = onModeClick,
            onFiltersClick = onFiltersClick,
            onClick = onClick,
            onCheckClick = onCheckClick,
            onCheckLongClick = onCheckLongClick,
            onLongClick = onLongClick,
            onBackClick = onBackClick,
        )
    }
}

@Composable
private fun TitleBar(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = spacedBy(12.dp),
        modifier = modifier
            .height(TraktTheme.size.titleBarHeight)
            .graphicsLayer {
                translationX = -2.dp.toPx()
            },
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_back_arrow),
            tint = TraktTheme.colors.textPrimary,
            contentDescription = null,
        )
        TraktHeader(
            title = stringResource(R.string.list_title_start_watching),
        )
    }
}

@Composable
private fun ContentList(
    modifier: Modifier = Modifier,
    listState: LazyListState,
    listItems: ImmutableList<WatchlistItem>,
    listFilter: GlobalFilter?,
    loading: Boolean,
    contentPadding: PaddingValues,
    onClick: (WatchlistItem) -> Unit,
    onCheckClick: (WatchlistItem) -> Unit,
    onCheckLongClick: (WatchlistItem) -> Unit,
    onLongClick: (WatchlistItem) -> Unit,
    onModeClick: (MediaMode) -> Unit,
    onFiltersClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    LazyColumn(
        state = listState,
        verticalArrangement = spacedBy(0.dp),
        contentPadding = contentPadding,
        overscrollEffect = null,
        modifier = modifier,
    ) {
        item {
            TitleBar(
                modifier = Modifier
                    .onClick { onBackClick() },
            )
        }

        if (listFilter != null) {
            item {
                ContentFilters(
                    filters = listFilter,
                    enabled = !loading,
                    onModeClick = onModeClick,
                    onFiltersClick = onFiltersClick,
                )
            }
        }

        items(
            items = listItems,
            key = { it.key },
        ) { item ->
            when (item) {
                is ShowItem -> AllHomeWatchlistEpisodeView(
                    item = item,
                    enabled = !loading,
                    onClick = { onClick(item) },
                    onLongClick = { onLongClick(item) },
                    onCheckClick = { onCheckClick(item) },
                    onCheckLongClick = { onCheckLongClick(item) },
                    modifier = Modifier
                        .padding(bottom = TraktTheme.spacing.mainListVerticalSpace)
                        .animateItem(
                            fadeInSpec = null,
                            fadeOutSpec = null,
                        ),
                )

                is MovieItem -> AllWatchlistMovieView(
                    item = item,
                    sorting = Sorting.Default,
                    enabled = !loading,
                    showCheck = true,
                    onClick = { onClick(item) },
                    onLongClick = { onLongClick(item) },
                    onCheckClick = { onCheckClick(item) },
                    onCheckLongClick = { onCheckLongClick(item) },
                    modifier = Modifier
                        .padding(bottom = TraktTheme.spacing.mainListVerticalSpace)
                        .animateItem(
                            fadeInSpec = null,
                            fadeOutSpec = null,
                        ),
                )
            }
        }

        if (loading && listItems.isEmpty()) {
            items(5) {
                PanelMediaSkeletonCard(
                    modifier = Modifier
                        .padding(bottom = TraktTheme.spacing.mainListVerticalSpace)
                        .animateItem(
                            fadeInSpec = null,
                            fadeOutSpec = null,
                        ),
                )
            }
        }

        if (listItems.isEmpty() && !loading) {
            item {
                ContentEmptyView(
                    modifier = Modifier
                        .animateItem(
                            fadeInSpec = null,
                            fadeOutSpec = null,
                        ),
                )
            }
        }
    }
}

@Composable
private fun ContentFilters(
    filters: GlobalFilter,
    enabled: Boolean,
    onModeClick: (MediaMode) -> Unit,
    onFiltersClick: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 19.dp),
    ) {
        MediaModeFilters(
            selected = filters.mode,
            height = 32.dp,
            onClick = onModeClick,
        )
        MediaFilterIcon(
            active = filters.isActive,
            enabled = enabled,
            onClick = onFiltersClick,
        )
    }
}

@Composable
private fun ContentEmptyView(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.list_placeholder_empty),
        color = TraktTheme.colors.textSecondary,
        style = TraktTheme.typography.heading6,
        modifier = modifier,
    )
}

@Composable
private fun AllHomeDateSelectionSheet(
    item: WatchlistItem?,
    onDateSelected: (DateSelectionResult?) -> Unit,
    onCheckIn: () -> Unit,
    onDismiss: () -> Unit,
) {
    DateSelectionSheet(
        active = item != null,
        title = item?.title.orEmpty(),
        subtitle = when (item) {
            is ShowItem -> item.progress?.nextEpisode?.seasonEpisodeString()
            is MovieItem -> null
            else -> null
        },
        nowWatchingVisible = true,
        onCheckIn = onCheckIn,
        onResult = {
            if (item == null) return@DateSelectionSheet
            onDateSelected(it)
        },
        onDismiss = onDismiss,
    )
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        AllHomeWatchlistContent(
            state = AllHomeWatchlistState(),
        )
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview2() {
    TraktTheme {
        AllHomeWatchlistContent(
            state = AllHomeWatchlistState(),
        )
    }
}
