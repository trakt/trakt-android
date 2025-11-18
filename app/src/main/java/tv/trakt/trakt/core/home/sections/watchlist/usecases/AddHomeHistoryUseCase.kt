package tv.trakt.trakt.core.home.sections.watchlist.usecases

import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.sync.usecases.UpdateEpisodeHistoryUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateMovieHistoryUseCase
import tv.trakt.trakt.core.user.data.local.UserWatchlistLocalDataSource
import tv.trakt.trakt.ui.components.dateselection.DateSelectionResult

internal class AddHomeHistoryUseCase(
    private val updateMovieHistoryUseCase: UpdateMovieHistoryUseCase,
    private val updateEpisodeHistoryUseCase: UpdateEpisodeHistoryUseCase,
    private val userWatchlistLocalSource: UserWatchlistLocalDataSource,
) {
    suspend fun addMovieToHistory(
        movieId: TraktId,
        customDate: DateSelectionResult? = null,
    ) {
        updateMovieHistoryUseCase.addToWatched(
            movieId,
            customDate,
        )
        userWatchlistLocalSource.removeMovies(
            ids = setOf(movieId),
            notify = true,
        )
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
        userWatchlistLocalSource.removeShows(
            ids = setOf(showId),
            notify = true,
        )
    }
}
