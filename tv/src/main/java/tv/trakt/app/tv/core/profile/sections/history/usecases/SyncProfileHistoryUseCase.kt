package tv.trakt.app.tv.core.profile.sections.history.usecases

import tv.trakt.app.tv.core.sync.data.local.episodes.EpisodesSyncLocalDataSource
import tv.trakt.app.tv.core.sync.data.local.movies.MoviesSyncLocalDataSource
import tv.trakt.app.tv.core.sync.data.local.shows.ShowsSyncLocalDataSource
import java.time.ZonedDateTime

internal class SyncProfileHistoryUseCase(
    private val localShowsSyncSource: ShowsSyncLocalDataSource,
    private val localMoviesSyncSource: MoviesSyncLocalDataSource,
    private val localEpisodesSyncSource: EpisodesSyncLocalDataSource,
) {
    suspend fun isSyncRequired(loadedAt: ZonedDateTime?): Boolean {
        if (loadedAt == null) {
            return false
        }

        val localShowsWatchedUpdatedAt = localShowsSyncSource.getWatchedUpdatedAt()
        val localShowsWatchlistUpdatedAt = localShowsSyncSource.getWatchlistUpdatedAt()

        val localWatchedMoviesUpdatedAt = localMoviesSyncSource.getWatchedUpdatedAt()
        val localWatchlistMoviesUpdatedAt = localMoviesSyncSource.getWatchlistUpdatedAt()

        val localEpisodeHistoryUpdatedAt = localEpisodesSyncSource.getHistoryUpdatedAt()

        if (localShowsWatchlistUpdatedAt == null &&
            localShowsWatchedUpdatedAt == null &&
            localEpisodeHistoryUpdatedAt == null &&
            localWatchedMoviesUpdatedAt == null &&
            localWatchlistMoviesUpdatedAt == null
        ) {
            return false
        }

        if (localShowsWatchlistUpdatedAt?.isAfter(loadedAt) == true ||
            localShowsWatchedUpdatedAt?.isAfter(loadedAt) == true ||
            localEpisodeHistoryUpdatedAt?.isAfter(loadedAt) == true ||
            localWatchedMoviesUpdatedAt?.isAfter(loadedAt) == true ||
            localWatchlistMoviesUpdatedAt?.isAfter(loadedAt) == true
        ) {
            return true
        }

        return false
    }
}
