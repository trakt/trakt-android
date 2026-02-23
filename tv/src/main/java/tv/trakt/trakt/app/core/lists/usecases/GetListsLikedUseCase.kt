package tv.trakt.trakt.app.core.lists.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.app.core.profile.data.remote.ProfileRemoteDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.pagination.Pagination

internal class GetListsLikedUseCase(
    private val remoteProfileSource: ProfileRemoteDataSource,
) {
    suspend fun getLists(): ImmutableList<CustomList> {
        return remoteProfileSource.getLikedLists(
            pagination = Pagination(1, 50),
        )
            .sortedByDescending { it.likedAt }
            .asyncMap { CustomList.fromDto(it) }
            .toImmutableList()
    }
}
