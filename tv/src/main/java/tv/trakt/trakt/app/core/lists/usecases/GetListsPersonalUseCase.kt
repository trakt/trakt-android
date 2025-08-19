package tv.trakt.trakt.app.core.lists.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.app.common.model.CustomList
import tv.trakt.trakt.app.core.profile.data.remote.ProfileRemoteDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap

internal class GetListsPersonalUseCase(
    private val remoteProfileSource: ProfileRemoteDataSource,
) {
    suspend fun getLists(): ImmutableList<CustomList> {
        return remoteProfileSource.getUserLists()
            .asyncMap {
                CustomList.fromDto(it)
            }
            .sortedByDescending { it.updatedAt }
            .toImmutableList()
    }
}
