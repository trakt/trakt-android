package tv.trakt.trakt.core.summary.shows.features.related.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.data.local.ShowLocalSource
import tv.trakt.trakt.common.data.remote.ShowsRemoteSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto

internal class GetShowRelatedUseCase(
    private val remoteSource: ShowsRemoteSource,
    private val localSource: ShowLocalSource,
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
