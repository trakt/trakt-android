package tv.trakt.trakt.app.core.profile.sections.favorites.shows.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.app.core.profile.data.remote.ProfileRemoteDataSource
import tv.trakt.trakt.app.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.app.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto

internal class GetFavoriteShowsUseCase(
    private val remoteUserSource: ProfileRemoteDataSource,
    private val localShowsSource: ShowLocalDataSource,
) {
    suspend fun getFavoriteShows(
        page: Int = 1,
        limit: Int,
    ): ImmutableList<Show> {
        return remoteUserSource.getUserFavoriteShows(page, limit)
            .asyncMap { Show.fromDto(it.show) }
            .toImmutableList()
            .also {
                localShowsSource.upsertShows(it)
            }
    }
}
