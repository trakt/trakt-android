package tv.trakt.trakt.app.core.lists.usecases.liked

import tv.trakt.trakt.common.core.lists.data.remote.ListsRemoteDataSource
import tv.trakt.trakt.common.core.user.data.local.liked.UserLikedListsLocalDataSource
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.model.TraktId

internal class AddLikedListUseCase(
    private val remoteSource: ListsRemoteDataSource,
    private val localSource: UserLikedListsLocalDataSource,
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
