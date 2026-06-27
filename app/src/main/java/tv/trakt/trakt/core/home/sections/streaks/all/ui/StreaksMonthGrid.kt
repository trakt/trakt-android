@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.home.sections.streaks.all.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults.rememberTooltipPositionProvider
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.launch
import tv.trakt.trakt.common.helpers.extensions.capitalize
import tv.trakt.trakt.common.helpers.extensions.fullDayFormat
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.yearMonthFormat
import tv.trakt.trakt.common.ui.theme.colors.Purple300
import tv.trakt.trakt.common.ui.theme.colors.Purple500
import tv.trakt.trakt.common.ui.theme.colors.Purple700
import tv.trakt.trakt.common.ui.theme.colors.Purple920
import tv.trakt.trakt.common.ui.theme.colors.Shade800
import tv.trakt.trakt.core.home.sections.streaks.model.MonthlyStreakData.StreakDataPoint
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.format.TextStyle as DayTextStyle

private val TileShape = RoundedCornerShape(10.dp)
private val TileGap = 6.dp

private val LegendSwatchShape = RoundedCornerShape(4.dp)
private val LegendSwatchSize = 14.dp

@Composable
internal fun StreaksMonthGrid(
    data: ImmutableMap<LocalDate, StreakDataPoint>,
    modifier: Modifier = Modifier,
    yearMonth: YearMonth = YearMonth.now(),
    showHeader: Boolean = true,
    showLegend: Boolean = true,
) {
    val locale = LocalLocale.current.platformLocale
    val scope = rememberCoroutineScope()

    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOffset = yearMonth.atDay(1).dayOfWeek.value - 1
    val rowCount = (firstDayOffset + daysInMonth + 6) / 7
    val today = LocalDate.now()
    val maxCount = data.values.maxOfOrNull { it.total } ?: 0
    val borderColor = Shade800
    val todayColor = Color.White

    Column(
        verticalArrangement = Arrangement.spacedBy(TileGap),
        modifier = modifier,
    ) {
        if (showHeader) {
            TraktHeader(
                title = yearMonth.format(yearMonthFormat()),
                titleStyle = TraktTheme.typography.heading6,
                modifier = Modifier
                    .padding(bottom = 12.dp),
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(TileGap),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 2.dp),
        ) {
            repeat(7) { col ->
                val dayOfWeek = DayOfWeek.of(col + 1)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = dayOfWeek
                            .getDisplayName(DayTextStyle.SHORT_STANDALONE, locale)
                            .capitalize(),
                        style = TraktTheme.typography.meta,
                        color = TraktTheme.colors.textSecondary,
                    )
                }
            }
        }

        repeat(rowCount) { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(TileGap),
                modifier = Modifier.fillMaxWidth(),
            ) {
                repeat(7) { col ->
                    val day = row * 7 + col - firstDayOffset + 1
                    val inMonth = day in 1..daysInMonth
                    val date = if (inMonth) yearMonth.atDay(day) else null
                    val isToday = date == today
                    val isPast = date?.isBefore(today) == true
                    val tappable = inMonth && (isToday || isPast)
                    val activityCount = if (inMonth) data[date]?.total ?: 0 else 0
                    val fillColor = when {
                        activityCount > 0 -> activityFillColor(activityCount, maxCount)
                        isPast -> borderColor
                        else -> Color.Transparent
                    }
                    val hasFill = activityCount > 0 || isPast
                    val tooltipState = rememberTooltipState(isPersistent = true)

                    if (inMonth) {
                        val tileBorderModifier = if (isToday || !hasFill) {
                            Modifier.border(
                                width = if (isToday) 1.75.dp else 1.dp,
                                color = if (isToday) todayColor else borderColor,
                                shape = TileShape,
                            )
                        } else {
                            Modifier
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f),
                        ) {
                            if (tappable) {
                                TooltipBox(
                                    state = tooltipState,
                                    positionProvider = rememberTooltipPositionProvider(
                                        positioning = TooltipAnchorPosition.Above,
                                    ),
                                    tooltip = {
                                        Box(
                                            modifier = Modifier
                                                .background(Shade800, RoundedCornerShape(8.dp))
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                        ) {
                                            val episodes = data[date]?.episodes ?: 0
                                            val movies = data[date]?.movies ?: 0

                                            val episodesText = stringResource(R.string.text_episodes_watched, episodes)
                                            val moviesText = stringResource(R.string.text_movies_watched, movies)

                                            val tooltipText = buildString {
                                                append(date.format(fullDayFormat()).capitalize())
                                                if (episodes > 0 || movies > 0) {
                                                    append("  •  ")
                                                }
                                                if (episodes > 0) {
                                                    append(episodesText)
                                                }
                                                if (movies > 0) {
                                                    if (episodes > 0) {
                                                        append(", ")
                                                    }
                                                    append(moviesText)
                                                }
                                            }

                                            Text(
                                                text = tooltipText,
                                                style = TraktTheme.typography.meta,
                                                color = Color.White,
                                            )
                                        }
                                    },
                                    enableUserInput = false,
                                    modifier = Modifier.fillMaxSize(),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(fillColor, TileShape)
                                            .then(tileBorderModifier)
                                            .onClick {
                                                scope.launch {
                                                    tooltipState.show()
                                                }
                                            },
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(fillColor, TileShape)
                                        .then(tileBorderModifier),
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f),
                        )
                    }
                }
            }
        }

        if (showLegend) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
            ) {
                Text(
                    text = "0",
                    style = TraktTheme.typography.meta,
                    color = TraktTheme.colors.textSecondary,
                    modifier = Modifier.padding(end = 2.dp),
                )

                LegendSwatch(color = borderColor)
                LegendSwatch(color = Purple920)
                LegendSwatch(color = Purple700)
                LegendSwatch(color = Purple500)
                LegendSwatch(color = Purple300)

                Text(
                    text = maxCount.toString(),
                    style = TraktTheme.typography.meta,
                    color = TraktTheme.colors.textSecondary,
                    modifier = Modifier.padding(start = 2.dp),
                )
            }
        }
    }
}

