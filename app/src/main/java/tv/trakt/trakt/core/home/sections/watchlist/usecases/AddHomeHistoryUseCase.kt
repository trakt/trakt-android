package tv.trakt.trakt.core.home.sections.watchlist.usecases

import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.sync.usecases.UpdateEpisodeHistoryUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateMovieHistoryUseCase
import tv.trakt.trakt.core.user.data.local.watchlist.UserWatchlistLocalDataSource
import tv.trakt.trakt.core.user.data.local.watchlist.WatchlistUpdates
import tv.trakt.trakt.core.user.data.local.watchlist.WatchlistUpdates.Source.Default
import tv.trakt.trakt.core.user.data.local.watchlist.minimal.UserWatchlistMinimalLocalDataSource
import tv.trakt.trakt.ui.components.dateselection.DateSelectionResult

internal class AddHomeHistoryUseCase(
    private val updateMovieHistoryUseCase: UpdateMovieHistoryUseCase,
    private val updateEpisodeHistoryUseCase: UpdateEpisodeHistoryUseCase,
    private val userWatchlistLocalSource: UserWatchlistLocalDataSource,
    private val userWatchlistMinLocalSource: UserWatchlistMinimalLocalDataSource,
    private val watchlistUpdates: WatchlistUpdates,
) {
    suspend fun addMovieToHistory(
        movieId: TraktId,
        customDate: DateSelectionResult? = null,
    ) {
        updateMovieHistoryUseCase.addToWatched(
            movieId,
            customDate,
        )

        userWatchlistLocalSource.removeMovies(ids = setOf(movieId))
        userWatchlistMinLocalSource.removeMovies(ids = setOf(movieId))

        watchlistUpdates.notifyUpdate(Default)
    }

    suspend fun addEpisodeToHistory(
        showId: TraktId,
        episodeId: TraktId,
        customDate: DateSelectionResult? = null,
    ) {
        updateEpisodeHistoryUseCase.addToHistory(
            episodeId,
            customDate,
        )

        userWatchlistLocalSource.removeShows(ids = setOf(showId))
        userWatchlistMinLocalSource.removeShows(ids = setOf(showId))

        watchlistUpdates.notifyUpdate(Default)
    }
}
