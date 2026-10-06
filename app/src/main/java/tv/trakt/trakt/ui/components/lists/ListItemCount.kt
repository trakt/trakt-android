package tv.trakt.trakt.ui.components.lists

import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tv.trakt.trakt.common.helpers.extensions.rememberThousandsFormat
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun ListItemCount(
    count: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = TraktTheme.typography.cardSubtitle.copy(fontSize = 12.sp),
) {
    Row(
        horizontalArrangement = spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_shows_movies),
            contentDescription = null,
            tint = TraktTheme.colors.textSecondary,
            modifier = Modifier.size(11.dp),
        )
        Text(
            text = rememberThousandsFormat(count),
            style = style,
            color = TraktTheme.colors.textSecondary,
            maxLines = 1,
        )
    }
}

@Preview
@Composable
private fun Preview() {
    TraktTheme {
        ListItemCount(count = 1234)
    }
}
