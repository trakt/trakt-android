package tv.trakt.trakt.core.lists.sections.collaborations.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.data.local.ListsCollaborationsLocalSource
import tv.trakt.trakt.common.data.remote.UserOtherListsRemoteSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.lists.CustomList

internal class GetCollaborationsListsUseCase(
    private val remoteSource: UserOtherListsRemoteSource,
    private val localSource: ListsCollaborationsLocalSource,
) {
    suspend fun getLocalLists(): ImmutableList<CustomList> {
        return localSource.getLists()
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
