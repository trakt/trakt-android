package tv.trakt.trakt.core.sync.usecases

import tv.trakt.trakt.common.core.favorites.FavoriteItem
import tv.trakt.trakt.common.core.sync.changes.ChangeOrigin
import tv.trakt.trakt.common.core.sync.changes.DataChange.Kind
import tv.trakt.trakt.common.core.sync.changes.DataChanges
import tv.trakt.trakt.common.data.local.UserFavoritesLocalSource
import tv.trakt.trakt.common.data.remote.MoviesSyncRemoteSource
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.TraktId

internal class UpdateMovieFavoritesUseCase(
    private val remoteSource: MoviesSyncRemoteSource,
    private val userFavoritesLocalSource: UserFavoritesLocalSource,
    private val dataChanges: DataChanges,
) {
    suspend fun addToFavorites(
        movie: Movie,
        origin: ChangeOrigin? = null,
    ) {
        remoteSource.addToFavorites(
            movieId = movie.ids.trakt,
        )

        userFavoritesLocalSource.addMovies(
            movies = listOf(
                FavoriteItem.MovieItem(
                    rank = 0,
                    movie = movie,
                    listedAt = nowUtcInstant(),
                ),
            ),
        )
        dataChanges.notify(Kind.Favorites, origin = origin)
    }

    suspend fun removeFromFavorites(
        movieId: TraktId,
        origin: ChangeOrigin? = null,
    ) {
        remoteSource.removeFromFavorites(
            movieId = movieId,
        )

        userFavoritesLocalSource.removeMovies(setOf(movieId))
        dataChanges.notify(Kind.Favorites, origin = origin)
    }
}
