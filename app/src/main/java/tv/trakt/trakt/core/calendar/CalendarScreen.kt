@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package tv.trakt.trakt.core.calendar

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType.Companion.Confirm
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import tv.trakt.trakt.LocalSnackbarState
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.fullDayFormat
import tv.trakt.trakt.common.helpers.extensions.nowLocalDay
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.ui.theme.colors.Purple400
import tv.trakt.trakt.core.calendar.model.CalendarItem
import tv.trakt.trakt.core.calendar.ui.CalendarEpisodeItemView
import tv.trakt.trakt.core.calendar.ui.CalendarMovieItemView
import tv.trakt.trakt.core.calendar.ui.controls.CalendarControlsView
import tv.trakt.trakt.helpers.SimpleScrollConnection
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.confirmation.RemoveConfirmationSheet
import tv.trakt.trakt.ui.components.mediacards.skeletons.EpisodeSkeletonCard
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.DayOfWeek.MONDAY
import java.time.LocalDate

@Composable
internal fun CalendarScreen(
    viewModel: CalendarViewModel,
    onNavigateBack: () -> Unit,
    onEpisodeClick: (showId: TraktId, episode: Episode) -> Unit,
    onShowClick: (TraktId) -> Unit,
    onMovieClick: (TraktId) -> Unit,
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val snackbar = LocalSnackbarState.current

    val state by viewModel.state.collectAsStateWithLifecycle()
    var confirmRemoveSheet by remember { mutableStateOf<CalendarItem?>(null) }

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
        state.info?.get(context)?.let {
            haptic.performHapticFeedback(Confirm)
            snackbar.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short,
            )
            viewModel.clearInfo()
        }
    }

    CalendarScreen(
        state = state,
        onTodayClick = viewModel::loadTodayData,
        onNextWeekClick = viewModel::loadNextWeekData,
        onPreviousWeekClick = viewModel::loadPreviousWeekData,
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
        onRemoveClick = { item ->
            if (state.loading.isLoading) return@CalendarScreen
            confirmRemoveSheet = item
        },
        onBackClick = onNavigateBack,
    )

    @OptIn(ExperimentalMaterial3Api::class)
    RemoveConfirmationSheet(
        active = confirmRemoveSheet != null,
        onYes = {
            (confirmRemoveSheet as? CalendarItem.EpisodeItem)?.let {
                viewModel.removeFromWatched(it.episode)
            }
            (confirmRemoveSheet as? CalendarItem.MovieItem)?.let {
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
}

@Composable
private fun CalendarScreen(
    state: CalendarState,
    modifier: Modifier = Modifier,
    onTodayClick: () -> Unit = {},
    onNextWeekClick: () -> Unit = {},
    onPreviousWeekClick: () -> Unit = {},
    onShowClick: (CalendarItem.EpisodeItem) -> Unit = {},
    onMovieClick: (CalendarItem.MovieItem) -> Unit = {},
    onEpisodeClick: (CalendarItem.EpisodeItem) -> Unit = {},
    onRemoveClick: (CalendarItem) -> Unit = {},
    onBackClick: () -> Unit = {},
) {
    val scope = rememberCoroutineScope()

    val contentPadding = PaddingValues(
        start = TraktTheme.spacing.mainPageHorizontalSpace,
        end = TraktTheme.spacing.mainPageHorizontalSpace,
        top = WindowInsets.statusBars.asPaddingValues()
            .calculateTopPadding()
            .plus(176.dp),
        bottom = WindowInsets.navigationBars.asPaddingValues()
            .calculateBottomPadding()
            .plus(TraktTheme.size.navigationBarHeight)
            .plus(64.dp),
    )

    val gridState = rememberLazyGridState()
    val scrollConnection = rememberSaveable(saver = SimpleScrollConnection.Saver) {
        SimpleScrollConnection()
    }

    val focusedDate by remember(state.items) {
        val keys = state.items?.keys?.toList() ?: EmptyImmutableList

        derivedStateOf {
            val firstVisibleIndex = gridState.firstVisibleItemIndex
            if (firstVisibleIndex < 0 || state.items.isNullOrEmpty()) {
                return@derivedStateOf null
            }

            var accumulatedCount = 0
            for (date in keys) {
                val itemsForDate = state.items[date] ?: EmptyImmutableList
                val itemCountForDate = when {
                    itemsForDate.isNotEmpty() -> itemsForDate.size + 1
                    else -> 0
                }

                if (firstVisibleIndex < accumulatedCount + itemCountForDate) {
                    return@derivedStateOf date
                }

                accumulatedCount += itemCountForDate
            }

            null
        }
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
            onRemoveClick = onRemoveClick,
        )

        val availableDates = remember(state.items) {
            state.items?.keys
                ?.filter { state.items[it]?.isNotEmpty() == true }
                ?.toImmutableSet()
        }

        // Mask for the top content under the calendar controls.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(TraktTheme.colors.backgroundPrimary),
        )

        val scrollOffset = with(LocalDensity.current) { 48.dp.toPx().toInt() }
        CalendarControlsView(
            enabled = !state.loading.isLoading,
            startDate = state.selectedStartDay,
            focusedDate = focusedDate,
            availableItems = state.items,
            availableDates = availableDates,
            onDayClick = { date ->
                scrollToDay(
                    scope = scope,
                    state = state,
                    date = date,
                    scrollOffset = scrollOffset,
                    gridState = gridState,
                )
            },
            onTodayClick = {
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
                    onTodayClick()
                }
            },
            onNextWeekClick = onNextWeekClick,
            onPreviousWeekClick = onPreviousWeekClick,
            onBackClick = onBackClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = WindowInsets.statusBars.asPaddingValues()
                        .calculateTopPadding(),
                    start = TraktTheme.spacing.mainPageHorizontalSpace,
                    end = TraktTheme.spacing.mainPageHorizontalSpace,
                )
                .onClick(onClick = {}),
        )
    }
}

