package tv.trakt.trakt.core.sync.usecases

import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.sync.data.remote.shows.ShowsSyncRemoteDataSource

internal class UpdateShowWatchlistUseCase(
    private val remoteSource: ShowsSyncRemoteDataSource,
    private val widgetsUpdater: tv.trakt.trakt.widgets.widget.calendar.CalendarWidgetUpdater,
) {
    suspend fun addToWatchlist(showId: TraktId) {
        remoteSource.addToWatchlist(
            showId = showId,
        )
        widgetsUpdater.refreshInBackground()
    }

    suspend fun removeFromWatchlist(showId: TraktId) {
        remoteSource.removeFromWatchlist(
            showId = showId,
        )
        widgetsUpdater.refreshInBackground()
    }
}
