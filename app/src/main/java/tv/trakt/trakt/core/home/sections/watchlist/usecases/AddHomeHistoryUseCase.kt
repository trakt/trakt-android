package tv.trakt.trakt.core.home.sections.watchlist.usecases

import tv.trakt.trakt.common.model.DateSelectionResult
import tv.trakt.trakt.common.model.SeasonEpisode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.sync.usecases.UpdateEpisodeHistoryUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateMovieHistoryUseCase
import tv.trakt.trakt.core.user.data.local.watchlist.UserWatchlistLocalDataSource
import tv.trakt.trakt.core.user.data.local.watchlist.minimal.UserWatchlistMinimalLocalDataSource

internal class AddHomeHistoryUseCase(
    private val updateMovieHistoryUseCase: UpdateMovieHistoryUseCase,
    private val updateEpisodeHistoryUseCase: UpdateEpisodeHistoryUseCase,
    private val userWatchlistLocalSource: UserWatchlistLocalDataSource,
    private val userWatchlistMinLocalSource: UserWatchlistMinimalLocalDataSource,
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
    }

    suspend fun addEpisodeToHistory(
        showId: TraktId,
        seasonEpisode: SeasonEpisode,
        customDate: DateSelectionResult? = null,
    ) {
        updateEpisodeHistoryUseCase.addToHistory(
            showId = showId,
            seasonEpisode = seasonEpisode,
            customDate = customDate,
        )

        userWatchlistLocalSource.removeShows(ids = setOf(showId))
        userWatchlistMinLocalSource.removeShows(ids = setOf(showId))
    }
}
