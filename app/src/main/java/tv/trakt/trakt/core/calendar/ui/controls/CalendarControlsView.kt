package tv.trakt.trakt.core.calendar.ui.controls

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.W800
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import tv.trakt.trakt.common.helpers.extensions.nowLocalDay
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.ui.theme.colors.Purple400
import tv.trakt.trakt.core.calendar.model.CalendarItem
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
    availableItems: ImmutableMap<LocalDate, ImmutableList<CalendarItem>>? = null,
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
            .shadow(6.dp, shape = shape)
            .background(
                color = TraktTheme.colors.dialogContainer,
                shape = shape,
            )
            .padding(top = 16.dp, bottom = 12.dp)
            .padding(horizontal = 12.dp),
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
                horizontalArrangement = spacedBy(8.dp),
                verticalAlignment = CenterVertically,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_right),
                    tint = TraktTheme.colors.textPrimary,
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(180F)
                        .onClick(throttle = false) {
                            onPreviousWeekClick()
                        },
                )
                GhostButton(
                    text = stringResource(R.string.text_today),
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
                        .size(24.dp)
                        .onClick(throttle = false) {
                            onNextWeekClick()
                        },
                )
            }
        }

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

                val episodes = remember(availableItems, date) {
                    availableItems?.get(date)
                        ?.filterIsInstance<CalendarItem.EpisodeItem>()
                        ?.size
                }

                val movies = remember(availableItems, date) {
                    availableItems?.get(date)
                        ?.filterIsInstance<CalendarItem.MovieItem>()
                        ?.size
                }

                DayRowItem(
                    enabled = enabled,
                    itemDate = date,
                    focusedDate = focusedDate,
                    availableDates = availableDates,
                    episodesCount = episodes ?: 0,
                    moviesCount = movies ?: 0,
                    onDayClick = onDayClick,
                    modifier = Modifier.weight(1F),
                )
            }
        }
    }
}

@Composable
private fun DayRowItem(
    enabled: Boolean,
    itemDate: LocalDate,
    focusedDate: LocalDate?,
    availableDates: ImmutableSet<LocalDate>?,
    episodesCount: Int,
    moviesCount: Int,
    onDayClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isToday = remember(itemDate) {
        itemDate == LocalDate.now()
    }

    val isAvailable = remember(itemDate, availableDates) {
        availableDates?.contains(itemDate) ?: false
    }

    val dayAvailable = enabled && isAvailable
    val dayFocused = enabled && (focusedDate == itemDate)

    val animatedColor by animateColorAsState(
        targetValue = when {
            dayFocused && dayAvailable -> TraktTheme.colors.dialogContent
            else -> TraktTheme.colors.dialogContainer
        },
        animationSpec = tween(200),
    )

    Column(
        horizontalAlignment = CenterHorizontally,
        verticalArrangement = spacedBy(4.dp),
        modifier = modifier
            .background(
                color = animatedColor,
                shape = RoundedCornerShape(10.dp),
            )
            .border(
                width = if (isToday) 1.dp else 0.dp,
                color = when {
                    isToday -> Purple400
                    else -> Color.Transparent
                },
                shape = RoundedCornerShape(10.dp),
            )
            .padding(
                vertical = 6.dp,
                horizontal = 2.dp,
            )
            .onClick(enabled = dayAvailable) {
                onDayClick(itemDate)
            },
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = CenterVertically,
        ) {
            Text(
                text = itemDate.dayOfWeek
                    .getDisplayName(TextStyle.SHORT, Locale.US),
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.meta.copy(
                    fontSize = 12.sp,
                ),
                maxLines = 1,
                modifier = Modifier
                    .alpha(if (dayAvailable) 1F else 0.25F),
            )
        }

        Text(
            text = itemDate.dayOfMonth.toString(),
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.meta.copy(
                fontSize = 14.sp,
                fontWeight = W800,
            ),
            maxLines = 1,
            modifier = Modifier
                .alpha(if (dayAvailable) 1F else 0.25F),
        )

        Text(
            text = itemDate.month
                .getDisplayName(TextStyle.SHORT, Locale.US),
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.meta.copy(
                fontSize = 12.sp,
            ),
            maxLines = 1,
            modifier = Modifier
                .alpha(if (dayAvailable) 1F else 0.25F),
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = CenterVertically,
            modifier = Modifier
                .heightIn(min = 12.dp),
        ) {
            if (episodesCount > 0) {
                Icon(
                    painter = painterResource(R.drawable.ic_shows_off),
                    tint = TraktTheme.colors.textSecondary,
                    contentDescription = null,
                    modifier = Modifier.size(9.dp),
                )
            }
            if (moviesCount > 0) {
                Icon(
                    painter = painterResource(R.drawable.ic_movies_off),
                    tint = TraktTheme.colors.textSecondary,
                    contentDescription = null,
                    modifier = Modifier.size(9.dp),
                )
            }
            val restCount = (episodesCount + moviesCount) - when {
                episodesCount > 0 && moviesCount > 0 -> 2
                episodesCount > 0 || moviesCount > 0 -> 1
                else -> 0
            }

            if (restCount > 0) {
                Text(
                    text = "+$restCount",
                    color = TraktTheme.colors.textSecondary,
                    style = TraktTheme.typography.meta.copy(
                        fontSize = 10.sp,
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
