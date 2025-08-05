package tv.trakt.trakt.app.core.shows.usecase

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.app.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.app.core.shows.data.remote.ShowsRemoteDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto

internal class GetPopularShowsUseCase(
    private val remoteSource: ShowsRemoteDataSource,
    private val localSource: ShowLocalDataSource,
) {
    suspend fun getPopularShows(): ImmutableList<Show> {
        return remoteSource.getPopularShows()
            .asyncMap {
                Show.fromDto(it)
            }
            .toImmutableList()
            .also {
                localSource.upsertShows(it)
            }
    }
}
