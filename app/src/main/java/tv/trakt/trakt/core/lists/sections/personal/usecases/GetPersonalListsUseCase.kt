package tv.trakt.trakt.core.lists.sections.personal.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.merge
import tv.trakt.trakt.common.data.local.ListsCollaborationsLocalSource
import tv.trakt.trakt.common.data.local.ListsPersonalLocalSource
import tv.trakt.trakt.common.data.remote.UserPersonalListsRemoteSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.lists.CustomList
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.common.model.sorting.ListsSorting
import tv.trakt.trakt.core.lists.sections.personal.model.sortPersonalLists
import java.time.Instant

internal class GetPersonalListsUseCase(
    private val remoteSource: UserPersonalListsRemoteSource,
    private val localSource: ListsPersonalLocalSource,
    private val sharedLocalSource: ListsCollaborationsLocalSource,
) {
    /**
     * Own lists with collaborators are stored with the shared lists, not the personal ones.
     */
    suspend fun getLocalList(listId: TraktId): CustomList? {
        return localSource.getItems().firstOrNull { it.ids.trakt == listId }
            ?: sharedLocalSource.getLists().firstOrNull { it.ids.trakt == listId }
    }

    fun observeLocalUpdates(): Flow<Instant?> {
        return merge(
            localSource.observeUpdates(),
            sharedLocalSource.observeUpdates(),
        )
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
        return remoteSource.getCollaborativeLists(
            collaborative = false,
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
