package tv.trakt.trakt.app.core.details.lists

import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight.Companion.W700
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.helpers.extensions.rememberThousandsFormat
import tv.trakt.trakt.common.model.lists.CustomList
import tv.trakt.trakt.resources.R

@Composable
internal fun ListAuthorSubtitle(
    userName: String,
    itemCount: Int?,
    modifier: Modifier = Modifier,
    style: TextStyle = TraktTheme.typography.paragraphSmall,
) {
    Row(
        horizontalArrangement = spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        Text(
            text = stringResource(R.string.text_by),
            style = style,
            color = TraktTheme.colors.textSecondary,
            maxLines = 1,
        )
        Text(
            text = userName,
            style = style.copy(fontWeight = W700),
            color = TraktTheme.colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1F, fill = false),
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

@Composable
internal fun ListPrivacySubtitle(
    privacy: CustomList.Privacy?,
    itemCount: Int?,
    modifier: Modifier = Modifier,
    style: TextStyle = TraktTheme.typography.paragraphSmall,
) {
    Row(
        horizontalArrangement = spacedBy(4.dp),
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

@Composable
private fun ListItemCount(
    count: Int,
    style: TextStyle,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_shows_movies),
            contentDescription = null,
            tint = TraktTheme.colors.textSecondary,
            modifier = Modifier.size(12.dp),
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
private fun ListSubtitlePreview() {
    TraktTheme {
        Column(verticalArrangement = spacedBy(8.dp)) {
            ListAuthorSubtitle(
                userName = "Trakt",
                itemCount = 42,
            )
            ListPrivacySubtitle(
                privacy = CustomList.Privacy.Private,
                itemCount = 1234,
            )
        }
    }
}
