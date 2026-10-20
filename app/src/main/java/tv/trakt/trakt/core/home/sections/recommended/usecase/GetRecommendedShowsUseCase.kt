package tv.trakt.trakt.core.home.sections.recommended.usecase

import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.recommended.RecommendedItem
import tv.trakt.trakt.core.discover.DiscoverConfig

internal interface GetRecommendedShowsUseCase {
    suspend fun getLocalShows(): ImmutableList<RecommendedItem.ShowItem>

    suspend fun getShows(
        limit: Int = DiscoverConfig.DEFAULT_SECTION_LIMIT,
        skipLocal: Boolean = false,
        filters: GlobalFilter,
    ): ImmutableList<RecommendedItem.ShowItem>
}
