package tv.trakt.trakt.core.summary.shows.features.lists.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.CustomList.Type
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.discover.data.remote.ShowsRemoteDataSource

internal class GetShowListsUseCase(
    private val remoteSource: ShowsRemoteDataSource,
) {
    suspend fun getOfficialLists(showId: TraktId) =
        getLists(
            showId = showId,
            type = Type.OFFICIAL,
            limit = 1,
        )

    suspend fun getPersonalLists(showId: TraktId) =
        getLists(
            showId = showId,
            type = Type.PERSONAL,
            limit = 1,
        )

    private suspend fun getLists(
        showId: TraktId,
        type: Type,
        limit: Int,
    ): ImmutableList<CustomList> {
        return remoteSource.getLists(
            showId = showId,
            type = type.value,
            limit = limit,
        ).asyncMap {
            CustomList.fromDto(it)
        }.toImmutableList()
    }
}
