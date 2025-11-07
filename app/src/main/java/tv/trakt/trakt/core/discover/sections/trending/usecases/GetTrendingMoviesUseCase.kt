package tv.trakt.trakt.core.discover.sections.trending.usecases

import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.core.discover.DiscoverConfig.DEFAULT_SECTION_LIMIT
import tv.trakt.trakt.core.discover.model.DiscoverItem

internal interface GetTrendingMoviesUseCase {
    suspend fun getLocalMovies(): ImmutableList<DiscoverItem>

    suspend fun getMovies(
        limit: Int = DEFAULT_SECTION_LIMIT,
        page: Int = 1,
        skipLocal: Boolean = false,
    ): ImmutableList<DiscoverItem>
}
