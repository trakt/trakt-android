package tv.trakt.trakt.ui.components.lists

import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tv.trakt.trakt.common.model.lists.CustomList
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun ListPrivacySubtitle(
    privacy: CustomList.Privacy?,
    itemCount: Int?,
    modifier: Modifier = Modifier,
) {
    val style = TraktTheme.typography.cardSubtitle.copy(fontSize = 12.sp)

    Row(
        horizontalArrangement = spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        privacy?.let {
            Text(
                text = stringResource(it.displayRes),
                style = style,
                color = TraktTheme.colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1F, fill = false),
            )
        }

        itemCount?.let {
            ListItemCount(
                count = it,
                style = style,
                modifier = Modifier.padding(start = if (privacy != null) 6.dp else 0.dp),
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    TraktTheme {
        ListPrivacySubtitle(
            privacy = CustomList.Privacy.Private,
            itemCount = 42,
        )
    }
}
