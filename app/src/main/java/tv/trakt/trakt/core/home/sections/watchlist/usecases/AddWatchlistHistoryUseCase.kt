package tv.trakt.trakt.core.home.sections.watchlist.usecases

import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.lists.sections.watchlist.data.local.ListsWatchlistLocalDataSource
import tv.trakt.trakt.core.sync.usecases.UpdateMovieHistoryUseCase

internal class AddWatchlistHistoryUseCase(
    private val updateHistoryUseCase: UpdateMovieHistoryUseCase,
    private val listsWatchlistLocalSource: ListsWatchlistLocalDataSource,
    private val listsWatchlistMoviesLocalSource: ListsWatchlistLocalDataSource,
) {
    suspend fun addToHistory(movieId: TraktId) {
        updateHistoryUseCase.addToHistory(movieId)

        listsWatchlistLocalSource.deleteItems(setOf(movieId))
        listsWatchlistMoviesLocalSource.deleteItems(setOf(movieId))
    }
}
