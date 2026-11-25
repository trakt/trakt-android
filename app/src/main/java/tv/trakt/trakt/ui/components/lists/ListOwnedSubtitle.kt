package tv.trakt.trakt.ui.components.lists

import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.W500
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun ListOwnedSubtitle(
    itemCount: Int?,
    modifier: Modifier = Modifier,
) {
    val style = TraktTheme.typography.cardSubtitle.copy(fontSize = 12.sp)

    Row(
        horizontalArrangement = spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        Text(
            text = stringResource(R.string.tag_text_owned_list),
            style = style.copy(fontWeight = W500),
            color = TraktTheme.colors.textPrimary,
            maxLines = 1,
        )

        itemCount?.let {
            ListItemCount(
                count = it,
                style = style,
                modifier = Modifier.padding(start = 6.dp),
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    TraktTheme {
        ListOwnedSubtitle(itemCount = 42)
    }
}
