package tv.trakt.trakt.common.core.user.data.local.liked

import tv.trakt.trakt.common.model.TraktId
import java.time.Instant

interface UserLikedListsLocalDataSource {
    suspend fun setLists(lists: Map<TraktId, Instant>)

    suspend fun getLists(): Map<TraktId, Instant>

    suspend fun addList(
        listId: TraktId,
        likedAt: Instant,
    )

    suspend fun removeList(listId: TraktId)

    suspend fun isLoaded(): Boolean

    fun clear()
}
