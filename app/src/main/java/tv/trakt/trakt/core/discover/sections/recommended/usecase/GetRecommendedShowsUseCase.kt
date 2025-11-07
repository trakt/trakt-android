package tv.trakt.trakt.core.discover.sections.recommended.usecase

import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.core.discover.DiscoverConfig
import tv.trakt.trakt.core.discover.model.DiscoverItem

internal interface GetRecommendedShowsUseCase {
    suspend fun getLocalShows(): ImmutableList<DiscoverItem>

    suspend fun getShows(
        limit: Int = DiscoverConfig.DEFAULT_SECTION_LIMIT,
        skipLocal: Boolean = false,
    ): ImmutableList<DiscoverItem>
}
