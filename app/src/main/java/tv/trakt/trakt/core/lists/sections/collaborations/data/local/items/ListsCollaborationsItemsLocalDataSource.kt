package tv.trakt.trakt.core.lists.sections.collaborations.data.local.items

import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.lists.model.CustomListItem

internal interface ListsCollaborationsItemsLocalDataSource {
    suspend fun setItems(
        listId: TraktId,
        items: List<CustomListItem>,
    )

    suspend fun getItems(listId: TraktId): List<CustomListItem>

    fun clear()
}
