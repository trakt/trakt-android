package tv.trakt.trakt.core.search.views.items

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.core.search.model.SearchItem
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun SearchListGridItem(
    item: SearchItem.List,
    onListClick: (CustomList) -> Unit,
    modifier: Modifier,
) {
    Text(
        text = item.list.name,
        style = TraktTheme.typography.cardTitle,
        color = TraktTheme.colors.textPrimary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .onClick { onListClick(item.list) },
    )
}
