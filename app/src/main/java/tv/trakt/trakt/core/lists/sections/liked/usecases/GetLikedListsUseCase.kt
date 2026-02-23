package tv.trakt.trakt.core.lists.sections.liked.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.user.data.remote.UserRemoteDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.lists.sections.liked.data.local.lists.ListsLikedLocalDataSource

internal class GetLikedListsUseCase(
    private val remoteSource: UserRemoteDataSource,
    private val localSource: ListsLikedLocalDataSource,
) {
    suspend fun getLocalList(listId: TraktId): CustomList? {
        return localSource
            .getLists()
            .firstOrNull { it.ids.trakt == listId }
    }

    suspend fun getLocalLists(): ImmutableList<CustomList> {
        return localSource.getLists()
            .sortedByDescending { it.updatedAt }
            .toImmutableList()
    }

    suspend fun getLists(): ImmutableList<CustomList> {
        return remoteSource.getLikedLists()
            .asyncMap {
                CustomList.fromDto(it)
            }
            .sortedByDescending { it.updatedAt }
            .toImmutableList()
            .also {
                localSource.setLists(it)
            }
    }
}
