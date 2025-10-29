package tv.trakt.trakt.core.shows.sections.trending.usecases.trending

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.shows.data.remote.ShowsRemoteDataSource
import tv.trakt.trakt.core.shows.model.WatchersShow
import tv.trakt.trakt.core.shows.sections.trending.data.local.TrendingShowsLocalDataSource
import tv.trakt.trakt.core.shows.sections.trending.usecases.DEFAULT_LIMIT
import tv.trakt.trakt.core.shows.sections.trending.usecases.GetTrendingShowsUseCase
import java.time.Instant

internal class HalloweenGetTrendingShowsUseCase(
    private val remoteSource: ShowsRemoteDataSource,
    private val localTrendingSource: TrendingShowsLocalDataSource,
    private val localShowSource: ShowLocalDataSource,
) : GetTrendingShowsUseCase {
    override suspend fun getLocalShows(): ImmutableList<WatchersShow> {
        return localTrendingSource.getShows()
            .asyncMap { entity ->
                WatchersShow(
                    watchers = entity.watchers,
                    show = entity.show,
                )
            }
            .sortedByDescending { it.watchers }
            .toImmutableList()
            .also {
                localShowSource.upsertShows(
                    it.asyncMap { item -> item.show },
                )
            }
    }

    override suspend fun getShows(
        limit: Int,
        page: Int,
        skipLocal: Boolean,
    ): ImmutableList<WatchersShow> {
        return remoteSource.getTrending(
            page = page,
            limit = limit,
            subgenres = listOf("halloween"),
        )
            .asyncMap {
                WatchersShow(
                    watchers = it.watchers,
                    show = Show.fromDto(it.show),
                )
            }
            .toImmutableList()
            .also { shows ->
                if (!skipLocal) {
                    localTrendingSource.addShows(
                        shows = shows.take(DEFAULT_LIMIT),
                        addedAt = Instant.now(),
                    )
                }

                localShowSource.upsertShows(
                    shows.asyncMap { item -> item.show },
                )
            }
    }
}
