package tv.trakt.trakt.core.home.sections.recommended.usecase

import tv.trakt.trakt.common.data.remote.MoviesSyncRemoteSource
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.home.sections.recommended.data.media.RecommendedMediaLocalSource
import tv.trakt.trakt.core.home.sections.recommended.data.movies.RecommendedMoviesLocalSource

internal class HideRecommendedMovieUseCase(
    private val remoteSource: MoviesSyncRemoteSource,
    private val localRecommendedSource: RecommendedMoviesLocalSource,
    private val localRecommendedMediaSource: RecommendedMediaLocalSource,
    private val analytics: Analytics,
) {
    suspend fun hideMovie(movieId: TraktId) {
        remoteSource.hideRecommendation(movieId)
        localRecommendedSource.removeMovie(movieId)
        localRecommendedMediaSource.removeItem(id = movieId, type = MediaType.Movie)
        analytics.progress.logHideRecommendation(MediaType.Movie.value)
    }
}
