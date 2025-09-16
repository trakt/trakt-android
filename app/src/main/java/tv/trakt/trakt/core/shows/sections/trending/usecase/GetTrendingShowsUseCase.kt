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

private const val DEFAULT_LIMIT = 24
internal const val DEFAULT_ALL_LIMIT = 102

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

    suspend fun getShows(
        limit: Int = DEFAULT_LIMIT,
        page: Int = 1,
        skipLocal: Boolean = false,
    ): ImmutableList<WatchersShow> {
        return remoteSource.getTrending(
            page = page,
            limit = limit,
        )
            .asyncMap {
                WatchersShow(
                    watchers = it.watchers,
                    show = Show.fromDto(it.show),
                )
            }
            .toImmutableList()
            .also { shows ->
                if (skipLocal) return@also
                localTrendingSource.addShows(
                    shows = shows.take(DEFAULT_LIMIT),
                    addedAt = Instant.now(),
                )
            }
    }
}
