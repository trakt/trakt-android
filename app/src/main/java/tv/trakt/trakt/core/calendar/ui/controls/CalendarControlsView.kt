package tv.trakt.trakt.core.calendar.ui.controls

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.W800
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import tv.trakt.trakt.common.helpers.extensions.nowLocalDay
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.ui.theme.colors.Purple400
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.buttons.GhostButton
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
internal fun CalendarControlsView(
    startDate: LocalDate,
    modifier: Modifier = Modifier,
    focusedDate: LocalDate? = null,
    availableDates: ImmutableSet<LocalDate>? = null,
    enabled: Boolean = false,
    onDayClick: (LocalDate) -> Unit = {},
    onTodayClick: () -> Unit = {},
    onNextWeekClick: () -> Unit = {},
    onPreviousWeekClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
) {
    val shape = RoundedCornerShape(24.dp)
    Column(
        modifier = modifier
            .shadow(4.dp, shape = shape)
            .background(
                color = TraktTheme.colors.dialogContainer,
                shape = shape,
            )
            .padding(top = 4.dp)
            .padding(
                horizontal = 12.dp,
                vertical = 12.dp,
            ),
    ) {
        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .padding(start = 2.dp)
                .fillMaxWidth(),
        ) {
            Row(
                verticalAlignment = CenterVertically,
                horizontalArrangement = spacedBy(12.dp),
                modifier = Modifier
                    .onClick(onClick = onBackClick),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_back_arrow),
                    tint = TraktTheme.colors.textPrimary,
                    contentDescription = null,
                )
                TraktHeader(
                    title = stringResource(R.string.page_title_calendar),
                )
            }

            Row(
                horizontalArrangement = spacedBy(12.dp),
                verticalAlignment = CenterVertically,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_right),
                    tint = TraktTheme.colors.textPrimary,
                    contentDescription = null,
                    modifier = Modifier
                        .rotate(180F)
                        .onClick(throttle = false) {
                            onPreviousWeekClick()
                        },
                )
                GhostButton(
                    text = "Today", // TODO string resource
                    onClick = { onTodayClick() },
                    fillWidth = false,
                    uppercase = false,
                    modifier = Modifier
                        .height(24.dp),
                )
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_right),
                    tint = TraktTheme.colors.textPrimary,
                    contentDescription = null,
                    modifier = Modifier
                        .onClick(throttle = false) {
                            onNextWeekClick()
                        },
                )
            }
        }

        DaysRow(
            enabled = enabled,
            startDate = startDate,
            focusedDate = focusedDate,
            availableDates = availableDates,
            onDayClick = onDayClick,
        )
    }
}

@Composable
private fun DaysRow(
    enabled: Boolean,
    startDate: LocalDate,
    focusedDate: LocalDate?,
    availableDates: ImmutableSet<LocalDate>?,
    onDayClick: (LocalDate) -> Unit,
) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = spacedBy(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
    ) {
        for (day in 0..6) {
            val date = remember(startDate) {
                startDate.plusDays(day.toLong())
            }

            val isToday = remember(date) {
                date == LocalDate.now()
            }

            val isAvailable = remember(date, availableDates) {
                availableDates?.contains(date) ?: false
            }

            val dayAvailable = enabled && isAvailable
            val dayFocused = enabled && (focusedDate == date)

            Column(
                horizontalAlignment = CenterHorizontally,
                verticalArrangement = spacedBy(6.dp),
                modifier = Modifier
                    .weight(1F)
                    .background(
                        color = when {
                            dayFocused && dayAvailable -> TraktTheme.colors.dialogContent
                            else -> Color.Transparent
                        },
                        shape = RoundedCornerShape(10.dp),
                    )
                    .padding(
                        vertical = 8.dp,
                        horizontal = 2.dp,
                    )
                    .alpha(if (dayAvailable) 1F else 0.25F)
                    .onClick(enabled = dayAvailable) {
                        onDayClick(date)
                    },
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = CenterVertically,
                ) {
                    if (isToday) {
                        Box(
                            modifier = Modifier
                                .graphicsLayer {
                                    translationY = 0.5.dp.toPx()
                                }
                                .background(color = Purple400, shape = CircleShape)
                                .size(5.dp),
                        )
                    }

                    Text(
                        text = date.dayOfWeek
                            .getDisplayName(TextStyle.SHORT, Locale.US),
                        color = TraktTheme.colors.textPrimary,
                        style = TraktTheme.typography.meta.copy(
                            fontSize = 12.sp,
                        ),
                        maxLines = 1,
                    )
                }

                Text(
                    text = date.dayOfMonth.toString(),
                    color = TraktTheme.colors.textPrimary,
                    style = TraktTheme.typography.meta.copy(
                        fontSize = 14.sp,
                        fontWeight = W800,
                    ),
                    maxLines = 1,
                )

                Text(
                    text = date.month
                        .getDisplayName(TextStyle.SHORT, Locale.US),
                    color = TraktTheme.colors.textPrimary,
                    style = TraktTheme.typography.meta.copy(
                        fontSize = 12.sp,
                    ),
                    maxLines = 1,
                )
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    val now = LocalDate.now()
    TraktTheme {
        CalendarControlsView(
            startDate = now.minusDays(3L),
            focusedDate = now,
        )
    }
}

@Preview
@Composable
private fun Preview2() {
    val today = LocalDate.now()
    TraktTheme {
        CalendarControlsView(
            startDate = today.minusDays(3L),
            focusedDate = today,
            enabled = true,
            availableDates = persistentSetOf(today),
        )
    }
}

@Preview
@Composable
private fun Preview3() {
    TraktTheme {
        val now = nowLocalDay()
        CalendarControlsView(
            startDate = now.minusDays(3L),
            focusedDate = now,
            enabled = true,
            availableDates = persistentSetOf(
                now.minusDays(2L),
                now,
                now.plusDays(2L),
            ),
        )
    }
}
