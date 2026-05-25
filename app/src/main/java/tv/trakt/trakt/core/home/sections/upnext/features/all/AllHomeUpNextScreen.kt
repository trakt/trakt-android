@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package tv.trakt.trakt.core.home.sections.upnext.features.all

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.core.filters.GlobalFiltersSheet
import tv.trakt.trakt.core.filters.navigation.GlobalFiltersOptions
import tv.trakt.trakt.core.home.sections.upnext.features.all.ui.AllUpNextMovieView
import tv.trakt.trakt.core.home.sections.upnext.features.all.ui.AllUpNextShowView
import tv.trakt.trakt.core.home.sections.upnext.features.context.sheets.UpNextItemContextSheet
import tv.trakt.trakt.core.home.sections.upnext.model.UpNextItem
import tv.trakt.trakt.core.home.sections.upnext.model.UpNextMovie
import tv.trakt.trakt.core.home.sections.upnext.model.UpNextShow
import tv.trakt.trakt.helpers.SimpleScrollConnection
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.MediaFilterIcon
import tv.trakt.trakt.ui.components.MediaModeFilters
import tv.trakt.trakt.ui.components.ScrollableBackdropImage
import tv.trakt.trakt.ui.components.dateselection.DateSelectionResult
import tv.trakt.trakt.ui.components.dateselection.DateSelectionSheet
import tv.trakt.trakt.ui.components.mediacards.skeletons.PanelMediaSkeletonCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun AllHomeUpNextScreen(
    modifier: Modifier = Modifier,
    viewModel: AllHomeUpNextViewModel = koinViewModel(),
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
    onNavigateToEpisode: (showId: TraktId, episode: Episode) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current

    var contextSheet by remember { mutableStateOf<UpNextItem?>(null) }
    var dateSheet by remember { mutableStateOf<UpNextShow?>(null) }
    var filtersSheet by remember { mutableStateOf(false) }

    LaunchedEffect(state.info) {
        if (state.info != null) {
            haptic.performHapticFeedback(Confirm)
            viewModel.clearInfo()
        }
    }

    AllHomeUpNextContent(
        state = state,
        modifier = modifier,
        onLoadMore = viewModel::loadMoreData,
        onModeClick = { mode ->
            state.filter?.let {
                viewModel.setFilter(it.copy(mode = mode))
            }
        },
        onFiltersClick = {
            filtersSheet = true
        },
        onClick = {
            if (!it.loading && it is UpNextShow && it.progress.nextEpisode != null) {
                onNavigateToEpisode(
                    it.show.ids.trakt,
                    it.progress.nextEpisode,
                )
            }

            if (!it.loading && it is UpNextMovie) {
                onNavigateToMovie(it.movie.ids.trakt)
            }
        },
        onLongClick = {
            if (!it.loading) {
                contextSheet = it
            }
        },
        onCheckClick = {
            viewModel.addToHistory(it.id)
        },
        onCheckLongClick = {
            if (!it.loading && it is UpNextShow) {
                dateSheet = it
            }
        },
        onShowClick = {
            if (!it.loading) {
                onNavigateToShow(it.show.ids.trakt)
            }
        },
        onBackClick = onNavigateBack,
    )

    UpNextItemContextSheet(
        sheetItem = contextSheet,
        onDismiss = { contextSheet = null },
        onAddWatched = {
            dateSheet = contextSheet as UpNextShow
        },
        onDropped = {
            when (it) {
                is UpNextShow -> viewModel.removeShow(it.show.ids.trakt)
                is UpNextMovie -> viewModel.removeMovie(it.movie.ids.trakt)
            }
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

    HomeDateSelectionSheet(
        item = dateSheet,
        onDateSelected = { date ->
            val episode = dateSheet?.progress?.nextEpisode
                ?: return@HomeDateSelectionSheet

            viewModel.addToHistory(
                episodeId = episode.ids.trakt,
                customDate = date,
            )
        },
        onCheckIn = {
            val item = dateSheet ?: return@HomeDateSelectionSheet
            val episode = item.progress.nextEpisode ?: return@HomeDateSelectionSheet
            viewModel.addEpisodeCheckIn(
                showId = item.show.ids.trakt,
                episodeId = episode.ids.trakt,
                seasonEpisode = episode.seasonEpisode,
            )
        },
        onDismiss = {
            dateSheet = null
        },
    )
}

@Composable
internal fun AllHomeUpNextContent(
    state: AllHomeUpNextState,
    modifier: Modifier = Modifier,
    onClick: (UpNextItem) -> Unit = {},
    onLongClick: (UpNextItem) -> Unit = {},
    onCheckClick: (UpNextItem) -> Unit = {},
    onCheckLongClick: (UpNextItem) -> Unit = {},
    onShowClick: (UpNextShow) -> Unit = {},
    onMovieClick: (UpNextMovie) -> Unit = {},
    onModeClick: (MediaMode) -> Unit = {},
    onFiltersClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onLoadMore: () -> Unit = {},
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
            listState = listState,
            listItems = (state.items ?: emptyList()).toImmutableList(),
            listFilter = state.filter,
            contentPadding = contentPadding,
            loading = state.loading.isLoading,
            loadingMore = state.loadingMore.isLoading,
            onEndOfList = onLoadMore,
            onFilterClick = onModeClick,
            onFiltersClick = onFiltersClick,
            onClick = onClick,
            onLongClick = onLongClick,
            onCheckClick = onCheckClick,
            onCheckLongClick = onCheckLongClick,
            onShowClick = onShowClick,
            onMovieClick = onMovieClick,
            onBackClick = onBackClick,
        )
    }
}

