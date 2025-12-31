@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package tv.trakt.trakt.core.lists.sections.personal.features.all

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.W400
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.sorting.SortTypeList
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.core.lists.model.PersonalListItem
import tv.trakt.trakt.core.lists.model.PersonalListItem.MovieItem
import tv.trakt.trakt.core.lists.model.PersonalListItem.ShowItem
import tv.trakt.trakt.core.lists.sections.personal.features.all.views.AllPersonalListMovieView
import tv.trakt.trakt.core.lists.sections.personal.features.all.views.AllPersonalListShowView
import tv.trakt.trakt.core.lists.sections.personal.features.context.movie.sheet.ListMovieContextSheet
import tv.trakt.trakt.core.lists.sections.personal.features.context.show.sheet.ListShowContextSheet
import tv.trakt.trakt.core.lists.sheets.EditListSheet
import tv.trakt.trakt.core.main.model.MediaMode
import tv.trakt.trakt.core.user.UserCollectionState
import tv.trakt.trakt.helpers.SimpleScrollConnection
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.MediaModeFilters
import tv.trakt.trakt.ui.components.ScrollableBackdropImage
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.mediacards.skeletons.PanelMediaSkeletonCard
import tv.trakt.trakt.ui.components.sorting.SortingSplitButton
import tv.trakt.trakt.ui.components.sorting.sheets.SortSelectionSheet
import tv.trakt.trakt.ui.theme.TraktTheme

private const val LIST_DESCRIPTION_LIMIT = 40

@Composable
internal fun AllPersonalListScreen(
    modifier: Modifier = Modifier,
    viewModel: AllPersonalListViewModel,
    onShowClick: (TraktId) -> Unit,
    onMovieClick: (TraktId) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.navigateMovie, state.navigateShow) {
        state.navigateShow?.let {
            onShowClick(it)
            viewModel.clearNavigation()
        }
        state.navigateMovie?.let {
            onMovieClick(it)
            viewModel.clearNavigation()
        }
    }

    var showContextSheet by remember { mutableStateOf<ShowItem?>(null) }
    var movieContextSheet by remember { mutableStateOf<MovieItem?>(null) }
    var editListSheet by remember { mutableStateOf<CustomList?>(null) }
    var sortSheet by remember { mutableStateOf<SortTypeList?>(null) }

    AllPersonalListContent(
        state = state,
        modifier = modifier,
        onLoadMoreData = viewModel::loadMoreData,
        onClick = {
            when (it) {
                is MovieItem -> viewModel.navigateToMovie(it.movie)
                is ShowItem -> viewModel.navigateToShow(it.show)
            }
        },
        onLongClick = {
            when (it) {
                is MovieItem -> movieContextSheet = it
                is ShowItem -> showContextSheet = it
            }
        },
        onFilterClick = viewModel::setFilter,
        onSortTypeClick = {
            if (!state.loading.isLoading && !state.loadingMore.isLoading) {
                sortSheet = state.sorting.type
            }
        },
        onSortOrderClick = {
            val sorting = state.sorting
            viewModel.setSorting(
                sorting.copy(
                    order = sorting.order.toggle(),
                ),
            )
        },
        onMoreClick = {
            editListSheet = state.list
        },
        onBackClick = onNavigateBack,
    )

    ListShowContextSheet(
        show = showContextSheet?.show,
        list = state.list,
        onRemoveListItem = {
            viewModel.removeItem(showContextSheet)
        },
        onDismiss = {
            showContextSheet = null
        },
    )

    ListMovieContextSheet(
        movie = movieContextSheet?.movie,
        list = state.list,
        onRemoveListItem = {
            viewModel.removeItem(movieContextSheet)
        },
        onDismiss = {
            movieContextSheet = null
        },
    )

    EditListSheet(
        active = editListSheet != null,
        list = editListSheet,
        onListEdited = viewModel::loadDetails,
        onListDeleted = onNavigateBack,
        onDismiss = { editListSheet = null },
    )

    SortSelectionSheet(
        active = sortSheet != null,
        selected = sortSheet,
        onResult = {
            viewModel.setSorting(
                state.sorting.copy(
                    type = it,
                ),
            )
        },
        onDismiss = {
            sortSheet = null
        },
    )
}

@Composable
internal fun AllPersonalListContent(
    state: AllPersonalListState,
    modifier: Modifier = Modifier,
    onLoadMoreData: () -> Unit = {},
    onClick: (PersonalListItem) -> Unit = {},
    onLongClick: (PersonalListItem) -> Unit = {},
    onFilterClick: (MediaMode) -> Unit = {},
    onSortTypeClick: () -> Unit = {},
    onSortOrderClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onMoreClick: () -> Unit = {},
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
            title = state.list?.name ?: "",
            subtitle = state.list?.description,
            loading = state.loading.isLoading,
            loadingMore = state.loadingMore.isLoading,
            listState = listState,
            listFilter = state.filter,
            listSorting = state.sorting,
            listItems = (state.items ?: emptyList()).toImmutableList(),
            collection = state.collection,
            contentPadding = contentPadding,
            onClick = onClick,
            onLongClick = onLongClick,
            onFilterClick = onFilterClick,
            onSortTypeClick = onSortTypeClick,
            onSortOrderClick = onSortOrderClick,
            onBackClick = onBackClick,
            onMoreClick = onMoreClick,
            onEndOfList = onLoadMoreData,
        )
    }
}

