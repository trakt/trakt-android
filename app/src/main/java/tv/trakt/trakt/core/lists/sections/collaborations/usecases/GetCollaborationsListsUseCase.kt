package tv.trakt.trakt.core.lists.sections.collaborations.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.user.data.remote.likedlists.UserLikedListsRemoteDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.core.lists.sections.collaborations.data.local.lists.ListsCollaborationsLocalDataSource

internal class GetCollaborationsListsUseCase(
    private val remoteSource: UserLikedListsRemoteDataSource,
    private val localSource: ListsCollaborationsLocalDataSource,
) {
    suspend fun getLocalList(listId: TraktId): CustomList? {
        return localSource
            .getLists()
            .firstOrNull { it.ids.trakt == listId }
    }

    suspend fun getLocalLists(pagination: Pagination): ImmutableList<CustomList> {
        return localSource.getLists()
            .take(pagination.limit)
            .toImmutableList()
    }

    suspend fun getLists(): ImmutableList<CustomList> {
        return remoteSource.getCollaborationLists()
            .asyncMap {
                CustomList.fromDto(it)
            }
            .toImmutableList()
            .also {
                localSource.setLists(it)
            }
    }
}
