package tv.trakt.trakt.app.core.profile.sections.favorites.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
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
) {
    suspend fun getFavoriteShows(
        sorting: Sorting,
        page: Int = 1,
        limit: Int,
    ): ImmutableList<FavoriteItem.ShowItem> {
        return remoteUserSource.getFavoriteShows(
            extended = "full,colors,streaming_ids",
            sorting = sorting,
            pagination = Pagination(page = page, limit = limit),
        )
            .asyncMap {
                FavoriteItem.ShowItem(
                    show = Show.fromDto(it.show),
                    rank = it.rank,
                    listedAt = it.listedAt.toInstant(),
                )
            }
            .toImmutableList()
            .also {
                localShowsSource.upsertShows(it.map { item -> item.show })
            }
    }
}
