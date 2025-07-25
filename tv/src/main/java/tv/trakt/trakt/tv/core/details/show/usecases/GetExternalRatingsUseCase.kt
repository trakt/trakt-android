package tv.trakt.trakt.tv.core.details.show.usecases

import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.tv.common.model.ExternalRating
import tv.trakt.trakt.tv.core.shows.data.remote.ShowsRemoteDataSource

internal class GetExternalRatingsUseCase(
    private val remoteSource: ShowsRemoteDataSource,
) {
    suspend fun getExternalRatings(showId: TraktId): ExternalRating {
        val ratings = remoteSource.getShowExternalRatings(showId)
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
