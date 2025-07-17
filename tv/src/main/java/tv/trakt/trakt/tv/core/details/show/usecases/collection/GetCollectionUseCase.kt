package tv.trakt.trakt.tv.core.details.show.usecases.collection

import tv.trakt.trakt.tv.common.model.TraktId
import tv.trakt.trakt.tv.common.model.toTraktId
import tv.trakt.trakt.tv.core.sync.data.local.shows.ShowsSyncLocalDataSource
import tv.trakt.trakt.tv.core.sync.data.remote.shows.ShowsSyncRemoteDataSource
import tv.trakt.trakt.tv.core.sync.model.WatchedShow
import tv.trakt.trakt.tv.helpers.extensions.asyncMap
import tv.trakt.trakt.tv.helpers.extensions.toZonedDateTime

internal class GetCollectionUseCase(
    private val remoteSource: ShowsSyncRemoteDataSource,
    private val syncLocalSource: ShowsSyncLocalDataSource,
) {
    suspend fun getWatchedShow(showId: TraktId): WatchedShow? {
        var localWatched = syncLocalSource.getWatched()
        if (localWatched == null) {
            val remoteWatched = remoteSource
                .getWatched()
                .asyncMap {
                    WatchedShow(
                        showId = it.show.ids.trakt.toTraktId(),
                        episodesPlays = it.plays,
                        episodesAiredCount = it.show.airedEpisodes,
                        lastWatchedAt = it.lastWatchedAt.toZonedDateTime(),
                    )
                }

            syncLocalSource.saveWatched(remoteWatched, null)
            localWatched = remoteWatched.associateBy { it.showId }
        }

        return localWatched[showId]
    }

    suspend fun getWatchlistShow(showId: TraktId): TraktId? {
        var localWatchlist = syncLocalSource.getWatchlist()

        if (localWatchlist == null) {
            val remoteWatchlist = remoteSource
                .getWatchlist(sort = "added")
                .asyncMap { it.show.ids.trakt.toTraktId() }
                .toSet()

            syncLocalSource.saveWatchlist(remoteWatchlist, null)
            localWatchlist = remoteWatchlist.toSet()
        }

        return localWatchlist
            .find { it.value == showId.value }
    }
}
