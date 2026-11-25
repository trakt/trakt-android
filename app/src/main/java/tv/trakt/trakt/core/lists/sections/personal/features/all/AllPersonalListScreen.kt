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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.launch
import tv.trakt.trakt.LocalBottomBarVisibility
import tv.trakt.trakt.LocalSnackbarState
import tv.trakt.trakt.common.core.user.UserCollectionState
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.toLocalDay
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.lists.CustomList
import tv.trakt.trakt.common.model.lists.SmartList
import tv.trakt.trakt.common.model.sorting.SortType.Added
import tv.trakt.trakt.common.model.sorting.SortType.Released
import tv.trakt.trakt.common.model.sorting.SortType.Title
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.common.ui.composables.rememberCacheWindow
import tv.trakt.trakt.core.filters.GlobalFiltersSheet
import tv.trakt.trakt.core.filters.navigation.GlobalFiltersOptions
import tv.trakt.trakt.core.lists.features.addfrom.components.ADD_FROM_LISTS_EMPTY_HEIGHT_FRACTION
import tv.trakt.trakt.core.lists.features.addfrom.components.AddFromListsEmpty
import tv.trakt.trakt.core.lists.features.convert.upgrade.SmartListUpgradeView
import tv.trakt.trakt.core.lists.features.details.info.ListInfoSheet
import tv.trakt.trakt.core.lists.features.details.ui.ListDetailsEpisodeView
import tv.trakt.trakt.core.lists.features.details.ui.ListDetailsMovieView
import tv.trakt.trakt.core.lists.features.details.ui.ListDetailsSeasonView
import tv.trakt.trakt.core.lists.features.details.ui.ListDetailsShowView
import tv.trakt.trakt.core.lists.helpers.shareList
import tv.trakt.trakt.core.lists.model.CustomListItem
import tv.trakt.trakt.core.lists.model.CustomListItem.EpisodeItem
import tv.trakt.trakt.core.lists.model.CustomListItem.MovieItem
import tv.trakt.trakt.core.lists.model.CustomListItem.SeasonItem
import tv.trakt.trakt.core.lists.model.CustomListItem.ShowItem
import tv.trakt.trakt.core.lists.sections.personal.features.context.item.ListItemContextSheet
import tv.trakt.trakt.core.lists.sections.personal.features.context.movie.sheet.ListMovieContextSheet
import tv.trakt.trakt.core.lists.sections.personal.features.context.show.sheet.ListShowContextSheet
import tv.trakt.trakt.core.lists.sheets.EditListSheet
import tv.trakt.trakt.core.lists.sheets.collaborators.ManageCollaboratorsSheet
import tv.trakt.trakt.core.lists.sheets.collaborators.showListMovedSnackbar
import tv.trakt.trakt.helpers.SimpleScrollConnection
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.MediaFilterIcon
import tv.trakt.trakt.ui.components.MediaModeFilters
import tv.trakt.trakt.ui.components.ScrollableBackdropImage
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.lists.ListItemCount
import tv.trakt.trakt.ui.components.mediacards.list.PersonalListDropdown
import tv.trakt.trakt.ui.components.mediacards.skeletons.PanelMediaSkeletonCard
import tv.trakt.trakt.ui.components.sorting.SortingSplitButton
import tv.trakt.trakt.ui.components.sorting.sheets.SortSelectionSheet
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun AllPersonalListScreen(
    modifier: Modifier = Modifier,
    viewModel: AllPersonalListViewModel,
    onShowClick: (TraktId) -> Unit,
    onMovieClick: (TraktId) -> Unit,
    onListClick: (CustomList) -> Unit,
    onEpisodeClick: (TraktId, Episode) -> Unit,
    onReorderClick: (CustomList) -> Unit,
    onPersonClick: (Person) -> Unit,
    onAddFromListsClick: (CustomList) -> Unit,
    onConvertToSmartListClick: (CustomList) -> Unit,
    onCreateSmartListClick: () -> Unit,
    onSmartListRedirect: (SmartList) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val context = LocalContext.current
    val resources = LocalResources.current
    val snackbar = LocalSnackbarState.current
    val scope = rememberCoroutineScope()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.navigateMovie, state.navigateShow, state.navigateEpisode) {
        state.navigateShow?.let {
            onShowClick(it)
            viewModel.clearNavigation()
        }
        state.navigateMovie?.let {
            onMovieClick(it)
            viewModel.clearNavigation()
        }
        state.navigateEpisode?.let {
            onEpisodeClick(it.first, it.second)
            viewModel.clearNavigation()
        }
    }

    var showContextSheet by remember { mutableStateOf<ShowItem?>(null) }
    var movieContextSheet by remember { mutableStateOf<MovieItem?>(null) }
    var itemContextSheet by remember { mutableStateOf<CustomListItem?>(null) }
    var editListSheet by remember { mutableStateOf<CustomList?>(null) }
    var collaboratorsSheet by remember { mutableStateOf<CustomList?>(null) }
    var sortSheet by remember { mutableStateOf<Sorting?>(null) }
    var filtersSheet by remember { mutableStateOf(false) }
    var infoSheet by remember { mutableStateOf<CustomList?>(null) }

    val upgradeList = state.list?.takeIf {
        it.staleOrigin != null || it.convertedTo != null
    }

    AllPersonalListContent(
        state = state,
        modifier = modifier,
        upgradeContent = upgradeList?.let { list ->
            {
                SmartListUpgradeView(
                    list = list,
                    onConvertClick = onConvertToSmartListClick,
                    onCreateSmartListClick = onCreateSmartListClick,
                    onSmartListRedirect = onSmartListRedirect,
                )
            }
        },
        onLoadMoreData = viewModel::loadMoreData,
        onClick = {
            when (it) {
                is MovieItem -> viewModel.navigateToMovie(it.movie)
                is ShowItem -> viewModel.navigateToShow(it.show)
                is SeasonItem -> viewModel.navigateToShow(it.show)
                is EpisodeItem -> viewModel.navigateToEpisode(it.show, it.episode)
            }
        },
        onLongClick = {
            when (it) {
                is MovieItem -> movieContextSheet = it
                is ShowItem -> showContextSheet = it
                is SeasonItem, is EpisodeItem -> itemContextSheet = it
            }
        },
        onModeClick = { mode ->
            state.filter?.let {
                viewModel.setFilter(it.copy(mode = mode))
            }
        },
        onFiltersClick = {
            filtersSheet = true
        },
        onSortTypeClick = {
            if (!state.loading.isLoading && !state.loadingMore.isLoading) {
                sortSheet = state.sorting
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
        onShareClick = {
            viewModel.logShare()
            shareList(
                context = context,
                user = state.user,
                list = state.list?.ids?.slug?.value.orEmpty(),
            )
        },
        onMoreClick = {
            editListSheet = state.list
        },
        onReorderClick = {
            state.list?.let(onReorderClick)
        },
        onInfoClick = {
            infoSheet = state.list
        },
        onAddFromListsClick = {
            state.list?.let(onAddFromListsClick)
        },
        onCollaboratorsClick = {
            collaboratorsSheet = state.list
        },
        onBackClick = onNavigateBack,
    )

    ListInfoSheet(
        list = infoSheet,
        onPersonClick = {
            infoSheet = null
            onPersonClick(it)
        },
        onDismiss = {
            infoSheet = null
        },
    )

    ListShowContextSheet(
        onListClick = onListClick,
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
        onListClick = onListClick,
        movie = movieContextSheet?.movie,
        list = state.list,
        onRemoveListItem = {
            viewModel.removeItem(movieContextSheet)
        },
        onDismiss = {
            movieContextSheet = null
        },
    )

    ListItemContextSheet(
        onListClick = onListClick,
        item = itemContextSheet,
        list = state.list,
        removable = true,
        onRemoveListItem = {
            viewModel.removeItem(itemContextSheet)
        },
        onDismiss = {
            itemContextSheet = null
        },
    )

    EditListSheet(
        active = editListSheet != null,
        list = editListSheet,
        onListEdited = viewModel::loadDetails,
        onListDeleted = onNavigateBack,
        onDismiss = { editListSheet = null },
    )

    ManageCollaboratorsSheet(
        active = collaboratorsSheet != null,
        list = collaboratorsSheet,
        onListMoved = { list, destination ->
            scope.launch {
                snackbar.showListMovedSnackbar(
                    resources = resources,
                    listName = list.name,
                    destination = destination,
                    viewAction = false,
                )
            }
        },
        onDismiss = { collaboratorsSheet = null },
    )

    SortSelectionSheet(
        active = sortSheet != null,
        selectedSorting = sortSheet,
        onResult = {
            viewModel.setSorting(
                state.sorting.copy(
                    type = it.type,
                    order = it.order,
                ),
            )
        },
        onDismiss = {
            sortSheet = null
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
internal fun AllPersonalListContent(
    state: AllPersonalListState,
    modifier: Modifier = Modifier,
    upgradeContent: (@Composable () -> Unit)? = null,
    onLoadMoreData: () -> Unit = {},
    onClick: (CustomListItem) -> Unit = {},
    onLongClick: (CustomListItem) -> Unit = {},
    onModeClick: (MediaMode) -> Unit = {},
    onFiltersClick: () -> Unit = {},
    onSortTypeClick: () -> Unit = {},
    onSortOrderClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onMoreClick: () -> Unit = {},
    onReorderClick: () -> Unit = {},
    onInfoClick: () -> Unit = {},
    onAddFromListsClick: () -> Unit = {},
    onCollaboratorsClick: () -> Unit = {},
) {
    val listState = rememberLazyListState()

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
            subtitle = state.list?.privacy?.let {
                stringResource(it.displayRes)
            },
            itemCount = state.list?.itemCount,
            infoEnabled = state.list != null,
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
            onModeClick = onModeClick,
            onFiltersClick = onFiltersClick,
            onSortTypeClick = onSortTypeClick,
            onSortOrderClick = onSortOrderClick,
            onBackClick = onBackClick,
            onMoreClick = onMoreClick,
            onShareClick = onShareClick,
            onReorderClick = onReorderClick,
            onInfoClick = onInfoClick,
            onAddFromListsClick = onAddFromListsClick,
            onCollaboratorsClick = onCollaboratorsClick,
            onEndOfList = onLoadMoreData,
        )

        upgradeContent?.let {
            val bottomBarVisible = LocalBottomBarVisibility.current.value
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(
                        start = TraktTheme.spacing.mainPageHorizontalSpace * 1.5F,
                        end = TraktTheme.spacing.mainPageHorizontalSpace * 1.5F,
                        bottom = WindowInsets.navigationBars.asPaddingValues()
                            .calculateBottomPadding()
                            .plus(
                                when {
                                    bottomBarVisible -> TraktTheme.size.navigationBarHeight
                                    else -> 0.dp
                                },
                            )
                            .plus(4.dp),
                    ),
            ) {
                it()
            }
        }
    }
}

@Composable
private fun TitleBar(
    enabled: Boolean,
    reorderEnabled: Boolean,
    title: String,
    subtitle: String?,
    itemCount: Int?,
    filters: GlobalFilter?,
    infoEnabled: Boolean,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onFiltersClick: () -> Unit = {},
    onReorderClick: () -> Unit = {},
    onMoreClick: () -> Unit = {},
    onInfoClick: () -> Unit = {},
    onAddFromListsClick: () -> Unit = {},
    onCollaboratorsClick: () -> Unit = {},
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
                subtitle = subtitle,
                subtitleTrailing = itemCount?.let {
                    { ListItemCount(count = it) }
                },
            )
        }

        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = spacedBy(22.dp),
            modifier = Modifier.padding(start = 16.dp),
        ) {
            if (infoEnabled) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .graphicsLayer {
                            translationX = 4.dp.toPx()
                        }
                        .size(22.dp)
                        .onClick(onClick = onInfoClick),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_info),
                        tint = TraktTheme.colors.textPrimary,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            filters?.let {
                MediaFilterIcon(
                    active = it.isActive,
                    enabled = enabled,
                    onClick = onFiltersClick,
                    modifier = Modifier
                        .graphicsLayer {
                            translationX = 2.dp.toPx()
                        },
                )
            }

            PersonalListDropdown(
                reorderEnabled = reorderEnabled,
                iconSize = 18.dp,
                onEditClick = onMoreClick,
                onReorderClick = onReorderClick,
                onShareClick = onShareClick,
                onAddFromListsClick = onAddFromListsClick,
                onCollaboratorsClick = onCollaboratorsClick,
            )
        }
    }
}

