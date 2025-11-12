package tv.trakt.trakt.core.home.sections.watchlist.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.helpers.extensions.toZonedDateTime
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.networking.ProgressShowDto
import tv.trakt.trakt.core.home.sections.upnext.model.Progress
import tv.trakt.trakt.core.home.sections.watchlist.data.local.HomeWatchlistLocalDataSource
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem
import tv.trakt.trakt.core.sync.data.remote.shows.ShowsSyncRemoteDataSource

private val SortComparator =
    compareByDescending<WatchlistItem> { it.released }
        .thenBy { it.title }

internal class GetHomeShowsWatchlistUseCase(
    private val remoteShowsSyncSource: ShowsSyncRemoteDataSource,
    private val homeWatchlistLocalSource: HomeWatchlistLocalDataSource,
) {
    suspend fun getLocalWatchlist(limit: Int? = null): ImmutableList<WatchlistItem> {
        return homeWatchlistLocalSource.getItems()
            .sortedWith(SortComparator)
            .take(limit ?: Int.MAX_VALUE)
            .toImmutableList()
    }

    suspend fun getWatchlist(limit: Int? = null): ImmutableList<WatchlistItem> {
        return remoteShowsSyncSource.getUpNext(
            limit = limit ?: Int.MAX_VALUE,
            page = 1,
            intent = "start",
        ).asyncMap {
            mapShowItem(it)
        }.also {
            homeWatchlistLocalSource.setItems(items = it)
        }
            .sortedWith(SortComparator)
            .take(limit ?: Int.MAX_VALUE)
            .toImmutableList()
    }

    private fun mapShowItem(item: ProgressShowDto): WatchlistItem.ShowItem {
        return WatchlistItem.ShowItem(
            show = Show.fromDto(item.show),
            progress = Progress(
                lastWatchedAt = item.progress.lastWatchedAt?.toZonedDateTime(),
                aired = item.progress.aired,
                completed = item.progress.completed,
                stats = item.progress.stats?.let {
                    Progress.Stats(
                        playCount = it.playCount,
                        minutesWatched = it.minutesWatched,
                        minutesLeft = it.minutesLeft,
                    )
                },
                lastEpisode = item.progress.lastEpisode?.let {
                    Episode.fromDto(it)
                },
                nextEpisode = Episode.fromDto(item.progress.nextEpisode),
            ),
            rank = 0,
            listedAt = nowUtcInstant(),
        )
    }
}
