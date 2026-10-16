package tv.trakt.trakt.common.core.ratings.data.remote

import tv.trakt.trakt.common.model.TraktId

interface RatingsRemoteDataSource {
    suspend fun postMovieRating(
        id: TraktId,
        rating: Int,
    )

    suspend fun deleteMovieRating(id: TraktId)

    suspend fun postShowRating(
        id: TraktId,
        rating: Int,
    )

    suspend fun deleteShowRating(id: TraktId)

    suspend fun postEpisodeRating(
        id: TraktId,
        rating: Int,
    )

    suspend fun deleteEpisodeRating(id: TraktId)

    suspend fun postSeasonRating(
        id: TraktId,
        rating: Int,
    )

    suspend fun deleteSeasonRating(id: TraktId)
}
