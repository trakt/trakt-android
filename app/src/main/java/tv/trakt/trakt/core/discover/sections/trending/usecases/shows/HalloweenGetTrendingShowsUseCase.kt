package tv.trakt.trakt.core.discover.sections.trending.usecases.shows

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.discover.data.remote.ShowsRemoteDataSource
import tv.trakt.trakt.core.discover.model.DiscoverItem
import tv.trakt.trakt.core.discover.sections.trending.data.local.shows.TrendingShowsLocalDataSource
import tv.trakt.trakt.core.discover.sections.trending.usecases.DEFAULT_LIMIT
import tv.trakt.trakt.core.discover.sections.trending.usecases.GetTrendingShowsUseCase

internal class HalloweenGetTrendingShowsUseCase(
    private val remoteSource: ShowsRemoteDataSource,
    private val localTrendingSource: TrendingShowsLocalDataSource,
    private val localShowSource: ShowLocalDataSource,
) : GetTrendingShowsUseCase {
    override suspend fun getLocalShows(): ImmutableList<DiscoverItem.ShowItem> {
        return localTrendingSource.getShows()
            .sortedByDescending { it.count }
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
    ): ImmutableList<DiscoverItem.ShowItem> {
        return remoteSource.getTrending(
            page = page,
            limit = limit,
            subgenres = listOf("halloween"),
        )
            .asyncMap {
                DiscoverItem.ShowItem(
                    show = Show.fromDto(it.show),
                    count = it.watchers,
                )
            }
            .toImmutableList()
            .also { shows ->
                if (!skipLocal) {
                    localTrendingSource.addShows(
                        shows = shows.take(DEFAULT_LIMIT),
                        addedAt = nowUtcInstant(),
                    )
                }

                localShowSource.upsertShows(
                    shows.asyncMap { item -> item.show },
                )
            }
    }
}
