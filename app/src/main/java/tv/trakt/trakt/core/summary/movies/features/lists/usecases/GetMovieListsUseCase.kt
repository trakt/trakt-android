package tv.trakt.trakt.core.summary.movies.features.lists.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.movies.data.remote.MoviesRemoteSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.lists.CustomList
import tv.trakt.trakt.common.model.lists.CustomList.Type

internal class GetMovieListsUseCase(
    private val remoteSource: MoviesRemoteSource,
) {
    suspend fun getOfficialLists(movieId: TraktId) =
        getLists(
            movieId = movieId,
            type = Type.Official,
            limit = 10,
        )

    suspend fun getPersonalLists(movieId: TraktId) =
        getLists(
            movieId = movieId,
            type = Type.Personal,
            limit = 10,
        )

    private suspend fun getLists(
        movieId: TraktId,
        type: Type,
        limit: Int,
    ): ImmutableList<CustomList> {
        return remoteSource.getLists(
            movieId = movieId,
            type = type.value,
            limit = limit,
        ).asyncMap {
            CustomList.fromDto(it)
        }.toImmutableList()
    }
}
