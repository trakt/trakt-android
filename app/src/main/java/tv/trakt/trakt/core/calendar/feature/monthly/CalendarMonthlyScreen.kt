@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.calendar.feature.monthly

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType.Companion.Confirm
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import tv.trakt.trakt.LocalSnackbarState
import tv.trakt.trakt.common.helpers.extensions.capitalize
import tv.trakt.trakt.common.helpers.extensions.nowLocalDay
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.rememberAppLocale
import tv.trakt.trakt.common.helpers.extensions.yearMonthFormat
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.core.calendar.feature.monthly.sheets.CalendarDaySheet
import tv.trakt.trakt.core.calendar.feature.monthly.ui.CalendarDisplayDropdown
import tv.trakt.trakt.core.calendar.feature.monthly.ui.CalendarMonthSelectorView
import tv.trakt.trakt.core.calendar.feature.monthly.ui.CalendarMonthView
import tv.trakt.trakt.core.calendar.feature.monthly.ui.CalendarWeekDaysView
import tv.trakt.trakt.core.calendar.feature.monthly.ui.INITIAL_MONTH_PAGE
import tv.trakt.trakt.core.calendar.feature.monthly.ui.MONTH_PAGE_COUNT
import tv.trakt.trakt.core.calendar.feature.monthly.ui.monthForPage
import tv.trakt.trakt.core.calendar.model.CalendarDayDisplay
import tv.trakt.trakt.core.calendar.model.CalendarItem
import tv.trakt.trakt.core.calendar.model.CalendarItem.EpisodeItem
import tv.trakt.trakt.core.calendar.model.CalendarItem.MovieItem
import tv.trakt.trakt.core.calendar.model.CalendarView
import tv.trakt.trakt.core.calendar.ui.CalendarTodayIcon
import tv.trakt.trakt.core.calendar.ui.CalendarTypeChips
import tv.trakt.trakt.core.calendar.ui.CalendarViewToggle
import tv.trakt.trakt.core.discover.sections.releases.model.ReleaseType
import tv.trakt.trakt.core.filters.GlobalFiltersSheet
import tv.trakt.trakt.core.filters.navigation.GlobalFiltersOptions
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.MediaFilterIcon
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.confirmation.RemoveConfirmationSheet
import tv.trakt.trakt.ui.components.dateselection.DateSelectionSheet
import tv.trakt.trakt.ui.snackbar.ShortSnackDuration
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle.FULL_STANDALONE

private val PageSpacing = 8.dp

