package tv.trakt.trakt.core.lists.sections.personal.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.core.lists.sections.personal.data.local.ListsPersonalLocalDataSource
import tv.trakt.trakt.core.profile.data.remote.UserRemoteDataSource

internal class GetPersonalListsUseCase(
    private val remoteSource: UserRemoteDataSource,
    private val localSource: ListsPersonalLocalDataSource,
) {
    suspend fun getLocalLists(): ImmutableList<CustomList> {
        return localSource.getItems()
            .sortedByDescending { it.updatedAt }
            .toImmutableList()
    }

    suspend fun getLists(): ImmutableList<CustomList> {
        return remoteSource.getPersonalLists()
            .asyncMap {
                CustomList.fromDto(it)
            }
            .sortedByDescending { it.updatedAt }
            .toImmutableList()
            .also {
                localSource.addItems(it)
            }
    }
}
