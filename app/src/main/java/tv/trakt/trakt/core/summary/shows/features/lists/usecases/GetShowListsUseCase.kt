package tv.trakt.trakt.core.summary.shows.features.lists.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.shows.data.remote.ShowsRemoteSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.lists.CustomList
import tv.trakt.trakt.common.model.lists.CustomList.Type

internal class GetShowListsUseCase(
    private val remoteSource: ShowsRemoteSource,
) {
    suspend fun getOfficialLists(showId: TraktId) =
        getLists(
            showId = showId,
            type = Type.Official,
            limit = 10,
        )

    suspend fun getPersonalLists(showId: TraktId) =
        getLists(
            showId = showId,
            type = Type.Personal,
            limit = 10,
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
