package tv.trakt.trakt.core.lists.sections.liked.data.local.items

import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.lists.model.CustomListItem

internal interface ListsLikedItemsLocalDataSource {
    suspend fun setItems(
        listId: TraktId,
        items: List<CustomListItem>,
    )

    suspend fun getItems(listId: TraktId): List<CustomListItem>

    fun clear()
}
