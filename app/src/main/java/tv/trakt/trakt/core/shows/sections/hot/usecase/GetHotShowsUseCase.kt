package tv.trakt.trakt.core.shows.sections.hot.usecase

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.shows.data.remote.ShowsRemoteDataSource
import tv.trakt.trakt.core.shows.model.WatchersShow
import tv.trakt.trakt.tv.helpers.extensions.asyncMap

internal class GetHotShowsUseCase(
    private val remoteSource: ShowsRemoteDataSource,
) {
    suspend fun getHotShows(): ImmutableList<WatchersShow> {
        return remoteSource.getHot(20)
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
