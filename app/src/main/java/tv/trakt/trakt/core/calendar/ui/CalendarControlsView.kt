package tv.trakt.trakt.core.calendar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight.Companion.W800
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.resources.R
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
    enabled: Boolean = false,
    onTodayClick: () -> Unit = {},
    onNextWeekClick: () -> Unit = {},
    onPreviousWeekClick: () -> Unit = {},
) {
    val shape = RoundedCornerShape(24.dp)
    Column(
        modifier = modifier
            .shadow(4.dp, shape = shape)
            .background(
                color = TraktTheme.colors.dialogContainer,
                shape = shape,
            )
            .padding(
                horizontal = 12.dp,
                vertical = 12.dp,
            ),
    ) {
        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth(),
        ) {
//            Text(
//                text = "Weekly",
//                color = TraktTheme.colors.textPrimary,
//                style = TraktTheme.typography.heading5,
//                maxLines = 1,
//            )

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
        )
    }
}

@Composable
private fun DaysRow(
    enabled: Boolean,
    startDate: LocalDate,
    focusedDate: LocalDate?,
) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = spacedBy(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1F else 0.25F)
            .padding(top = 16.dp),
    ) {
        for (day in 0..6) {
            val date = startDate.plusDays(day.toLong())

            Column(
                horizontalAlignment = CenterHorizontally,
                verticalArrangement = spacedBy(6.dp),
                modifier = Modifier
                    .weight(1F)
                    .background(
                        color = when {
                            focusedDate == date && enabled -> TraktTheme.colors.dialogContent
                            else -> Color.Transparent
                        },
                        shape = RoundedCornerShape(10.dp),
                    )
                    .padding(
                        vertical = 8.dp,
                        horizontal = 2.dp,
                    ),
            ) {
                Text(
                    text = date.dayOfWeek
                        .getDisplayName(TextStyle.SHORT, Locale.US),
                    color = TraktTheme.colors.textPrimary,
                    style = TraktTheme.typography.meta.copy(
                        fontSize = 12.sp,
                    ),
                    maxLines = 1,
                )

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
    TraktTheme {
        CalendarControlsView(
            startDate = LocalDate.now().minusDays(3L),
            focusedDate = LocalDate.now(),
        )
    }
}

@Preview
@Composable
private fun Preview2() {
    TraktTheme {
        CalendarControlsView(
            startDate = LocalDate.now().minusDays(3L),
            focusedDate = LocalDate.now(),
            enabled = true,
        )
    }
}
