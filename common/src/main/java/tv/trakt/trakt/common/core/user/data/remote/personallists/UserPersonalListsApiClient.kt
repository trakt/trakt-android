package tv.trakt.trakt.common.core.user.data.remote.personallists

import org.openapitools.client.apis.UsersApi
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.common.networking.ListDto
import tv.trakt.trakt.common.networking.ListItemDto
import tv.trakt.trakt.common.networking.ListMovieItemDto
import tv.trakt.trakt.common.networking.ListShowItemDto
import tv.trakt.trakt.common.networking.api.v3.V3Api
import tv.trakt.trakt.common.networking.api.v3.model.V3MinimalList

class UserPersonalListsApiClient(
    private val usersApi: UsersApi,
    private val v3Api: V3Api,
) : UserPersonalListsRemoteDataSource {
    override suspend fun getPersonalLists(pagination: Pagination): List<ListDto> {
        val response = usersApi.getUsersListsPersonal(
            id = "me",
            extended = "cloud9,images",
            page = pagination.page,
            limit = pagination.limit,
        )
        return response.body()
    }

    override suspend fun getPersonalListsMinimal(): List<V3MinimalList> {
        return v3Api.getListsMinimal()
    }

    override suspend fun getPersonalListItems(
        listId: TraktId,
        limit: Int,
        page: Int,
        extended: String,
        sorting: Sorting,
    ): List<ListItemDto> {
        val response = usersApi.getUsersListsListItemsAll(
            id = "me",
            listId = listId.value.toString(),
            extended = extended,
            sortBy = sorting.type.value,
            sortHow = sorting.order.value,
            watchnow = null,
            genres = null,
            years = null,
            subgenres = null,
            ratings = null,
            startDate = null,
            endDate = null,
            runtimes = null,
            page = page,
            limit = limit.toString(),
        )

        return response.body()
    }

    override suspend fun getPersonalListShowItems(
        listId: TraktId,
        limit: Int?,
        page: Int,
        extended: String,
        sorting: Sorting,
    ): List<ListShowItemDto> {
        val response = usersApi.getUsersListsListItemsShow(
            id = "me",
            listId = listId.value.toString(),
            extended = extended,
            sortBy = sorting.type.value,
            sortHow = sorting.order.value,
            watchnow = null,
            genres = null,
            years = null,
            subgenres = null,
            ratings = null,
            startDate = null,
            endDate = null,
            runtimes = null,
            page = page,
            limit = when {
                limit == null -> "all"
                else -> limit.toString()
            },
        )

        return response.body()
    }

    override suspend fun getPersonalListMovieItems(
        listId: TraktId,
        limit: Int?,
        page: Int,
        extended: String,
        sorting: Sorting,
    ): List<ListMovieItemDto> {
        val response = usersApi.getUsersListsListItemsMovie(
            id = "me",
            listId = listId.value.toString(),
            extended = extended,
            sortBy = sorting.type.value,
            sortHow = sorting.order.value,
            watchnow = null,
            genres = null,
            years = null,
            subgenres = null,
            ratings = null,
            startDate = null,
            endDate = null,
            runtimes = null,
            page = page,
            limit = when {
                limit == null -> "all"
                else -> limit.toString()
            },
        )

        return response.body()
    }
}
