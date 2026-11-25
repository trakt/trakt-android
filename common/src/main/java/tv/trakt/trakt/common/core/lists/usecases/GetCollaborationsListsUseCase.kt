package tv.trakt.trakt.common.core.lists.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import tv.trakt.trakt.common.data.local.ListsCollaborationsLocalSource
import tv.trakt.trakt.common.data.remote.UserOtherListsRemoteSource
import tv.trakt.trakt.common.data.remote.UserPersonalListsRemoteSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.lists.CustomList
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.common.networking.ListDto

private const val OWN_SHARED_PAGE_SIZE = 100

class GetCollaborationsListsUseCase(
    private val remoteSource: UserOtherListsRemoteSource,
    private val personalRemoteSource: UserPersonalListsRemoteSource,
    private val localSource: ListsCollaborationsLocalSource,
) {
    suspend fun getLocalLists(): ImmutableList<CustomList> {
        return localSource.getLists()
            .toImmutableList()
    }

    suspend fun getLists(): ImmutableList<CustomList> {
        return coroutineScope {
            val collaborations = async { remoteSource.getCollaborationLists() }
            val ownShared = async { getOwnSharedLists() }
            collaborations.await() + ownShared.await()
        }
            .distinctBy { it.ids.trakt }
            .asyncMap {
                CustomList.fromDto(it)
            }
            .toImmutableList()
            .also {
                localSource.setLists(it)
            }
    }

    private suspend fun getOwnSharedLists(): List<ListDto> {
        tailrec suspend fun load(
            page: Int,
            loaded: List<ListDto>,
        ): List<ListDto> {
            val items = personalRemoteSource.getCollaborativeLists(
                collaborative = true,
                pagination = Pagination(page, OWN_SHARED_PAGE_SIZE),
            )
            val all = loaded + items
            return if (items.size < OWN_SHARED_PAGE_SIZE) all else load(page + 1, all)
        }

        return load(page = 1, loaded = emptyList())
    }
}

/**
 * Own shared lists first, then the lists shared with the user, each newest update first.
 */
fun sortSharedLists(
    lists: List<CustomList>,
    ownerId: TraktId?,
): List<CustomList> {
    return lists.sortedWith(
        compareByDescending<CustomList> { it.user.ids.trakt == ownerId }
            .thenByDescending { it.updatedAt },
    )
}
