package tv.trakt.trakt.core.profile.sections.screentime.all

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.persistentMapOf
import tv.trakt.trakt.common.helpers.extensions.DeviceSheetPreview
import tv.trakt.trakt.common.helpers.extensions.mediumDateFormat
import tv.trakt.trakt.common.helpers.extensions.rememberDurationFormat
import tv.trakt.trakt.common.ui.theme.colors.Green400
import tv.trakt.trakt.common.ui.theme.colors.Red400
import tv.trakt.trakt.core.profile.sections.screentime.model.ScreenTimeData
import tv.trakt.trakt.core.profile.sections.screentime.model.ScreenTimeData.Stats
import tv.trakt.trakt.core.profile.sections.screentime.ui.ScreenTimeDailyBreakdownCard
import tv.trakt.trakt.core.profile.sections.screentime.ui.ScreenTimePeakHoursCard
import tv.trakt.trakt.core.profile.sections.screentime.ui.ScreenTimeStatCard
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.LocalDate
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

private val StatCardHeight = 96.dp
private val DailyCardHeight = 132.dp
private val PeakCardHeight = 132.dp

@Composable
internal fun ScreenTimeAllView(
    viewModel: ScreenTimeAllViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    state.data?.let { data ->
        ScreenTimeAllContent(
            rangeStart = state.rangeStart,
            data = data,
            modifier = modifier,
        )
    }
}

@Composable
private fun ScreenTimeAllContent(
    rangeStart: LocalDate?,
    data: ScreenTimeData,
    modifier: Modifier = Modifier,
) {
    val cardColor = TraktTheme.colors.dialogOnContainer

    Column(
        verticalArrangement = spacedBy(12.dp),
        modifier = modifier
            .verticalScroll(
                state = rememberScrollState(),
            ),
    ) {
        TraktHeader(
            title = stringResource(R.string.header_screen_time),
            subtitle = rangeSubtitle(rangeStart),
            modifier = Modifier.padding(bottom = 8.dp),
        )

        ScreenTimeDailyBreakdownCard(
            data = data.dailyHours,
            containerColor = cardColor,
            modifier = Modifier
                .fillMaxWidth()
                .height(DailyCardHeight),
        )

        ScreenTimePeakHoursCard(
            data = data.peakHours,
            containerColor = cardColor,
            modifier = Modifier
                .fillMaxWidth()
                .height(PeakCardHeight),
        )

        Row(
            horizontalArrangement = spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            DurationStatCard(
                labelRes = R.string.label_stats_screen_time_total,
                value = data.stats.totalTime,
                change = data.stats.totalTimeChange,
                containerColor = cardColor,
                modifier = Modifier
                    .weight(1F)
                    .height(StatCardHeight),
            )
            DurationStatCard(
                labelRes = R.string.label_stats_avg_per_day,
                value = data.stats.averagePerDay,
                change = data.stats.averagePerDayChange,
                containerColor = cardColor,
                modifier = Modifier
                    .weight(1F)
                    .height(StatCardHeight),
            )
        }

        Row(
            horizontalArrangement = spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            DurationStatCard(
                labelRes = R.string.label_stats_shows,
                value = data.stats.showsTime,
                change = data.stats.showsTimeChange,
                containerColor = cardColor,
                modifier = Modifier
                    .weight(1F)
                    .height(StatCardHeight),
            )
            DurationStatCard(
                labelRes = R.string.label_stats_movies,
                value = data.stats.moviesTime,
                change = data.stats.moviesTimeChange,
                containerColor = cardColor,
                modifier = Modifier
                    .weight(1F)
                    .height(StatCardHeight),
            )
        }

        Row(
            horizontalArrangement = spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = TraktTheme.spacing.shadowClipSpace),
        ) {
            WakingHoursStatCard(
                percent = data.stats.wakingHoursPercent,
                change = data.stats.wakingHoursPercentChange,
                containerColor = cardColor,
                modifier = Modifier
                    .weight(1F)
                    .height(StatCardHeight),
            )
            Spacer(modifier = Modifier.weight(1F))
        }
    }
}

@Composable
private fun DurationStatCard(
    @StringRes labelRes: Int,
    value: Duration,
    change: Duration,
    containerColor: Color,
    modifier: Modifier = Modifier,
) {
    val changeMinutes = change.inWholeMinutes
    val changeText = rememberDurationFormat(changeMinutes)

    ScreenTimeStatCard(
        value = rememberDurationFormat(value.inWholeMinutes),
        label = stringResource(labelRes),
        subtitle = when {
            changeMinutes > 0 -> "+$changeText"
            changeMinutes < 0 -> changeText
            else -> stringResource(R.string.text_stats_delta_same)
        },
        subtitleColor = when {
            changeMinutes > 0 -> Green400
            changeMinutes < 0 -> Red400
            else -> TraktTheme.colors.textSecondary
        },
        containerColor = containerColor,
        modifier = modifier,
    )
}

@Composable
private fun WakingHoursStatCard(
    percent: Int,
    change: Int,
    containerColor: Color,
    modifier: Modifier = Modifier,
) {
    ScreenTimeStatCard(
        value = "$percent%",
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
        containerColor = containerColor,
        modifier = modifier,
    )
}

@Composable
private fun rangeSubtitle(rangeStart: LocalDate?): String? {
    rangeStart ?: return null
    val format = mediumDateFormat()
    val todayText = stringResource(R.string.text_stats_today)
    return "${rangeStart.format(format)}  –  $todayText"
}

@DeviceSheetPreview
@Composable
private fun Preview() {
    TraktTheme {
        ScreenTimeAllContent(
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
            modifier = Modifier.padding(24.dp),
        )
    }
}
