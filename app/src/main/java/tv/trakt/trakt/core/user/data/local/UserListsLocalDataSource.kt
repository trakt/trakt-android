package tv.trakt.trakt.core.user.data.local

import kotlinx.coroutines.flow.Flow
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.lists.model.PersonalListItem
import java.time.Instant

internal interface UserListsLocalDataSource {
    suspend fun setLists(
        lists: Map<CustomList, List<PersonalListItem>>,
        notify: Boolean = false,
    )

    suspend fun addLists(
        lists: Map<CustomList, List<PersonalListItem>>,
        notify: Boolean = false,
    )

    suspend fun removeLists(
        listsIds: Set<TraktId>,
        notify: Boolean = false,
    )

    suspend fun containsList(listId: TraktId): Boolean

    suspend fun isLoaded(): Boolean

    suspend fun getLists(): Map<CustomList, List<PersonalListItem>>

    suspend fun getListItems(listId: TraktId): List<PersonalListItem>?

    suspend fun addListItem(
        listId: TraktId,
        item: PersonalListItem,
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
