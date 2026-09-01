package tv.trakt.trakt.core.discover.sections.all.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.core.discover.DiscoverConfig.DEFAULT_ALL_LIMIT
import tv.trakt.trakt.core.discover.model.DiscoverItem
import tv.trakt.trakt.core.discover.model.DiscoverSection
import tv.trakt.trakt.core.discover.model.DiscoverSection.Anticipated
import tv.trakt.trakt.core.discover.model.DiscoverSection.Popular
import tv.trakt.trakt.core.discover.model.DiscoverSection.Recommended
import tv.trakt.trakt.core.discover.model.DiscoverSection.Trending
import tv.trakt.trakt.core.discover.sections.anticipated.usecases.GetAnticipatedMoviesUseCase
import tv.trakt.trakt.core.discover.sections.popular.usecases.GetPopularMoviesUseCase
import tv.trakt.trakt.core.discover.sections.trending.usecases.GetTrendingMoviesUseCase
import tv.trakt.trakt.core.home.sections.recommended.model.RecommendedItem
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
            Trending -> getTrendingMoviesUseCase.getMovies(
                page = page,
                limit = DEFAULT_ALL_LIMIT,
                skipLocal = skipLocal,
                filters = filters,
            )

            Anticipated -> getAnticipatedMoviesUseCase.getMovies(
                page = page,
                limit = DEFAULT_ALL_LIMIT,
                skipLocal = skipLocal,
                filters = filters,
            )

            Popular -> getPopularMoviesUseCase.getMovies(
                page = page,
                limit = DEFAULT_ALL_LIMIT,
                skipLocal = skipLocal,
                filters = filters,
            )

            Recommended -> getRecommendedMoviesUseCase.getMovies(
                limit = DEFAULT_ALL_LIMIT,
                skipLocal = skipLocal,
                filters = filters,
            ).toDiscoverItems()
        }
    }

    suspend fun getLocalMovies(source: DiscoverSection): ImmutableList<DiscoverItem> {
        return when (source) {
            Trending -> getTrendingMoviesUseCase.getLocalMovies()
            Anticipated -> getAnticipatedMoviesUseCase.getLocalMovies()
            Popular -> getPopularMoviesUseCase.getLocalMovies()
            Recommended -> getRecommendedMoviesUseCase.getLocalMovies().toDiscoverItems()
        }
    }
}

private fun List<RecommendedItem.MovieItem>.toDiscoverItems(): ImmutableList<DiscoverItem> =
    map { DiscoverItem.MovieItem(movie = it.movie, sources = it.sources) }.toImmutableList()
