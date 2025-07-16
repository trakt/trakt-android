package tv.trakt.app.tv.core.shows.usecase

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.app.tv.core.shows.data.local.ShowLocalDataSource
import tv.trakt.app.tv.core.shows.data.remote.ShowsRemoteDataSource
import tv.trakt.app.tv.core.shows.model.Show
import tv.trakt.app.tv.core.shows.model.fromDto
import tv.trakt.app.tv.helpers.extensions.asyncMap

internal class GetRecommendedShowsUseCase(
    private val remoteSource: ShowsRemoteDataSource,
    private val localSource: ShowLocalDataSource,
) {
    suspend fun getRecommendedShows(): ImmutableList<Show> {
        return remoteSource.getRecommendedShows()
            .asyncMap {
                Show.fromDto(it)
            }
            .toImmutableList()
            .also {
                localSource.upsertShows(it)
            }
    }
}
