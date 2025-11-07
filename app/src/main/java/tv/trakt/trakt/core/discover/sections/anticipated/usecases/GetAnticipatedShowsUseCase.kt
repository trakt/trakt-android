package tv.trakt.trakt.core.discover.sections.anticipated.usecases

import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.core.discover.DiscoverConfig.DEFAULT_SECTION_LIMIT
import tv.trakt.trakt.core.discover.model.DiscoverItem

internal interface GetAnticipatedShowsUseCase {
    suspend fun getLocalShows(): ImmutableList<DiscoverItem>

    suspend fun getShows(
        limit: Int = DEFAULT_SECTION_LIMIT,
        page: Int = 1,
        skipLocal: Boolean = false,
    ): ImmutableList<DiscoverItem>
}
