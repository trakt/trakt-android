package tv.trakt.trakt.app.core.lists.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.app.core.lists.filters.TvListPage
import tv.trakt.trakt.app.core.lists.filters.TvListRequest
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.core.user.data.remote.watchlist.UserWatchlistRemoteDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto

internal class GetListsShowsWatchlistUseCase(
    private val remoteSource: UserWatchlistRemoteDataSource,
    private val localShowSource: ShowLocalDataSource,
) {
    suspend fun getShows(request: TvListRequest): TvListPage<Show> {
        val response = remoteSource.getWatchlistShows(
            page = request.page,
            limit = request.limit,
            extended = "full,cloud9,colors,streaming_ids",
            sorting = request.sorting,
            filters = request.filter,
        )
        val shows: ImmutableList<Show> = response.asyncMap {
            Show.fromDto(it.show)
        }.toImmutableList()

        localShowSource.upsertShows(shows)

        return TvListPage(
            items = shows,
            nextPage = request.page + 1,
            hasMore = response.size >= request.limit,
        )
    }
}
