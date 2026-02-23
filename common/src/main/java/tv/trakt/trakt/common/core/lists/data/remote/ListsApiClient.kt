package tv.trakt.trakt.common.core.lists.data.remote

import org.openapitools.client.apis.ListsApi
import org.openapitools.client.models.PostCheckinStartRequestOneOf1MovieIds
import org.openapitools.client.models.PostCheckinStartRequestOneOfOneOfEpisodeIds
import org.openapitools.client.models.PostUsersListsCreateRequest
import org.openapitools.client.models.PostUsersListsListAddRequest
import org.openapitools.client.models.PostUsersListsListAddRequestMoviesInner
import org.openapitools.client.models.PostUsersListsListAddRequestShowsInner
import org.openapitools.client.models.PutUsersListsListUpdateRequest
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.common.networking.ListMediaItemDto
import tv.trakt.trakt.common.networking.ListMovieItemDto
import tv.trakt.trakt.common.networking.ListShowItemDto
import tv.trakt.trakt.common.networking.helpers.CacheMarkerProvider

class ListsApiClient(
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
                        ids = PostCheckinStartRequestOneOfOneOfEpisodeIds(
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
                        ids = PostCheckinStartRequestOneOfOneOfEpisodeIds(
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
                        ids = PostCheckinStartRequestOneOf1MovieIds(
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
                        ids = PostCheckinStartRequestOneOf1MovieIds(
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

    override suspend fun getMediaListItems(
        listId: TraktId,
        extended: String?,
        sorting: Sorting,
        pagination: Pagination,
    ): List<ListMediaItemDto> {
        val response = listsApi.getListsItemsMedia(
            id = listId.value.toString(),
            extended = extended,
            watchnow = null,
            genres = null,
            subgenres = null,
            years = null,
            ratings = null,
            startDate = null,
            endDate = null,
            page = pagination.page,
            limit = pagination.limit.toString(),
            sortBy = sorting.type.value,
            sortHow = sorting.order.value,
            runtimes = null,
        )
        return response.body()
    }

    override suspend fun getShowListItems(
        listId: TraktId,
        extended: String?,
        sorting: Sorting,
        pagination: Pagination,
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
            page = pagination.page,
            limit = pagination.limit.toString(),
            sortBy = sorting.type.value,
            sortHow = sorting.order.value,
            runtimes = null,
        )
        return response.body()
    }

    override suspend fun getMovieListItems(
        listId: TraktId,
        extended: String?,
        sorting: Sorting,
        pagination: Pagination,
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
            subgenres = null,
            page = pagination.page,
            limit = pagination.limit.toString(),
            sortBy = sorting.type.value,
            sortHow = sorting.order.value,
            runtimes = null,
        )
        return response.body()
    }

    override suspend fun addLikedList(listId: TraktId) {
        listsApi.postListsLike(
            id = listId.value.toString(),
            body = null,
        )
        cacheMarker.invalidate()
    }

    override suspend fun removeLikedList(listId: TraktId) {
        listsApi.deleteListsUnlike(
            id = listId.value.toString(),
        )
        cacheMarker.invalidate()
    }
}
