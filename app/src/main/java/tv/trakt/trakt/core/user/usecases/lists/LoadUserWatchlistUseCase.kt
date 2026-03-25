package tv.trakt.trakt.core.user.usecases.lists

import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toImmutableSet
import tv.trakt.trakt.common.core.user.data.remote.watchlist.UserWatchlistRemoteDataSource
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.user.data.local.watchlist.minimal.UserWatchlistMinimalLocalDataSource

internal class LoadUserWatchlistUseCase(
    private val remoteSource: UserWatchlistRemoteDataSource,
    private val localSource: UserWatchlistMinimalLocalDataSource,
) {
    suspend fun loadLocalShows(): ImmutableSet<TraktId> {
        return localSource.getShows()
            .toImmutableSet()
    }

    suspend fun loadLocalMovies(): ImmutableSet<TraktId> {
        return localSource.getMovies()
            .toImmutableSet()
    }

    suspend fun isShowsLoaded(): Boolean {
        return localSource.isShowsLoaded()
    }

    suspend fun isMoviesLoaded(): Boolean {
        return localSource.isMoviesLoaded()
    }

    suspend fun isLoaded(): Boolean {
        return localSource.isShowsLoaded() &&
            localSource.isMoviesLoaded()
    }

    suspend fun loadWatchlist(): Pair<ImmutableSet<TraktId>, ImmutableSet<TraktId>> {
        val (shows, movies) = remoteSource.getWatchlistMinimal()

        with(localSource) {
            setShows(shows)
            setMovies(movies)
        }

        return shows.toImmutableSet() to movies.toImmutableSet()
    }
}
