package tv.trakt.trakt.core.discover.sections.all.usecases

import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.core.discover.DiscoverConfig.DEFAULT_ALL_LIMIT
import tv.trakt.trakt.core.discover.model.DiscoverItem
import tv.trakt.trakt.core.discover.model.DiscoverSection
import tv.trakt.trakt.core.discover.model.DiscoverSection.Anticipated
import tv.trakt.trakt.core.discover.model.DiscoverSection.Popular
import tv.trakt.trakt.core.discover.model.DiscoverSection.Recommended
import tv.trakt.trakt.core.discover.model.DiscoverSection.Trending
import tv.trakt.trakt.core.discover.sections.anticipated.usecases.GetAnticipatedShowsUseCase
import tv.trakt.trakt.core.discover.sections.popular.usecases.GetPopularShowsUseCase
import tv.trakt.trakt.core.discover.sections.trending.usecases.GetTrendingShowsUseCase
import tv.trakt.trakt.core.home.sections.recommended.usecase.GetRecommendedShowsUseCase

internal class GetAllDiscoverShowsUseCase(
    private val getTrendingShowsUseCase: GetTrendingShowsUseCase,
    private val getAnticipatedShowsUseCase: GetAnticipatedShowsUseCase,
    private val getPopularShowsUseCase: GetPopularShowsUseCase,
    private val getRecommendedShowsUseCase: GetRecommendedShowsUseCase,
) {
    suspend fun getShows(
        source: DiscoverSection,
        page: Int = 1,
        skipLocal: Boolean = false,
        filters: GlobalFilter,
    ): ImmutableList<DiscoverItem> {
        return when (source) {
            Trending -> getTrendingShowsUseCase.getShows(
                page = page,
                limit = DEFAULT_ALL_LIMIT,
                skipLocal = skipLocal,
                filters = filters,
            )

            Anticipated -> getAnticipatedShowsUseCase.getShows(
                page = page,
                limit = DEFAULT_ALL_LIMIT,
                skipLocal = skipLocal,
                filters = filters,
            )

            Popular -> getPopularShowsUseCase.getShows(
                page = page,
                limit = DEFAULT_ALL_LIMIT,
                skipLocal = skipLocal,
                filters = filters,
            )

            Recommended -> getRecommendedShowsUseCase.getShows(
                limit = DEFAULT_ALL_LIMIT,
                skipLocal = skipLocal,
                filters = filters,
            )
        }
    }

    suspend fun getLocalShows(source: DiscoverSection): ImmutableList<DiscoverItem> {
        return when (source) {
            Trending -> getTrendingShowsUseCase.getLocalShows()
            Anticipated -> getAnticipatedShowsUseCase.getLocalShows()
            Popular -> getPopularShowsUseCase.getLocalShows()
            Recommended -> getRecommendedShowsUseCase.getLocalShows()
        }
    }
}
