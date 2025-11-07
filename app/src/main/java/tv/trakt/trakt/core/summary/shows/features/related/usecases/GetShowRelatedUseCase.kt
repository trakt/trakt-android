package tv.trakt.trakt.core.summary.shows.features.related.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.discover.data.remote.ShowsRemoteDataSource

internal class GetShowRelatedUseCase(
    private val remoteSource: ShowsRemoteDataSource,
    private val localSource: ShowLocalDataSource,
) {
    suspend fun getRelatedShows(showId: TraktId): ImmutableList<Show> {
        val relatedShows = remoteSource.getRelated(showId)

        val shows = relatedShows
            .take(30)
            .asyncMap { Show.fromDto(it) }

        localSource.upsertShows(shows)

        return shows.toImmutableList()
    }
}
