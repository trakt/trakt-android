package tv.trakt.trakt.app.common.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import tv.trakt.trakt.app.ui.theme.TraktTheme

@Composable
internal fun TvVipChip(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(2.dp),
        modifier = modifier
            .background(
                shape = RoundedCornerShape(100),
                color = Color.Red,
            )
            .padding(
                horizontal = 6.dp,
                vertical = 2.dp,
            ),
    ) {
        Text(
            text = "VIP",
            style = TraktTheme.typography.meta,
            color = TraktTheme.colors.chipContent,
            maxLines = 1,
        )
    }
}

@Preview
@Composable
private fun Preview() {
    TraktTheme {
        TvVipChip()
    }
}
