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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap
import tv.trakt.trakt.common.ui.theme.colors.Shade800
import tv.trakt.trakt.core.profile.sections.screentime.StatCardSize
import tv.trakt.trakt.core.profile.sections.screentime.model.ScreenTimeData.PeakHour
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme
import kotlin.random.Random

@Composable
internal fun ScreenTimePeakHoursCard(
    modifier: Modifier = Modifier,
    data: ImmutableMap<PeakHour, Int>,
    containerColor: Color = TraktTheme.colors.dialogContainer,
) {
    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .background(
                color = containerColor,
                shape = RoundedCornerShape(16.dp),
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

        val total = data.maxOfOrNull { it.value } ?: 0
        PeakHour.entries.forEach { peakHour ->
            Row(
                verticalAlignment = CenterVertically,
                horizontalArrangement = SpaceEvenly,
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
                        val current = data[peakHour] ?: 0
                        when {
                            total > 0 -> current.toFloat() / total
                            else -> 0F
                        }.coerceAtLeast(0.01F)
                    },
                    gapSize = 2.dp,
                    color = TraktTheme.colors.accent,
                    trackColor = Shade800,
                    drawStopIndicator = { },
                    modifier = Modifier.weight(5F),
                )

                Text(
                    text = "${data[peakHour] ?: 0}",
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
}

@Preview(widthDp = 300)
@Composable
private fun ScreenTimePeakHoursCardPreview() {
    TraktTheme {
        ScreenTimePeakHoursCard(
            data = PeakHour.entries
                .associateWith { Random.nextInt(20) }
                .toImmutableMap(),
            modifier = Modifier
                .height(StatCardSize)
                .padding(16.dp),
        )
    }
}
