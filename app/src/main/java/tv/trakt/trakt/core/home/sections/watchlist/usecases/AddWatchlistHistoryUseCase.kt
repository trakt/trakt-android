package tv.trakt.trakt.core.home.sections.watchlist.usecases

import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.sync.usecases.UpdateMovieHistoryUseCase
import tv.trakt.trakt.core.user.data.local.UserWatchlistLocalDataSource

internal class AddWatchlistHistoryUseCase(
    private val updateHistoryUseCase: UpdateMovieHistoryUseCase,
    private val userWatchlistLocalSource: UserWatchlistLocalDataSource,
) {
    suspend fun addToHistory(movieId: TraktId) {
        updateHistoryUseCase.addToHistory(movieId)
        userWatchlistLocalSource.removeMovies(setOf(movieId))
    }
}
