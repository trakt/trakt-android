package tv.trakt.trakt.core.user.usecase.progress

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.sync.model.ProgressItem
import tv.trakt.trakt.core.user.data.local.UserProgressLocalDataSource
import tv.trakt.trakt.core.user.data.remote.UserRemoteDataSource

/**
 * Loads the user's watchlist from the remote source and updates the local cache.
 */
internal class LoadUserProgressUseCase(
    private val remoteSource: UserRemoteDataSource,
    private val localSource: UserProgressLocalDataSource,
) {
    suspend fun loadLocalAll(): ImmutableList<ProgressItem> {
        return localSource.getAll()
            .toImmutableList()
    }

    suspend fun loadLocalShows(): ImmutableList<ProgressItem.ShowItem> {
        return localSource.getShows()
            .toImmutableList()
    }

    suspend fun loadLocalMovies(): ImmutableList<ProgressItem.MovieItem> {
        return localSource.getMovies()
            .toImmutableList()
    }

    suspend fun isLoaded(): Boolean {
        return localSource.isMoviesLoaded() && localSource.isShowsLoaded()
    }

    suspend fun loadMoviesProgress(): ImmutableList<ProgressItem.MovieItem> {
        val response = remoteSource.getWatchedMovies().asyncMap {
            ProgressItem.MovieItem(
                plays = it.plays,
                lastWatchedAt = it.lastWatchedAt.toInstant(),
                lastUpdatedAt = it.lastUpdatedAt.toInstant(),
                movie = Movie.fromDto(it.movie),
            )
        }

        with(localSource) {
//            setShows(response.filterIsInstance<ProgressItem.ShowItem>())
            setMovies(response)
        }

        return response.toImmutableList()
    }
}
