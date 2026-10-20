package tv.trakt.trakt.app.core.lists.usecases.liked

import tv.trakt.trakt.common.data.local.UserLikedListsLocalSource
import tv.trakt.trakt.common.data.remote.ListsRemoteSource
import tv.trakt.trakt.common.model.TraktId

internal class RemoveLikedListUseCase(
    private val remoteSource: ListsRemoteSource,
    private val localSource: UserLikedListsLocalSource,
) {
    suspend fun removeFromLiked(listId: TraktId) {
        remoteSource.removeLikedList(
            listId = listId,
        )

        localSource.removeList(
            listId = listId,
        )
    }
}
