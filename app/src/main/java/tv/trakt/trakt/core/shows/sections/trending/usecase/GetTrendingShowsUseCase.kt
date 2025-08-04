package tv.trakt.trakt.core.shows.sections.trending.usecase

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.app.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.shows.data.remote.ShowsRemoteDataSource
import tv.trakt.trakt.core.shows.model.WatchersShow

internal class GetTrendingShowsUseCase(
    private val remoteSource: ShowsRemoteDataSource,
) {
    suspend fun getTrendingShows(): ImmutableList<WatchersShow> {
        return remoteSource.getTrending(20)
            .asyncMap {
                WatchersShow(
                    watchers = it.watchers,
                    show = Show.fromDto(it.show),
                )
            }
            .toImmutableList()
            .also { shows ->
//                localSource.upsertShows(shows.map { it.show })
            }
    }
}
