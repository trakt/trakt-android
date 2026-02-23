package tv.trakt.trakt.core.lists.sections.liked.usecases.manage

import tv.trakt.trakt.common.core.user.data.local.liked.UserLikedListsLocalDataSource
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.lists.data.remote.ListsRemoteDataSource
import tv.trakt.trakt.core.lists.sections.liked.data.local.lists.ListsLikedLocalDataSource

internal class RemoveLikedListUseCase(
    private val remoteSource: ListsRemoteDataSource,
    private val localUserLikedListsSource: UserLikedListsLocalDataSource,
    private val listsLocalSource: ListsLikedLocalDataSource,
) {
    suspend fun removeFromLiked(listId: TraktId) {
        remoteSource.removeLikedList(
            listId = listId,
        )

        localUserLikedListsSource.removeList(
            listId = listId,
        )

        with(listsLocalSource) {
            removeList(listId)
            notifyUpdate()
        }
    }
}
