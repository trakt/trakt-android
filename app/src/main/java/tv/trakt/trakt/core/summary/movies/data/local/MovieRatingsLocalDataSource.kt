package tv.trakt.trakt.core.summary.movies.data.local

import tv.trakt.trakt.common.model.ExternalRating
import tv.trakt.trakt.common.model.TraktId

internal interface MovieRatingsLocalDataSource {
    suspend fun getRatings(movieId: TraktId): ExternalRating?

    suspend fun addRatings(
        movieId: TraktId,
        ratings: ExternalRating,
    )
}
