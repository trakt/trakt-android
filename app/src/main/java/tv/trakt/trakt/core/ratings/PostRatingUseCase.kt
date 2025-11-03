package tv.trakt.trakt.core.ratings

import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.MediaType.EPISODE
import tv.trakt.trakt.common.model.MediaType.MOVIE
import tv.trakt.trakt.common.model.MediaType.SEASON
import tv.trakt.trakt.common.model.MediaType.SHOW
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.ratings.data.remote.RatingsRemoteDataSource

internal class PostRatingUseCase(
    private val remoteSource: RatingsRemoteDataSource,
) {
    suspend fun postRating(
        mediaId: TraktId,
        mediaType: MediaType,
        rating: Int,
    ) {
        when (mediaType) {
            MOVIE -> remoteSource.postMovieRating(
                id = mediaId,
                rating = rating,
            )
            SHOW -> remoteSource.postShowRating(
                id = mediaId,
                rating = rating,
            )
            EPISODE -> remoteSource.postEpisodeRating(
                id = mediaId,
                rating = rating,
            )
            SEASON -> throw IllegalStateException("Rating a season is not supported")
        }
    }
}
