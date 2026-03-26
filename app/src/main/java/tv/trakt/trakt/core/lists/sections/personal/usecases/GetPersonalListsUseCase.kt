package tv.trakt.trakt.core.lists.sections.personal.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.user.data.remote.personallists.UserPersonalListsRemoteDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.core.lists.sections.personal.data.local.ListsPersonalLocalDataSource

internal class GetPersonalListsUseCase(
    private val remoteSource: UserPersonalListsRemoteDataSource,
    private val localSource: ListsPersonalLocalDataSource,
) {
    suspend fun getLocalList(listId: TraktId): CustomList? {
        return localSource
            .getItems()
            .firstOrNull { it.ids.trakt == listId }
    }

    suspend fun getLocalLists(pagination: Pagination): ImmutableList<CustomList> {
        return localSource.getItems()
            .take(pagination.limit)
            .toImmutableList()
    }

    suspend fun getLists(
        pagination: Pagination,
        notify: Boolean = false,
    ): ImmutableList<CustomList> {
        return remoteSource.getPersonalLists(pagination)
            .asyncMap {
                CustomList.fromDto(it)
            }
            .toImmutableList()
            .also {
                if (pagination.page == 1) {
                    localSource.setItems(it, notify)
                } else {
                    localSource.addItems(it, notify)
                }
            }
    }
}
