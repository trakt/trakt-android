package tv.trakt.trakt.core.user.data.local

import kotlinx.coroutines.flow.Flow
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.lists.model.CustomListItem
import java.time.Instant

internal interface UserListsLocalDataSource {
    suspend fun setLists(
        lists: Map<CustomList, List<CustomListItem>>,
        notify: Boolean = false,
    )

    suspend fun isLoaded(): Boolean

    suspend fun getLists(): Map<CustomList, List<CustomListItem>>

    suspend fun addListItem(
        listId: TraktId,
        item: CustomListItem,
        notify: Boolean = false,
    )

    suspend fun removeListItem(
        listId: TraktId,
        itemId: TraktId,
        itemType: MediaType,
        notify: Boolean = false,
    )

    fun observeUpdates(): Flow<Instant?>

    fun clear()
}
