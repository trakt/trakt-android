package tv.trakt.trakt.app.core.lists.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.data.local.ShowLocalSource
import tv.trakt.trakt.common.data.remote.ShowsSyncRemoteSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.sorting.Sorting

internal class GetListsShowsWatchlistUseCase(
    private val remoteSyncSource: ShowsSyncRemoteSource,
    private val localShowSource: ShowLocalSource,
) {
    suspend fun getShows(
        limit: Int,
        page: Int? = null,
    ): ImmutableList<Show> {
        val shows = remoteSyncSource.getWatchlist(
            page = page,
            limit = limit,
            extended = "full,images,colors,streaming_ids",
            sorting = Sorting.RecentlyAdded,
        ).asyncMap {
            Show.fromDto(it.show)
        }

        return shows
            .toImmutableList()
            .also {
                localShowSource.upsertShows(it)
            }
    }
}
