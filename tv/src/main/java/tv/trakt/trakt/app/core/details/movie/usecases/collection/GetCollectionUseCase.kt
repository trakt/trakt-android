package tv.trakt.trakt.app.core.details.movie.usecases.collection

import tv.trakt.trakt.app.core.sync.data.local.movies.MoviesSyncLocalDataSource
import tv.trakt.trakt.app.core.sync.data.remote.movies.MoviesSyncRemoteDataSource
import tv.trakt.trakt.app.core.sync.model.WatchedMovie
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.toZonedDateTime
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.toTraktId

internal class GetCollectionUseCase(
    private val remoteSource: MoviesSyncRemoteDataSource,
    private val syncLocalSource: MoviesSyncLocalDataSource,
) {
    suspend fun getWatchedMovie(movieId: TraktId): WatchedMovie? {
        var localWatched = syncLocalSource.getWatched()
        if (localWatched == null) {
            val remoteWatched = remoteSource
                .getWatched()
                .map { (movieId, plays) ->
                    WatchedMovie(
                        movieId = movieId.toInt().toTraktId(),
                        plays = plays.size,
                        lastWatchedAt = plays.maxOf { it.toZonedDateTime() },
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
