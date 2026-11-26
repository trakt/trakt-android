package tv.trakt.trakt.core.calendar.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.core.calendar.model.CalendarView
import tv.trakt.trakt.ui.components.HintIcon
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun CalendarViewToggle(
    current: CalendarView,
    modifier: Modifier = Modifier,
    attention: Boolean = false,
    onViewClick: (CalendarView) -> Unit = {},
) {
    val target = current.toggle

    HintIcon(
        painter = painterResource(target.iconRes),
        attention = attention,
        size = 25.dp,
        modifier = modifier.onClick { onViewClick(target) },
    )
}

@Preview
@Composable
private fun Preview() {
    TraktTheme {
        Row {
            CalendarViewToggle(current = CalendarView.Weekly)
            CalendarViewToggle(current = CalendarView.Monthly, attention = true)
        }
    }
}
