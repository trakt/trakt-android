package tv.trakt.trakt.core.summary.movies.usecases

import tv.trakt.trakt.common.model.ExternalRating
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.movies.data.remote.MoviesRemoteDataSource

internal class GetMovieRatingsUseCase(
    private val remoteSource: MoviesRemoteDataSource,
) {
    suspend fun getExternalRatings(movieId: TraktId): ExternalRating {
        val ratings = remoteSource.getExternalRatings(movieId)
        return ExternalRating(
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
        )
    }
}
