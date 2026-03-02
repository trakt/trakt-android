package tv.trakt.trakt.app.core.home.sections.startwatching.usecases

import tv.trakt.trakt.app.core.home.sections.shows.upnext.model.Progress
import tv.trakt.trakt.app.core.home.sections.startwatching.model.WatchlistItem
import tv.trakt.trakt.app.core.sync.data.remote.shows.ShowsSyncRemoteDataSource
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.nowLocal
import tv.trakt.trakt.common.helpers.extensions.nowUtc
import tv.trakt.trakt.common.helpers.extensions.toZonedDateTime
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.pagination.Pagination

private val SortComparator =
    compareByDescending<WatchlistItem> { it.released }
        .thenByDescending { it.listedAt }

internal class GetHomeShowsWatchlistItemsUseCase(
    private val remoteSyncSource: ShowsSyncRemoteDataSource,
    private val localShowsSource: ShowLocalDataSource,
) {
    suspend fun getItems(pagination: Pagination): List<WatchlistItem> {
        val nowDay = nowLocal().toString()

        val response = remoteSyncSource.getUpNextProgress(
            page = pagination.page,
            limit = pagination.limit,
            intent = "start",
        ).filter {
            !it.show.firstAired.isNullOrBlank() && it.show.firstAired!! <= nowDay
        }.asyncMap { item ->
            WatchlistItem.ShowItem(
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
                    nextEpisode = Episode.fromDto(item.progress.nextEpisode),
                    lastEpisode = null,
                ),
                rank = 0,
                listedAt = nowUtc(),
            )
        }.sortedWith(SortComparator)

        return response
            .also {
                val shows = response.asyncMap { it.show }
                localShowsSource.upsertShows(shows)
            }
    }
}
