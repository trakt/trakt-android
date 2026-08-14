package tv.trakt.trakt.core.profile.sections.screentime

import androidx.annotation.StringRes
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.persistentMapOf
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.mediumDateFormat
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.rememberDurationFormat
import tv.trakt.trakt.common.ui.theme.colors.Green400
import tv.trakt.trakt.common.ui.theme.colors.Red400
import tv.trakt.trakt.core.profile.sections.screentime.all.ScreenTimeAllSheet
import tv.trakt.trakt.core.profile.sections.screentime.model.ScreenTimeData
import tv.trakt.trakt.core.profile.sections.screentime.model.ScreenTimeData.Stats
import tv.trakt.trakt.core.profile.sections.screentime.ui.ScreenTimeDailyBreakdownCard
import tv.trakt.trakt.core.profile.sections.screentime.ui.ScreenTimePeakHoursCard
import tv.trakt.trakt.core.profile.sections.screentime.ui.ScreenTimeStatCard
import tv.trakt.trakt.core.profile.sections.screentime.ui.ScreenTimeStatCardSkeleton
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktSectionHeader
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.LocalDate
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

internal val StatCardSize = 128.dp
internal val StatWideCardWidth = 272.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ProfileScreenTimeView(
    modifier: Modifier = Modifier,
    viewModel: ProfileScreenTimeViewModel = koinViewModel(),
    headerPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var screenTimeSheet by remember { mutableStateOf<ScreenTimeData?>(null) }

    ProfileScreenTimeContent(
        state = state,
        modifier = modifier,
        headerPadding = headerPadding,
        contentPadding = contentPadding,
        onCollapse = viewModel::setCollapsed,
        onAllClick = {
            state.data?.let {
                screenTimeSheet = it
            }
        },
    )

    screenTimeSheet?.let { data ->
        ScreenTimeAllSheet(
            data = data,
            visible = true,
            onDismiss = { screenTimeSheet = null },
        )
    }
}

