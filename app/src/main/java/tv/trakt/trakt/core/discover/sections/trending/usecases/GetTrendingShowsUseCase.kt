package tv.trakt.trakt.core.discover.sections.trending.usecases

import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.core.discover.model.DiscoverItem

internal const val DEFAULT_LIMIT = 24
internal const val DEFAULT_ALL_LIMIT = 102

internal interface GetTrendingShowsUseCase {
    suspend fun getLocalShows(): ImmutableList<DiscoverItem>

    suspend fun getShows(
        limit: Int = DEFAULT_LIMIT,
        page: Int = 1,
        skipLocal: Boolean = false,
    ): ImmutableList<DiscoverItem>
}
