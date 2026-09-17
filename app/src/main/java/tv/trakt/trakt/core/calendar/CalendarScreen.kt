@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package tv.trakt.trakt.core.calendar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Arrangement.SpaceBetween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.hapticfeedback.HapticFeedbackType.Companion.Confirm
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tv.trakt.trakt.LocalSnackbarState
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.capitalize
import tv.trakt.trakt.common.helpers.extensions.fullDayFormat
import tv.trakt.trakt.common.helpers.extensions.nowLocalDay
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.ui.theme.colors.Purple400
import tv.trakt.trakt.core.calendar.model.CalendarItem
import tv.trakt.trakt.core.calendar.model.CalendarItem.EpisodeItem
import tv.trakt.trakt.core.calendar.model.CalendarItem.MovieItem
import tv.trakt.trakt.core.calendar.ui.CalendarEpisodeItemView
import tv.trakt.trakt.core.calendar.ui.CalendarMovieItemView
import tv.trakt.trakt.core.calendar.ui.controls.CalendarControlsView
import tv.trakt.trakt.core.discover.sections.releases.model.ReleaseType
import tv.trakt.trakt.core.filters.GlobalFiltersSheet
import tv.trakt.trakt.core.filters.navigation.GlobalFiltersOptions
import tv.trakt.trakt.helpers.SimpleScrollConnection
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.MediaFilterIcon
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.chips.FilterChip
import tv.trakt.trakt.ui.components.chips.FilterChipGroup
import tv.trakt.trakt.ui.components.confirmation.RemoveConfirmationSheet
import tv.trakt.trakt.ui.components.dateselection.DateSelectionSheet
import tv.trakt.trakt.ui.components.mediacards.skeletons.EpisodeSkeletonCard
import tv.trakt.trakt.ui.snackbar.ShortSnackDuration
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.DayOfWeek.MONDAY
import java.time.LocalDate
import kotlin.time.Duration.Companion.milliseconds

private val MinAlpha = 0.25F
private val LoadMoreThreshold = 4
private val FiltersRowHeight = 40.dp

