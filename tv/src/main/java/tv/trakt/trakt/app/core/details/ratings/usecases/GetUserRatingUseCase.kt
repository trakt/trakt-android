package tv.trakt.trakt.app.core.details.ratings.usecases

import kotlinx.collections.immutable.ImmutableMap
import tv.trakt.trakt.common.core.user.usecases.ratings.LoadUserRatingsUseCase
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.ratings.UserRating

internal class GetUserRatingUseCase(
    private val loadUserRatingsUseCase: LoadUserRatingsUseCase,
) {
    suspend fun getRating(
        mediaId: TraktId,
        mediaType: MediaType,
    ): Int? {
        return loadRatings(mediaType)[mediaId]
            ?.rating
            ?.takeIf { it > 0 }
    }

    private suspend fun loadRatings(mediaType: MediaType): ImmutableMap<TraktId, UserRating> {
        with(loadUserRatingsUseCase) {
            return when (mediaType) {
                MediaType.Movie -> if (isMoviesLoaded()) loadLocalMovies() else loadMovies()
                MediaType.Show -> if (isShowsLoaded()) loadLocalShows() else loadShows()
                MediaType.Episode -> if (isEpisodesLoaded()) loadLocalEpisodes() else loadEpisodes()
                MediaType.Season -> if (isSeasonsLoaded()) loadLocalSeasons() else loadSeasons()
            }
        }
    }
}
