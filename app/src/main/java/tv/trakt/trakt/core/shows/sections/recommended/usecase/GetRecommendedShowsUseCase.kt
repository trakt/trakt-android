package tv.trakt.trakt.core.shows.sections.recommended.usecase

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.shows.data.remote.ShowsRemoteDataSource

internal class GetRecommendedShowsUseCase(
    private val remoteSource: ShowsRemoteDataSource,
) {
    suspend fun getRecommendedShows(): ImmutableList<Show> {
        return remoteSource.getRecommended(20)
            .asyncMap {
                Show.fromDto(it)
            }
            .toImmutableList()
            .also { shows ->
                // TODO
            }
    }
}
