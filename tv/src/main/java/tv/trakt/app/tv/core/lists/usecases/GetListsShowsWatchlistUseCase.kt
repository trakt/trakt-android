package tv.trakt.app.tv.core.lists.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.app.tv.core.shows.data.local.ShowLocalDataSource
import tv.trakt.app.tv.core.shows.model.Show
import tv.trakt.app.tv.core.shows.model.fromDto
import tv.trakt.app.tv.core.sync.data.remote.shows.ShowsSyncRemoteDataSource
import tv.trakt.app.tv.helpers.extensions.asyncMap

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
            extended = "full,cloud9,colors",
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
