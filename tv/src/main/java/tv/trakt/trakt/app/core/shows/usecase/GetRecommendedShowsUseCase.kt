package tv.trakt.trakt.app.core.shows.usecase

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.app.core.shows.ShowsConfig.SHOWS_SECTION_LIMIT
import tv.trakt.trakt.common.data.local.ShowLocalSource
import tv.trakt.trakt.common.data.remote.ShowsRemoteSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto

internal class GetRecommendedShowsUseCase(
    private val remoteSource: ShowsRemoteSource,
    private val localSource: ShowLocalSource,
) {
    suspend fun getRecommendedShows(
        limit: Int = SHOWS_SECTION_LIMIT,
        page: Int = 1,
    ): ImmutableList<Show> {
        return remoteSource.getRecommendedV2(limit)
            .asyncMap {
                Show.fromDto(it)
            }
            .toImmutableList()
            .also {
                localSource.upsertShows(it)
            }
    }
}
