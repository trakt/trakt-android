package tv.trakt.trakt.core.search.data.remote

import tv.trakt.trakt.common.networking.SearchItemDto
import tv.trakt.trakt.common.networking.TrendingSearchDto

internal interface SearchRemoteDataSource {
    suspend fun getPeople(
        query: String,
        limit: Int,
        extended: String = "full,cloud9",
    ): List<SearchItemDto>

    suspend fun getShows(
        query: String,
        limit: Int,
        extended: String = "full,cloud9,colors,streaming_ids",
    ): List<SearchItemDto>

    suspend fun getMovies(
        query: String,
        limit: Int,
        extended: String = "full,cloud9,colors,streaming_ids",
    ): List<SearchItemDto>

    suspend fun getShowsMovies(
        query: String,
        limit: Int,
        extended: String = "full,cloud9,colors,streaming_ids",
    ): List<SearchItemDto>

    suspend fun getPopularShows(limit: Int): List<TrendingSearchDto>

    suspend fun getPopularMovies(limit: Int): List<TrendingSearchDto>
}
