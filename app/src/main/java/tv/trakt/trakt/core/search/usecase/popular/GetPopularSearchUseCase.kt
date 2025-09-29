package tv.trakt.trakt.core.search.usecase.popular

import tv.trakt.trakt.common.networking.TrendingSearchDto
import tv.trakt.trakt.core.search.data.remote.SearchRemoteDataSource

private const val TRENDING_SEARCH_LIMIT = 36

internal class GetPopularSearchUseCase(
    private val remoteSource: SearchRemoteDataSource,
) {
    suspend fun getShows(): List<TrendingSearchDto> {
        return remoteSource.getPopularShows(
            limit = TRENDING_SEARCH_LIMIT,
        )
    }

    suspend fun getMovies(): List<TrendingSearchDto> {
        return remoteSource.getPopularMovies(
            limit = TRENDING_SEARCH_LIMIT,
        )
    }
}
