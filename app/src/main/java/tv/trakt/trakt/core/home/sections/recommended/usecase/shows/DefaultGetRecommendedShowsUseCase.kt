package tv.trakt.trakt.core.home.sections.recommended.usecase.shows

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.data.local.ShowLocalSource
import tv.trakt.trakt.common.data.remote.ShowsRemoteSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.core.discover.DiscoverConfig.DEFAULT_SECTION_LIMIT
import tv.trakt.trakt.core.home.sections.recommended.data.shows.RecommendedShowsLocalSource
import tv.trakt.trakt.core.home.sections.recommended.model.RecommendedItem
import tv.trakt.trakt.core.home.sections.recommended.model.RecommendedSource
import tv.trakt.trakt.core.home.sections.recommended.usecase.GetRecommendedShowsUseCase

internal class DefaultGetRecommendedShowsUseCase(
    private val remoteSource: ShowsRemoteSource,
    private val localRecommendedSource: RecommendedShowsLocalSource,
    private val localShowSource: ShowLocalSource,
) : GetRecommendedShowsUseCase {
    override suspend fun getLocalShows(): ImmutableList<RecommendedItem.ShowItem> {
        return localRecommendedSource.getShows()
            .toImmutableList()
            .also { items ->
                val shows = items.asyncMap { it.show }
                localShowSource.upsertShows(shows)
            }
    }

    override suspend fun getShows(
        limit: Int,
        skipLocal: Boolean,
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
