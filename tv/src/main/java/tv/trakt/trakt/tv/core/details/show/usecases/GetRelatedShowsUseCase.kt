package tv.trakt.trakt.tv.core.details.show.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.tv.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.tv.core.shows.data.remote.ShowsRemoteDataSource
import tv.trakt.trakt.tv.core.shows.model.Show
import tv.trakt.trakt.tv.core.shows.model.fromDto
import tv.trakt.trakt.tv.helpers.extensions.asyncMap

internal class GetRelatedShowsUseCase(
    private val remoteSource: ShowsRemoteDataSource,
    private val localSource: ShowLocalDataSource,
) {
    suspend fun getRelatedShows(showId: TraktId): ImmutableList<Show> {
        return remoteSource.getRelatedShows(showId)
            .asyncMap {
                Show.fromDto(it)
            }
            .toImmutableList()
            .also {
                localSource.upsertShows(it)
            }
    }
}