@Composable
private fun ContentFilters(
    watchlistFilter: GlobalFilter,
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
                top = 8.dp,
                bottom = 17.dp,
            ),
    ) {
        MediaModeFilters(
            selected = watchlistFilter.mode,
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
    itemCount: Int?,
    infoEnabled: Boolean,
    loading: Boolean,
    loadingMore: Boolean,
    listState: LazyListState,
    listItems: ImmutableList<CustomListItem>,
    listFilter: GlobalFilter?,
    listSorting: Sorting,
    collection: UserCollectionState,
    contentPadding: PaddingValues,
    onClick: (CustomListItem) -> Unit,
    onLongClick: (CustomListItem) -> Unit,
    onModeClick: (MediaMode) -> Unit,
    onFiltersClick: () -> Unit,
    onSortTypeClick: () -> Unit,
    onSortOrderClick: () -> Unit,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    onMoreClick: () -> Unit,
    onReorderClick: () -> Unit,
    onInfoClick: () -> Unit,
    onAddFromListsClick: () -> Unit,
    onCollaboratorsClick: () -> Unit,
    onEndOfList: () -> Unit,
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

    val itemsGroup = remember(listItems) {
        when (listSorting.type) {
            Title -> listItems.groupBy { it.title.firstOrNull()?.uppercaseChar()?.toString() ?: "#" }
            Added -> listItems.groupBy { it.listedAt.toLocalDay().year.toString() }
            Released -> listItems.groupBy { it.released?.toLocalDay()?.year?.toString() ?: "N/A" }
            else -> listItems.groupBy { null }
        }.toImmutableMap()
    }

    LazyColumn(
        state = listState,
        cacheWindow = rememberCacheWindow(0.5F),
        verticalArrangement = spacedBy(0.dp),
        contentPadding = contentPadding,
        overscrollEffect = null,
        modifier = modifier,
    ) {
        item {
            TitleBar(
                enabled = !loading,
                reorderEnabled = !loading && listItems.isNotEmpty(),
                title = title,
                subtitle = subtitle,
                itemCount = itemCount,
                filters = listFilter,
                infoEnabled = infoEnabled,
                onBackClick = onBackClick,
                onShareClick = onShareClick,
                onMoreClick = onMoreClick,
                onReorderClick = onReorderClick,
                onFiltersClick = onFiltersClick,
                onInfoClick = onInfoClick,
                onAddFromListsClick = onAddFromListsClick,
                onCollaboratorsClick = onCollaboratorsClick,
            )
        }

        if (listFilter != null) {
            item {
                ContentFilters(
                    watchlistFilter = listFilter,
                    watchlistSort = listSorting,
                    onFilterClick = onModeClick,
                    onSortTypeClick = onSortTypeClick,
                    onSortOrderClick = onSortOrderClick,
                )
            }
        }

        itemsGroup.keys.forEachIndexed { index, key ->
            key?.let {
                item(
                    key = "header-$key",
                ) {
                    TraktHeader(
                        title = key,
                        modifier = Modifier
                            .padding(
                                top = if (index == 0) 0.dp else 16.dp,
                                bottom = 12.dp,
                            )
                            .animateItem(
                                fadeInSpec = null,
                                fadeOutSpec = null,
                            ),
                    )
                }
            }

            items(
                items = itemsGroup[key] ?: EmptyImmutableList,
                key = { it.key },
            ) { item ->
                when (item) {
                    is ShowItem -> ListDetailsShowView(
                        show = item.show,
                        showUserRating = item.userRating,
                        sorting = listSorting,
                        enabled = !loading,
                        showIcon = true,
                        watched = collection.isWatched(item.id, item.type, item.show.airedEpisodes),
                        watching = collection.isWatching(item.id, item.type, item.show.airedEpisodes),
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

                    is MovieItem -> ListDetailsMovieView(
                        movie = item.movie,
                        movieUserRating = item.userRating,
                        sorting = listSorting,
                        enabled = !loading,
                        showIcon = true,
                        watched = collection.isWatched(item.id, item.type, null),
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

                    is SeasonItem -> ListDetailsSeasonView(
                        item = item,
                        sorting = listSorting,
                        enabled = !loading,
                        onClick = { onClick(item) },
                        onLongClick = { onLongClick(item) },
                        modifier = Modifier
                            .padding(bottom = TraktTheme.spacing.mainListVerticalSpace)
                            .animateItem(
                                fadeInSpec = null,
                                fadeOutSpec = null,
                            ),
                    )

                    is EpisodeItem -> ListDetailsEpisodeView(
                        item = item,
                        sorting = listSorting,
                        enabled = !loading,
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
                when (itemCount) {
                    0 -> AddFromListsEmpty(
                        onClick = onAddFromListsClick,
                        modifier = Modifier.fillParentMaxHeight(ADD_FROM_LISTS_EMPTY_HEIGHT_FRACTION),
                    )
                    else -> ContentEmpty(filter = listFilter?.mode)
                }
            }
        }
    }
}

@Composable
private fun ContentEmpty(filter: MediaMode?) {
    Text(
        text = stringResource(
            when (filter) {
                MediaMode.Movies -> R.string.list_placeholder_personal_list_empty_movies
                MediaMode.Shows -> R.string.list_placeholder_personal_list_empty_shows
                else -> R.string.list_placeholder_empty
            },
        ),
        color = TraktTheme.colors.textSecondary,
        style = TraktTheme.typography.heading6,
    )
}
