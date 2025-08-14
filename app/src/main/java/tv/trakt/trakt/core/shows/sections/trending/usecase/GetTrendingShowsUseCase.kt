package tv.trakt.trakt.core.shows.sections.trending.usecase

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.shows.data.remote.ShowsRemoteDataSource
import tv.trakt.trakt.core.shows.model.WatchersShow
import tv.trakt.trakt.core.shows.sections.trending.data.local.TrendingShowsLocalDataSource
import java.time.Instant

internal class GetTrendingShowsUseCase(
    private val remoteSource: ShowsRemoteDataSource,
    private val localTrendingSource: TrendingShowsLocalDataSource,
) {
    suspend fun getLocalShows(): ImmutableList<WatchersShow> {
        return localTrendingSource.getShows()
            .asyncMap { entity ->
                WatchersShow(
                    watchers = entity.watchers,
                    show = entity.show,
                )
            }
            .sortedByDescending { it.watchers }
            .toImmutableList()
    }

    suspend fun getShows(): ImmutableList<WatchersShow> {
        return remoteSource.getTrending(20)
            .asyncMap {
                WatchersShow(
                    watchers = it.watchers,
                    show = Show.fromDto(it.show),
                )
            }
            .toImmutableList()
            .also { shows ->
                localTrendingSource.addShows(
                    shows = shows,
                    addedAt = Instant.now(),
                )
            }
    }
}
