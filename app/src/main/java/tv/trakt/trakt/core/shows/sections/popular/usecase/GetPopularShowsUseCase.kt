package tv.trakt.trakt.core.shows.sections.popular.usecase

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.shows.data.remote.ShowsRemoteDataSource
import tv.trakt.trakt.core.shows.sections.popular.data.local.PopularShowsLocalDataSource
import java.time.Instant

internal class GetPopularShowsUseCase(
    private val remoteSource: ShowsRemoteDataSource,
    private val localPopularSource: PopularShowsLocalDataSource,
) {
    suspend fun getLocalShows(): ImmutableList<Show> {
        return localPopularSource.getShows()
            .toImmutableList()
    }

    suspend fun getShows(): ImmutableList<Show> {
        return remoteSource.getPopular(20)
            .asyncMap {
                Show.fromDto(it)
            }
            .toImmutableList()
            .also { shows ->
                localPopularSource.addShows(
                    shows = shows,
                    addedAt = Instant.now(),
                )
            }
    }
}
