package tv.trakt.trakt.core.summary.shows.features.seasons.watcheduntil.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.core.summary.shows.features.seasons.watcheduntil.WatchedUntilAction
import tv.trakt.trakt.core.summary.shows.features.seasons.watcheduntil.WatchedUntilAction.Now
import tv.trakt.trakt.core.summary.shows.features.seasons.watcheduntil.WatchedUntilAction.OtherDate
import tv.trakt.trakt.core.summary.shows.features.seasons.watcheduntil.WatchedUntilAction.ReleaseDate
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun ActionButtons(
    selected: WatchedUntilAction,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onNowClick: () -> Unit = {},
    onReleaseClick: () -> Unit = {},
    onOtherClick: () -> Unit = {},
) {
    Column(
        verticalArrangement = spacedBy(SegmentGap),
        modifier = modifier,
    ) {
        ActionSegment(
            text = stringResource(R.string.button_text_mark_as_watched_now),
            icon = painterResource(R.drawable.ic_check),
            selected = selected == Now,
            enabled = enabled,
            shape = SegmentPosition.Top.shape(),
            onClick = onNowClick,
        )
        ActionSegment(
            text = stringResource(R.string.button_text_mark_as_watched_release_date),
            icon = painterResource(R.drawable.ic_calendar_time_trakt),
            selected = selected == ReleaseDate,
            enabled = enabled,
            shape = SegmentPosition.Middle.shape(),
            onClick = onReleaseClick,
        )
        ActionSegment(
            text = stringResource(R.string.button_text_mark_as_watched_other_date),
            icon = painterResource(R.drawable.ic_edit),
            selected = selected == OtherDate,
            enabled = enabled,
            shape = SegmentPosition.Bottom.shape(),
            onClick = onOtherClick,
        )
    }
}

@Composable
private fun ActionSegment(
    text: String,
    icon: Painter,
    selected: Boolean,
    enabled: Boolean,
    shape: Shape,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor = when {
        selected -> TraktTheme.colors.accent
        else -> TraktTheme.colors.primaryButtonContainerDisabled
    }

    Row(
        horizontalArrangement = spacedBy(12.dp),
        verticalAlignment = CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .height(SegmentHeight)
            .clip(shape)
            .background(containerColor)
            .onClick(
                enabled = enabled,
                onClick = onClick,
            )
            .padding(horizontal = 16.dp),
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = TraktTheme.colors.textPrimary,
            modifier = Modifier.size(21.dp),
        )
        Text(
            text = text,
            style = TraktTheme.typography.buttonPrimary,
            color = TraktTheme.colors.textPrimary,
        )
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
        ActionButtons(
            selected = OtherDate,
            enabled = true,
        )
    }
}
