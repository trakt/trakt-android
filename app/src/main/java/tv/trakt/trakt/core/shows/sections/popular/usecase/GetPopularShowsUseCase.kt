package tv.trakt.trakt.core.shows.sections.popular.usecase

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.shows.data.remote.ShowsRemoteDataSource

internal class GetPopularShowsUseCase(
    private val remoteSource: ShowsRemoteDataSource,
) {
    suspend fun getPopularShows(): ImmutableList<Show> {
        return remoteSource.getPopular(20)
            .asyncMap {
                Show.fromDto(it)
            }
            .toImmutableList()
            .also {
//                localSource.upsertShows(it)
            }
    }
}
