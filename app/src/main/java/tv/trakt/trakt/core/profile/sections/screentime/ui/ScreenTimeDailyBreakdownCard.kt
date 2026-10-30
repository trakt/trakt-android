package tv.trakt.trakt.core.profile.sections.screentime.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.rememberDurationFormat
import tv.trakt.trakt.common.ui.theme.colors.HalloweenDarkColors
import tv.trakt.trakt.core.profile.sections.screentime.StatWideCardWidth
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.AbstractMap.SimpleImmutableEntry
import java.util.Locale
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

private const val MIN_BAR_FRACTION = 0.06F
private const val BAR_WIDTH_FRACTION = 0.64F
private const val FALLBACK_DAYS = 7

private val cardShape = RoundedCornerShape(16.dp)

private val barCornerRadius = 6.dp
private val barShape = RoundedCornerShape(barCornerRadius)
private val columnSpacing = 2.dp
private val ringWidth = 1.dp
private val ringOffset = 1.5.dp

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
    val peakIndex = remember(days, maxMinutes) {
        days.indexOfFirst { it.value.inWholeMinutes == maxMinutes }.takeIf { maxMinutes > 0 }
    }

    var activeIndex by remember(days) { mutableStateOf<Int?>(null) }
    val shownIndex = activeIndex ?: peakIndex

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
            verticalArrangement = spacedBy(5.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1F),
        ) {
            ValueTrack(
                text = rememberDurationFormat(
                    duration = shownIndex?.let { days[it].value.inWholeMinutes } ?: 0L,
                    spaces = true,
                ),
                visible = shownIndex != null,
                columnIndex = shownIndex ?: 0,
                columnCount = days.size,
            )

            Row(
                horizontalArrangement = spacedBy(columnSpacing),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1F),
            ) {
                days.forEachIndexed { index, (date, duration) ->
                    DayColumn(
                        label = when (date) {
                            today -> stringResource(R.string.text_stats_today)
                            else -> date.dayOfWeek.getDisplayName(TextStyle.SHORT_STANDALONE, locale)
                        },
                        minutes = duration.inWholeMinutes,
                        maxMinutes = maxMinutes,
                        active = index == activeIndex,
                        onClick = {
                            activeIndex = if (activeIndex == index) null else index
                        },
                        modifier = Modifier
                            .weight(1F)
                            .fillMaxHeight(),
                    )
                }
            }
        }
    }
}

@Composable
private fun ValueTrack(
    text: String,
    visible: Boolean,
    columnIndex: Int,
    columnCount: Int,
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth(),
    ) {
        val columnWidth = (maxWidth - columnSpacing * (columnCount - 1)) / columnCount
        val center by animateDpAsState(
            targetValue = (columnWidth + columnSpacing) * columnIndex + columnWidth / 2,
            label = "valueCenter",
        )
        val alpha by animateFloatAsState(
            targetValue = if (visible) 1F else 0F,
            label = "valueAlpha",
        )

        Text(
            text = text,
            style = TraktTheme.typography.cardTitle.copy(fontSize = 10.sp),
            color = TraktTheme.colors.textPrimary,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints.copy(minWidth = 0))
                    val maxX = (constraints.maxWidth - placeable.width).coerceAtLeast(0)
                    val x = (center.roundToPx() - placeable.width / 2).coerceIn(0, maxX)
                    layout(constraints.maxWidth, placeable.height) {
                        placeable.place(x, 0)
                    }
                }
                .alpha(alpha),
        )
    }
}

@Composable
private fun DayColumn(
    label: String,
    minutes: Long,
    maxMinutes: Long,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val strength = when {
        maxMinutes > 0 -> minutes.toFloat() / maxMinutes
        else -> 0F
    }
    val ringColor by animateColorAsState(
        targetValue = when {
            active -> TraktTheme.colors.textPrimary
            else -> Color.Transparent
        },
        label = "ringColor",
    )

    Column(
        horizontalAlignment = CenterHorizontally,
        verticalArrangement = spacedBy(4.dp),
        modifier = modifier.onClick(throttle = false, onClick = onClick),
    ) {
        Box(
            contentAlignment = BottomCenter,
            modifier = Modifier
                .fillMaxWidth(BAR_WIDTH_FRACTION)
                .weight(1F)
                .drawBehind {
                    if (ringColor.alpha == 0F) return@drawBehind
                    val outset = (ringOffset + ringWidth / 2).toPx()
                    drawRoundRect(
                        color = ringColor,
                        topLeft = Offset(-outset, -outset),
                        size = Size(size.width + outset * 2, size.height + outset * 2),
                        cornerRadius = CornerRadius(barCornerRadius.toPx() + outset),
                        style = Stroke(width = ringWidth.toPx()),
                    )
                }
                .background(
                    color = TraktTheme.colors.chipContainer.copy(
                        alpha = 0.5F,
                    ),
                    shape = barShape,
                ),
        ) {
            if (minutes > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(strength.coerceAtLeast(MIN_BAR_FRACTION))
                        .background(
                            color = lerp(
                                start = TraktTheme.colors.screenTimeHigh,
                                stop = TraktTheme.colors.screenTimeMedium,
                                fraction = strength,
                            ),
                            shape = barShape,
                        ),
                )
            }
        }

        Text(
            text = label,
            style = TraktTheme.typography.cardTitle.copy(fontSize = 10.sp),
            color = TraktTheme.colors.textPrimary,
            maxLines = 1,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun configurationLocale(): Locale {
    val configuration = LocalConfiguration.current
    return remember(configuration) {
        configuration.locales.get(0) ?: Locale.getDefault()
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
private fun HalloweenPreview() {
    val base = LocalDate.now().minusDays(6)
    val sample = listOf(108L, 145L, 51L, 51L, 51L, 320L, 95L)
        .mapIndexed { index, mins -> base.plusDays(index.toLong()) to mins.minutes }
        .toMap()
        .toImmutableMap()

    TraktTheme(colors = HalloweenDarkColors) {
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
