package tv.trakt.trakt.common.core.user.data.local

import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.lists.CustomListMinimal

interface UserListsLocalDataSource {
    suspend fun setLists(lists: List<CustomListMinimal>)

    suspend fun getLists(): Map<TraktId, CustomListMinimal>

    suspend fun isLoaded(): Boolean

    fun clear()
}
