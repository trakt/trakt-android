package tv.trakt.trakt.app.core.details.show.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.app.common.model.CustomList
import tv.trakt.trakt.app.common.model.CustomList.Type
import tv.trakt.trakt.app.core.shows.data.remote.ShowsRemoteDataSource
import tv.trakt.trakt.app.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.TraktId

internal class GetCustomListsUseCase(
    private val remoteSource: ShowsRemoteDataSource,
) {
    suspend fun getOfficialLists(showId: TraktId) = getLists(showId, Type.OFFICIAL, 3)

    suspend fun getPersonalLists(showId: TraktId) = getLists(showId, Type.PERSONAL, 5)

    private suspend fun getLists(
        showId: TraktId,
        type: Type,
        limit: Int,
    ): ImmutableList<CustomList> {
        return remoteSource.getShowLists(
            showId = showId,
            type = type.value,
            limit = limit,
        ).asyncMap {
            CustomList.fromDto(it)
        }.toImmutableList()
    }
}
