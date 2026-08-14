package tv.trakt.trakt.core.summary.shows.features.seasons.watcheduntil.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.W500
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tv.trakt.trakt.common.helpers.extensions.longDateFormat
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.timeFormat
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.core.summary.shows.features.seasons.watcheduntil.OtherDateBound
import tv.trakt.trakt.helpers.extensions.TraktThemeLightDark
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.Instant

@Composable
internal fun OtherDateBoundButtons(
    selected: OtherDateBound?,
    anchor: Instant?,
    enabled: Boolean,
    onBoundClick: (OtherDateBound) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dateFormat = longDateFormat()
    val timeFormat = timeFormat()

    val anchorText = anchor?.toLocal()?.let {
        "${it.format(dateFormat)} • ${it.format(timeFormat)}"
    }

    Row(
        horizontalArrangement = spacedBy(SegmentGap),
        modifier = modifier.fillMaxWidth(),
    ) {
        BoundSegment(
            text = stringResource(R.string.button_text_watch_until_here_anchor_start),
            subLabel = anchorText.takeIf { selected == OtherDateBound.Start },
            selected = selected == OtherDateBound.Start,
            enabled = enabled,
            shape = leadingSegmentShape(),
            onClick = { onBoundClick(OtherDateBound.Start) },
            modifier = Modifier.weight(1f),
        )
        BoundSegment(
            text = stringResource(R.string.button_text_watch_until_here_anchor_end),
            subLabel = anchorText.takeIf { selected == OtherDateBound.End },
            selected = selected == OtherDateBound.End,
            enabled = enabled,
            shape = trailingSegmentShape(),
            onClick = { onBoundClick(OtherDateBound.End) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun BoundSegment(
    text: String,
    subLabel: String?,
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

    val textColor = when {
        selected -> TraktTheme.colors.textPrimaryOnAccent
        else -> TraktTheme.colors.textPrimary
    }

    Column(
        verticalArrangement = spacedBy(2.dp, CenterVertically),
        modifier = modifier
            .height(SegmentHeight)
            .clip(shape)
            .background(containerColor)
            .onClick(enabled = enabled, onClick = onClick)
            .padding(horizontal = 8.dp),
    ) {
        Text(
            text = text,
            style = TraktTheme.typography.buttonPrimary,
            color = textColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        if (subLabel != null) {
            Text(
                text = subLabel,
                style = TraktTheme.typography.meta.copy(
                    fontSize = 10.sp,
                    fontWeight = W500,
                ),
                color = textColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF212427,
)
@Composable
private fun Preview() {
    TraktThemeLightDark {
        OtherDateBoundButtons(
            selected = OtherDateBound.Start,
            anchor = nowUtcInstant(),
            enabled = true,
            onBoundClick = {},
        )
    }
}
