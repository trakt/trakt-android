package tv.trakt.trakt.core.home.sections.recommended.usecase

import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.home.sections.recommended.local.movies.RecommendedMoviesLocalDataSource
import tv.trakt.trakt.core.sync.data.remote.movies.MoviesSyncRemoteDataSource

internal class HideRecommendedMovieUseCase(
    private val remoteSource: MoviesSyncRemoteDataSource,
    private val localRecommendedSource: RecommendedMoviesLocalDataSource,
) {
    suspend fun hideMovie(movieId: TraktId) {
        remoteSource.hideRecommendation(movieId)
        localRecommendedSource.removeMovie(movieId)
    }
}