@Composable
private fun CalendarContent(
    state: CalendarState,
    gridState: LazyGridState,
    contentPadding: PaddingValues,
    onEpisodeClick: (CalendarItem.EpisodeItem) -> Unit,
    onShowClick: (CalendarItem.EpisodeItem) -> Unit,
    onMovieClick: (CalendarItem.MovieItem) -> Unit,
    onRemoveClick: (CalendarItem) -> Unit,
) {
    val currentList = remember { mutableIntStateOf(state.items.hashCode()) }

    LaunchedEffect(state.items) {
        val hashCode = state.items.hashCode()
        if (currentList.intValue != hashCode) {
            currentList.intValue = hashCode
            gridState.scrollToItem(0)
        }
    }

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
        ContentLoadingGrid(
            visible = state.loading.isLoading,
            contentPadding = contentPadding,
        )
    } else if (!state.items.isNullOrEmpty()) {
        ContentItemsGrid(
            items = state.items,
            itemsLoading = state.itemsLoading,
            gridState = gridState,
            contentPadding = contentPadding,
            loading = state.loading.isLoading,
            onShowClick = onShowClick,
            onMovieClick = onMovieClick,
            onEpisodeClick = onEpisodeClick,
            onRemoveClick = onRemoveClick,
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
    items: Map<LocalDate, ImmutableList<CalendarItem>?>,
    itemsLoading: ImmutableSet<TraktId>?,
    gridState: LazyGridState,
    contentPadding: PaddingValues,
    loading: Boolean,
    onShowClick: (CalendarItem.EpisodeItem) -> Unit,
    onMovieClick: (CalendarItem.MovieItem) -> Unit,
    onEpisodeClick: (CalendarItem.EpisodeItem) -> Unit,
    onRemoveClick: (CalendarItem) -> Unit,
) {
    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(2),
        horizontalArrangement = spacedBy(TraktTheme.spacing.mainGridHorizontalSpace),
        verticalArrangement = spacedBy(0.dp),
        contentPadding = contentPadding,
        overscrollEffect = null,
        modifier = Modifier
            .alpha(if (loading) 0.25F else 1F),
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
                    val isToday = remember(date) {
                        date == nowLocalDay()
                    }

                    if (isToday) {
                        Box(
                            modifier = Modifier
                                .graphicsLayer {
                                    translationY = 0.5.dp.toPx()
                                }
                                .background(color = Purple400, shape = CircleShape)
                                .size(6.dp),
                        )
                    }
                    TraktHeader(
                        title = remember { date.format(fullDayFormat) },
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
                    if (item is CalendarItem.EpisodeItem) {
                        CalendarEpisodeItemView(
                            item = item,
                            itemLoading = itemsLoading?.contains(item.id) == true,
                            onClick = { onEpisodeClick(item) },
                            onShowClick = { onShowClick(item) },
                            onRemoveClick = { onRemoveClick(item) },
                            modifier = Modifier.padding(top = 14.dp),
                        )
                    }

                    if (item is CalendarItem.MovieItem) {
                        CalendarMovieItemView(
                            item = item,
                            itemLoading = itemsLoading?.contains(item.id) == true,
                            onClick = { onMovieClick(item) },
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
                        text = "No items found for this day.", // TODO string resource
                        color = TraktTheme.colors.textSecondary,
                        style = TraktTheme.typography.meta,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ContentLoadingGrid(
    visible: Boolean = true,
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
        columns = GridCells.Fixed(2),
        horizontalArrangement = spacedBy(TraktTheme.spacing.mainGridHorizontalSpace),
        verticalArrangement = spacedBy(TraktTheme.spacing.mainGridVerticalSpace),
        contentPadding = contentPadding,
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
            state = CalendarState(
                selectedStartDay = LocalDate.now().with(MONDAY),
                loading = LoadingState.LOADING,
            ),
        )
    }
}
