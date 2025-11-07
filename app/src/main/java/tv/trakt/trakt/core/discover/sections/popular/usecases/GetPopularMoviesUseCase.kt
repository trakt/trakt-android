package tv.trakt.trakt.core.discover.sections.popular.usecases

import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.core.discover.DiscoverConfig
import tv.trakt.trakt.core.discover.model.DiscoverItem

internal interface GetPopularMoviesUseCase {
    suspend fun getLocalMovies(): ImmutableList<DiscoverItem>

    suspend fun getMovies(
        limit: Int = DiscoverConfig.DEFAULT_SECTION_LIMIT,
        page: Int = 1,
        skipLocal: Boolean = false,
    ): ImmutableList<DiscoverItem>
}
