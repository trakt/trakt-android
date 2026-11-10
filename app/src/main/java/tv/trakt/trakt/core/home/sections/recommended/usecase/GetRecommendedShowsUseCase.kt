package tv.trakt.trakt.core.home.sections.recommended.usecase

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.data.local.RecommendedShowsLocalSource
import tv.trakt.trakt.common.data.local.ShowLocalSource
import tv.trakt.trakt.common.data.remote.ShowsRemoteSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.recommended.RecommendedItem
import tv.trakt.trakt.common.model.recommended.RecommendedSource
import tv.trakt.trakt.core.discover.DiscoverConfig
import tv.trakt.trakt.core.discover.DiscoverConfig.DEFAULT_SECTION_LIMIT

internal class GetRecommendedShowsUseCase(
    private val remoteSource: ShowsRemoteSource,
    private val localRecommendedSource: RecommendedShowsLocalSource,
    private val localShowSource: ShowLocalSource,
) {
    suspend fun getLocalShows(): ImmutableList<RecommendedItem.ShowItem> {
        return localRecommendedSource.getShows()
            .toImmutableList()
            .also { items ->
                val shows = items.asyncMap { it.show }
                localShowSource.upsertShows(shows)
            }
    }

    suspend fun getShows(
        limit: Int = DiscoverConfig.DEFAULT_SECTION_LIMIT,
        skipLocal: Boolean = false,
        filters: GlobalFilter,
    ): ImmutableList<RecommendedItem.ShowItem> {
        return remoteSource.getRecommended(limit = limit, filters = filters)
            .asyncMap { entry ->
                RecommendedItem.ShowItem(
                    show = Show.fromDto(entry.show),
                    sources = entry.sources.orEmpty().map { RecommendedSource.fromDto(it) }.toImmutableList(),
                )
            }
            .toImmutableList()
            .also { shows ->
                if (!skipLocal) {
                    localRecommendedSource.setShows(
                        shows = shows.take(DEFAULT_SECTION_LIMIT),
                    )
                }

                localShowSource.upsertShows(
                    shows.asyncMap { item -> item.show },
                )
            }
    }
}
