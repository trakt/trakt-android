@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package tv.trakt.trakt.core.home.sections.activity.features.all.social

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.capitalize
import tv.trakt.trakt.common.helpers.extensions.longDateFormat
import tv.trakt.trakt.common.helpers.extensions.nowLocalDay
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.relativePastDateString
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.core.filters.GlobalFiltersSheet
import tv.trakt.trakt.core.filters.navigation.GlobalFiltersOptions
import tv.trakt.trakt.core.home.sections.activity.features.all.AllActivityState
import tv.trakt.trakt.core.home.sections.activity.features.all.views.AllActivityEpisodeItem
import tv.trakt.trakt.core.home.sections.activity.features.all.views.AllActivityMovieItem
import tv.trakt.trakt.core.home.sections.activity.features.all.views.UserFilterChip
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem.EpisodeItem
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem.MovieItem
import tv.trakt.trakt.core.home.sections.upnext.features.all.AllHomeUpNextContent
import tv.trakt.trakt.core.home.sections.upnext.features.all.AllHomeUpNextState
import tv.trakt.trakt.helpers.SimpleScrollConnection
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.MediaFilterIcon
import tv.trakt.trakt.ui.components.ScrollableBackdropImage
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.chips.FilterChip
import tv.trakt.trakt.ui.components.chips.FilterChipGroup
import tv.trakt.trakt.ui.components.mediacards.skeletons.PanelMediaSkeletonCard
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.LocalDate
import java.time.ZoneId

@Composable
internal fun AllActivitySocialScreen(
    modifier: Modifier = Modifier,
    viewModel: AllActivitySocialViewModel = koinViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToEpisode: (showId: TraktId, episode: Episode) -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
    onNavigateToUser: (User) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(
        state.navigateShow,
        state.navigateMovie,
        state.navigateEpisode,
    ) {
        state.navigateShow?.let {
            onNavigateToShow(it)
            viewModel.clearNavigation()
        }
        state.navigateEpisode?.let {
            onNavigateToEpisode(it.first, it.second)
            viewModel.clearNavigation()
        }
        state.navigateMovie?.let {
            onNavigateToMovie(it)
            viewModel.clearNavigation()
        }
    }

    var filtersSheet by remember { mutableStateOf(false) }

    AllActivitySocialContent(
        state = state,
        modifier = modifier,
        onUserFilterClick = viewModel::setUserFilter,
        onModeClick = { mode ->
            state.itemsFilter?.let {
                viewModel.setFilter(it.copy(mode = mode))
            }
        },
        onFiltersClick = { filtersSheet = true },
        onBackClick = onNavigateBack,
        onLoadMore = {
            // No pagination at the moment.
        },
        onShowClick = { episodeItem ->
            viewModel.navigateToShow(episodeItem.show)
        },
        onEpisodeClick = { episodeItem ->
            viewModel.navigateToEpisode(
                episodeItem.show,
                episodeItem.episode,
            )
        },
        onMovieClick = { movie ->
            viewModel.navigateToMovie(movie)
        },
        onUserClick = onNavigateToUser,
    )

    GlobalFiltersSheet(
        active = filtersSheet,
        options = GlobalFiltersOptions(
            global = false,
            initial = state.itemsFilter,
        ),
        onUpdate = viewModel::setFilter,
        onDismiss = { filtersSheet = false },
    )
}

@Composable
private fun TitleBar(
    isFilterActive: Boolean,
    onFiltersClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .height(TraktTheme.size.titleBarHeight)
            .graphicsLayer {
                translationX = -2.dp.toPx()
            },
    ) {
        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = spacedBy(12.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_back_arrow),
                tint = TraktTheme.colors.textPrimary,
                contentDescription = null,
            )
            Text(
                text = stringResource(R.string.list_title_social_activity),
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.heading5,
            )
        }
        MediaFilterIcon(
            active = isFilterActive,
            onClick = onFiltersClick,
        )
    }
}

@Composable
internal fun AllActivitySocialContent(
    state: AllActivityState,
    modifier: Modifier = Modifier,
    onUserFilterClick: (User) -> Unit = {},
    onModeClick: (MediaMode) -> Unit = {},
    onFiltersClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onLoadMore: () -> Unit = {},
    onShowClick: (EpisodeItem) -> Unit = {},
    onEpisodeClick: (EpisodeItem) -> Unit = {},
    onMovieClick: (Movie) -> Unit = {},
    onUserClick: (User) -> Unit = {},
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
            listItems = remember(state.items) {
                (state.items ?: emptyMap()).toImmutableMap()
            },
            listUsersFilters = state.usersFilter,
            listItemsFilters = state.itemsFilter,
            contentPadding = contentPadding,
            loading = state.loading.isLoading,
            onEndOfList = onLoadMore,
            onItemsFilterClick = onModeClick,
            onUserFilterClick = onUserFilterClick,
            onFiltersClick = onFiltersClick,
            onBackClick = onBackClick,
            onShowClick = onShowClick,
            onEpisodeClick = onEpisodeClick,
            onMovieClick = onMovieClick,
            onUserClick = onUserClick,
        )
    }
}

