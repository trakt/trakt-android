package tv.trakt.trakt.core.search.data.remote

import tv.trakt.trakt.common.networking.SearchItemDto

internal interface SearchRemoteDataSource {
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
}
