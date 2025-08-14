package tv.trakt.trakt.core.shows.sections.anticipated.usecase

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.shows.data.remote.ShowsRemoteDataSource
import tv.trakt.trakt.core.shows.model.WatchersShow
import tv.trakt.trakt.core.shows.sections.anticipated.data.local.AnticipatedShowsLocalDataSource
import java.time.Instant

internal class GetAnticipatedShowsUseCase(
    private val remoteSource: ShowsRemoteDataSource,
    private val localAnticipatedSource: AnticipatedShowsLocalDataSource,
) {
    suspend fun getLocalShows(): ImmutableList<WatchersShow> {
        return localAnticipatedSource.getShows()
            .sortedByDescending { it.watchers }
            .toImmutableList()
    }

    suspend fun getShows(): ImmutableList<WatchersShow> {
        return remoteSource.getAnticipated(20)
            .asyncMap {
                WatchersShow(
                    watchers = it.listCount,
                    show = Show.fromDto(it.show),
                )
            }
            .toImmutableList()
            .also { shows ->
                localAnticipatedSource.addShows(
                    shows = shows,
                    addedAt = Instant.now(),
                )
            }
    }
}