@Composable
internal fun ProfileScreenTimeContent(
    state: ProfileScreenTimeState,
    modifier: Modifier = Modifier,
    headerPadding: PaddingValues = PaddingValues(),
    contentPadding: PaddingValues = PaddingValues(),
    onCollapse: (collapsed: Boolean) -> Unit = {},
    onAllClick: () -> Unit = {},
) {
    val subtitle = state.rangeStart?.let { start ->
        "${start.format(mediumDateFormat())}  –  ${stringResource(R.string.text_stats_today)}"
    }

    var animateCollapse by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .animateContentSize(
                animationSpec = if (animateCollapse) spring() else snap(),
            ),
    ) {
        TraktSectionHeader(
            title = stringResource(R.string.header_screen_time),
            subtitle = subtitle,
            chevron = true,
            collapsed = state.collapsed ?: false,
            onCollapseClick = {
                animateCollapse = true
                val current = (state.collapsed ?: false)
                onCollapse(!current)
            },
            modifier = Modifier
                .padding(headerPadding)
                .onClick(
                    enabled = state.loading.isDone,
                    onClick = onAllClick,
                ),
        )

        if (state.collapsed != true) {
            Crossfade(
                targetState = state.loading,
                animationSpec = tween(200),
            ) { loading ->
                when (loading) {
                    Idle, Loading -> {
                        ContentLoading(contentPadding = contentPadding)
                    }

                    Done -> {
                        if (state.data != null) {
                            ContentRow(
                                data = state.data,
                                contentPadding = contentPadding,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ContentRow(
    data: ScreenTimeData,
    contentPadding: PaddingValues,
) {
    val dataCards = rememberDataCards(data)
    LazyRow(
        horizontalArrangement = spacedBy(TraktTheme.spacing.mainRowSpace),
        contentPadding = contentPadding,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = TraktTheme.spacing.shadowClipSpace),
    ) {
        item(
            key = "daily",
        ) {
            ScreenTimeDailyBreakdownCard(
                data = data.dailyHours,
                modifier = Modifier
                    .height(StatCardSize)
                    .width(StatWideCardWidth),
            )
        }

        item(
            key = "peak",
        ) {
            ScreenTimePeakHoursCard(
                data = data.peakHours,
                modifier = Modifier
                    .height(StatCardSize)
                    .width(StatWideCardWidth),
            )
        }

        items(
            items = dataCards,
            key = { it.labelRes },
        ) { card ->
            val changeMinutes = card.change.inWholeMinutes
            val change = rememberDurationFormat(changeMinutes)

            ScreenTimeStatCard(
                value = rememberDurationFormat(card.value.inWholeMinutes),
                label = stringResource(card.labelRes),
                subtitle = when {
                    changeMinutes > 0 -> "+$change"
                    changeMinutes < 0 -> change
                    else -> stringResource(R.string.text_stats_delta_same)
                },
                subtitleColor = when {
                    changeMinutes > 0 -> Green400
                    changeMinutes < 0 -> Red400
                    else -> TraktTheme.colors.textSecondary
                },
                modifier = Modifier
                    .widthIn(min = StatCardSize)
                    .height(StatCardSize),
            )
        }

        item(
            key = "waking",
        ) {
            val change = data.stats.wakingHoursPercentChange
            ScreenTimeStatCard(
                value = "${data.stats.wakingHoursPercent}%",
                label = stringResource(R.string.label_stats_screen_time_share),
                subtitle = when {
                    change > 0 -> "+$change%"
                    change < 0 -> "$change%"
                    else -> stringResource(R.string.text_stats_delta_same)
                },
                subtitleColor = when {
                    change > 0 -> Green400
                    change < 0 -> Red400
                    else -> TraktTheme.colors.textSecondary
                },
                modifier = Modifier
                    .widthIn(min = StatCardSize)
                    .height(StatCardSize),
            )
        }
    }
}

@Composable
private fun ContentLoading(contentPadding: PaddingValues) {
    LazyRow(
        horizontalArrangement = spacedBy(TraktTheme.spacing.mainRowSpace),
        contentPadding = contentPadding,
        userScrollEnabled = false,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = TraktTheme.spacing.shadowClipSpace),
    ) {
        repeat(3) {
            item(
                key = "loading_$it",
            ) {
                ScreenTimeStatCardSkeleton(
                    modifier = Modifier
                        .width(StatWideCardWidth)
                        .height(StatCardSize),
                )
            }
        }
    }
}

@Composable
private fun rememberDataCards(data: ScreenTimeData): List<ScreenTimeStatItem> {
    return remember(data) {
        listOf(
            ScreenTimeStatItem(
                R.string.label_stats_screen_time_total,
                data.stats.totalTime,
                data.stats.totalTimeChange,
            ),
            ScreenTimeStatItem(
                R.string.label_stats_avg_per_day,
                data.stats.averagePerDay,
                data.stats.averagePerDayChange,
            ),
            ScreenTimeStatItem(
                R.string.label_stats_shows,
                data.stats.showsTime,
                data.stats.showsTimeChange,
            ),
            ScreenTimeStatItem(
                R.string.label_stats_movies,
                data.stats.moviesTime,
                data.stats.moviesTimeChange,
            ),
        )
    }
}

private data class ScreenTimeStatItem(
    @param:StringRes val labelRes: Int,
    val value: Duration,
    val change: Duration,
)

@Preview
@Composable
private fun ProfileScreenTimeContentPreview() {
    TraktTheme {
        ProfileScreenTimeContent(
            state = ProfileScreenTimeState(
                rangeStart = LocalDate.now().minusDays(6),
                data = ScreenTimeData(
                    stats = Stats(
                        totalTime = 12.hours + 6.minutes,
                        totalTimeChange = 1.hours + 30.minutes,
                        averagePerDay = 1.hours + 43.minutes,
                        averagePerDayChange = 12.minutes,
                        showsTime = 9.hours,
                        showsTimeChange = (-45).minutes,
                        moviesTime = 3.hours + 6.minutes,
                        moviesTimeChange = Duration.ZERO,
                        wakingHoursPercent = 10,
                        wakingHoursPercentChange = -17,
                    ),
                    dailyHours = persistentMapOf(),
                    peakHours = persistentMapOf(),
                ),
            ),
        )
    }
}
