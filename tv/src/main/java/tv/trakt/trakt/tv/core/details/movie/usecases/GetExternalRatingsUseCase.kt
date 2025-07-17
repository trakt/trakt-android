package tv.trakt.trakt.tv.core.details.movie.usecases

import tv.trakt.trakt.tv.common.model.ExternalRating
import tv.trakt.trakt.tv.common.model.TraktId
import tv.trakt.trakt.tv.core.movies.data.remote.MoviesRemoteDataSource

internal class GetExternalRatingsUseCase(
    private val remoteSource: MoviesRemoteDataSource,
) {
    suspend fun getExternalRatings(movieId: TraktId): ExternalRating {
        val ratings = remoteSource.getMovieExternalRatings(movieId)
        return ExternalRating(
            tmdb = ExternalRating.TmdbRating(
                rating = ratings.tmdb?.rating ?: 0F,
                votes = ratings.tmdb?.votes ?: 0,
                link = ratings.tmdb?.link,
            ),
            imdb = ExternalRating.ImdbRating(
                rating = ratings.imdb?.rating ?: 0F,
                votes = ratings.imdb?.votes ?: 0,
                link = ratings.imdb?.link,
            ),
            meta = ExternalRating.MetaRating(
                rating = ratings.metascore.rating ?: 0,
                link = ratings.metascore.link,
            ),
            rotten = ExternalRating.RottenRating(
                rating = ratings.rottenTomatoes?.rating ?: 0F,
                state = ratings.rottenTomatoes?.state,
                userRating = ratings.rottenTomatoes?.userRating,
                userState = ratings.rottenTomatoes?.userState,
                link = ratings.rottenTomatoes?.link,
            ),
        )
    }
}
