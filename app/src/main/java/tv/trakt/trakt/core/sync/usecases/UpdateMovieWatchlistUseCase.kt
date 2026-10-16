package tv.trakt.trakt.core.sync.usecases

import tv.trakt.trakt.common.core.sync.data.remote.movies.MoviesSyncRemoteDataSource
import tv.trakt.trakt.common.model.TraktId

internal class UpdateMovieWatchlistUseCase(
    private val remoteSource: MoviesSyncRemoteDataSource,
    private val widgetsUpdater: tv.trakt.trakt.widgets.widget.calendar.CalendarWidgetUpdater,
) {
    suspend fun addToWatchlist(movieId: TraktId) {
        remoteSource.addToWatchlist(
            movieId = movieId,
        )
        widgetsUpdater.refreshInBackground()
    }

    suspend fun removeFromWatchlist(movieId: TraktId) {
        remoteSource.removeFromWatchlist(
            movieId = movieId,
        )
        widgetsUpdater.refreshInBackground()
    }
}
