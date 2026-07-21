@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package tv.trakt.trakt.core.lists.sections.personal.features.all

import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.lazy.layout.LazyLayoutCacheWindow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.W400
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import timber.log.Timber
import tv.trakt.trakt.common.Config
import tv.trakt.trakt.common.core.user.UserCollectionState
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.toLocalDay
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.sorting.SortType.Added
import tv.trakt.trakt.common.model.sorting.SortType.Released
import tv.trakt.trakt.common.model.sorting.SortType.Title
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.core.filters.GlobalFiltersSheet
import tv.trakt.trakt.core.filters.navigation.GlobalFiltersOptions
import tv.trakt.trakt.core.lists.features.details.ui.ListDetailsEpisodeView
import tv.trakt.trakt.core.lists.features.details.ui.ListDetailsMovieView
import tv.trakt.trakt.core.lists.features.details.ui.ListDetailsSeasonView
import tv.trakt.trakt.core.lists.features.details.ui.ListDetailsShowView
import tv.trakt.trakt.core.lists.model.CustomListItem
import tv.trakt.trakt.core.lists.model.CustomListItem.EpisodeItem
import tv.trakt.trakt.core.lists.model.CustomListItem.MovieItem
import tv.trakt.trakt.core.lists.model.CustomListItem.SeasonItem
import tv.trakt.trakt.core.lists.model.CustomListItem.ShowItem
import tv.trakt.trakt.core.lists.sections.personal.features.context.movie.sheet.ListMovieContextSheet
import tv.trakt.trakt.core.lists.sections.personal.features.context.show.sheet.ListShowContextSheet
import tv.trakt.trakt.core.lists.sheets.EditListSheet
import tv.trakt.trakt.helpers.SimpleScrollConnection
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.MediaFilterIcon
import tv.trakt.trakt.ui.components.MediaModeFilters
import tv.trakt.trakt.ui.components.ScrollableBackdropImage
import tv.trakt.trakt.ui.components.TraktHeader
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
    onEpisodeClick: (TraktId, Episode) -> Unit,
    onReorderClick: (CustomList) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val context = LocalContext.current
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
    var editListSheet by remember { mutableStateOf<CustomList?>(null) }
    var sortSheet by remember { mutableStateOf<Sorting?>(null) }
    var filtersSheet by remember { mutableStateOf(false) }

    AllPersonalListContent(
        state = state,
        modifier = modifier,
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
                is SeasonItem -> Unit
                is EpisodeItem -> Unit
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
            subtitle = state.list?.privacy?.let {
                stringResource(it.displayRes)
            },
            description = state.list?.description,
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
            onEndOfList = onLoadMoreData,
        )
    }
}

@Composable
private fun TitleBar(
    enabled: Boolean,
    reorderEnabled: Boolean,
    title: String,
    subtitle: String?,
    filters: GlobalFilter?,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onFiltersClick: () -> Unit = {},
    onReorderClick: () -> Unit = {},
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
                subtitle = subtitle,
            )
        }

        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = spacedBy(20.dp),
            modifier = Modifier.padding(start = 16.dp),
        ) {
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

            Box {
                var showMenu by remember { mutableStateOf(false) }

                Icon(
                    painter = painterResource(R.drawable.ic_more_vertical),
                    contentDescription = null,
                    tint = TraktTheme.colors.textPrimary,
                    modifier = Modifier
                        .size(18.dp)
                        .onClick {
                            showMenu = true
                        },
                )

                DropdownMenu(
                    expanded = showMenu,
                    containerColor = TraktTheme.colors.dialogContainer,
                    shape = RoundedCornerShape(16.dp),
                    onDismissRequest = {
                        showMenu = false
                    },
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(R.string.page_title_edit_list),
                                style = TraktTheme.typography.buttonTertiary,
                                color = TraktTheme.colors.textPrimary,
                            )
                        },
                        onClick = {
                            onMoreClick()
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_edit),
                                contentDescription = null,
                                tint = TraktTheme.colors.textPrimary,
                                modifier = Modifier.size(22.dp),
                            )
                        },
                    )

                    DropdownMenuItem(
                        enabled = reorderEnabled,
                        text = {
                            Text(
                                text = stringResource(R.string.button_text_reorder),
                                style = TraktTheme.typography.buttonTertiary,
                                color = TraktTheme.colors.textPrimary,
                            )
                        },
                        onClick = {
                            onReorderClick()
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_reorder),
                                contentDescription = null,
                                tint = TraktTheme.colors.textPrimary,
                                modifier = Modifier.size(24.dp),
                            )
                        },
                    )

                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(R.string.button_text_share),
                                style = TraktTheme.typography.buttonTertiary,
                                color = TraktTheme.colors.textPrimary,
                            )
                        },
                        onClick = {
                            onShareClick()
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_share),
                                contentDescription = null,
                                tint = TraktTheme.colors.textPrimary,
                                modifier = Modifier.size(22.dp),
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ContentFilters(
    hasSubtitle: Boolean,
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
                top = if (hasSubtitle) 8.dp else 0.dp,
                bottom = 19.dp,
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
    description: String?,
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
                filters = listFilter,
                onBackClick = onBackClick,
                onShareClick = onShareClick,
                onMoreClick = onMoreClick,
                onReorderClick = onReorderClick,
                onFiltersClick = onFiltersClick,
            )
        }

        if (!description.isNullOrBlank()) {
            item {
                var collapsed by remember { mutableStateOf(true) }
                Text(
                    text = description,
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

        if (listFilter != null) {
            item {
                ContentFilters(
                    hasSubtitle = !subtitle.isNullOrEmpty(),
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
                        item = item,
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
                        item = item,
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
                ContentEmpty(filter = listFilter?.mode)
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

private fun shareList(
    user: User?,
    list: String,
    context: Context,
) {
    if (user == null) {
        Timber.e("Unable to share: user is null")
        return
    }

    val intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(
            Intent.EXTRA_TEXT,
            Config.webListUrl(
                userId = user.ids.slug.value,
                listId = list,
            ),
        )
        type = "text/plain"
    }

    context.startActivity(Intent.createChooser(intent, user.displayName))
}
