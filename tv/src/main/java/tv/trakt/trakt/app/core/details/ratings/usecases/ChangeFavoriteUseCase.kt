package tv.trakt.trakt.app.core.details.ratings.usecases

import tv.trakt.trakt.common.core.favorites.usecases.UpdateMovieFavoritesUseCase
import tv.trakt.trakt.common.core.favorites.usecases.UpdateShowFavoritesUseCase
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show

internal class ChangeFavoriteUseCase(
    private val updateMovieFavoritesUseCase: UpdateMovieFavoritesUseCase,
    private val updateShowFavoritesUseCase: UpdateShowFavoritesUseCase,
    private val analytics: Analytics,
) {
    suspend fun changeMovieFavorite(
        movie: Movie,
        favorite: Boolean,
    ) {
        if (favorite) {
            updateMovieFavoritesUseCase.addToFavorites(movie)
            analytics.ratings.logFavoriteAdd(mediaType = MediaType.Movie.value)
        } else {
            updateMovieFavoritesUseCase.removeFromFavorites(movie.ids.trakt)
            analytics.ratings.logFavoriteRemove(
                mediaType = MediaType.Movie.value,
                source = "movie_details",
            )
        }
    }

    suspend fun changeShowFavorite(
        show: Show,
        favorite: Boolean,
    ) {
        if (favorite) {
            updateShowFavoritesUseCase.addToFavorites(show)
            analytics.ratings.logFavoriteAdd(mediaType = MediaType.Show.value)
        } else {
            updateShowFavoritesUseCase.removeFromFavorites(show.ids.trakt)
            analytics.ratings.logFavoriteRemove(
                mediaType = MediaType.Show.value,
                source = "show_details",
            )
        }
    }
}