@Composable
private fun ContentList(
    modifier: Modifier = Modifier,
    listItems: ImmutableMap<LocalDate, ImmutableList<HomeActivityItem>>,
    listUsersFilters: AllActivityState.UsersFilter,
    listItemsFilters: GlobalFilter?,
    listState: LazyListState,
    contentPadding: PaddingValues,
    loading: Boolean,
    onUserFilterClick: (User) -> Unit,
    onItemsFilterClick: (MediaMode) -> Unit,
    onFiltersClick: () -> Unit,
    onEndOfList: () -> Unit,
    onBackClick: () -> Unit,
    onShowClick: (EpisodeItem) -> Unit,
    onEpisodeClick: (EpisodeItem) -> Unit,
    onMovieClick: (Movie) -> Unit,
    onUserClick: (User) -> Unit,
) {
    val isScrolledToBottom by remember(listItems.size) {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleIndex >= layoutInfo.totalItemsCount - 5
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
                isFilterActive = listItemsFilters?.isActive == true,
                onFiltersClick = onFiltersClick,
                modifier = Modifier
                    .padding(
                        start = TraktTheme.spacing.mainPageHorizontalSpace,
                        end = TraktTheme.spacing.mainPageHorizontalSpace,
                        bottom = 2.dp,
                    )
                    .onClick {
                        onBackClick()
                    },
            )
        }

        if (listItemsFilters != null) {
            item {
                ContentFilters(
                    itemsFilter = listItemsFilters,
                    usersFilter = listUsersFilters,
                    onUserFilterClick = onUserFilterClick,
                    onItemFilterClick = onItemsFilterClick,
                )
            }
        }

        val today = nowLocalDay()
        listItems.keys.forEachIndexed { index, date ->
            val itemsForDate = listItems[date] ?: EmptyImmutableList

            item(key = "header-$date") {
                TraktHeader(
                    title = if (today == date || today.minusDays(1) == date) {
                        date.atStartOfDay(ZoneId.systemDefault()).relativePastDateString()
                    } else {
                        date.format(longDateFormat()).capitalize()
                    },
                    modifier = Modifier
                        .padding(
                            top = when (index) {
                                0 -> 0.dp
                                else -> 16.dp
                            },
                            bottom = 12.dp,
                            start = TraktTheme.spacing.mainPageHorizontalSpace,
                            end = TraktTheme.spacing.mainPageHorizontalSpace,
                        ),
                )
            }

            items(
                items = itemsForDate,
                key = { it.id },
            ) { item ->
                when (item) {
                    is MovieItem -> {
                        AllActivityMovieItem(
                            item = item,
                            enabled = !loading,
                            onClick = {
                                onMovieClick(item.movie)
                            },
                            onUserClick = onUserClick,
                            moreButton = false,
                            modifier = Modifier
                                .padding(
                                    start = TraktTheme.spacing.mainPageHorizontalSpace,
                                    end = TraktTheme.spacing.mainPageHorizontalSpace,
                                    bottom = TraktTheme.spacing.mainListVerticalSpace,
                                )
                                .animateItem(
                                    fadeInSpec = null,
                                    fadeOutSpec = null,
                                ),
                        )
                    }

                    is EpisodeItem -> {
                        AllActivityEpisodeItem(
                            item = item,
                            enabled = !loading,
                            onClick = { onEpisodeClick(item) },
                            onShowClick = { onShowClick(item) },
                            onUserClick = onUserClick,
                            moreButton = false,
                            modifier = Modifier
                                .padding(
                                    start = TraktTheme.spacing.mainPageHorizontalSpace,
                                    end = TraktTheme.spacing.mainPageHorizontalSpace,
                                    bottom = TraktTheme.spacing.mainListVerticalSpace,
                                )
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
                        .padding(
                            start = TraktTheme.spacing.mainPageHorizontalSpace,
                            end = TraktTheme.spacing.mainPageHorizontalSpace,
                        )
                        .padding(bottom = TraktTheme.spacing.mainListVerticalSpace)
                        .animateItem(
                            fadeInSpec = null,
                            fadeOutSpec = null,
                        ),
                )
            }
        }

        if (!loading && listItems.isEmpty()) {
            item {
                ContentEmptyView(
                    modifier = Modifier
                        .padding(
                            start = TraktTheme.spacing.mainPageHorizontalSpace,
                            end = TraktTheme.spacing.mainPageHorizontalSpace,
                        )
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
    itemsFilter: GlobalFilter,
    usersFilter: AllActivityState.UsersFilter,
    onItemFilterClick: (MediaMode) -> Unit,
    onUserFilterClick: (User) -> Unit,
) {
    FilterChipGroup(
        paddingHorizontal = PaddingValues(
            start = TraktTheme.spacing.mainPageHorizontalSpace,
            end = TraktTheme.spacing.mainPageHorizontalSpace,
        ),
        paddingVertical = PaddingValues(bottom = 20.dp),
    ) {
        for (filter in MediaMode.entries) {
            FilterChip(
                selected = itemsFilter.mode == filter,
                text = stringResource(filter.displayRes),
                unselectedTextVisible = false,
                height = 32.dp,
                leadingContent = {
                    Icon(
                        painter = painterResource(filter.offIcon),
                        contentDescription = null,
                        tint = when {
                            itemsFilter.mode == filter -> TraktTheme.colors.textPrimaryOnAccent
                            else -> TraktTheme.colors.textPrimary
                        },
                        modifier = Modifier
                            .size(16.dp),
                    )
                },
                onClick = {
                    onItemFilterClick(filter)
                },
            )
        }

        Box(
            modifier = Modifier
                .padding(horizontal = 6.dp)
                .size(width = 1.25.dp, height = 12.dp)
                .background(TraktTheme.colors.chipContainer),
        )

        for (user in usersFilter.users) {
            UserFilterChip(
                user = user,
                height = 32.dp,
                selected = usersFilter.selectedUser?.ids?.trakt == user.ids.trakt,
                onClick = { onUserFilterClick(user) },
            )
        }
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
