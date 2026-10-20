package tv.trakt.trakt.app.core.profile.sections.history.usecases

import tv.trakt.trakt.app.core.sync.data.local.episodes.EpisodesSyncLocalSource
import tv.trakt.trakt.app.core.sync.data.local.movies.MoviesSyncLocalSource
import tv.trakt.trakt.app.core.sync.data.local.shows.ShowsSyncLocalSource
import java.time.ZonedDateTime

internal class SyncProfileHistoryUseCase(
    private val localShowsSyncSource: ShowsSyncLocalSource,
    private val localMoviesSyncSource: MoviesSyncLocalSource,
    private val localEpisodesSyncSource: EpisodesSyncLocalSource,
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
