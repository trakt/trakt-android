package tv.trakt.trakt.core.shows.sections.anticipated.usecase

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.shows.data.remote.ShowsRemoteDataSource
import tv.trakt.trakt.core.shows.model.WatchersShow

internal class GetAnticipatedShowsUseCase(
    private val remoteSource: ShowsRemoteDataSource,
) {
    suspend fun getAnticipatedShows(): ImmutableList<WatchersShow> {
        return remoteSource.getAnticipated(20)
            .asyncMap {
                WatchersShow(
                    watchers = it.listCount,
                    show = Show.fromDto(it.show),
                )
            }
            .toImmutableList()
            .also { shows ->
//                localSource.upsertShows(shows.map { it.show })
            }
    }
}
