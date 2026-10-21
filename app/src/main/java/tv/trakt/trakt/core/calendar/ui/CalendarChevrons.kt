package tv.trakt.trakt.core.calendar.ui

import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

private val GlyphEdgeOffset = 2.dp

@Composable
internal fun CalendarChevrons(
    modifier: Modifier = Modifier,
    onPreviousClick: () -> Unit = {},
    onNextClick: () -> Unit = {},
) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = spacedBy(16.dp),
        modifier = modifier.offset(x = GlyphEdgeOffset),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_chevron_right),
            tint = TraktTheme.colors.textPrimary,
            contentDescription = null,
            modifier = Modifier
                .size(24.dp)
                .rotate(180F)
                .onClick(throttle = false, onClick = onPreviousClick),
        )
        Icon(
            painter = painterResource(R.drawable.ic_chevron_right),
            tint = TraktTheme.colors.textPrimary,
            contentDescription = null,
            modifier = Modifier
                .size(24.dp)
                .onClick(throttle = false, onClick = onNextClick),
        )
    }
}

@Preview
@Composable
private fun Preview() {
    TraktTheme {
        CalendarChevrons()
    }
}
