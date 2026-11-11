package tv.trakt.trakt.core.sync.usecases

import tv.trakt.trakt.common.core.lists.model.WatchlistItem
import tv.trakt.trakt.common.core.sync.changes.DataChange.Kind
import tv.trakt.trakt.common.core.sync.changes.DataChanges
import tv.trakt.trakt.common.data.local.UserWatchlistLocalSource
import tv.trakt.trakt.common.data.local.UserWatchlistMinimalLocalSource
import tv.trakt.trakt.common.data.remote.ShowsSyncRemoteSource
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId

internal class UpdateShowWatchlistUseCase(
    private val remoteSource: ShowsSyncRemoteSource,
    private val widgetsUpdater: tv.trakt.trakt.widgets.widget.calendar.CalendarWidgetUpdater,
    private val userWatchlistLocalSource: UserWatchlistLocalSource,
    private val userWatchlistMinLocalSource: UserWatchlistMinimalLocalSource,
    private val dataChanges: DataChanges,
) {
    suspend fun addToWatchlist(show: Show) {
        val showId = show.ids.trakt

        remoteSource.addToWatchlist(
            showId = showId,
        )
        widgetsUpdater.refreshInBackground()

        userWatchlistLocalSource.addShows(
            shows = listOf(
                WatchlistItem.ShowItem(
                    rank = 0,
                    show = show,
                    listedAt = nowUtcInstant(),
                ),
            ),
        )
        userWatchlistMinLocalSource.addShows(shows = setOf(showId))
        dataChanges.notify(Kind.Watchlist)
    }

    suspend fun removeFromWatchlist(showId: TraktId) {
        remoteSource.removeFromWatchlist(
            showId = showId,
        )
        widgetsUpdater.refreshInBackground()

        userWatchlistLocalSource.removeShows(ids = setOf(showId))
        userWatchlistMinLocalSource.removeShows(ids = setOf(showId))
        dataChanges.notify(Kind.Watchlist)
    }
}
