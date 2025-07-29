package tv.trakt.trakt.core.shows.sections.anticipated.usecase

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.shows.data.remote.ShowsRemoteDataSource
import tv.trakt.trakt.core.shows.model.WatchersShow
import tv.trakt.trakt.tv.helpers.extensions.asyncMap

internal class GetAnticipatedShowsUseCase(
    private val remoteSource: ShowsRemoteDataSource,
) {
    suspend fun getAnticipatedShows(): ImmutableList<WatchersShow> {
        return remoteSource.getAnticipated(30)
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
