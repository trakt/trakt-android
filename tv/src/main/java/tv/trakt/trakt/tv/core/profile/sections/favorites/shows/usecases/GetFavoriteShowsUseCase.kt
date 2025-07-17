package tv.trakt.trakt.tv.core.profile.sections.favorites.shows.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.tv.core.profile.data.remote.ProfileRemoteDataSource
import tv.trakt.trakt.tv.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.tv.core.shows.model.Show
import tv.trakt.trakt.tv.core.shows.model.fromDto
import tv.trakt.trakt.tv.helpers.extensions.asyncMap

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
