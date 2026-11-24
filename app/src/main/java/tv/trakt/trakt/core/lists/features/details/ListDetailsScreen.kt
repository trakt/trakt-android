@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package tv.trakt.trakt.core.lists.features.details

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType.Companion.Confirm
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import tv.trakt.trakt.LocalBottomBarVisibility
import tv.trakt.trakt.LocalSnackbarState
import tv.trakt.trakt.common.core.user.UserCollectionState
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.rememberThousandsFormat
import tv.trakt.trakt.common.helpers.extensions.toLocalDay
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.lists.CustomList
import tv.trakt.trakt.common.model.lists.SmartList
import tv.trakt.trakt.common.model.sorting.SortType.Added
import tv.trakt.trakt.common.model.sorting.SortType.Released
import tv.trakt.trakt.common.model.sorting.SortType.Title
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.core.filters.GlobalFiltersSheet
import tv.trakt.trakt.core.filters.navigation.GlobalFiltersOptions
import tv.trakt.trakt.core.lists.features.addfrom.components.ADD_FROM_LISTS_EMPTY_HEIGHT_FRACTION
import tv.trakt.trakt.core.lists.features.addfrom.components.AddFromListsEmpty
import tv.trakt.trakt.core.lists.features.convert.upgrade.SmartListUpgradeView
import tv.trakt.trakt.core.lists.features.details.ListDetailsState.LikedInfo
import tv.trakt.trakt.core.lists.features.details.info.ListInfoSheet
import tv.trakt.trakt.core.lists.features.details.ui.ListDetailsEpisodeView
import tv.trakt.trakt.core.lists.features.details.ui.ListDetailsMovieView
import tv.trakt.trakt.core.lists.features.details.ui.ListDetailsSeasonView
import tv.trakt.trakt.core.lists.features.details.ui.ListDetailsShowView
import tv.trakt.trakt.core.lists.model.CustomListItem
import tv.trakt.trakt.core.lists.model.CustomListItem.EpisodeItem
import tv.trakt.trakt.core.lists.model.CustomListItem.MovieItem
import tv.trakt.trakt.core.lists.model.CustomListItem.SeasonItem
import tv.trakt.trakt.core.lists.model.CustomListItem.ShowItem
import tv.trakt.trakt.core.lists.sections.personal.features.context.item.ListItemContextSheet
import tv.trakt.trakt.core.movies.ui.context.sheet.MovieContextSheet
import tv.trakt.trakt.core.shows.ui.context.sheet.ShowContextSheet
import tv.trakt.trakt.helpers.SimpleScrollConnection
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.MediaFilterIcon
import tv.trakt.trakt.ui.components.MediaModeFilters
import tv.trakt.trakt.ui.components.ScrollableBackdropImage
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.confirmation.RemoveConfirmationSheet
import tv.trakt.trakt.ui.components.lists.ListAuthorSubtitle
import tv.trakt.trakt.ui.components.mediacards.skeletons.PanelMediaSkeletonCard
import tv.trakt.trakt.ui.components.sorting.SortingSplitButton
import tv.trakt.trakt.ui.components.sorting.sheets.SortSelectionSheet
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun ListDetailsScreen(
    modifier: Modifier = Modifier,
    viewModel: ListDetailsViewModel,
    onShowClick: (TraktId) -> Unit,
    onMovieClick: (TraktId) -> Unit,
    onListClick: (CustomList) -> Unit,
    onEpisodeClick: (TraktId, Episode) -> Unit,
    onPersonClick: (Person) -> Unit,
    onAddFromListsClick: (CustomList) -> Unit,
    onConvertToSmartListClick: (CustomList) -> Unit,
    onCreateSmartListClick: () -> Unit,
    onSmartListRedirect: (SmartList) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val snack = LocalSnackbarState.current
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(state.info, state.error) {
        if (state.info != null) {
            haptic.performHapticFeedback(Confirm)
            snack.showSnackbar(
                message = state.info?.get(context) ?: "",
                duration = SnackbarDuration.Short,
            )
            viewModel.clearInfoError()
        }

        if (state.error != null) {
            snack.showSnackbar(
                message = state.error?.localizedMessage ?: "",
                duration = SnackbarDuration.Long,
            )
            viewModel.clearInfoError()
        }
    }

    LaunchedEffect(
        state.navigateMovie,
        state.navigateShow,
        state.navigateEpisode,
    ) {
        state.navigateShow?.let {
            onShowClick(it)
            viewModel.clearNavigation()
        }
        state.navigateMovie?.let {
            onMovieClick(it)
            viewModel.clearNavigation()
        }
        state.navigateEpisode?.let { (showId, episode) ->
            onEpisodeClick(showId, episode)
            viewModel.clearNavigation()
        }
    }

    var showContextSheet by remember { mutableStateOf<ShowItem?>(null) }
    var movieContextSheet by remember { mutableStateOf<MovieItem?>(null) }
    var itemContextSheet by remember { mutableStateOf<CustomListItem?>(null) }
    var sortSheet by remember { mutableStateOf<Sorting?>(null) }
    var confirmRemoveLikeSheet by remember { mutableStateOf(false) }
    var filtersSheet by remember { mutableStateOf(false) }
    var infoSheet by remember { mutableStateOf<CustomList?>(null) }

    val upgradeList = state.list?.list?.takeIf {
        it.user.ids.trakt == state.user?.ids?.trakt &&
            (it.staleOrigin != null || it.convertedTo != null)
    }

    ListDetailsContent(
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
            if (it.id == state.list?.mediaId) {
                onNavigateBack()
                return@ListDetailsContent
            }

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
                is EpisodeItem, is SeasonItem -> itemContextSheet = it
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
        onInfoClick = {
            infoSheet = state.list?.list
        },
        onSortTypeClick = {
            if (!state.loading.isLoading) {
                sortSheet = state.sorting
            }
        },
        onSortOrderClick = {
            if (!state.loading.isLoading) {
                val sorting = state.sorting
                viewModel.setSorting(
                    sorting.copy(
                        order = sorting.order.toggle(),
                    ),
                )
            }
        },
        onLikeClick = {
            state.liked
                ?.takeIf { !it.loading }
                ?.let {
                    when {
                        it.liked -> confirmRemoveLikeSheet = true
                        else -> viewModel.setLiked(true)
                    }
                }
        },
        onAddFromListsClick = {
            state.list?.list?.let(onAddFromListsClick)
        },
        onBackClick = onNavigateBack,
    )

    ShowContextSheet(
        onListClick = onListClick,
        show = showContextSheet?.show,
        onDismiss = {
            showContextSheet = null
        },
    )

    MovieContextSheet(
        onListClick = onListClick,
        movie = movieContextSheet?.movie,
        onDismiss = {
            movieContextSheet = null
        },
    )

    ListItemContextSheet(
        onListClick = onListClick,
        item = itemContextSheet,
        list = state.list?.list,
        removable = false,
        onDismiss = {
            itemContextSheet = null
        },
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

    RemoveConfirmationSheet(
        active = confirmRemoveLikeSheet,
        onYes = {
            viewModel.setLiked(false)
            confirmRemoveLikeSheet = false
        },
        onNo = { confirmRemoveLikeSheet = false },
        title = stringResource(R.string.list_title_liked_lists),
        message = stringResource(
            R.string.warning_prompt_remove_from_liked_lists,
            state.list?.list?.name ?: "",
        ),
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
internal fun ListDetailsContent(
    state: ListDetailsState,
    modifier: Modifier = Modifier,
    upgradeContent: (@Composable () -> Unit)? = null,
    onClick: (CustomListItem) -> Unit = {},
    onLongClick: (CustomListItem) -> Unit = {},
    onModeClick: (MediaMode) -> Unit = {},
    onFiltersClick: () -> Unit = {},
    onInfoClick: () -> Unit = {},
    onSortTypeClick: () -> Unit = {},
    onSortOrderClick: () -> Unit = {},
    onLikeClick: () -> Unit = {},
    onAddFromListsClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onLoadMoreData: () -> Unit = {},
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

        val userId = state.user?.ids?.trakt
        val listUserId = state.list?.list?.user?.ids?.trakt

        ContentList(
            title = state.list?.list?.name ?: "",
            user = state.list?.list?.user,
            itemCount = state.list?.list?.itemCount,
            infoEnabled = state.list != null,
            listItems = (state.items ?: emptyList()).toImmutableList(),
            listState = listState,
            listFilter = state.filter,
            listSorting = state.sorting,
            listLiked = when {
                userId == listUserId -> null
                else -> state.liked
            },
            listLikes = state.list?.list?.likes,
            collectionState = state.collection,
            contentPadding = contentPadding,
            loading = state.loading.isLoading,
            loadingMore = state.loadingMore.isLoading,
            onEndOfList = onLoadMoreData,
            onClick = onClick,
            onLongClick = onLongClick,
            onModeClick = onModeClick,
            onFiltersClick = onFiltersClick,
            onInfoClick = onInfoClick,
            onSortTypeClick = onSortTypeClick,
            onSortOrderClick = onSortOrderClick,
            onLikeClick = onLikeClick,
            onAddFromListsClick = onAddFromListsClick.takeIf { state.canAddFromLists },
            onBackClick = onBackClick,
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
    title: String,
    user: User?,
    itemCount: Int?,
    liked: LikedInfo?,
    likesCount: Int?,
    filters: GlobalFilter?,
    infoEnabled: Boolean,
    modifier: Modifier = Modifier,
    onLikeClick: () -> Unit,
    onBackClick: () -> Unit,
    onFiltersClick: () -> Unit,
    onInfoClick: () -> Unit,
    onAddFromListsClick: (() -> Unit)?,
) {
    Row(
        verticalAlignment = CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .height(TraktTheme.size.titleBarHeight),
    ) {
        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = spacedBy(12.dp),
            modifier = Modifier
                .weight(1F)
                .graphicsLayer {
                    translationX = -2.dp.toPx()
                },
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_back_arrow),
                tint = TraktTheme.colors.textPrimary,
                contentDescription = null,
                modifier = Modifier
                    .onClick(onClick = onBackClick),
            )

            TraktHeader(
                title = title,
                subtitleTrailing = user?.let {
                    { ListAuthorSubtitle(user = it, itemCount = itemCount) }
                },
                modifier = Modifier
                    .weight(1F, fill = false)
                    .onClick(onClick = onBackClick),
            )
        }

        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = spacedBy(14.dp),
            modifier = Modifier.padding(start = 24.dp),
        ) {
            AnimatedVisibility(
                visible = liked != null,
                enter = fadeIn(tween(150)),
                exit = fadeOut(tween(150)),
            ) {
                Row(
                    verticalAlignment = CenterVertically,
                    horizontalArrangement = spacedBy(4.dp),
                    modifier = Modifier
                        .alpha(if (liked?.loading == true) 0.25F else 1F)
                        .onClick(
                            enabled = liked?.loading != true,
                            onClick = onLikeClick,
                        ),
                ) {
                    Icon(
                        painter = painterResource(
                            when {
                                liked?.liked ?: false -> R.drawable.ic_thumb_up_fill
                                else -> R.drawable.ic_thumb_up_like
                            },
                        ),
                        tint = TraktTheme.colors.textPrimary,
                        contentDescription = null,
                        modifier = Modifier
                            .size(18.dp)
                            .graphicsLayer {
                                translationY = -0.75.dp.toPx()
                            },
                    )
                    likesCount?.let {
                        Text(
                            text = rememberThousandsFormat(it),
                            style = TraktTheme.typography.buttonTertiary.copy(
                                fontSize = 12.sp,
                            ),
                            color = TraktTheme.colors.textPrimary,
                        )
                    }
                }
            }

            onAddFromListsClick?.let {
                Icon(
                    painter = painterResource(R.drawable.ic_plus),
                    tint = TraktTheme.colors.textPrimary,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .size(20.dp)
                        .onClick(onClick = it),
                )
            }

            if (infoEnabled) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(start = 4.dp)
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
                )
            }
        }
    }
}

