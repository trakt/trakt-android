package tv.trakt.trakt.common.networking.api.v3

import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.http.HttpStatusCode
import io.ktor.http.encodeURLPathPart
import org.openapitools.client.infrastructure.ApiClient
import tv.trakt.trakt.common.data.dto.ListCollaboratorDto
import tv.trakt.trakt.common.data.dto.ListFeaturedPeopleDto
import tv.trakt.trakt.common.data.dto.SmartListFilterOptionDto
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.common.networking.ListDto
import tv.trakt.trakt.common.networking.SmartListItemDto
import tv.trakt.trakt.common.networking.api.v3.model.V3MinimalList
import tv.trakt.trakt.common.networking.api.v3.model.V3MinimalWatchlistResponse

class V3ListsApi(
    private val baseUrl: String,
    private val baseV3Url: String,
    httpClientEngine: HttpClientEngine,
    httpClientConfig: ((HttpClientConfig<*>) -> Unit),
) : ApiClient(
        baseV3Url,
        httpClientEngine,
        httpClientConfig,
    ) {
    suspend fun getWatchlistMinimal(): Pair<Set<TraktId>, Set<TraktId>> {
        val response = client.get("${baseV3Url}users/me/watchlist/minimal")
        val responseBody = response.body<V3MinimalWatchlistResponse>()

        val shows = responseBody.shows.orEmpty().map { it.toTraktId() }.toSet()
        val movies = responseBody.movies.orEmpty().map { it.toTraktId() }.toSet()

        return shows to movies
    }

    suspend fun getListsMinimal(): List<V3MinimalList> {
        val response = client.get("${baseV3Url}users/me/lists")
        return response.body()
    }

    suspend fun getMovieMeLists(movieId: TraktId): List<Int> {
        val response = client.get("${baseV3Url}movies/${movieId.value}/me/lists")
        return response.body<List<Int>>()
    }

    suspend fun getShowMeLists(showId: TraktId): List<Int> {
        val response = client.get("${baseV3Url}shows/${showId.value}/me/lists")
        return response.body<List<Int>>()
    }

    suspend fun getSeasonMeLists(seasonId: TraktId): List<Int> {
        val response = client.get("${baseV3Url}seasons/${seasonId.value}/me/lists")
        return response.body<List<Int>>()
    }

    suspend fun getEpisodeMeLists(episodeId: TraktId): List<Int> {
        val response = client.get("${baseV3Url}episodes/${episodeId.value}/me/lists")
        return response.body<List<Int>>()
    }

    suspend fun getUserLists(
        userId: String,
        collaborative: Boolean,
        extended: String,
        sortBy: String?,
        sortHow: String?,
        pagination: Pagination,
    ): List<ListDto> {
        val response = client.get("${baseUrl}users/${userId.encodeURLPathPart()}/lists") {
            parameter("extended", extended)
            parameter("collaborative", collaborative)
            parameter("sort_by", sortBy)
            parameter("sort_how", sortHow)
            parameter("page", pagination.page)
            parameter("limit", pagination.limit)
        }
        return response.body()
    }

    suspend fun getListCollaborators(listId: TraktId): List<ListCollaboratorDto> {
        val response = client.get("${baseUrl}lists/${listId.value}/collaborators")
        return response.body()
    }

    suspend fun postListCollaborator(
        listId: TraktId,
        userSlug: String,
    ) {
        client.post("${baseUrl}lists/${listId.value}/collaborators/${userSlug.encodeURLPathPart()}")
    }

    suspend fun deleteListCollaborator(
        listId: TraktId,
        userSlug: String,
    ) {
        client.delete("${baseUrl}lists/${listId.value}/collaborators/${userSlug.encodeURLPathPart()}")
    }

    suspend fun getListFeaturedPeople(listId: TraktId): ListFeaturedPeopleDto? {
        val response = client.get("${baseUrl}lists/${listId.value}") {
            parameter("extended", "featured")
        }
        if (response.status == HttpStatusCode.NoContent) {
            return null
        }
        return response.body()
    }

    suspend fun getSmartListFilterOptions(
        kind: String,
        query: String,
        limit: Int,
    ): List<SmartListFilterOptionDto> {
        val response = client.get("${baseUrl}smart-lists/filter-options/$kind") {
            parameter("query", query)
            parameter("limit", limit)
        }
        return response.body()
    }

    suspend fun getSmartListPreviewItems(
        params: Map<String, String>,
        pagination: Pagination,
    ): List<SmartListItemDto> {
        val response = client.get("${baseUrl}smart-lists/preview/items") {
            params.forEach { (key, value) -> parameter(key, value) }
            parameter("page", pagination.page)
            parameter("limit", pagination.limit)
        }
        return response.body()
    }

    suspend fun getSmartListDraftItems(
        listId: TraktId,
        extended: String,
        pagination: Pagination,
    ): List<SmartListItemDto> {
        val response = client.get("${baseUrl}users/me/lists/${listId.value}/smart-list-draft/items") {
            parameter("extended", extended)
            parameter("page", pagination.page)
            parameter("limit", pagination.limit)
        }
        return response.body()
    }
}
