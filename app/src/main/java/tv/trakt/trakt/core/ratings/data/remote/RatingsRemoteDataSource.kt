package tv.trakt.trakt.core.ratings.data.remote

import tv.trakt.trakt.common.model.TraktId

internal interface RatingsRemoteDataSource {
    suspend fun postMovieRating(
        id: TraktId,
        rating: Int,
    )

    suspend fun postShowRating(
        id: TraktId,
        rating: Int,
    )
}
