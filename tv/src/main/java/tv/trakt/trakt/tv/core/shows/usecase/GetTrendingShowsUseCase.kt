package tv.trakt.trakt.tv.core.shows.usecase

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.tv.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.tv.core.shows.data.remote.ShowsRemoteDataSource
import tv.trakt.trakt.tv.core.shows.model.TrendingShow
import tv.trakt.trakt.tv.helpers.extensions.asyncMap

internal class GetTrendingShowsUseCase(
    private val remoteSource: ShowsRemoteDataSource,
    private val localSource: ShowLocalDataSource,
) {
    suspend fun getTrendingShows(): ImmutableList<TrendingShow> {
        return remoteSource.getTrendingShows()
            .asyncMap {
                TrendingShow(
                    watchers = it.watchers,
                    show = Show.fromDto(it.show),
                )
            }
            .toImmutableList()
            .also { shows ->
                localSource.upsertShows(shows.map { it.show })
            }
    }
}
