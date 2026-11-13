package tv.trakt.trakt.app.core.lists.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import tv.trakt.trakt.app.core.details.lists.details.media.model.ListMediaItem
import tv.trakt.trakt.common.data.local.ShowLocalSource
import tv.trakt.trakt.common.data.remote.ShowsSyncRemoteSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.sorting.Sorting

internal class GetListsShowsWatchlistUseCase(
    private val remoteSyncSource: ShowsSyncRemoteSource,
    private val localShowSource: ShowLocalSource,
    private val getListSortRatingsUseCase: GetListSortRatingsUseCase,
) {
    suspend fun getShows(
        limit: Int,
        sorting: Sorting,
        page: Int? = null,
    ): ImmutableList<ListMediaItem.ShowItem> {
        return coroutineScope {
            val ratingsAsync = async {
                getListSortRatingsUseCase.getShowRatings(sorting, refresh = page == null || page == 1)
            }

            val dtos = remoteSyncSource.getWatchlist(
                page = page,
                limit = limit,
                extended = "full,colors,streaming_ids",
                sorting = sorting,
            )
            val ratings = ratingsAsync.await()

            dtos
                .asyncMap {
                    val show = Show.fromDto(it.show)
                    ListMediaItem.ShowItem(
                        show = show,
                        listedAt = it.listedAt.toInstant(),
                        userRating = ratings[show.ids.trakt],
                    )
                }
                .toImmutableList()
                .also {
                    localShowSource.upsertShows(it.map { item -> item.show })
                }
        }
    }
}
