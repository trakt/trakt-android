package tv.trakt.trakt.core.profile.sections.screentime.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.Bottom
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap
import tv.trakt.trakt.common.helpers.extensions.rememberDurationFormat
import tv.trakt.trakt.common.ui.theme.colors.Purple200
import tv.trakt.trakt.common.ui.theme.colors.Purple500
import tv.trakt.trakt.common.ui.theme.colors.Purple850
import tv.trakt.trakt.core.profile.sections.screentime.StatWideCardWidth
import tv.trakt.trakt.core.profile.sections.screentime.usecase.WAKING_HOURS_PER_DAY
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.AbstractMap.SimpleImmutableEntry
import java.util.Locale
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

// Bars below this fraction of the tallest day still render a visible stub.
private const val MIN_BAR_FRACTION = 0.06F

// Days shown when there is no data, so the graph still renders with zeroed bars.
private const val FALLBACK_DAYS = 7

private val cardShape = RoundedCornerShape(16.dp)

@Composable
internal fun ScreenTimeDailyBreakdownCard(
    modifier: Modifier = Modifier,
    data: ImmutableMap<LocalDate, Duration>,
    containerColor: Color = TraktTheme.colors.dialogContainer,
) {
    val locale = configurationLocale()
    val today = remember { LocalDate.now() }
    val days = remember(data, today) {
        when {
            data.isEmpty() -> (FALLBACK_DAYS - 1 downTo 0).map { offset ->
                SimpleImmutableEntry(today.minusDays(offset.toLong()), Duration.ZERO)
            }
            else -> data.entries.sortedBy { it.key }
        }
    }
    val maxMinutes = remember(days) { days.maxOfOrNull { it.value.inWholeMinutes } ?: 0L }

    Column(
        verticalArrangement = spacedBy(12.dp),
        modifier = modifier
            .shadow(
                elevation = TraktTheme.colors.shadowDynamicDefault,
                shape = cardShape,
            )
            .background(
                color = containerColor,
                shape = cardShape,
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Text(
            text = stringResource(R.string.header_stats_graph_screen_time_daily),
            style = TraktTheme.typography.cardSubtitle,
            color = TraktTheme.colors.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Column(
            verticalArrangement = spacedBy(4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1F),
        ) {
            Row(
                verticalAlignment = Bottom,
                horizontalArrangement = spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1F),
            ) {
                days.forEach { (_, duration) ->
                    val minutes = duration.inWholeMinutes
                    val fraction = when {
                        maxMinutes > 0 -> (minutes.toFloat() / maxMinutes).coerceAtLeast(MIN_BAR_FRACTION)
                        else -> MIN_BAR_FRACTION
                    }
                    Column(
                        horizontalAlignment = CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier
                            .weight(1F)
                            .fillMaxHeight(),
                    ) {
                        Text(
                            text = rememberDurationFormat(
                                duration = minutes,
                                spaces = false,
                            ),
                            style = TraktTheme.typography.cardTitle.copy(fontSize = 8.sp),
                            color = TraktTheme.colors.textPrimary,
                            maxLines = 1,
                            textAlign = TextAlign.Center,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(fraction)
                                .background(
                                    color = barColor(minutes),
                                    shape = RoundedCornerShape(
                                        topStart = 6.dp,
                                        topEnd = 6.dp,
                                    ),
                                ),
                        )
                    }
                }
            }

            Row(
                horizontalArrangement = spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                days.forEach { (date, _) ->
                    Text(
                        text = when (date) {
                            today -> stringResource(R.string.text_stats_today)
                            else -> date.dayOfWeek.getDisplayName(TextStyle.SHORT_STANDALONE, locale)
                        },
                        style = TraktTheme.typography.cardTitle.copy(fontSize = 8.sp),
                        color = TraktTheme.colors.textPrimary,
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1F),
                    )
                }
            }
        }
    }
}

@Composable
private fun configurationLocale(): Locale {
    val configuration = LocalConfiguration.current
    return remember(configuration) {
        configuration.locales.get(0) ?: Locale.getDefault()
    }
}

private fun barColor(minutes: Long): Color {
    val pct = minutes.toFloat() / (WAKING_HOURS_PER_DAY * 60) * 100
    return when {
        pct >= 30 -> Purple200
        pct >= 15 -> Purple500
        else -> Purple850
    }
}

@Preview(widthDp = 320)
@Composable
private fun Preview() {
    val base = LocalDate.now().minusDays(6)
    val sample = listOf(108L, 145L, 51L, 51L, 51L, 320L, 95L)
        .mapIndexed { index, mins -> base.plusDays(index.toLong()) to mins.minutes }
        .toMap()
        .toImmutableMap()

    TraktTheme {
        ScreenTimeDailyBreakdownCard(
            data = sample,
            modifier = Modifier
                .width(StatWideCardWidth)
                .height(156.dp)
                .padding(16.dp),
        )
    }
}

@Preview(widthDp = 320)
@Composable
private fun PreviewEmpty() {
    TraktTheme {
        ScreenTimeDailyBreakdownCard(
            data = emptyMap<LocalDate, Duration>().toImmutableMap(),
            modifier = Modifier
                .width(StatWideCardWidth)
                .height(156.dp)
                .padding(16.dp),
        )
    }
}
