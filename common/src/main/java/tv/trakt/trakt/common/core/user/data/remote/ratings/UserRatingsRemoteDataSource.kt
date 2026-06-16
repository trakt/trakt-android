package tv.trakt.trakt.common.core.user.data.remote.ratings

import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.common.networking.UserRatingDto

interface UserRatingsRemoteDataSource {
    suspend fun getRatingsShows(): List<UserRatingDto>

    suspend fun getRatingsMovies(): List<UserRatingDto>

    suspend fun getRatingsEpisodes(): List<UserRatingDto>

    suspend fun getAllRatings(pagination: Pagination): List<UserRatingDto>
}
