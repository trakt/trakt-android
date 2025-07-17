package tv.trakt.trakt.tv.core.shows.usecase

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.tv.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.tv.core.shows.data.remote.ShowsRemoteDataSource
import tv.trakt.trakt.tv.core.shows.model.AnticipatedShow
import tv.trakt.trakt.tv.core.shows.model.Show
import tv.trakt.trakt.tv.core.shows.model.fromDto
import tv.trakt.trakt.tv.helpers.extensions.asyncMap

internal class GetAnticipatedShowsUseCase(
    private val remoteSource: ShowsRemoteDataSource,
    private val localSource: ShowLocalDataSource,
) {
    suspend fun getAnticipatedShows(): ImmutableList<AnticipatedShow> {
        return remoteSource.getAnticipatedShows()
            .asyncMap {
                AnticipatedShow(
                    listCount = it.listCount,
                    show = Show.fromDto(it.show),
                )
            }
            .toImmutableList()
            .also { shows ->
                localSource.upsertShows(shows.map { it.show })
            }
    }
}
