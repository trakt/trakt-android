package tv.trakt.trakt.core.user.data.local

import tv.trakt.trakt.common.model.CustomListMinimal
import tv.trakt.trakt.common.model.TraktId

internal interface UserListsLocalDataSource {
    suspend fun setLists(lists: List<CustomListMinimal>)

    suspend fun getLists(): Map<TraktId, CustomListMinimal>

    suspend fun isLoaded(): Boolean

    fun clear()
}
