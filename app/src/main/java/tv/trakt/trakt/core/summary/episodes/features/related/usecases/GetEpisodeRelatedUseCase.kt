package tv.trakt.trakt.core.summary.episodes.features.related.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.shows.data.remote.ShowsRemoteDataSource

internal class GetEpisodeRelatedUseCase(
    private val remoteSource: ShowsRemoteDataSource,
    private val localSource: ShowLocalDataSource,
) {
    suspend fun getRelatedShows(show: Show): ImmutableList<Show> {
        val relatedShows = remoteSource.getRelated(show.ids.trakt)

        val shows = relatedShows
            .take(30)
            .asyncMap { Show.fromDto(it) }

        localSource.upsertShows(shows)

        return shows.toImmutableList()
    }
}
