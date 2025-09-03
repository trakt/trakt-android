package tv.trakt.trakt.core.search.data.remote

import org.openapitools.client.apis.SearchApi
import tv.trakt.trakt.common.networking.SearchItemDto

internal class SearchApiClient(
    private val api: SearchApi,
) : SearchRemoteDataSource {
    override suspend fun getShows(
        query: String,
        limit: Int,
        extended: String,
    ): List<SearchItemDto> {
        if (query.trim().isBlank()) {
            return emptyList()
        }
        val response = api.getSearchQuery(
            type = "show",
            query = query,
            page = 1,
            limit = limit,
            extended = extended,
            engine = "typesense",
        )

        return response.body()
    }

    override suspend fun getMovies(
        query: String,
        limit: Int,
        extended: String,
    ): List<SearchItemDto> {
        if (query.trim().isBlank()) {
            return emptyList()
        }

        val response = api.getSearchQuery(
            type = "movie",
            query = query,
            page = 1,
            limit = limit,
            extended = extended,
            engine = "typesense",
        )

        return response.body()
    }

    override suspend fun getShowsMovies(
        query: String,
        limit: Int,
        extended: String,
    ): List<SearchItemDto> {
        if (query.trim().isBlank()) {
            return emptyList()
        }

        val response = api.getSearchQuery(
            type = "show,movie",
            query = query,
            page = 1,
            limit = limit,
            extended = extended,
            engine = "typesense",
        )

        return response.body()
    }
}
