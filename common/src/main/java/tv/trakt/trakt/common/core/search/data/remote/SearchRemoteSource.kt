package tv.trakt.trakt.common.core.search.data.remote

import io.ktor.client.call.body
import org.openapitools.client.apis.SearchApi
import org.openapitools.client.models.PostSearchRecentAddRequest
import org.openapitools.client.models.PostSearchRecentAddRequest.Type
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.SearchItemDto
import tv.trakt.trakt.common.networking.TrendingSearchDto

class SearchRemoteSource(
    private val api: SearchApi,
    private val authorizedApi: SearchApi,
) {
    suspend fun getPeople(
        query: String,
        limit: Int,
        extended: String = "full,cloud9,markdown",
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
        )

        return response.body()
    }

    suspend fun getLists(
        query: String,
        limit: Int,
        extended: String = "full,cloud9",
    ): List<SearchItemDto> {
        if (query.trim().isBlank()) {
            return emptyList()
        }
        val response = api.getSearchQuery(
            type = "list",
            query = query,
            page = 1,
            limit = limit,
            extended = extended,
        )

        return response.body()
    }

    suspend fun getShows(
        query: String,
        limit: Int,
        exact: Boolean = false,
        extended: String = "full,cloud9,colors,streaming_ids",
    ): List<SearchItemDto> {
        if (query.trim().isBlank()) {
            return emptyList()
        }
        val response = when {
            exact -> api.getSearchExact(
                type = "show",
                query = query,
                page = 1,
                limit = limit,
                extended = extended,
            )
            else -> api.getSearchQuery(
                type = "show",
                query = query,
                page = 1,
                limit = limit,
                extended = extended,
            )
        }
        return response.body()
    }

    suspend fun getMovies(
        query: String,
        limit: Int,
        exact: Boolean = false,
        extended: String = "full,cloud9,colors,streaming_ids",
    ): List<SearchItemDto> {
        if (query.trim().isBlank()) {
            return emptyList()
        }

        val response = when {
            exact -> api.getSearchExact(
                type = "movie",
                query = query,
                page = 1,
                limit = limit,
                extended = extended,
            )
            else -> api.getSearchQuery(
                type = "movie",
                query = query,
                page = 1,
                limit = limit,
                extended = extended,
            )
        }
        return response.body()
    }

    suspend fun getShowsMovies(
        query: String,
        limit: Int,
        exact: Boolean = false,
        extended: String = "full,cloud9,colors,streaming_ids",
    ): List<SearchItemDto> {
        if (query.trim().isBlank()) {
            return emptyList()
        }

        val response = when {
            exact -> api.getSearchExact(
                type = "show,movie",
                query = query,
                page = 1,
                limit = limit,
                extended = extended,
            )
            else -> api.getSearchQuery(
                type = "show,movie",
                query = query,
                page = 1,
                limit = limit,
                extended = extended,
            )
        }

        return response.body()
    }

    suspend fun getImdbLookup(imdbId: String): List<SearchItemDto> {
        return api.getSearchLookup(
            idType = "imdb",
            id = imdbId,
        ).body()
    }

    suspend fun getPopularShows(limit: Int): List<TrendingSearchDto> {
        val response = api.getSearchTrending(
            type = "shows",
            page = 1,
            limit = limit,
            extended = "full,cloud9,colors",
            query = null,
        )

        return response.body()
    }

    suspend fun getPopularMovies(limit: Int): List<TrendingSearchDto> {
        val response = api.getSearchTrending(
            type = "movies",
            page = 1,
            limit = limit,
            extended = "full,cloud9,colors",
            query = null,
        )

        return response.body()
    }

    suspend fun getPopularLists(limit: Int): List<SearchItemDto> {
        return api.getSearchQuery(
            type = "list",
            query = null,
            page = 1,
            limit = limit,
            extended = "full,cloud9,colors",
        ).response.body()
    }

    suspend fun postShowUserSearch(
        showId: TraktId,
        query: String,
    ) {
        authorizedApi.postSearchRecentAdd(
            postSearchRecentAddRequest = PostSearchRecentAddRequest(
                query = query,
                id = showId.value,
                type = Type.SHOWS,
            ),
        )
    }

    suspend fun postMovieUserSearch(
        movieId: TraktId,
        query: String,
    ) {
        authorizedApi.postSearchRecentAdd(
            postSearchRecentAddRequest = PostSearchRecentAddRequest(
                query = query,
                id = movieId.value,
                type = Type.MOVIES,
            ),
        )
    }

    suspend fun postPersonUserSearch(
        personId: TraktId,
        query: String,
    ) {
        authorizedApi.postSearchRecentAdd(
            postSearchRecentAddRequest = PostSearchRecentAddRequest(
                query = query,
                id = personId.value,
                type = Type.PEOPLE,
            ),
        )
    }

    suspend fun postListUserSearch(
        listId: TraktId,
        query: String,
    ) {
        authorizedApi.postSearchRecentAdd(
            postSearchRecentAddRequest = PostSearchRecentAddRequest(
                query = query,
                id = listId.value,
                type = Type.LISTS,
            ),
        )
    }
}