@Composable
internal fun CalendarScreen(
    viewModel: CalendarViewModel,
    onNavigateBack: () -> Unit,
    onEpisodeClick: (showId: TraktId, episode: Episode) -> Unit,
    onShowClick: (TraktId) -> Unit,
    onMovieClick: (TraktId) -> Unit,
) {
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val snackbar = LocalSnackbarState.current

    val state by viewModel.state.collectAsStateWithLifecycle()

    var dateSelectionSheet by remember { mutableStateOf<CalendarItem?>(null) }
    var confirmRemoveSheet by remember { mutableStateOf<CalendarItem?>(null) }
    var filtersSheet by remember { mutableStateOf(false) }

    LaunchedEffect(
        state.navigateShow,
        state.navigateMovie,
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
        state.navigateEpisode?.let {
            onEpisodeClick(it.first, it.second)
            viewModel.clearNavigation()
        }
    }

    LaunchedEffect(state.info) {
        if (state.info == null) return@LaunchedEffect
        haptic.performHapticFeedback(Confirm)
        with(scope) {
            val job = launch {
                state.info?.get(context)?.let {
                    snackbar.showSnackbar(it)
                }
            }
            delay(ShortSnackDuration)
            job.cancel()
        }
        viewModel.clearInfo()
    }

    CalendarScreen(
        scope = scope,
        state = state,
        onTodayClick = viewModel::loadTodayData,
        onLoadWeek = viewModel::loadWeek,
        onLoadMore = viewModel::loadMoreData,
        onShowClick = { item ->
            if (state.loading.isLoading) return@CalendarScreen
            viewModel.navigateToShow(item.show)
        },
        onMovieClick = { item ->
            if (state.loading.isLoading) return@CalendarScreen
            viewModel.navigateToMovie(item.movie)
        },
        onEpisodeClick = { item ->
            if (state.loading.isLoading) return@CalendarScreen
            viewModel.navigateToEpisode(item.show, item.episode)
        },
        onCheckClick = {
            if (state.loading.isLoading) return@CalendarScreen
            when (it) {
                is EpisodeItem -> viewModel.addToHistory(it.episode)
                is MovieItem -> viewModel.addToHistory(it.movie)
            }
        },
        onCheckLongClick = {
            if (state.loading.isLoading) return@CalendarScreen
            dateSelectionSheet = it
        },
        onRemoveClick = { item ->
            if (state.loading.isLoading) return@CalendarScreen
            confirmRemoveSheet = item
        },
        onFiltersClick = {
            filtersSheet = true
        },
        onTypeClick = viewModel::setType,
        onBackClick = onNavigateBack,
    )

    // Sheets

    DateSelectionSheet(
        active = dateSelectionSheet != null,
        title = dateSelectionSheet?.title.orEmpty(),
        subtitle = when (dateSelectionSheet) {
            is EpisodeItem -> (dateSelectionSheet as EpisodeItem).episode.seasonEpisodeString()
            else -> null
        },
        onResult = { date ->
            if (dateSelectionSheet == null) return@DateSelectionSheet
            (dateSelectionSheet as? EpisodeItem)?.let {
                viewModel.addToHistory(
                    episode = it.episode,
                    customDate = date,
                )
            }
            (dateSelectionSheet as? MovieItem)?.let {
                viewModel.addToHistory(
                    movie = it.movie,
                    customDate = date,
                )
            }
        },
        onDismiss = {
            dateSelectionSheet = null
        },
    )

    RemoveConfirmationSheet(
        active = confirmRemoveSheet != null,
        onYes = {
            (confirmRemoveSheet as? EpisodeItem)?.let {
                viewModel.removeFromWatched(it.episode)
            }
            (confirmRemoveSheet as? MovieItem)?.let {
                viewModel.removeFromWatched(it.movie)
            }
            confirmRemoveSheet = null
        },
        onNo = { confirmRemoveSheet = null },
        title = stringResource(R.string.button_text_remove_from_history),
        message = stringResource(
            R.string.warning_prompt_remove_from_watched,
            confirmRemoveSheet?.title.orEmpty(),
        ),
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
private fun CalendarScreen(
    scope: CoroutineScope,
    state: CalendarState,
    modifier: Modifier = Modifier,
    onTodayClick: () -> Unit = {},
    onLoadWeek: (LocalDate) -> Unit = {},
    onLoadMore: () -> Unit = {},
    onShowClick: (EpisodeItem) -> Unit = {},
    onMovieClick: (MovieItem) -> Unit = {},
    onEpisodeClick: (EpisodeItem) -> Unit = {},
    onCheckClick: (CalendarItem) -> Unit = {},
    onCheckLongClick: (CalendarItem) -> Unit = {},
    onRemoveClick: (CalendarItem) -> Unit = {},
    onFiltersClick: () -> Unit = {},
    onTypeClick: (ReleaseType) -> Unit = {},
    onBackClick: () -> Unit = {},
) {
    val scrollOffset = with(LocalDensity.current) { 70.dp.toPx().toInt() }

    val gridState = rememberLazyGridState()
    val scrollConnection = rememberSaveable(saver = SimpleScrollConnection.Saver) {
        SimpleScrollConnection()
    }

    val itemsKeys by remember(state.items) {
        derivedStateOf {
            state.items?.keys?.toList() ?: EmptyImmutableList
        }
    }

    val atTop by remember {
        derivedStateOf {
            gridState.firstVisibleItemIndex == 0 && gridState.firstVisibleItemScrollOffset == 0
        }
    }

    val contentPadding = PaddingValues(
        start = TraktTheme.spacing.mainPageHorizontalSpace,
        end = TraktTheme.spacing.mainPageHorizontalSpace,
        top = WindowInsets.statusBars.asPaddingValues()
            .calculateTopPadding()
            .plus(202.dp)
            .plus(FiltersRowHeight),
        bottom = WindowInsets.navigationBars.asPaddingValues()
            .calculateBottomPadding()
            .plus(TraktTheme.size.navigationBarHeight)
            .plus(TraktTheme.spacing.mainPageBottomSpace),
    )

    val focusedDate by remember(itemsKeys) {
        derivedStateOf {
            val firstVisibleIndex = gridState.firstVisibleItemIndex
            if (firstVisibleIndex < 0 || state.items.isNullOrEmpty()) {
                return@derivedStateOf null
            }

            var accumulatedCount = 0
            for (date in itemsKeys) {
                val itemsForDate = state.items[date] ?: EmptyImmutableList
                val itemCountForDate = when {
                    itemsForDate.isNotEmpty() -> itemsForDate.size + 1
                    else -> 2
                }

                if (firstVisibleIndex < accumulatedCount + itemCountForDate) {
                    return@derivedStateOf date
                }

                accumulatedCount += itemCountForDate
            }

            null
        }
    }

    var scrolledAnchorEpochDay by rememberSaveable { mutableLongStateOf(Long.MIN_VALUE) }
    LaunchedEffect(state.selectedStartDay, state.items != null) {
        if (state.items.isNullOrEmpty()) return@LaunchedEffect

        val anchorEpochDay = state.selectedStartDay.toEpochDay()
        if (scrolledAnchorEpochDay == anchorEpochDay) return@LaunchedEffect
        scrolledAnchorEpochDay = anchorEpochDay

        val today = nowLocalDay()
        val selectedStartDay = state.selectedStartDay
        val selectedWeek = selectedStartDay..selectedStartDay.plusDays(6)

        if (today in selectedWeek) {
            scrollToDay(
                scope = scope,
                state = state,
                date = today,
                scrollOffset = scrollOffset,
                gridState = gridState,
            )
        } else {
            gridState.scrollToItem(0)
        }
    }

    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = gridState.layoutInfo
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index
                ?: return@derivedStateOf false
            lastVisibleIndex >= layoutInfo.totalItemsCount - 1 - LoadMoreThreshold
        }
    }
    LaunchedEffect(shouldLoadMore, state.loading, state.loadingMore) {
        val canLoadMore = shouldLoadMore &&
            state.loading.isDone &&
            !state.loadingMore.isLoading &&
            !state.items.isNullOrEmpty()
        if (canLoadMore) {
            onLoadMore()
        }
    }

    var lastTapFocusedDay by remember { mutableStateOf<LocalDate?>(null) }
    var scrollingUp by remember { mutableStateOf(false) }
    val directionThreshold = with(LocalDensity.current) { 32.dp.toPx() }
    LaunchedEffect(scrollConnection) {
        var lastOffset = scrollConnection.resultOffset
        var accumulated = 0F
        snapshotFlow { scrollConnection.resultOffset }
            .collect { offset ->
                if (lastTapFocusedDay != null && offset != 0F) {
                    lastTapFocusedDay = null
                }

                val delta = offset - lastOffset
                lastOffset = offset

                // Reset accumulation on direction change so travel counts from the turn.
                accumulated = when {
                    delta > 0F && accumulated < 0F -> delta
                    delta < 0F && accumulated > 0F -> delta
                    else -> accumulated + delta
                }
                when {
                    accumulated > directionThreshold -> scrollingUp = true
                    accumulated < -directionThreshold -> scrollingUp = false
                }
            }
    }

    val stripStartDate = when {
        state.loading.isLoading -> state.selectedStartDay
        else -> (lastTapFocusedDay ?: focusedDate)?.with(MONDAY) ?: state.selectedStartDay
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary)
            .nestedScroll(scrollConnection),
    ) {
        CalendarContent(
            state = state,
            gridState = gridState,
            contentPadding = contentPadding,
            onEpisodeClick = onEpisodeClick,
            onShowClick = onShowClick,
            onMovieClick = onMovieClick,
            onCheckClick = onCheckClick,
            onCheckLongClick = onCheckLongClick,
            onRemoveClick = onRemoveClick,
        )

        // Mask for the top content under the calendar controls.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(132.dp + FiltersRowHeight)
                .background(TraktTheme.colors.backgroundPrimary),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = WindowInsets.statusBars.asPaddingValues()
                        .calculateTopPadding(),
                    start = TraktTheme.spacing.mainPageHorizontalSpace,
                    end = TraktTheme.spacing.mainPageHorizontalSpace,
                ),
        ) {
            Row(
                verticalAlignment = CenterVertically,
                horizontalArrangement = spacedBy(12.dp),
                modifier = Modifier
                    .padding(start = 2.dp, bottom = 12.dp)
                    .onClick(onClick = onBackClick),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_back_arrow),
                    tint = TraktTheme.colors.textPrimary,
                    contentDescription = null,
                )
                TraktHeader(
                    title = stringResource(R.string.page_title_calendar),
                    subtitle = state.filter?.mode?.let {
                        stringResource(it.displayRes)
                    } ?: stringResource(MediaMode.Media.displayRes),
                )
            }

            AnimatedVisibility(visible = atTop || scrollingUp) {
                Row(
                    verticalAlignment = CenterVertically,
                    horizontalArrangement = SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                ) {
                    FilterChipGroup(
                        paddingVertical = PaddingValues.Zero,
                    ) {
                        for (type in ReleaseType.entries) {
                            FilterChip(
                                selected = state.type == type,
                                text = stringResource(type.textRes),
                                leadingContent = {
                                    Icon(
                                        painter = painterResource(type.iconRes),
                                        contentDescription = null,
                                        tint = TraktTheme.colors.textPrimaryOnAccent,
                                        modifier = Modifier.size(type.iconSize),
                                    )
                                },
                                onClick = { onTypeClick(type) },
                            )
                        }
                    }
                    MediaFilterIcon(
                        active = state.filter?.isActive == true,
                        enabled = state.loading.isDone,
                        onClick = onFiltersClick,
                        modifier = Modifier
                            .padding(bottom = 1.dp),
                    )
                }
            }

            CalendarControlsView(
                enabled = !state.loading.isLoading,
                startDate = stripStartDate,
                focusedDate = focusedDate,
                lastTapFocusedDate = lastTapFocusedDay,
                availableItems = state.items,
                availableDates = remember(state.items) {
                    state.items?.keys
                        ?.filter { state.items[it]?.isNotEmpty() == true }
                        ?.toImmutableSet()
                },
                onDayClick = { date ->
                    scrollToDay(
                        scope = scope,
                        state = state,
                        date = date,
                        scrollOffset = scrollOffset,
                        gridState = gridState,
                    )
                    lastTapFocusedDay = date
                },
                onTodayClick = {
                    val today = nowLocalDay()
                    if (state.items?.containsKey(today) == true) {
                        scrollToDay(
                            scope = scope,
                            state = state,
                            date = today,
                            scrollOffset = scrollOffset,
                            gridState = gridState,
                        )
                        lastTapFocusedDay = today
                    } else {
                        lastTapFocusedDay = null
                        onTodayClick()
                    }
                },
                onNextWeekClick = {
                    val target = stripStartDate.plusWeeks(1)
                    if (state.items?.containsKey(target) == true) {
                        scrollToDay(
                            scope = scope,
                            state = state,
                            date = target,
                            scrollOffset = scrollOffset,
                            gridState = gridState,
                        )
                        lastTapFocusedDay = target
                    } else {
                        // Stale tapped day would win over the fresh week once it loads.
                        lastTapFocusedDay = null
                        onLoadWeek(target)
                    }
                },
                onPreviousWeekClick = {
                    val target = stripStartDate.minusWeeks(1)
                    if (state.items?.containsKey(target) == true) {
                        scrollToDay(
                            scope = scope,
                            state = state,
                            date = target,
                            scrollOffset = scrollOffset,
                            gridState = gridState,
                        )
                        lastTapFocusedDay = target
                    } else {
                        lastTapFocusedDay = null
                        onLoadWeek(target)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .onClick(onClick = {}),
            )
        }
    }
}

