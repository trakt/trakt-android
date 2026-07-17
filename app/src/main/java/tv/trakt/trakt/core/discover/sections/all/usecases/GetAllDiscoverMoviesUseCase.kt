package tv.trakt.trakt.core.discover.sections.all.usecases

import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.core.discover.DiscoverConfig.DEFAULT_ALL_LIMIT
import tv.trakt.trakt.core.discover.model.DiscoverItem
import tv.trakt.trakt.core.discover.model.DiscoverSection
import tv.trakt.trakt.core.discover.model.DiscoverSection.ANTICIPATED
import tv.trakt.trakt.core.discover.model.DiscoverSection.POPULAR
import tv.trakt.trakt.core.discover.model.DiscoverSection.RECOMMENDED
import tv.trakt.trakt.core.discover.model.DiscoverSection.TRENDING
import tv.trakt.trakt.core.discover.sections.anticipated.usecases.GetAnticipatedMoviesUseCase
import tv.trakt.trakt.core.discover.sections.popular.usecases.GetPopularMoviesUseCase
import tv.trakt.trakt.core.discover.sections.trending.usecases.GetTrendingMoviesUseCase
import tv.trakt.trakt.core.home.sections.recommended.usecase.GetRecommendedMoviesUseCase

internal class GetAllDiscoverMoviesUseCase(
    private val getTrendingMoviesUseCase: GetTrendingMoviesUseCase,
    private val getAnticipatedMoviesUseCase: GetAnticipatedMoviesUseCase,
    private val getPopularMoviesUseCase: GetPopularMoviesUseCase,
    private val getRecommendedMoviesUseCase: GetRecommendedMoviesUseCase,
) {
    suspend fun getMovies(
        source: DiscoverSection,
        page: Int = 1,
        skipLocal: Boolean = false,
        filters: GlobalFilter,
    ): ImmutableList<DiscoverItem> {
        return when (source) {
            TRENDING -> getTrendingMoviesUseCase.getMovies(
                page = page,
                limit = DEFAULT_ALL_LIMIT,
                skipLocal = skipLocal,
                filters = filters,
            )

            ANTICIPATED -> getAnticipatedMoviesUseCase.getMovies(
                page = page,
                limit = DEFAULT_ALL_LIMIT,
                skipLocal = skipLocal,
                filters = filters,
            )

            POPULAR -> getPopularMoviesUseCase.getMovies(
                page = page,
                limit = DEFAULT_ALL_LIMIT,
                skipLocal = skipLocal,
                filters = filters,
            )

            RECOMMENDED -> getRecommendedMoviesUseCase.getMovies(
                limit = DEFAULT_ALL_LIMIT,
                skipLocal = skipLocal,
                filters = filters,
            )
        }
    }

    suspend fun getLocalMovies(source: DiscoverSection): ImmutableList<DiscoverItem> {
        return when (source) {
            TRENDING -> getTrendingMoviesUseCase.getLocalMovies()
            ANTICIPATED -> getAnticipatedMoviesUseCase.getLocalMovies()
            POPULAR -> getPopularMoviesUseCase.getLocalMovies()
            RECOMMENDED -> getRecommendedMoviesUseCase.getLocalMovies()
        }
    }
}
