package tv.trakt.trakt.core.home.sections.recommended.usecase

import tv.trakt.trakt.common.core.sync.data.remote.movies.MoviesSyncRemoteDataSource
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.home.sections.recommended.data.media.RecommendedMediaLocalDataSource
import tv.trakt.trakt.core.home.sections.recommended.data.movies.RecommendedMoviesLocalDataSource

internal class HideRecommendedMovieUseCase(
    private val remoteSource: MoviesSyncRemoteDataSource,
    private val localRecommendedSource: RecommendedMoviesLocalDataSource,
    private val localRecommendedMediaSource: RecommendedMediaLocalDataSource,
) {
    suspend fun hideMovie(movieId: TraktId) {
        remoteSource.hideRecommendation(movieId)
        localRecommendedSource.removeMovie(movieId)
        localRecommendedMediaSource.removeItem(id = movieId, type = MediaType.Movie)
    }
}
