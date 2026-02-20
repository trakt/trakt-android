package tv.trakt.trakt.core.lists.sections.liked.usecases.manage

import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.lists.data.remote.ListsRemoteDataSource
import tv.trakt.trakt.core.lists.sections.liked.data.local.lists.ListsLikedLocalDataSource
import tv.trakt.trakt.core.user.data.local.liked.UserLikedListsLocalDataSource

internal class RemoveLikedListUseCase(
    private val remoteSource: ListsRemoteDataSource,
    private val localSource: UserLikedListsLocalDataSource,
    private val listsLocalSource: ListsLikedLocalDataSource,
) {
    suspend fun removeFromLiked(listId: TraktId) {
        remoteSource.removeLikedList(
            listId = listId,
        )

        localSource.removeList(
            listId = listId,
        )

        listsLocalSource.notifyUpdate()
    }
}
