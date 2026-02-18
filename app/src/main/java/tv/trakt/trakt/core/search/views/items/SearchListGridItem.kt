package tv.trakt.trakt.core.search.views.items

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import tv.trakt.trakt.common.helpers.extensions.DevicePreview
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.core.search.model.SearchItem
import tv.trakt.trakt.ui.components.mediacards.CustomListCard
import tv.trakt.trakt.ui.theme.HorizontalImageAspectRatio
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun SearchListGridItem(
    item: SearchItem.List,
    onListClick: (CustomList) -> Unit,
    modifier: Modifier = Modifier,
) {
    CustomListCard(
        list = item.list,
        likesVisible = true,
        onClick = {
            onListClick(item.list)
        },
        modifier = modifier
            .aspectRatio(HorizontalImageAspectRatio),
    )
}

@DevicePreview
@Composable
private fun SearchListGridItemPreview() {
    TraktTheme {
        SearchListGridItem(
            item = SearchItem.List(
                list = PreviewData.customList1,
            ),
            onListClick = {},
        )
    }
}
