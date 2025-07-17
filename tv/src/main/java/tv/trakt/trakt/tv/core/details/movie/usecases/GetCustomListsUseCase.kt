package tv.trakt.trakt.tv.core.details.movie.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.tv.common.model.CustomList
import tv.trakt.trakt.tv.common.model.CustomList.Type
import tv.trakt.trakt.tv.common.model.TraktId
import tv.trakt.trakt.tv.core.movies.data.remote.MoviesRemoteDataSource
import tv.trakt.trakt.tv.helpers.extensions.asyncMap

internal class GetCustomListsUseCase(
    private val remoteSource: MoviesRemoteDataSource,
) {
    suspend fun getOfficialLists(movieId: TraktId): ImmutableList<CustomList> = getLists(movieId, Type.OFFICIAL, 3)

    suspend fun getPersonalLists(movieId: TraktId): ImmutableList<CustomList> = getLists(movieId, Type.PERSONAL, 5)

    private suspend fun getLists(
        movieId: TraktId,
        type: Type,
        limit: Int,
    ): ImmutableList<CustomList> {
        return remoteSource.getMovieLists(
            movieId = movieId,
            type = type.value,
            limit = limit,
        ).asyncMap {
            CustomList.fromDto(it)
        }.toImmutableList()
    }
}