@Composable
private fun TitleBar(
    title: String,
    subtitle: String?,
    subtitleVisible: Boolean,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onMoreClick: () -> Unit = {},
) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .height(TraktTheme.size.titleBarHeight),
    ) {
        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = spacedBy(12.dp),
            modifier = Modifier
                .weight(1F, fill = false)
                .graphicsLayer {
                    translationX = -2.dp.toPx()
                }
                .onClick {
                    onBackClick()
                },
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_back_arrow),
                tint = TraktTheme.colors.textPrimary,
                contentDescription = null,
            )
            TraktHeader(
                title = title,
                subtitle = when {
                    subtitleVisible -> subtitle
                    else -> null
                },
            )
        }

        Icon(
            painter = painterResource(R.drawable.ic_edit),
            contentDescription = null,
            tint = TraktTheme.colors.textPrimary,
            modifier = Modifier
                .padding(start = 16.dp)
                .fillMaxHeight()
                .onClick { onMoreClick() }
                .size(20.dp),
        )
    }
}

@Composable
private fun ContentFilters(
    hasSubtitle: Boolean,
    watchlistFilter: MediaMode,
    watchlistSort: Sorting,
    onSortTypeClick: () -> Unit,
    onSortOrderClick: () -> Unit,
    onFilterClick: (MediaMode) -> Unit,
) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = if (hasSubtitle) 8.dp else 0.dp,
                bottom = 19.dp,
            ),
    ) {
        MediaModeFilters(
            selected = watchlistFilter,
            onClick = onFilterClick,
            height = 32.dp,
            unselectedTextVisible = false,
        )

        SortingSplitButton(
            text = stringResource(watchlistSort.type.displayStringRes),
            order = watchlistSort.order,
            height = 32.dp,
            onLeadingClick = onSortTypeClick,
            onTrailingClick = onSortOrderClick,
        )
    }
}

@Composable
private fun ContentList(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String?,
    loading: Boolean,
    loadingMore: Boolean,
    listState: LazyListState,
    listItems: ImmutableList<PersonalListItem>,
    listFilter: MediaMode?,
    listSorting: Sorting?,
    collection: UserCollectionState,
    contentPadding: PaddingValues,
    onClick: (PersonalListItem) -> Unit,
    onLongClick: (PersonalListItem) -> Unit,
    onFilterClick: (MediaMode) -> Unit,
    onSortTypeClick: () -> Unit,
    onSortOrderClick: () -> Unit,
    onBackClick: () -> Unit,
    onMoreClick: () -> Unit,
    onEndOfList: () -> Unit = {},
) {
    val subtitleVisible = remember(subtitle) {
        (subtitle?.length ?: 0) <= LIST_DESCRIPTION_LIMIT
    }

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
                title = title,
                subtitle = subtitle,
                subtitleVisible = subtitleVisible,
                onBackClick = onBackClick,
                onMoreClick = onMoreClick,
            )
        }

        if (!subtitleVisible) {
            item {
                var collapsed by remember { mutableStateOf(true) }
                Text(
                    text = subtitle ?: "",
                    color = TraktTheme.colors.textSecondary,
                    style = TraktTheme.typography.meta.copy(
                        fontWeight = W400,
                        lineHeight = 1.1.em,
                    ),
                    maxLines = when {
                        collapsed -> 3
                        else -> Int.MAX_VALUE
                    },
                    overflow = Ellipsis,
                    modifier = Modifier
                        .padding(bottom = 10.dp)
                        .onClick {
                            collapsed = !collapsed
                        },
                )
            }
        }

        if (listFilter != null && listSorting != null) {
            item {
                ContentFilters(
                    hasSubtitle = !subtitle.isNullOrEmpty(),
                    watchlistFilter = listFilter,
                    watchlistSort = listSorting,
                    onFilterClick = onFilterClick,
                    onSortTypeClick = onSortTypeClick,
                    onSortOrderClick = onSortOrderClick,
                )
            }
        }

        items(
            items = listItems,
            key = { it.key },
        ) { item ->
            when (item) {
                is ShowItem -> AllPersonalListShowView(
                    item = item,
                    enabled = !loading,
                    showIcon = true,
                    watched = collection.isWatched(item.id, item.type),
                    watchlist = collection.isWatchlist(item.id, item.type),
                    onClick = { onClick(item) },
                    onLongClick = { onLongClick(item) },
                    modifier = Modifier
                        .padding(bottom = TraktTheme.spacing.mainListVerticalSpace)
                        .animateItem(
                            fadeInSpec = null,
                            fadeOutSpec = null,
                        ),
                )

                is MovieItem -> AllPersonalListMovieView(
                    item = item,
                    enabled = !loading,
                    showIcon = true,
                    watched = collection.isWatched(item.id, item.type),
                    watchlist = collection.isWatchlist(item.id, item.type),
                    onClick = { onClick(item) },
                    onLongClick = { onLongClick(item) },
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
            items(10) {
                PanelMediaSkeletonCard(
                    modifier = Modifier
                        .padding(bottom = TraktTheme.spacing.mainListVerticalSpace)
                        .animateItem(
                            fadeInSpec = null,
                            fadeOutSpec = null,
                        ),
                )
            }
        } else if (loadingMore && listItems.isNotEmpty()) {
            items(1) {
                PanelMediaSkeletonCard(
                    modifier = Modifier
                        .padding(bottom = TraktTheme.spacing.mainListVerticalSpace)
                        .animateItem(
                            fadeInSpec = null,
                            fadeOutSpec = null,
                        ),
                )
            }
        } else if (listItems.isEmpty()) {
            item {
                ContentEmpty()
            }
        }
    }
}

@Composable
private fun ContentEmpty() {
    Text(
        text = stringResource(R.string.list_placeholder_empty),
        color = TraktTheme.colors.textSecondary,
        style = TraktTheme.typography.heading6,
    )
}
