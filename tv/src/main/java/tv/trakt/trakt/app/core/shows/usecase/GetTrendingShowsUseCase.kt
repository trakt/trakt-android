package tv.trakt.trakt.app.core.shows.usecase

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.app.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.app.core.shows.data.remote.ShowsRemoteDataSource
import tv.trakt.trakt.app.core.shows.model.TrendingShow
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto

internal class GetTrendingShowsUseCase(
    private val remoteSource: ShowsRemoteDataSource,
    private val localSource: ShowLocalDataSource,
) {
    suspend fun getTrendingShows(limit: Int = 30): ImmutableList<TrendingShow> {
        return remoteSource.getTrendingShows(limit)
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
