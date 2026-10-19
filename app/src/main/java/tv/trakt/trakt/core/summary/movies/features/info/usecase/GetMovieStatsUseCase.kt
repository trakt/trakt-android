package tv.trakt.trakt.core.summary.movies.features.info.usecase

import tv.trakt.trakt.common.data.remote.MoviesRemoteSource
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.MovieStatsDto

internal class GetMovieStatsUseCase(
    private val remoteSource: MoviesRemoteSource,
) {
    suspend fun getStats(movieId: TraktId): MovieStatsDto {
        return remoteSource.getStats(movieId)
    }
}
