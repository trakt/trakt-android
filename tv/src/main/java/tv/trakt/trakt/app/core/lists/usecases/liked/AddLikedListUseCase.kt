package tv.trakt.trakt.app.core.lists.usecases.liked

import tv.trakt.trakt.common.data.local.UserLikedListsLocalSource
import tv.trakt.trakt.common.data.remote.ListsRemoteSource
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.model.TraktId

internal class AddLikedListUseCase(
    private val remoteSource: ListsRemoteSource,
    private val localSource: UserLikedListsLocalSource,
) {
    suspend fun addToLiked(listId: TraktId) {
        remoteSource.addLikedList(
            listId = listId,
        )

        localSource.addList(
            listId = listId,
            likedAt = nowUtcInstant(),
        )
    }
}
