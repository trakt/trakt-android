package tv.trakt.trakt.core.ratings

import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.MediaType.Episode
import tv.trakt.trakt.common.model.MediaType.Movie
import tv.trakt.trakt.common.model.MediaType.Season
import tv.trakt.trakt.common.model.MediaType.Show
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
            Movie -> remoteSource.postMovieRating(
                id = mediaId,
                rating = rating,
            )

            Show -> remoteSource.postShowRating(
                id = mediaId,
                rating = rating,
            )

            Episode -> remoteSource.postEpisodeRating(
                id = mediaId,
                rating = rating,
            )

            Season -> remoteSource.postSeasonRating(
                id = mediaId,
                rating = rating,
            )
        }
    }
}
