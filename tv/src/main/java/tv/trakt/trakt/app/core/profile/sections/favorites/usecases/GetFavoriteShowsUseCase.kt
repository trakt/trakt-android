package tv.trakt.trakt.app.core.profile.sections.favorites.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.media.shows.local.ShowLocalDataSource
import tv.trakt.trakt.common.data.remote.UserFavoritesRemoteSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.common.model.sorting.Sorting

internal class GetFavoriteShowsUseCase(
    private val remoteUserSource: UserFavoritesRemoteSource,
    private val localShowsSource: ShowLocalDataSource,
) {
    suspend fun getFavoriteShows(
        page: Int = 1,
        limit: Int,
    ): ImmutableList<Show> {
        return remoteUserSource.getFavoriteShows(
            extended = "full,images,colors,streaming_ids",
            sorting = Sorting.RecentlyAdded,
            pagination = Pagination(page = page, limit = limit),
        )
            .asyncMap { Show.fromDto(it.show) }
            .toImmutableList()
            .also {
                localShowsSource.upsertShows(it)
            }
    }
}
