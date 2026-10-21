@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.profile.sections.screentime.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.SpaceEvenly
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults.rememberTooltipPositionProvider
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.launch
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.profile.ScreenTimeData.PeakHour
import tv.trakt.trakt.common.model.profile.ScreenTimeData.PeakHourCount
import tv.trakt.trakt.core.profile.sections.screentime.StatCardSize
import tv.trakt.trakt.helpers.extensions.TraktThemeLightDark
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme
import kotlin.random.Random

private val cardShape = RoundedCornerShape(16.dp)
private val tooltipShape = RoundedCornerShape(8.dp)

@Composable
internal fun ScreenTimePeakHoursCard(
    modifier: Modifier = Modifier,
    data: ImmutableMap<PeakHour, PeakHourCount>,
    containerColor: Color = TraktTheme.colors.dialogContainer,
) {
    Column(
        verticalArrangement = Arrangement.SpaceBetween,
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
            text = stringResource(R.string.header_stats_graph_peak),
            style = TraktTheme.typography.cardSubtitle,
            color = TraktTheme.colors.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        val max = data.maxOfOrNull { it.value.total } ?: 0
        PeakHour.entries.forEach { peakHour ->
            PeakHourRow(
                peakHour = peakHour,
                count = data[peakHour] ?: PeakHourCount(movies = 0, episodes = 0),
                max = max,
            )
        }
    }
}

@Composable
private fun PeakHourRow(
    peakHour: PeakHour,
    count: PeakHourCount,
    max: Int,
) {
    val scope = rememberCoroutineScope()
    val tooltipState = rememberTooltipState(isPersistent = true)

    TooltipBox(
        state = tooltipState,
        positionProvider = rememberTooltipPositionProvider(
            positioning = TooltipAnchorPosition.Above,
        ),
        tooltip = {
            Text(
                text = stringResource(R.string.text_stats_movies_count, count.movies) +
                    " · " +
                    stringResource(R.string.text_stats_episodes_count, count.episodes),
                style = TraktTheme.typography.meta,
                color = TraktTheme.colors.tooltipContent,
                modifier = Modifier
                    .background(TraktTheme.colors.tooltipContainer, tooltipShape)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            )
        },
        enableUserInput = false,
    ) {
        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = SpaceEvenly,
            modifier = Modifier.onClick {
                scope.launch {
                    tooltipState.show()
                }
            },
        ) {
            Text(
                text = stringResource(peakHour.displayRes),
                style = TraktTheme.typography.cardTitle.copy(
                    fontSize = 10.sp,
                ),
                color = TraktTheme.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(3F)
                    .padding(end = 8.dp),
            )

            LinearProgressIndicator(
                progress = {
                    when {
                        max > 0 -> count.total.toFloat() / max
                        else -> 0F
                    }.coerceAtLeast(0.01F)
                },
                gapSize = 2.dp,
                color = TraktTheme.colors.accent,
                trackColor = TraktTheme.colors.chipContainer,
                drawStopIndicator = { },
                modifier = Modifier.weight(5F),
            )

            Text(
                text = "${count.total}",
                style = TraktTheme.typography.cardTitle.copy(
                    fontSize = 10.sp,
                ),
                color = TraktTheme.colors.textPrimary,
                maxLines = 1,
                textAlign = TextAlign.End,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .width(40.dp)
                    .padding(start = 16.dp),
            )
        }
    }
}

@Preview(widthDp = 300)
@Composable
private fun ScreenTimePeakHoursCardPreview() {
    TraktThemeLightDark {
        ScreenTimePeakHoursCard(
            data = PeakHour.entries
                .associateWith { PeakHourCount(movies = Random.nextInt(10), episodes = Random.nextInt(20)) }
                .toImmutableMap(),
            modifier = Modifier
                .height(StatCardSize)
                .padding(16.dp),
        )
    }
}