@Composable
private fun CalendarContent(
    modifier: Modifier = Modifier,
    state: CalendarState,
    gridState: LazyGridState,
    contentPadding: PaddingValues,
    onEpisodeClick: (EpisodeItem) -> Unit,
    onShowClick: (EpisodeItem) -> Unit,
    onMovieClick: (MovieItem) -> Unit,
    onCheckClick: (CalendarItem) -> Unit,
    onCheckLongClick: (CalendarItem) -> Unit,
    onRemoveClick: (CalendarItem) -> Unit,
) {
    var animateIn by rememberSaveable { mutableStateOf(false) }

    if (state.error != null) {
        Text(
            text = "${
                stringResource(
                    R.string.error_text_unexpected_error_short,
                )
            }\n\n${state.error}",
            color = TraktTheme.colors.textSecondary,
            style = TraktTheme.typography.meta,
            maxLines = 10,
            modifier = Modifier.padding(contentPadding),
        )
    } else if (state.items.isNullOrEmpty() && state.loading.isLoading) {
        val today = remember { nowLocalDay() }
        ContentLoadingGrid(
            visible = state.loading.isLoading,
            isFirstWeekDay = state.selectedStartDay == today,
            contentPadding = contentPadding,
        )
    } else if (!state.items.isNullOrEmpty()) {
        LaunchedEffect(Unit) {
            delay(50.milliseconds)
            animateIn = true
        }

        val animateInAlpha by animateFloatAsState(
            targetValue = 1F,
            animationSpec = tween(durationMillis = 250),
            label = "initialAlpha",
        )

        ContentItemsGrid(
            items = state.items,
            itemsLoading = state.itemsLoading,
            loadingMore = state.loadingMore.isLoading,
            gridState = gridState,
            contentPadding = contentPadding,
            onShowClick = onShowClick,
            onMovieClick = onMovieClick,
            onEpisodeClick = onEpisodeClick,
            onCheckClick = onCheckClick,
            onCheckLongClick = onCheckLongClick,
            onRemoveClick = onRemoveClick,
            modifier = modifier
                .alpha(
                    when {
                        state.loading.isLoading -> MinAlpha
                        else -> if (animateIn) animateInAlpha else 0F
                    },
                ),
        )
    } else if (state.items.isNullOrEmpty()) {
        Text(
            text = stringResource(R.string.list_placeholder_empty),
            color = TraktTheme.colors.textSecondary,
            style = TraktTheme.typography.heading6,
            modifier = Modifier.padding(contentPadding),
        )
    }
}

