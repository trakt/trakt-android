package tv.trakt.trakt.core.summary.movies.features.lists.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.CustomList.Type
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.movies.data.remote.MoviesRemoteDataSource

internal class GetMovieListsUseCase(
    private val remoteSource: MoviesRemoteDataSource,
) {
    suspend fun getOfficialLists(movieId: TraktId) =
        getLists(
            movieId = movieId,
            type = Type.OFFICIAL,
            limit = 3,
        )

    suspend fun getPersonalLists(movieId: TraktId) =
        getLists(
            movieId = movieId,
            type = Type.PERSONAL,
            limit = 5,
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
