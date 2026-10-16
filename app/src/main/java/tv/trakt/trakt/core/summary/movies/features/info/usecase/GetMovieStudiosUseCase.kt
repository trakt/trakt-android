package tv.trakt.trakt.core.summary.movies.features.info.usecase

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.movies.data.remote.MoviesRemoteDataSource
import tv.trakt.trakt.common.model.TraktId

internal class GetMovieStudiosUseCase(
    private val remoteSource: MoviesRemoteDataSource,
) {
    suspend fun getStudios(movieId: TraktId): ImmutableList<String> {
        return remoteSource.getStudios(movieId).toImmutableList()
    }
}
