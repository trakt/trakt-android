package tv.trakt.trakt.app.core.lists.usecases.liked

import tv.trakt.trakt.common.core.lists.data.remote.ListsRemoteSource
import tv.trakt.trakt.common.core.user.data.local.liked.UserLikedListsLocalDataSource
import tv.trakt.trakt.common.model.TraktId

internal class RemoveLikedListUseCase(
    private val remoteSource: ListsRemoteSource,
    private val localSource: UserLikedListsLocalDataSource,
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