@Composable
private fun LegendSwatch(color: Color) {
    Box(
        modifier = Modifier
            .size(LegendSwatchSize)
            .background(color, LegendSwatchShape),
    )
}

@Composable
private fun activityFillColor(
    count: Int,
    maxCount: Int,
): Color {
    if (count <= 0 || maxCount <= 0) return Color.Transparent
    return when {
        count <= maxCount / 4 -> Purple920
        count <= maxCount / 2 -> Purple700
        count <= maxCount * 3 / 4 -> Purple500
        else -> Purple300
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF212427,
)
@Composable
private fun Preview() {
    TraktTheme {
        StreaksMonthGrid(
            yearMonth = YearMonth.of(2026, Month.JUNE),
            data = persistentMapOf(
                LocalDate.of(2026, Month.JUNE, 1) to StreakDataPoint(episodes = 1, movies = 0),
                LocalDate.of(2026, Month.JUNE, 2) to StreakDataPoint(episodes = 2, movies = 1),
                LocalDate.of(2026, Month.JUNE, 3) to StreakDataPoint(episodes = 5, movies = 2),
                LocalDate.of(2026, Month.JUNE, 4) to StreakDataPoint(episodes = 10, movies = 2),
                LocalDate.of(2026, Month.JUNE, 5) to StreakDataPoint(episodes = 2, movies = 0),
                LocalDate.of(2026, Month.JUNE, 6) to StreakDataPoint(episodes = 4, movies = 1),
                LocalDate.of(2026, Month.JUNE, 8) to StreakDataPoint(episodes = 0, movies = 0),
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF212427,
)
@Composable
private fun PreviewFebruary() {
    TraktTheme {
        StreaksMonthGrid(
            yearMonth = YearMonth.of(2026, Month.FEBRUARY),
            data = persistentMapOf(
                LocalDate.of(2026, Month.FEBRUARY, 2) to StreakDataPoint(episodes = 1, movies = 0),
                LocalDate.of(2026, Month.FEBRUARY, 3) to StreakDataPoint(episodes = 3, movies = 1),
                LocalDate.of(2026, Month.FEBRUARY, 4) to StreakDataPoint(episodes = 8, movies = 1),
                LocalDate.of(2026, Month.FEBRUARY, 5) to StreakDataPoint(episodes = 16, movies = 2),
                LocalDate.of(2026, Month.FEBRUARY, 9) to StreakDataPoint(episodes = 2, movies = 0),
                LocalDate.of(2026, Month.FEBRUARY, 10) to StreakDataPoint(episodes = 5, movies = 1),
                LocalDate.of(2026, Month.FEBRUARY, 11) to StreakDataPoint(episodes = 12, movies = 2),
                LocalDate.of(2026, Month.FEBRUARY, 16) to StreakDataPoint(episodes = 2, movies = 1),
                LocalDate.of(2026, Month.FEBRUARY, 17) to StreakDataPoint(episodes = 10, movies = 1),
                LocalDate.of(2026, Month.FEBRUARY, 25) to StreakDataPoint(episodes = 7, movies = 1),
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
