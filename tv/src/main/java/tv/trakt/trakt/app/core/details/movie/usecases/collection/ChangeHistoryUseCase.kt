package tv.trakt.trakt.app.core.details.movie.usecases.collection

import tv.trakt.trakt.app.core.sync.data.local.movies.MoviesSyncLocalDataSource
import tv.trakt.trakt.app.core.sync.data.remote.movies.MoviesSyncRemoteDataSource
import tv.trakt.trakt.app.core.sync.model.WatchedMovie
import tv.trakt.trakt.common.helpers.extensions.nowUtc
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.model.DateSelectionResult
import tv.trakt.trakt.common.model.TraktId

internal class ChangeHistoryUseCase(
    private val remoteSource: MoviesSyncRemoteDataSource,
    private val syncLocalSource: MoviesSyncLocalDataSource,
) {
    suspend fun addToHistory(
        movieId: TraktId,
        plays: Int,
        customDate: DateSelectionResult?,
    ) {
        val timestamp = nowUtc()
        val watchedAt = customDate?.dateString
            ?: nowUtcInstant().toString()

        remoteSource.addToHistory(
            movieId = movieId,
            watchedAt = watchedAt,
        )

        with(syncLocalSource) {
            saveWatched(
                listOf(
                    WatchedMovie(
                        movieId = movieId,
                        plays = plays + 1,
                        lastWatchedAt = timestamp,
                    ),
                ),
                timestamp,
            )
            removeWatchlist(setOf(movieId), timestamp)
        }
    }

    suspend fun removeFromHistory(movieId: TraktId) {
        remoteSource.removeFromHistory(movieId)

        with(syncLocalSource) {
            removeWatched(setOf(movieId))
        }
    }
}
