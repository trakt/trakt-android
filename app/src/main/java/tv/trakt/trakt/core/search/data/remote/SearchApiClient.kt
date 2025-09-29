package tv.trakt.trakt.core.search.data.remote

import org.openapitools.client.apis.SearchApi
import org.openapitools.client.models.PostSearchRecentAddRequest
import org.openapitools.client.models.PostSearchRecentAddRequest.Type
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.SearchItemDto
import tv.trakt.trakt.common.networking.TrendingSearchDto

internal class SearchApiClient(
    private val api: SearchApi,
) : SearchRemoteDataSource {
    override suspend fun getPeople(
        query: String,
        limit: Int,
        extended: String,
    ): List<SearchItemDto> {
        if (query.trim().isBlank()) {
            return emptyList()
        }
        val response = api.getSearchQuery(
            type = "person",
            query = query,
            page = 1,
            limit = limit,
            extended = extended,
            engine = "typesense",
        )

        return response.body()
    }

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

    override suspend fun getPopularShows(limit: Int): List<TrendingSearchDto> {
        val response = api.getSearchTrending(
            type = "shows",
            page = 1,
            limit = limit,
            extended = "full,cloud9",
        )

        return response.body()
    }

    override suspend fun getPopularMovies(limit: Int): List<TrendingSearchDto> {
        val response = api.getSearchTrending(
            type = "movies",
            page = 1,
            limit = limit,
            extended = "full,cloud9",
        )

        return response.body()
    }

    override suspend fun postShowUserSearch(
        showId: TraktId,
        query: String,
    ) {
        api.postSearchRecentAdd(
            postSearchRecentAddRequest = PostSearchRecentAddRequest(
                query = query,
                id = showId.value,
                type = Type.SHOWS,
            ),
        )
    }

    override suspend fun postMovieUserSearch(
        movieId: TraktId,
        query: String,
    ) {
        api.postSearchRecentAdd(
            postSearchRecentAddRequest = PostSearchRecentAddRequest(
                query = query,
                id = movieId.value,
                type = Type.MOVIES,
            ),
        )
    }
}
