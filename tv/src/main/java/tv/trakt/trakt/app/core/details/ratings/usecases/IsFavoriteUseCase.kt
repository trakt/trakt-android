package tv.trakt.trakt.app.core.details.ratings.usecases

import tv.trakt.trakt.common.core.user.usecases.lists.LoadUserFavoritesUseCase
import tv.trakt.trakt.common.model.TraktId

internal class IsFavoriteUseCase(
    private val loadUserFavoritesUseCase: LoadUserFavoritesUseCase,
) {
    suspend fun isMovieFavorite(movieId: TraktId): Boolean {
        with(loadUserFavoritesUseCase) {
            if (!isMoviesLoaded()) loadMovies()
            return loadLocalMovies().any { it.movie.ids.trakt == movieId }
        }
    }

    suspend fun isShowFavorite(showId: TraktId): Boolean {
        with(loadUserFavoritesUseCase) {
            if (!isShowsLoaded()) loadShows()
            return loadLocalShows().any { it.show.ids.trakt == showId }
        }
    }
}
