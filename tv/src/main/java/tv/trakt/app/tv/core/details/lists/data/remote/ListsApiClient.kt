package tv.trakt.app.tv.core.details.lists.data.remote

import org.openapitools.client.apis.ListsApi
import tv.trakt.app.tv.common.model.TraktId
import tv.trakt.app.tv.networking.openapi.ListMovieItemDto
import tv.trakt.app.tv.networking.openapi.ListShowItemDto

internal class ListsApiClient(
    private val api: ListsApi,
) : ListsRemoteDataSource {
    override suspend fun getShowListItems(
        listId: TraktId,
        limit: Int,
        page: Int,
        extended: String,
    ): List<ListShowItemDto> {
        val response = api.getListsItemsShow(
            id = listId.value.toString(),
            extended = extended,
            watchnow = null,
            genres = null,
            years = null,
            ratings = null,
            page = page,
            limit = limit.toString(),
            startDate = null,
            endDate = null,
        )

        return response.body()
    }

    override suspend fun getMovieListItems(
        listId: TraktId,
        limit: Int,
        page: Int,
        extended: String,
    ): List<ListMovieItemDto> {
        val response = api.getListsItemsMovie(
            id = listId.value.toString(),
            extended = extended,
            watchnow = null,
            genres = null,
            years = null,
            ratings = null,
            page = page,
            limit = limit.toString(),
            startDate = null,
            endDate = null,
        )

        return response.body()
    }
}
