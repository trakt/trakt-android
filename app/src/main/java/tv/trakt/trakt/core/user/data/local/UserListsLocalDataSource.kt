package tv.trakt.trakt.core.user.data.local

import kotlinx.coroutines.flow.Flow
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.lists.model.PersonalListItem
import java.time.Instant

internal interface UserListsLocalDataSource {
    suspend fun setLists(
        lists: Map<TraktId, List<PersonalListItem>>,
        notify: Boolean = false,
    )

    suspend fun addLists(
        lists: Map<TraktId, List<PersonalListItem>>,
        notify: Boolean = false,
    )

    suspend fun removeLists(
        listsIds: Set<TraktId>,
        notify: Boolean = false,
    )

    suspend fun containsList(listId: TraktId): Boolean

    suspend fun isLoaded(): Boolean

    suspend fun getLists(): Map<TraktId, List<PersonalListItem>>

    suspend fun getList(listId: TraktId): List<PersonalListItem>?

    fun observeUpdates(): Flow<Instant?>

    fun clear()
}
