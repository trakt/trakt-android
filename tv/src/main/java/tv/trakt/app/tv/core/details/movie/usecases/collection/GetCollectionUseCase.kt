package tv.trakt.app.tv.core.details.movie.usecases.collection

import tv.trakt.app.tv.common.model.TraktId
import tv.trakt.app.tv.common.model.toTraktId
import tv.trakt.app.tv.core.sync.data.local.movies.MoviesSyncLocalDataSource
import tv.trakt.app.tv.core.sync.data.remote.movies.MoviesSyncRemoteDataSource
import tv.trakt.app.tv.core.sync.model.WatchedMovie
import tv.trakt.app.tv.helpers.extensions.asyncMap
import tv.trakt.app.tv.helpers.extensions.toZonedDateTime

internal class GetCollectionUseCase(
    private val remoteSource: MoviesSyncRemoteDataSource,
    private val syncLocalSource: MoviesSyncLocalDataSource,
) {
    suspend fun getWatchedMovie(movieId: TraktId): WatchedMovie? {
        var localWatched = syncLocalSource.getWatched()
        if (localWatched == null) {
            val remoteWatched = remoteSource
                .getWatched()
                .asyncMap {
                    WatchedMovie(
                        movieId = it.movie.ids.trakt.toTraktId(),
                        plays = it.plays,
                        lastWatchedAt = it.lastWatchedAt.toZonedDateTime(),
                    )
                }

            syncLocalSource.saveWatched(remoteWatched, null)
            localWatched = remoteWatched.associateBy { it.movieId }
        }

        return localWatched[movieId]
    }

    suspend fun getWatchlistMovie(movieId: TraktId): TraktId? {
        var localWatchlist = syncLocalSource.getWatchlist()

        if (localWatchlist == null) {
            val remoteWatchlist = remoteSource
                .getWatchlist(sort = "added")
                .asyncMap { it.movie.ids.trakt.toTraktId() }
                .toSet()

            syncLocalSource.saveWatchlist(remoteWatchlist, null)
            localWatchlist = remoteWatchlist.toSet()
        }

        return localWatchlist
            .find { it.value == movieId.value }
    }
}
