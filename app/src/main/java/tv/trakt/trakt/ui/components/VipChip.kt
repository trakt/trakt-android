package tv.trakt.trakt.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun VipChip(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(5.dp),
        modifier = modifier
            .clip(RoundedCornerShape(100))
            .clickable(onClick = onClick)
            .background(
                color = TraktTheme.colors.vipAccent,
            )
            .padding(
                horizontal = 10.dp,
                vertical = 5.dp,
            ),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_crown),
            contentDescription = "VIP",
            tint = TraktTheme.colors.chipContent,
            modifier = Modifier
                .size(17.dp)
                .graphicsLayer {
                    translationY = -(0.5).dp.toPx()
                },
        )

        Text(
            text = stringResource(R.string.badge_text_get_vip).uppercase(),
            style = TraktTheme.typography.buttonPrimary,
            color = TraktTheme.colors.chipContent,
            maxLines = 1,
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
