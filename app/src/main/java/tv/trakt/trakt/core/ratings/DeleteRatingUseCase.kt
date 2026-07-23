package tv.trakt.trakt.core.ratings

import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.MediaType.Episode
import tv.trakt.trakt.common.model.MediaType.Movie
import tv.trakt.trakt.common.model.MediaType.Season
import tv.trakt.trakt.common.model.MediaType.Show
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
            Movie -> remoteSource.deleteMovieRating(id = mediaId)
            Show -> remoteSource.deleteShowRating(id = mediaId)
            Episode -> remoteSource.deleteEpisodeRating(id = mediaId)
            Season -> remoteSource.deleteSeasonRating(id = mediaId)
        }
    }
}
