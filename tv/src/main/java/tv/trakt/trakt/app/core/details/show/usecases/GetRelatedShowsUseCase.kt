package tv.trakt.trakt.app.core.details.show.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.media.shows.local.ShowLocalDataSource
import tv.trakt.trakt.common.data.remote.ShowsRemoteSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto

internal class GetRelatedShowsUseCase(
    private val remoteSource: ShowsRemoteSource,
    private val localSource: ShowLocalDataSource,
) {
    suspend fun getRelatedShows(showId: TraktId): ImmutableList<Show> {
        return remoteSource.getRelated(
            showId = showId,
            limit = 20,
        )
            .asyncMap {
                Show.fromDto(it)
            }
            .toImmutableList()
            .also {
                localSource.upsertShows(it)
            }
    }
}