@Composable
private fun ContentItemsGrid(
    modifier: Modifier = Modifier,
    items: Map<LocalDate, ImmutableList<CalendarItem>?>,
    itemsLoading: ImmutableSet<TraktId>?,
    loadingMore: Boolean,
    gridState: LazyGridState,
    contentPadding: PaddingValues,
    onShowClick: (EpisodeItem) -> Unit,
    onMovieClick: (MovieItem) -> Unit,
    onEpisodeClick: (EpisodeItem) -> Unit,
    onCheckClick: (CalendarItem) -> Unit,
    onCheckLongClick: (CalendarItem) -> Unit,
    onRemoveClick: (CalendarItem) -> Unit,
) {
    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(TraktTheme.size.calendarGridColumns),
        horizontalArrangement = spacedBy(TraktTheme.spacing.mainGridHorizontalSpace),
        verticalArrangement = spacedBy(0.dp),
        contentPadding = contentPadding,
        overscrollEffect = null,
        modifier = modifier,
    ) {
        items.keys.forEachIndexed { index, date ->
            val gridItems = items[date] ?: EmptyImmutableList

            item(
                span = { GridItemSpan(maxLineSpan) },
                key = "header_$date",
            ) {
                Row(
                    horizontalArrangement = spacedBy(6.dp),
                    verticalAlignment = CenterVertically,
                    modifier = Modifier
                        .padding(top = if (index == 0) 0.dp else 38.dp),
                ) {
                    val isToday = remember(date) { date == nowLocalDay() }
                    if (isToday) {
                        Box(
                            modifier = Modifier
                                .background(color = Purple400, shape = RoundedCornerShape(100))
                                .size(3.dp, 16.dp),
                        )
                    }
                    TraktHeader(
                        title = date.format(fullDayFormat()).capitalize(),
                        titleColor = when {
                            gridItems.isNotEmpty() -> TraktTheme.colors.textPrimary
                            else -> TraktTheme.colors.textSecondary
                        },
                    )
                }
            }

            if (gridItems.isNotEmpty()) {
                items(
                    count = gridItems.size,
                    key = { index -> gridItems[index].id.value },
                ) { index ->
                    val item = gridItems[index]
                    if (item is EpisodeItem) {
                        CalendarEpisodeItemView(
                            item = item,
                            itemLoading = itemsLoading?.contains(item.id) == true,
                            midReleases = true,
                            onClick = { onEpisodeClick(item) },
                            onShowClick = { onShowClick(item) },
                            onCheckClick = { onCheckClick(item) },
                            onCheckLongClick = { onCheckLongClick(item) },
                            onRemoveClick = { onRemoveClick(item) },
                            modifier = Modifier.padding(top = 14.dp),
                        )
                    }

                    if (item is MovieItem) {
                        CalendarMovieItemView(
                            item = item,
                            itemLoading = itemsLoading?.contains(item.id) == true,
                            onClick = { onMovieClick(item) },
                            onCheckClick = { onCheckClick(item) },
                            onCheckLongClick = { onCheckLongClick(item) },
                            onRemoveClick = { onRemoveClick(item) },
                            modifier = Modifier.padding(top = 14.dp),
                        )
                    }
                }
            } else {
                item(
                    span = { GridItemSpan(maxLineSpan) },
                    key = "empty_$date",
                ) {
                    Text(
                        text = stringResource(R.string.text_calendar_placeholder_2),
                        color = TraktTheme.colors.textSecondary,
                        style = TraktTheme.typography.meta,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }

        if (loadingMore) {
            item(key = "loading_more") {
                Box(modifier = Modifier.padding(top = 14.dp)) {
                    EpisodeSkeletonCard()
                }
            }
        }
    }
}

@Composable
private fun ContentLoadingGrid(
    visible: Boolean = true,
    isFirstWeekDay: Boolean = false,
    contentPadding: PaddingValues,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "infiniteTransition")
    val shimmerTransition by infiniteTransition
        .animateColor(
            initialValue = TraktTheme.colors.skeletonContainer,
            targetValue = TraktTheme.colors.skeletonShimmer,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1000),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "shimmerTransition",
        )

    LazyVerticalGrid(
        columns = GridCells.Fixed(TraktTheme.size.calendarGridColumns),
        horizontalArrangement = spacedBy(TraktTheme.spacing.mainGridHorizontalSpace),
        verticalArrangement = spacedBy(TraktTheme.spacing.mainGridVerticalSpace),
        contentPadding = PaddingValues(
            top = when {
                isFirstWeekDay -> contentPadding.calculateTopPadding() + 8.dp
                else -> contentPadding.calculateTopPadding() + 8.dp
            },
            bottom = contentPadding.calculateBottomPadding(),
            start = TraktTheme.spacing.mainPageHorizontalSpace,
            end = TraktTheme.spacing.mainPageHorizontalSpace,
        ),
        overscrollEffect = null,
        userScrollEnabled = false,
        modifier = Modifier
            .fillMaxSize()
            .alpha(if (visible) 1F else 0F),
    ) {
        item(
            span = { GridItemSpan(maxLineSpan) },
        ) {
            Box {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5F)
                        .height(20.dp)
                        .background(shimmerTransition, CircleShape)
                        .padding(bottom = TraktTheme.spacing.mainGridVerticalSpace),
                )
            }
        }
        items(count = 12) {
            EpisodeSkeletonCard()
        }
    }
}

private fun scrollToDay(
    state: CalendarState,
    date: LocalDate,
    scope: CoroutineScope,
    scrollOffset: Int,
    gridState: LazyGridState,
) {
    val items = state.items ?: return
    var accumulatedCount = 0

    for (itemDate in items.keys) {
        if (itemDate == date) {
            scope.launch {
                val offset = when {
                    accumulatedCount == 0 -> 0
                    else -> scrollOffset
                }
                gridState.scrollToItem(accumulatedCount, offset)
            }
            return
        }

        val itemsForDate = items[itemDate] ?: EmptyImmutableList
        accumulatedCount += when {
            itemsForDate.isNotEmpty() -> itemsForDate.size + 1
            else -> 2 // Include header and empty state item
        }
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        CalendarScreen(
            scope = rememberCoroutineScope(),
            state = CalendarState(
                selectedStartDay = LocalDate.now().with(MONDAY),
                loading = LoadingState.Loading,
            ),
        )
    }
}
