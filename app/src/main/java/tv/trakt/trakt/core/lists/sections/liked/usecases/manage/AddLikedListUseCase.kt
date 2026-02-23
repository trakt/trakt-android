package tv.trakt.trakt.core.lists.sections.liked.usecases.manage

import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.core.lists.data.remote.ListsRemoteDataSource
import tv.trakt.trakt.core.lists.sections.liked.data.local.lists.ListsLikedLocalDataSource
import tv.trakt.trakt.core.user.data.local.liked.UserLikedListsLocalDataSource

internal class AddLikedListUseCase(
    private val remoteSource: ListsRemoteDataSource,
    private val localSource: UserLikedListsLocalDataSource,
    private val listsLocalSource: ListsLikedLocalDataSource,
) {
    suspend fun addToLiked(list: CustomList) {
        remoteSource.addLikedList(
            listId = list.ids.trakt,
        )

        localSource.addList(
            listId = list.ids.trakt,
            likedAt = nowUtcInstant(),
        )

        with(listsLocalSource) {
            addList(list)
            notifyUpdate()
        }
    }
}
