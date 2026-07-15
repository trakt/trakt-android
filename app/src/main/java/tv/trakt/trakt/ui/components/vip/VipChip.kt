package tv.trakt.trakt.ui.components.vip

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun VipChip(
    modifier: Modifier = Modifier,
    text: String = " VIP ",
    icon: Painter? = null,
    color: Color = TraktTheme.colors.vipAccent,
    onClick: () -> Unit = {},
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(4.dp, Alignment.CenterHorizontally),
        modifier = modifier
            .clip(RoundedCornerShape(100))
            .onClick(onClick = onClick)
            .background(
                color = color,
            )
            .padding(
                horizontal = if (icon != null) 8.dp else 10.dp,
                vertical = 5.dp,
            ),
    ) {
        icon?.let {
            Icon(
                painter = it,
                contentDescription = null,
                tint = TraktTheme.colors.chipContent,
                modifier = Modifier
                    .size(18.dp)
                    .align(Alignment.CenterVertically),
            )
        }
        Text(
            text = text.uppercase(),
            style = TraktTheme.typography.buttonPrimary,
            color = TraktTheme.colors.chipContent,
            maxLines = 1,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(
                    end = if (icon != null) 2.dp else 0.dp,
                ),
        )
    }
}

@Preview
@Composable
private fun Preview() {
    TraktTheme {
        VipChip()
    }
}

@Preview
@Composable
private fun Preview2() {
    TraktTheme {
        VipChip(
            text = stringResource(R.string.badge_text_get_vip),
            icon = painterResource(R.drawable.ic_stars),
        )
    }
}