@Composable
private fun ContentList(
    modifier: Modifier = Modifier,
    listItems: ImmutableList<UpNextItem>,
    listState: LazyListState,
    listFilter: GlobalFilter?,
    contentPadding: PaddingValues,
    loading: Boolean,
    loadingMore: Boolean,
    onEndOfList: () -> Unit,
    onFilterClick: (MediaMode) -> Unit,
    onFiltersClick: () -> Unit,
    onClick: (UpNextItem) -> Unit,
    onLongClick: (UpNextItem) -> Unit,
    onCheckClick: (UpNextItem) -> Unit,
    onCheckLongClick: (UpNextItem) -> Unit,
    onShowClick: (UpNextShow) -> Unit,
    onMovieClick: (UpNextMovie) -> Unit,
    onBackClick: () -> Unit,
) {
    val isScrolledToBottom by remember(listItems.size) {
        derivedStateOf {
            listState.firstVisibleItemIndex >= (listItems.size - 5)
        }
    }

    LaunchedEffect(isScrolledToBottom) {
        if (isScrolledToBottom) {
            onEndOfList()
        }
    }

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
                    .padding(bottom = 2.dp)
                    .onClick { onBackClick() },
            )
        }

        if (listFilter != null) {
            item {
                ContentFilters(
                    filters = listFilter,
                    enabled = !loading,
                    onFilterClick = onFilterClick,
                    onFiltersClick = onFiltersClick,
                )
            }
        }

        items(
            items = listItems,
            key = { it.key },
        ) { item ->
            if (item is UpNextShow) {
                AllUpNextShowView(
                    item = item,
                    enabled = !loading,
                    onClick = { onClick(item) },
                    onLongClick = { onLongClick(item) },
                    onCheckClick = { onCheckClick(item) },
                    onCheckLongClick = { onCheckLongClick(item) },
                    onShowClick = { onShowClick(item) },
                    modifier = Modifier.animateItem(
                        fadeInSpec = null,
                        fadeOutSpec = null,
                    ),
                )
            } else if (item is UpNextMovie) {
                AllUpNextMovieView(
                    item = item,
                    enabled = !loading,
                    onClick = { onClick(item) },
                    onLongClick = { onLongClick(item) },
                    onMovieClick = { onMovieClick(item) },
                    modifier = Modifier.animateItem(
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
        } else if (loadingMore) {
            item {
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
    onFilterClick: (MediaMode) -> Unit,
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
            onClick = onFilterClick,
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
        Text(
            text = stringResource(R.string.list_title_up_next),
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.heading5,
        )
    }
}

@Composable
private fun HomeDateSelectionSheet(
    item: UpNextShow?,
    onDateSelected: (DateSelectionResult?) -> Unit,
    onCheckIn: () -> Unit,
    onDismiss: () -> Unit,
) {
    DateSelectionSheet(
        active = item != null,
        title = item?.show?.title.orEmpty(),
        subtitle = item?.progress?.nextEpisode?.seasonEpisodeString(),
        nowWatchingVisible = true,
        onResult = {
            if (item == null) return@DateSelectionSheet
            onDateSelected(it)
        },
        onCheckIn = onCheckIn,
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
        AllHomeUpNextContent(
            state = AllHomeUpNextState(
                loading = LoadingState.Done,
            ),
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
        AllHomeUpNextContent(
            state = AllHomeUpNextState(
                loading = Loading,
            ),
        )
    }
}
