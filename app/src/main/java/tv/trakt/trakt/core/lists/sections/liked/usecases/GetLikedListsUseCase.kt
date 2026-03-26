package tv.trakt.trakt.core.lists.sections.liked.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.user.data.remote.otherlists.UserOtherListsRemoteDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.core.lists.sections.liked.data.local.lists.ListsLikedLocalDataSource

internal class GetLikedListsUseCase(
    private val remoteSource: UserOtherListsRemoteDataSource,
    private val localSource: ListsLikedLocalDataSource,
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

    suspend fun getLists(pagination: Pagination): ImmutableList<CustomList> {
        return remoteSource.getLikedLists(
            minimal = false,
            pagination = pagination,
        )
            .asyncMap {
                CustomList.fromDto(it)
            }
            .toImmutableList()
            .also {
                if (pagination.page == 1) {
                    localSource.setLists(it)
                } else {
                    localSource.addLists(it)
                }
            }
    }
}
