package tv.trakt.trakt.core.lists.sections.liked.usecases.manage

import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.lists.data.remote.ListsRemoteDataSource
import tv.trakt.trakt.core.lists.sections.liked.data.local.lists.ListsLikedLocalDataSource
import tv.trakt.trakt.core.user.data.local.liked.UserLikedListsLocalDataSource

internal class AddLikedListUseCase(
    private val remoteSource: ListsRemoteDataSource,
    private val localSource: UserLikedListsLocalDataSource,
    private val listsLocalSource: ListsLikedLocalDataSource,
) {
    suspend fun addToLiked(listId: TraktId) {
        remoteSource.addLikedList(
            listId = listId,
        )

        localSource.addList(
            listId = listId,
            likedAt = nowUtcInstant(),
        )

        listsLocalSource.notifyUpdate()
    }
}
