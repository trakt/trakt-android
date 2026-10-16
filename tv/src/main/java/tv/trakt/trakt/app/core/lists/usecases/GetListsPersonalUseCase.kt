package tv.trakt.trakt.app.core.lists.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.user.data.remote.personallists.UserPersonalListsRemoteDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.lists.CustomList
import tv.trakt.trakt.common.model.pagination.Pagination

internal class GetListsPersonalUseCase(
    private val remoteProfileSource: UserPersonalListsRemoteDataSource,
) {
    suspend fun getLists(): ImmutableList<CustomList> {
        return remoteProfileSource.getPersonalLists(
            pagination = Pagination(page = 1, limit = 100),
        )
            .asyncMap {
                CustomList.fromDto(it)
            }
            .sortedByDescending { it.updatedAt }
            .toImmutableList()
    }
}
