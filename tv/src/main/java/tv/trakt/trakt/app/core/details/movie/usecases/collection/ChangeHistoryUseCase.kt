package tv.trakt.trakt.app.core.details.movie.usecases.collection

import tv.trakt.trakt.app.core.sync.data.local.movies.MoviesSyncLocalDataSource
import tv.trakt.trakt.app.core.sync.data.remote.movies.MoviesSyncRemoteDataSource
import tv.trakt.trakt.app.core.sync.model.WatchedMovie
import tv.trakt.trakt.common.helpers.extensions.nowUtc
import tv.trakt.trakt.common.model.TraktId

internal class ChangeHistoryUseCase(
    private val remoteSource: MoviesSyncRemoteDataSource,
    private val syncLocalSource: MoviesSyncLocalDataSource,
) {
    suspend fun addToHistory(
        movieId: TraktId,
        plays: Int,
    ) {
        val watchedAt = nowUtc()

        remoteSource.addToHistory(
            movieId = movieId,
            watchedAt = watchedAt,
        )

        with(syncLocalSource) {
            val timestamp = nowUtc()
            saveWatched(
                listOf(
                    WatchedMovie(
                        movieId = movieId,
                        plays = plays + 1,
                        lastWatchedAt = watchedAt,
                    ),
                ),
                timestamp,
            )
            removeWatchlist(setOf(movieId), timestamp)
        }
    }
}
