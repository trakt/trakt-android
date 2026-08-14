package tv.trakt.trakt.core.sync.usecases

import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.sync.data.remote.movies.MoviesSyncRemoteDataSource
import tv.trakt.trakt.widgets.calendar.CalendarWidgetUpdater

internal class UpdateMovieWatchlistUseCase(
    private val remoteSource: MoviesSyncRemoteDataSource,
    private val widgetsUpdater: CalendarWidgetUpdater,
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
