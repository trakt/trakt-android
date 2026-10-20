package tv.trakt.trakt.core.lists.sections.liked.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.data.local.ListsLikedLocalSource
import tv.trakt.trakt.common.data.remote.UserOtherListsRemoteSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.lists.CustomList
import tv.trakt.trakt.common.model.pagination.Pagination

internal class GetLikedListsUseCase(
    private val remoteSource: UserOtherListsRemoteSource,
    private val localSource: ListsLikedLocalSource,
) {
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
