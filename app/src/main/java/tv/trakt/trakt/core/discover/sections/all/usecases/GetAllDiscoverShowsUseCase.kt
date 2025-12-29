package tv.trakt.trakt.core.discover.sections.all.usecases

import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.core.discover.DiscoverConfig.DEFAULT_ALL_LIMIT
import tv.trakt.trakt.core.discover.model.DiscoverItem
import tv.trakt.trakt.core.discover.model.DiscoverSection
import tv.trakt.trakt.core.discover.model.DiscoverSection.ANTICIPATED
import tv.trakt.trakt.core.discover.model.DiscoverSection.POPULAR
import tv.trakt.trakt.core.discover.model.DiscoverSection.RECOMMENDED
import tv.trakt.trakt.core.discover.model.DiscoverSection.TRENDING
import tv.trakt.trakt.core.discover.sections.anticipated.usecases.GetAnticipatedShowsUseCase
import tv.trakt.trakt.core.discover.sections.popular.usecases.GetPopularShowsUseCase
import tv.trakt.trakt.core.discover.sections.recommended.usecase.GetRecommendedShowsUseCase
import tv.trakt.trakt.core.discover.sections.trending.usecases.GetTrendingShowsUseCase

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
    ): ImmutableList<DiscoverItem> {
        return when (source) {
            TRENDING -> getTrendingShowsUseCase.getShows(
                page = page,
                limit = DEFAULT_ALL_LIMIT,
                skipLocal = skipLocal,
            )

            ANTICIPATED -> getAnticipatedShowsUseCase.getShows(
                page = page,
                limit = DEFAULT_ALL_LIMIT,
                skipLocal = skipLocal,
            )

            POPULAR -> getPopularShowsUseCase.getShows(
                page = page,
                limit = DEFAULT_ALL_LIMIT,
                skipLocal = skipLocal,
            )

            RECOMMENDED -> getRecommendedShowsUseCase.getShows(
                limit = DEFAULT_ALL_LIMIT,
                skipLocal = skipLocal,
            )
        }
    }

    suspend fun getLocalShows(source: DiscoverSection): ImmutableList<DiscoverItem> {
        return when (source) {
            TRENDING -> getTrendingShowsUseCase.getLocalShows()
            ANTICIPATED -> getAnticipatedShowsUseCase.getLocalShows()
            POPULAR -> getPopularShowsUseCase.getLocalShows()
            RECOMMENDED -> getRecommendedShowsUseCase.getLocalShows()
        }
    }
}
