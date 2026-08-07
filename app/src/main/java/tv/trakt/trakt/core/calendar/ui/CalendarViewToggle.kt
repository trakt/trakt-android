package tv.trakt.trakt.core.calendar.ui

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.core.calendar.model.CalendarView
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun CalendarViewToggle(
    current: CalendarView,
    modifier: Modifier = Modifier,
    onViewClick: (CalendarView) -> Unit = {},
) {
    // Shows the layout it switches to, not the one on screen.
    val target = current.toggle

    Icon(
        painter = painterResource(target.iconRes),
        contentDescription = stringResource(target.textRes),
        tint = TraktTheme.colors.textPrimary,
        modifier = modifier
            .size(
                when (target) {
                    CalendarView.Weekly -> 22.dp
                    CalendarView.Monthly -> 23.dp
                },
            )
            .onClick { onViewClick(target) },
    )
}

@Preview
@Composable
private fun Preview() {
    TraktTheme {
        CalendarViewToggle(current = CalendarView.Weekly)
    }
}
