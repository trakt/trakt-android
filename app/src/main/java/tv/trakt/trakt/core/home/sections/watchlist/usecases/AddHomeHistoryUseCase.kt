package tv.trakt.trakt.core.home.sections.watchlist.usecases

import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.sync.usecases.UpdateEpisodeHistoryUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateMovieHistoryUseCase
import tv.trakt.trakt.core.user.data.local.UserWatchlistLocalDataSource

internal class AddHomeHistoryUseCase(
    private val updateMovieHistoryUseCase: UpdateMovieHistoryUseCase,
    private val updateEpisodeHistoryUseCase: UpdateEpisodeHistoryUseCase,
    private val userWatchlistLocalSource: UserWatchlistLocalDataSource,
) {
    suspend fun addMovieToHistory(movieId: TraktId) {
        updateMovieHistoryUseCase.addToWatched(movieId)
        userWatchlistLocalSource.removeMovies(
            ids = setOf(movieId),
            notify = true,
        )
    }

    suspend fun addEpisodeToHistory(
        showId: TraktId,
        episodeId: TraktId,
    ) {
        updateEpisodeHistoryUseCase.addToHistory(episodeId)
        userWatchlistLocalSource.removeShows(
            ids = setOf(showId),
            notify = true,
        )
    }
}
