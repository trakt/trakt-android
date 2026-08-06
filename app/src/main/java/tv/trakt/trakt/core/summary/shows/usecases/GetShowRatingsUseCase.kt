package tv.trakt.trakt.core.summary.shows.usecases

import tv.trakt.trakt.common.model.ExternalRating
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.shows.data.remote.ShowsRemoteDataSource

internal class GetShowRatingsUseCase(
    private val remoteSource: ShowsRemoteDataSource,
) {
    suspend fun getExternalRatings(showId: TraktId): ExternalRating {
        val ratings = remoteSource.getExternalRatings(showId)
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
            trakt = ratings.trakt?.let(ExternalRating.TraktRating::fromDto),
        )
    }
}
