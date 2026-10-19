package tv.trakt.trakt.core.lists.sections.liked.usecases.manage

import tv.trakt.trakt.common.data.local.ListsLikedLocalSource
import tv.trakt.trakt.common.data.local.UserLikedListsLocalSource
import tv.trakt.trakt.common.data.remote.ListsRemoteSource
import tv.trakt.trakt.common.model.TraktId

internal class RemoveLikedListUseCase(
    private val remoteSource: ListsRemoteSource,
    private val localUserLikedListsSource: UserLikedListsLocalSource,
    private val listsLocalSource: ListsLikedLocalSource,
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