@Composable
internal fun CalendarMonthlyScreen(
    viewModel: tv.trakt.trakt.core.calendar.feature.monthly.CalendarMonthlyViewModel,
    onNavigateBack: () -> Unit,
    onEpisodeClick: (showId: TraktId, episode: Episode) -> Unit,
    onShowClick: (TraktId) -> Unit,
    onMovieClick: (TraktId) -> Unit,
    onViewClick: (CalendarView) -> Unit,
) {
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val snackbar = LocalSnackbarState.current

    val state by viewModel.state.collectAsStateWithLifecycle()

    var filtersSheet by remember { mutableStateOf(false) }
    var daySheet by remember { mutableStateOf<LocalDate?>(null) }
    var dateSelectionSheet by remember { mutableStateOf<CalendarItem?>(null) }
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

    CalendarMonthlyContent(
        state = state,
        onMonthChange = viewModel::setMonth,
        onDayClick = { date ->
            if (state.loading.isLoading) return@CalendarMonthlyContent
            // Nothing to show for a day without releases.
            if (state.items?.get(date).isNullOrEmpty()) return@CalendarMonthlyContent
            daySheet = date
        },
        onFiltersClick = {
            filtersSheet = true
        },
        onTypeClick = viewModel::setType,
        onDisplayClick = viewModel::setDisplay,
        onViewClick = onViewClick,
        onBackClick = onNavigateBack,
    )

    // Sheets

    CalendarDaySheet(
        visible = daySheet != null,
        date = daySheet,
        items = daySheet?.let { state.items?.get(it) },
        itemsLoading = state.itemsLoading,
        onEpisodeClick = { viewModel.navigateToEpisode(it.show, it.episode) },
        onShowClick = { viewModel.navigateToShow(it.show) },
        onMovieClick = { viewModel.navigateToMovie(it.movie) },
        onCheckClick = { item ->
            when (item) {
                is EpisodeItem -> viewModel.addToHistory(item.episode)
                is MovieItem -> viewModel.addToHistory(item.movie)
            }
        },
        onCheckLongClick = { dateSelectionSheet = it },
        onRemoveClick = { confirmRemoveSheet = it },
        onDismiss = { daySheet = null },
    )

    DateSelectionSheet(
        active = dateSelectionSheet != null,
        title = dateSelectionSheet?.title.orEmpty(),
        subtitle = when (val item = dateSelectionSheet) {
            is EpisodeItem -> item.episode.seasonEpisodeString()
            else -> null
        },
        onResult = { date ->
            when (val item = dateSelectionSheet) {
                is EpisodeItem -> viewModel.addToHistory(episode = item.episode, customDate = date)
                is MovieItem -> viewModel.addToHistory(movie = item.movie, customDate = date)
                null -> Unit
            }
        },
        onDismiss = {
            dateSelectionSheet = null
        },
    )

    RemoveConfirmationSheet(
        active = confirmRemoveSheet != null,
        onYes = {
            when (val item = confirmRemoveSheet) {
                is EpisodeItem -> viewModel.removeFromWatched(item.episode)
                is MovieItem -> viewModel.removeFromWatched(item.movie)
                null -> Unit
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
private fun CalendarMonthlyContent(
    state: CalendarMonthlyState,
    modifier: Modifier = Modifier,
    onMonthChange: (YearMonth) -> Unit = {},
    onDayClick: (LocalDate) -> Unit = {},
    onFiltersClick: () -> Unit = {},
    onTypeClick: (ReleaseType) -> Unit = {},
    onDisplayClick: (CalendarDayDisplay) -> Unit = {},
    onViewClick: (CalendarView) -> Unit = {},
    onBackClick: () -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val locale = rememberAppLocale()
    val monthFormat = yearMonthFormat()

    val anchorMonth = remember { YearMonth.from(nowLocalDay()) }

    val pagerState = rememberPagerState(initialPage = INITIAL_MONTH_PAGE) { MONTH_PAGE_COUNT }
    var selectorVisible by rememberSaveable { mutableStateOf(false) }

    val selectedMonth by remember {
        derivedStateOf { anchorMonth.monthForPage(pagerState.currentPage) }
    }

    val selectorRotation by animateFloatAsState(
        targetValue = if (selectorVisible) 180F else 0F,
        label = "selectorRotation",
    )

    // Load the month the pager settles on; swiping past a month doesn't fetch it.
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .map(anchorMonth::monthForPage)
            .distinctUntilChanged()
            .collect(onMonthChange)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary)
            .padding(
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
            ),
    ) {
        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .padding(horizontal = TraktTheme.spacing.mainPageHorizontalSpace),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_back_arrow),
                tint = TraktTheme.colors.textPrimary,
                contentDescription = null,
                modifier = Modifier.onClick(onClick = onBackClick),
            )
            Row(
                verticalAlignment = CenterVertically,
                horizontalArrangement = spacedBy(12.dp),
                modifier = Modifier.onClick { selectorVisible = !selectorVisible },
            ) {
                TraktHeader(
                    title = when (selectedMonth.year) {
                        anchorMonth.year -> {
                            selectedMonth.month
                                .getDisplayName(FULL_STANDALONE, locale)
                                .capitalize()
                        }

                        else -> {
                            selectedMonth.format(monthFormat).capitalize()
                        }
                    },
                    subtitle = state.filter?.mode?.let {
                        stringResource(it.displayRes)
                    } ?: stringResource(MediaMode.Media.displayRes),
                )
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_down_small),
                    tint = TraktTheme.colors.textPrimary,
                    contentDescription = null,
                    modifier = Modifier
                        .size(14.dp)
                        .rotate(selectorRotation),
                )
            }

            Spacer(modifier = Modifier.weight(1F))

            Row(
                verticalAlignment = CenterVertically,
                horizontalArrangement = spacedBy(20.dp),
            ) {
                CalendarTodayIcon(
                    visible = true,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(INITIAL_MONTH_PAGE)
                        }
                    },
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .graphicsLayer {
                            translationY = -1.dp.toPx()
                        },
                )

                CalendarViewToggle(
                    current = CalendarView.Monthly,
                    onViewClick = onViewClick,
                )

                MediaFilterIcon(
                    active = state.filter?.isActive == true,
                    enabled = state.loading.isDone,
                    onClick = onFiltersClick,
                )

                CalendarDisplayDropdown(
                    current = state.display,
                    onDisplayClick = onDisplayClick,
                )
            }
        }

        AnimatedVisibility(
            visible = selectorVisible,
        ) {
            CalendarMonthSelectorView(
                anchorMonth = anchorMonth,
                selectedPage = pagerState.currentPage,
                onMonthClick = { page ->
                    scope.launch {
                        pagerState.animateScrollToPage(page)
                    }
                },
                modifier = Modifier.padding(bottom = 12.dp),
            )
        }

        Box(
            contentAlignment = Alignment.CenterEnd,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = TraktTheme.spacing.mainPageHorizontalSpace)
                .padding(bottom = 6.dp),
        ) {
            CalendarTypeChips(
                selected = state.type,
                onTypeClick = onTypeClick,
                modifier = Modifier
                    .fillMaxWidth(),
            )

            if (state.loading.isLoading) {
                FilmProgressIndicator(
                    size = 16.dp,
                )
            }
        }

        val pagerPadding = TraktTheme.spacing.mainPageHorizontalSpace / 2

        CalendarWeekDaysView(
            modifier = Modifier
                .padding(horizontal = pagerPadding)
                .padding(vertical = 8.dp),
        )

        HorizontalPager(
            state = pagerState,
            pageSpacing = PageSpacing,
            contentPadding = PaddingValues(
                start = pagerPadding,
                end = pagerPadding,
                bottom = WindowInsets.navigationBars.asPaddingValues()
                    .calculateBottomPadding()
                    .plus(TraktTheme.size.navigationBarHeight)
                    .plus(8.dp),
            ),
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            CalendarMonthView(
                month = remember(anchorMonth, page) { anchorMonth.monthForPage(page) },
                display = state.display,
                items = state.items,
                onDayClick = onDayClick,
            )
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
        CalendarMonthlyContent(
            state = CalendarMonthlyState(),
        )
    }
}
