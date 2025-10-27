package tv.trakt.trakt.core.user.usecases.lists

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.profile.model.FavoriteItem
import tv.trakt.trakt.core.user.data.local.favorites.UserFavoritesLocalDataSource
import tv.trakt.trakt.core.user.data.remote.UserRemoteDataSource

/**
 * Loads the user's favorites from the remote source and updates the local cache.
 */
internal class LoadUserFavoritesUseCase(
    private val remoteSource: UserRemoteDataSource,
    private val localSource: UserFavoritesLocalDataSource,
) {
    suspend fun loadLocalAll(): ImmutableList<FavoriteItem> {
        return localSource.getAll()
            .sortedByDescending { it.listedAt }
            .toImmutableList()
    }

    suspend fun loadLocalShows(): ImmutableList<FavoriteItem.ShowItem> {
        return localSource.getShows()
            .sortedByDescending { it.listedAt }
            .toImmutableList()
    }

    suspend fun loadLocalMovies(): ImmutableList<FavoriteItem.MovieItem> {
        return localSource.getMovies()
            .sortedByDescending { it.listedAt }
            .toImmutableList()
    }

    suspend fun isShowsLoaded(): Boolean {
        return localSource.isShowsLoaded()
    }

    suspend fun isMoviesLoaded(): Boolean {
        return localSource.isMoviesLoaded()
    }

    suspend fun loadAll(): ImmutableList<FavoriteItem> {
        return coroutineScope {
            val showsAsync = async { loadShows() }
            val moviesAsync = async { loadMovies() }

            val shows = showsAsync.await()
            val movies = moviesAsync.await()

            (shows + movies)
                .sortedByDescending { it.listedAt }
                .toImmutableList()
        }
    }

    suspend fun loadShows(): ImmutableList<FavoriteItem> {
        return remoteSource.getFavoriteShows(
            sort = "added",
            extended = "full,cloud9,colors",
        ).asyncMap {
            val listedAt = it.listedAt.toInstant()
            FavoriteItem.ShowItem(
                show = Show.fromDto(it.show),
                rank = it.rank,
                listedAt = listedAt,
            )
        }
            .toImmutableList()
            .also { localSource.setShows(it) }
    }

    suspend fun loadMovies(): ImmutableList<FavoriteItem> {
        return remoteSource.getFavoriteMovies(
            sort = "added",
            extended = "full,cloud9,colors",
        ).asyncMap {
            val listedAt = it.listedAt.toInstant()
            FavoriteItem.MovieItem(
                movie = Movie.fromDto(it.movie),
                rank = it.rank,
                listedAt = listedAt,
            )
        }
            .toImmutableList()
            .also { localSource.setMovies(it) }
    }
}
