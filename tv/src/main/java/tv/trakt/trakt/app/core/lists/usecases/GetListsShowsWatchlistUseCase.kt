package tv.trakt.trakt.app.core.lists.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.app.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.app.core.sync.data.remote.shows.ShowsSyncRemoteDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto

internal class GetListsShowsWatchlistUseCase(
    private val remoteSyncSource: ShowsSyncRemoteDataSource,
    private val localShowSource: ShowLocalDataSource,
) {
    suspend fun getShows(
        limit: Int,
        page: Int? = null,
    ): ImmutableList<Show> {
        val shows = remoteSyncSource.getWatchlist(
            page = page,
            limit = limit,
            extended = "full,cloud9,colors,streaming_ids",
            sort = "added",
        ).sortedByDescending {
            it.listedAt
        }.asyncMap {
            Show.fromDto(it.show)
        }

        return shows
            .toImmutableList()
            .also {
                localShowSource.upsertShows(it)
            }
    }
}
