package tv.trakt.trakt.core.ratings

import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.MediaType.EPISODE
import tv.trakt.trakt.common.model.MediaType.MOVIE
import tv.trakt.trakt.common.model.MediaType.SEASON
import tv.trakt.trakt.common.model.MediaType.SHOW
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.ratings.data.remote.RatingsRemoteDataSource

internal class DeleteRatingUseCase(
    private val remoteSource: RatingsRemoteDataSource,
) {
    suspend fun deleteRating(
        mediaId: TraktId,
        mediaType: MediaType,
    ) {
        when (mediaType) {
            MOVIE -> remoteSource.deleteMovieRating(id = mediaId)
            SHOW -> remoteSource.deleteShowRating(id = mediaId)
            EPISODE -> remoteSource.deleteEpisodeRating(id = mediaId)
            SEASON -> throw IllegalStateException("Rating a season is not supported")
        }
    }
}
