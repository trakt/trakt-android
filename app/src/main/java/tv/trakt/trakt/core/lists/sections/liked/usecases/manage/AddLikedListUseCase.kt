package tv.trakt.trakt.core.lists.sections.liked.usecases.manage

import tv.trakt.trakt.common.data.local.ListsLikedLocalSource
import tv.trakt.trakt.common.data.local.UserLikedListsLocalSource
import tv.trakt.trakt.common.data.remote.ListsRemoteSource
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.model.lists.CustomList

internal class AddLikedListUseCase(
    private val remoteSource: ListsRemoteSource,
    private val localSource: UserLikedListsLocalSource,
    private val listsLocalSource: ListsLikedLocalSource,
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
