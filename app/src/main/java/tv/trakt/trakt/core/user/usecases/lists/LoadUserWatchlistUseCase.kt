package tv.trakt.trakt.core.user.usecases.lists

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem
import tv.trakt.trakt.core.user.data.local.UserWatchlistLocalDataSource
import tv.trakt.trakt.core.user.data.remote.UserRemoteDataSource

/**
 * Loads the user's watchlist from the remote source and updates the local cache.
 */
internal class LoadUserWatchlistUseCase(
    private val remoteSource: UserRemoteDataSource,
    private val localSource: UserWatchlistLocalDataSource,
) {
    suspend fun loadLocalAll(): ImmutableList<WatchlistItem> {
        return localSource.getAll()
            .toImmutableList()
    }

    suspend fun loadLocalShows(): ImmutableList<WatchlistItem.ShowItem> {
        return localSource.getShows()
            .toImmutableList()
    }

    suspend fun loadLocalMovies(): ImmutableList<WatchlistItem.MovieItem> {
        return localSource.getMovies()
            .toImmutableList()
    }

    suspend fun isShowsLoaded(): Boolean {
        return localSource.isShowsLoaded()
    }

    suspend fun isMoviesLoaded(): Boolean {
        return localSource.isMoviesLoaded()
    }

    suspend fun isLoaded(): Boolean {
        return localSource.isShowsLoaded() && localSource.isMoviesLoaded()
    }

    suspend fun loadWatchlist(): ImmutableList<WatchlistItem> {
        val response = remoteSource.getWatchlist(
            sort = "rank",
            extended = "full,cloud9,colors",
        ).asyncMap {
            val listedAt = it.listedAt.toInstant()

            when {
                it.movie != null -> {
                    WatchlistItem.MovieItem(
                        movie = Movie.fromDto(it.movie!!),
                        rank = it.rank,
                        listedAt = listedAt,
                    )
                }
                it.show != null -> {
                    WatchlistItem.ShowItem(
                        show = Show.fromDto(it.show!!),
                        rank = it.rank,
                        listedAt = listedAt,
                    )
                }
                else -> throw IllegalStateException("Watchlist item unknown type!")
            }
        }

        with(localSource) {
            setShows(response.filterIsInstance<WatchlistItem.ShowItem>())
            setMovies(response.filterIsInstance<WatchlistItem.MovieItem>())
        }

        return response.toImmutableList()
    }
}
