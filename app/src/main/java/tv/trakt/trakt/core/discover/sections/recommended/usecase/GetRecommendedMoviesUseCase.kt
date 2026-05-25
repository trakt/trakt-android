package tv.trakt.trakt.core.discover.sections.recommended.usecase

import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.core.discover.DiscoverConfig
import tv.trakt.trakt.core.discover.model.DiscoverItem

internal interface GetRecommendedMoviesUseCase {
    suspend fun getLocalMovies(): ImmutableList<DiscoverItem.MovieItem>

    suspend fun getMovies(
        limit: Int = DiscoverConfig.DEFAULT_SECTION_LIMIT,
        skipLocal: Boolean = false,
        filters: GlobalFilter,
    ): ImmutableList<DiscoverItem.MovieItem>
}
