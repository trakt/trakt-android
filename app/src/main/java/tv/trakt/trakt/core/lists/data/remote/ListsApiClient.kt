package tv.trakt.trakt.core.lists.data.remote

import org.openapitools.client.apis.ListsApi
import org.openapitools.client.models.PostCheckinMovieRequestMovieIds
import org.openapitools.client.models.PostUsersListsCreateRequest
import org.openapitools.client.models.PostUsersListsListAddRequest
import org.openapitools.client.models.PostUsersListsListAddRequestMoviesInner
import org.openapitools.client.models.PostUsersListsListAddRequestShowsInner
import org.openapitools.client.models.PostUsersListsListAddRequestShowsInnerOneOfIds
import org.openapitools.client.models.PutUsersListsListUpdateRequest
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.common.networking.ListItemDto
import tv.trakt.trakt.common.networking.ListMovieItemDto
import tv.trakt.trakt.common.networking.ListShowItemDto
import tv.trakt.trakt.common.networking.helpers.CacheMarkerProvider

internal class ListsApiClient(
    private val listsApi: ListsApi,
    private val cacheMarker: CacheMarkerProvider,
) : ListsRemoteDataSource {
    override suspend fun createList(
        name: String,
        description: String?,
    ) {
        val request = PostUsersListsCreateRequest(
            name = name,
            description = description,
        )

        listsApi.postUsersListsCreate(
            id = "me",
            postUsersListsCreateRequest = request,
        )

        cacheMarker.invalidate()
    }

    override suspend fun editList(
        listId: TraktId,
        name: String,
        description: String?,
    ) {
        val request = PutUsersListsListUpdateRequest(
            name = name,
            description = description,
        )

        listsApi.putUsersListsListUpdate(
            id = "me",
            listId = listId.value.toString(),
            putUsersListsListUpdateRequest = request,
        )

        cacheMarker.invalidate()
    }

    override suspend fun deleteList(listId: TraktId) {
        listsApi.deleteUsersListsListDelete(
            id = "me",
            listId = listId.value.toString(),
        )

        cacheMarker.invalidate()
    }

    override suspend fun addShowToList(
        listId: TraktId,
        showId: TraktId,
    ) {
        listsApi.postUsersListsListAdd(
            id = "me",
            listId = listId.value.toString(),
            postUsersListsListAddRequest = PostUsersListsListAddRequest(
                shows = listOf(
                    PostUsersListsListAddRequestShowsInner(
                        ids = PostUsersListsListAddRequestShowsInnerOneOfIds(
                            trakt = showId.value,
                            slug = null,
                            imdb = null,
                            tmdb = null,
                            tvdb = 0,
                        ),
                        title = "",
                        year = 0,
                    ),
                ),
            ),
        )
        cacheMarker.invalidate()
    }

    override suspend fun removeShowFromList(
        listId: TraktId,
        showId: TraktId,
    ) {
        listsApi.postUsersListsListRemove(
            id = "me",
            listId = listId.value.toString(),
            postUsersListsListAddRequest = PostUsersListsListAddRequest(
                shows = listOf(
                    PostUsersListsListAddRequestShowsInner(
                        ids = PostUsersListsListAddRequestShowsInnerOneOfIds(
                            trakt = showId.value,
                            slug = null,
                            imdb = null,
                            tmdb = null,
                            tvdb = 0,
                        ),
                        title = "",
                        year = 0,
                    ),
                ),
            ),
        )
        cacheMarker.invalidate()
    }

    override suspend fun addMovieToList(
        listId: TraktId,
        movieId: TraktId,
    ) {
        listsApi.postUsersListsListAdd(
            id = "me",
            listId = listId.value.toString(),
            postUsersListsListAddRequest = PostUsersListsListAddRequest(
                movies = listOf(
                    PostUsersListsListAddRequestMoviesInner(
                        ids = PostCheckinMovieRequestMovieIds(
                            trakt = movieId.value,
                            slug = null,
                            imdb = null,
                            tmdb = 0,
                        ),
                        title = "",
                        year = 0,
                    ),
                ),
            ),
        )
        cacheMarker.invalidate()
    }

    override suspend fun removeMovieFromList(
        listId: TraktId,
        movieId: TraktId,
    ) {
        listsApi.postUsersListsListRemove(
            id = "me",
            listId = listId.value.toString(),
            postUsersListsListAddRequest = PostUsersListsListAddRequest(
                movies = listOf(
                    PostUsersListsListAddRequestMoviesInner(
                        ids = PostCheckinMovieRequestMovieIds(
                            trakt = movieId.value,
                            slug = null,
                            imdb = null,
                            tmdb = 0,
                        ),
                        title = "",
                        year = 0,
                    ),
                ),
            ),
        )
        cacheMarker.invalidate()
    }

    override suspend fun getAllListItems(
        listId: TraktId,
        extended: String?,
        limit: String?,
    ): List<ListItemDto> {
        val response = listsApi.getListsItemsAll(
            id = listId.value.toString(),
            extended = extended,
            watchnow = null,
            genres = null,
            years = null,
            ratings = null,
            startDate = null,
            endDate = null,
            page = null,
            limit = limit,
            subgenres = null,
            sortBy = null,
            sortHow = null,
            runtimes = null,
        )

        return response.body()
    }

    override suspend fun getShowListItems(
        listId: TraktId,
        extended: String?,
        limit: String?,
        sorting: Sorting,
    ): List<ListShowItemDto> {
        val response = listsApi.getListsItemsShow(
            id = listId.value.toString(),
            extended = extended,
            watchnow = null,
            genres = null,
            subgenres = null,
            years = null,
            ratings = null,
            startDate = null,
            endDate = null,
            page = null,
            limit = limit,
            sortBy = sorting.type.value,
            sortHow = sorting.order.value,
            runtimes = null,
        )
        return response.body()
    }

    override suspend fun getMovieListItems(
        listId: TraktId,
        extended: String?,
        limit: String?,
        sorting: Sorting,
    ): List<ListMovieItemDto> {
        val response = listsApi.getListsItemsMovie(
            id = listId.value.toString(),
            extended = extended,
            watchnow = null,
            genres = null,
            years = null,
            ratings = null,
            startDate = null,
            endDate = null,
            page = null,
            subgenres = null,
            limit = limit,
            sortBy = sorting.type.value,
            sortHow = sorting.order.value,
            runtimes = null,
        )
        return response.body()
    }
}
