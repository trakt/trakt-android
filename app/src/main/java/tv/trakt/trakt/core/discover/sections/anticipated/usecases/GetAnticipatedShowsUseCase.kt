package tv.trakt.trakt.core.discover.sections.anticipated.usecases

import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.model.discover.DiscoverItem
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.core.discover.DiscoverConfig.DEFAULT_SECTION_LIMIT

internal interface GetAnticipatedShowsUseCase {
    suspend fun getLocalShows(): ImmutableList<DiscoverItem>

    suspend fun getShows(
        limit: Int = DEFAULT_SECTION_LIMIT,
        page: Int = 1,
        skipLocal: Boolean = false,
        filters: GlobalFilter,
    ): ImmutableList<DiscoverItem>
}
