package tv.trakt.trakt.core.lists.sections.personal.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.user.data.remote.personallists.UserPersonalListsRemoteDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.lists.CustomList
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.common.model.sorting.ListsSorting
import tv.trakt.trakt.core.lists.sections.personal.data.local.ListsPersonalLocalDataSource
import tv.trakt.trakt.core.lists.sections.personal.model.sortPersonalLists

internal class GetPersonalListsUseCase(
    private val remoteSource: UserPersonalListsRemoteDataSource,
    private val localSource: ListsPersonalLocalDataSource,
) {
    suspend fun getLocalList(listId: TraktId): CustomList? {
        return localSource
            .getItems()
            .firstOrNull { it.ids.trakt == listId }
    }

    suspend fun getLocalLists(
        pagination: Pagination,
        sorting: ListsSorting = ListsSorting.Default,
    ): ImmutableList<CustomList> {
        return sortPersonalLists(localSource.getItems(), sorting)
            .take(pagination.limit)
            .toImmutableList()
    }

    suspend fun getLists(
        pagination: Pagination,
        sorting: ListsSorting = ListsSorting.Default,
        notify: Boolean = false,
    ): ImmutableList<CustomList> {
        return remoteSource.getPersonalLists(
            pagination = pagination,
            sorting = sorting,
        )
            .asyncMap {
                CustomList.fromDto(it)
            }
            .toImmutableList()
            .also {
                // Local storage keeps rank order, which other screens rely on.
                if (sorting != ListsSorting.Default) {
                    return@also
                }
                if (pagination.page == 1) {
                    localSource.setItems(it, notify)
                } else {
                    localSource.addItems(it, notify)
                }
            }
    }
}