@Composable
private fun ContentList(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues,
    title: String,
    user: User?,
    itemCount: Int?,
    infoEnabled: Boolean,
    listState: LazyListState,
    listItems: ImmutableList<CustomListItem>,
    listFilter: GlobalFilter?,
    listSorting: Sorting,
    listLiked: LikedInfo?,
    listLikes: Int?,
    collectionState: UserCollectionState,
    loading: Boolean,
    loadingMore: Boolean,
    onClick: (CustomListItem) -> Unit,
    onLongClick: (CustomListItem) -> Unit,
    onModeClick: (MediaMode) -> Unit,
    onFiltersClick: () -> Unit,
    onInfoClick: () -> Unit,
    onSortTypeClick: () -> Unit,
    onSortOrderClick: () -> Unit,
    onLikeClick: () -> Unit,
    onAddFromListsClick: (() -> Unit)?,
    onBackClick: () -> Unit,
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
                title = title,
                user = user,
                itemCount = itemCount,
                liked = listLiked,
                likesCount = listLikes,
                filters = listFilter,
                infoEnabled = infoEnabled,
                onLikeClick = onLikeClick,
                onBackClick = onBackClick,
                onFiltersClick = onFiltersClick,
                onInfoClick = onInfoClick,
                onAddFromListsClick = onAddFromListsClick,
            )
        }

        item {
            ContentFilters(
                filter = listFilter,
                sorting = listSorting,
                onFilterClick = onModeClick,
                onSortTypeClick = onSortTypeClick,
                onSortOrderClick = onSortOrderClick,
            )
        }

        if (listItems.isNotEmpty()) {
            itemsGroup.keys.forEachIndexed { groupIndex, key ->
                key?.let {
                    item(
                        key = "header-$key",
                    ) {
                        TraktHeader(
                            title = key,
                            modifier = Modifier
                                .padding(
                                    top = if (groupIndex == 0) 0.dp else 16.dp,
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
                    key = { item -> item.key },
                ) { item ->
                    when (item) {
                        is ShowItem -> ListDetailsShowView(
                            show = item.show,
                            showUserRating = item.userRating,
                            sorting = listSorting,
                            enabled = !loading,
                            watched = collectionState.isWatched(item.id, item.type, item.show.airedEpisodes),
                            watching = collectionState.isWatching(item.id, item.type, item.show.airedEpisodes),
                            watchlist = collectionState.isWatchlist(item.id, item.type),
                            showIcon = true,
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
                            watched = collectionState.isWatched(item.id, item.type, null),
                            watchlist = collectionState.isWatchlist(item.id, item.type),
                            showIcon = true,
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
                if (itemCount == 0 && onAddFromListsClick != null) {
                    AddFromListsEmpty(
                        onClick = onAddFromListsClick,
                        modifier = Modifier.fillParentMaxHeight(ADD_FROM_LISTS_EMPTY_HEIGHT_FRACTION),
                    )
                } else {
                    ContentEmpty(filter = listFilter?.mode)
                }
            }
        }
    }
}

@Composable
private fun ContentFilters(
    filter: GlobalFilter?,
    sorting: Sorting,
    onFilterClick: (MediaMode) -> Unit,
    onSortTypeClick: () -> Unit,
    onSortOrderClick: () -> Unit,
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
        if (filter != null) {
            MediaModeFilters(
                selected = filter.mode,
                onClick = onFilterClick,
                height = 32.dp,
                unselectedTextVisible = false,
            )
        }

        SortingSplitButton(
            text = stringResource(sorting.type.displayStringRes),
            order = sorting.order,
            height = 32.dp,
            onLeadingClick = onSortTypeClick,
            onTrailingClick = onSortOrderClick,
        )
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

@OptIn(ExperimentalCoilApi::class)
@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
    locale = "en",
)
@Composable
private fun Preview() {
    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.Blue.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            ListDetailsContent(
                state = ListDetailsState(
                    loading = LoadingState.Loading,
                    liked = LikedInfo(
                        liked = false,
                        loading = false,
                    ),
                    list = ListDetailsState.ListDetails(
                        list = PreviewData.customList1,
                        mediaId = TraktId(1),
                    ),
                ),
            )
        }
    }
}
