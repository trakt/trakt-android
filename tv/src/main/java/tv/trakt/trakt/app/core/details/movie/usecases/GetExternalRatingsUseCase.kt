package tv.trakt.trakt.app.core.details.movie.usecases

import tv.trakt.trakt.app.core.movies.data.remote.MoviesRemoteDataSource
import tv.trakt.trakt.common.model.ExternalRating
import tv.trakt.trakt.common.model.TraktId

internal class GetExternalRatingsUseCase(
    private val remoteSource: MoviesRemoteDataSource,
) {
    suspend fun getExternalRatings(movieId: TraktId): ExternalRating {
        val ratings = remoteSource.getMovieExternalRatings(movieId)
        return ExternalRating(
            trakt = ratings.trakt?.let(ExternalRating.TraktRating::fromDto),
            imdb = ExternalRating.ImdbRating(
                rating = ratings.imdb?.rating ?: 0F,
                votes = ratings.imdb?.votes ?: 0,
                link = ratings.imdb?.link,
            ),
            meta = ExternalRating.MetaRating(
                rating = ratings.metascore?.rating ?: 0,
                link = ratings.metascore?.link,
            ),
            rotten = ExternalRating.RottenRating(
                rating = ratings.rottenTomatoes?.rating ?: 0F,
                state = ratings.rottenTomatoes?.state,
                userRating = ratings.rottenTomatoes?.userRating,
                userState = ratings.rottenTomatoes?.userState,
                link = ratings.rottenTomatoes?.link,
            ),
            tmdb = ExternalRating.TmdbRating(
                rating = ratings.tmdb?.rating ?: 0F,
                votes = ratings.tmdb?.votes ?: 0,
                link = ratings.tmdb?.link,
            ),
            mal = ExternalRating.MalRating(
                rating = ratings.mal?.rating ?: 0F,
                votes = ratings.mal?.votes ?: 0,
                link = ratings.mal?.link,
            ),
            letterboxd = ExternalRating.LetterboxdRating(
                rating = ratings.letterboxd?.rating ?: 0F,
                votes = ratings.letterboxd?.votes ?: 0,
                link = ratings.letterboxd?.link,
            ),
        )
    }
}
