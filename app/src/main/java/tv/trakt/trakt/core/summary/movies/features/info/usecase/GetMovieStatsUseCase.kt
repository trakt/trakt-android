package tv.trakt.trakt.core.summary.movies.features.info.usecase

import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.MovieStatsDto
import tv.trakt.trakt.core.movies.data.remote.MoviesRemoteDataSource

internal class GetMovieStatsUseCase(
    private val remoteSource: MoviesRemoteDataSource,
) {
    suspend fun getStats(movieId: TraktId): MovieStatsDto {
        return remoteSource.getStats(movieId)
    }
}
