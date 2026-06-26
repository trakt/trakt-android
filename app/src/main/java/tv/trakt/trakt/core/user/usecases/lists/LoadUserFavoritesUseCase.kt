package tv.trakt.trakt.core.user.usecases.lists

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import tv.trakt.trakt.common.core.user.data.remote.favorites.UserFavoritesRemoteDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.core.favorites.model.FavoriteItem
import tv.trakt.trakt.core.favorites.model.getFavoriteSorting
import tv.trakt.trakt.core.user.data.local.favorites.UserFavoritesLocalDataSource

/**
 * Loads the user's favorites from the remote source and updates the local cache.
 */
internal class LoadUserFavoritesUseCase(
    private val remoteSource: UserFavoritesRemoteDataSource,
    private val localSource: UserFavoritesLocalDataSource,
) {
    suspend fun loadLocalAll(): ImmutableList<FavoriteItem> {
        return localSource.getAll()
            .toImmutableList()
    }

    suspend fun loadLocalShows(): ImmutableList<FavoriteItem.ShowItem> {
        return localSource.getShows()
            .toImmutableList()
    }

    suspend fun loadLocalMovies(): ImmutableList<FavoriteItem.MovieItem> {
        return localSource.getMovies()
            .toImmutableList()
    }

    suspend fun isShowsLoaded(): Boolean {
        return localSource.isShowsLoaded()
    }

    suspend fun isMoviesLoaded(): Boolean {
        return localSource.isMoviesLoaded()
    }

    suspend fun loadAll(sort: Sorting? = null): ImmutableList<FavoriteItem> {
        return coroutineScope {
            val showsAsync = async { loadShows() }
            val moviesAsync = async { loadMovies() }

            val shows = showsAsync.await()
            val movies = moviesAsync.await()

            (shows + movies)
                .sortedWith(getFavoriteSorting(sort))
                .toImmutableList()
        }
    }

    suspend fun loadShows(sort: Sorting? = null): ImmutableList<FavoriteItem> {
        return remoteSource.getFavoriteShows(
            sorting = sort,
            extended = "full,cloud9,colors",
        ).asyncMap {
            val listedAt = it.listedAt.toInstant()
            FavoriteItem.ShowItem(
                show = Show.fromDto(it.show),
                rank = it.rank,
                listedAt = listedAt,
            )
        }
            .also { localSource.setShows(it) }
            .sortedWith(getFavoriteSorting(sort))
            .toImmutableList()
    }

    suspend fun loadMovies(sort: Sorting? = null): ImmutableList<FavoriteItem> {
        return remoteSource.getFavoriteMovies(
            sorting = sort,
            extended = "full,cloud9,colors",
        ).asyncMap {
            val listedAt = it.listedAt.toInstant()
            FavoriteItem.MovieItem(
                movie = Movie.fromDto(it.movie),
                rank = it.rank,
                listedAt = listedAt,
            )
        }
            .also { localSource.setMovies(it) }
            .sortedWith(getFavoriteSorting(sort))
            .toImmutableList()
    }
}
