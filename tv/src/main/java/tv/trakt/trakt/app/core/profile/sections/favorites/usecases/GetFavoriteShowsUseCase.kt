package tv.trakt.trakt.app.core.profile.sections.favorites.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import tv.trakt.trakt.app.core.lists.usecases.GetListSortRatingsUseCase
import tv.trakt.trakt.app.core.profile.sections.favorites.model.FavoriteItem
import tv.trakt.trakt.common.data.local.ShowLocalSource
import tv.trakt.trakt.common.data.remote.UserFavoritesRemoteSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.common.model.sorting.Sorting

internal class GetFavoriteShowsUseCase(
    private val remoteUserSource: UserFavoritesRemoteSource,
    private val localShowsSource: ShowLocalSource,
    private val getListSortRatingsUseCase: GetListSortRatingsUseCase,
) {
    suspend fun getFavoriteShows(
        sorting: Sorting,
        page: Int = 1,
        limit: Int,
    ): ImmutableList<FavoriteItem.ShowItem> {
        return coroutineScope {
            val ratingsAsync = async {
                getListSortRatingsUseCase.getShowRatings(sorting, refresh = true)
            }

            val shows = remoteUserSource.getFavoriteShows(
                extended = "full,colors,streaming_ids",
                sorting = sorting,
                pagination = Pagination(page = page, limit = limit),
            ).asyncMap { Show.fromDto(it.show) to it }

            val ratings = ratingsAsync.await()
            shows
                .map { (show, dto) ->
                    FavoriteItem.ShowItem(
                        show = show,
                        rank = dto.rank,
                        listedAt = dto.listedAt.toInstant(),
                        userRating = ratings[show.ids.trakt],
                    )
                }
                .toImmutableList()
                .also {
                    localShowsSource.upsertShows(it.map { item -> item.show })
                }
        }
    }
}
