package tv.trakt.trakt.app.core.shows.usecase

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.app.core.shows.ShowsConfig.SHOWS_SECTION_LIMIT
import tv.trakt.trakt.app.core.shows.model.TrendingShow
import tv.trakt.trakt.common.data.local.ShowLocalSource
import tv.trakt.trakt.common.data.remote.ShowsRemoteSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto

internal class GetTrendingShowsUseCase(
    private val remoteSource: ShowsRemoteSource,
    private val localSource: ShowLocalSource,
) {
    suspend fun getTrendingShows(
        limit: Int = SHOWS_SECTION_LIMIT,
        page: Int = 1,
    ): ImmutableList<TrendingShow> {
        return remoteSource.getTrending(
            page = page,
            limit = limit,
        )
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
