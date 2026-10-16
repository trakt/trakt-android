package tv.trakt.trakt.app.core.details.show.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.shows.data.remote.ShowsRemoteSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.lists.CustomList
import tv.trakt.trakt.common.model.lists.CustomList.Type

internal class GetCustomListsUseCase(
    private val remoteSource: ShowsRemoteSource,
) {
    suspend fun getOfficialLists(showId: TraktId) = getLists(showId, Type.Official, 3)

    suspend fun getPersonalLists(showId: TraktId) = getLists(showId, Type.Personal, 5)

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
