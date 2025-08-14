package tv.trakt.trakt.core.shows.sections.hot.usecase

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.shows.data.remote.ShowsRemoteDataSource
import tv.trakt.trakt.core.shows.model.WatchersShow
import tv.trakt.trakt.core.shows.sections.hot.data.local.HotShowsLocalDataSource
import java.time.Instant

internal class GetHotShowsUseCase(
    private val remoteSource: ShowsRemoteDataSource,
    private val localHotSource: HotShowsLocalDataSource,
) {
    suspend fun getLocalShows(): ImmutableList<WatchersShow> {
        return localHotSource.getShows()
            .sortedByDescending { it.watchers }
            .toImmutableList()
    }

    suspend fun getShows(): ImmutableList<WatchersShow> {
        return remoteSource.getHot(20)
            .asyncMap {
                WatchersShow(
                    watchers = it.watchers,
                    show = Show.fromDto(it.show),
                )
            }
            .toImmutableList()
            .also { shows ->
                localHotSource.addShows(
                    shows = shows,
                    addedAt = Instant.now(),
                )
            }
    }
}
