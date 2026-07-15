package tv.trakt.trakt.common.core.lists.data.remote

import org.openapitools.client.apis.ListsApi
import org.openapitools.client.models.PostCheckinStartRequestOneOf1MovieIds
import org.openapitools.client.models.PostCheckinStartRequestOneOfOneOfEpisodeIds
import org.openapitools.client.models.PostUsersListsCreateRequest
import org.openapitools.client.models.PostUsersListsListAddRequest
import org.openapitools.client.models.PostUsersListsListAddRequestMoviesInner
import org.openapitools.client.models.PostUsersListsListAddRequestShowsInner
import org.openapitools.client.models.PostUsersListsReorderRequest
import org.openapitools.client.models.PutUsersListsListUpdateRequest
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.common.networking.ListItemDto
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
        privacy: String,
    ) {
        val request = PostUsersListsCreateRequest(
            name = name,
            description = description,
            privacy = privacy,
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
        privacy: String,
    ) {
        val request = PutUsersListsListUpdateRequest(
            name = name,
            description = description,
            privacy = privacy,
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
        userId: TraktId,
        listId: TraktId,
        showId: TraktId,
    ) {
        listsApi.postUsersListsListAdd(
            id = userId.value.toString(),
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
        userId: TraktId,
        listId: TraktId,
        showId: TraktId,
    ) {
        listsApi.postUsersListsListRemove(
            id = userId.value.toString(),
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
        userId: TraktId,
        listId: TraktId,
        movieId: TraktId,
    ) {
        listsApi.postUsersListsListAdd(
            id = userId.value.toString(),
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
        userId: TraktId,
        listId: TraktId,
        movieId: TraktId,
    ) {
        listsApi.postUsersListsListRemove(
            id = userId.value.toString(),
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

    override suspend fun getAllListItems(
        listId: TraktId,
        extended: String?,
        sorting: Sorting,
        pagination: Pagination,
        filters: GlobalFilter?,
    ): List<ListItemDto> {
        val response = listsApi.getListsItemsAll(
            id = listId.value.toString(),
            extended = extended,
            watchnow = filters?.availability?.joinToString(",") { it.slug },
            genres = filters?.genre?.joinToString(",") { it.slug },
            subgenres = filters?.subgenre?.joinToString(","),
            years = filters?.years?.let { "${it.first}-${it.second}" },
            ratings = filters?.rating?.let { "${it.first}-${it.second}" },
            startDate = null,
            endDate = null,
            page = pagination.page,
            limit = pagination.limit.toString(),
            sortBy = sorting.type.value,
            sortHow = sorting.order.value,
            runtimes = filters?.runtime?.let { "${it.first}-${it.second}" },
            countries = filters?.countries?.joinToString(",") ?: filters?.region?.slug,
            certifications = filters?.certification?.joinToString(",") { it.slug },
            ignoreWatched = filters?.hideWatched,
            ignoreWatchlisted = filters?.hideWatchlist,
            ignoreCollected = false,
        )
        return response.body()
    }

    override suspend fun getMediaListItems(
        listId: TraktId,
        extended: String?,
        sorting: Sorting,
        pagination: Pagination,
        filters: GlobalFilter?,
    ): List<ListMediaItemDto> {
        val response = listsApi.getListsItemsMedia(
            id = listId.value.toString(),
            extended = extended,
            watchnow = filters?.availability?.joinToString(",") { it.slug },
            genres = filters?.genre?.joinToString(",") { it.slug },
            subgenres = filters?.subgenre?.joinToString(","),
            years = filters?.years?.let { "${it.first}-${it.second}" },
            ratings = filters?.rating?.let { "${it.first}-${it.second}" },
            startDate = null,
            endDate = null,
            page = pagination.page,
            limit = pagination.limit.toString(),
            sortBy = sorting.type.value,
            sortHow = sorting.order.value,
            runtimes = filters?.runtime?.let { "${it.first}-${it.second}" },
            countries = filters?.countries?.joinToString(",") ?: filters?.region?.slug,
            certifications = filters?.certification?.joinToString(",") { it.slug },
            ignoreWatched = filters?.hideWatched,
            ignoreWatchlisted = filters?.hideWatchlist,
            ignoreCollected = false,
        )
        return response.body()
    }

    override suspend fun getShowListItems(
        listId: TraktId,
        extended: String?,
        sorting: Sorting,
        pagination: Pagination,
        filters: GlobalFilter?,
    ): List<ListShowItemDto> {
        val response = listsApi.getListsItemsShow(
            id = listId.value.toString(),
            extended = extended,
            watchnow = filters?.availability?.joinToString(",") { it.slug },
            genres = filters?.genre?.joinToString(",") { it.slug },
            subgenres = filters?.subgenre?.joinToString(","),
            years = filters?.years?.let { "${it.first}-${it.second}" },
            ratings = filters?.rating?.let { "${it.first}-${it.second}" },
            startDate = null,
            endDate = null,
            page = pagination.page,
            limit = pagination.limit.toString(),
            sortBy = sorting.type.value,
            sortHow = sorting.order.value,
            runtimes = filters?.runtime?.let { "${it.first}-${it.second}" },
            countries = filters?.countries?.joinToString(",") ?: filters?.region?.slug,
            certifications = filters?.certification?.joinToString(",") { it.slug },
            ignoreWatched = filters?.hideWatched,
            ignoreWatchlisted = filters?.hideWatchlist,
            ignoreCollected = false,
        )
        return response.body()
    }

    override suspend fun getMovieListItems(
        listId: TraktId,
        extended: String?,
        sorting: Sorting,
        pagination: Pagination,
        filters: GlobalFilter?,
    ): List<ListMovieItemDto> {
        val response = listsApi.getListsItemsMovie(
            id = listId.value.toString(),
            extended = extended,
            watchnow = filters?.availability?.joinToString(",") { it.slug },
            genres = filters?.genre?.joinToString(",") { it.slug },
            years = filters?.years?.let { "${it.first}-${it.second}" },
            ratings = filters?.rating?.let { "${it.first}-${it.second}" },
            startDate = null,
            endDate = null,
            subgenres = filters?.subgenre?.joinToString(","),
            page = pagination.page,
            limit = pagination.limit.toString(),
            sortBy = sorting.type.value,
            sortHow = sorting.order.value,
            runtimes = filters?.runtime?.let { "${it.first}-${it.second}" },
            countries = filters?.countries?.joinToString(",") ?: filters?.region?.slug,
            certifications = filters?.certification?.joinToString(",") { it.slug },
            ignoreWatched = filters?.hideWatched,
            ignoreWatchlisted = filters?.hideWatchlist,
            ignoreCollected = false,
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

    override suspend fun reorderListItems(
        listId: TraktId,
        itemsIds: List<Int>,
    ) {
        listsApi.postUsersListsListReorderItems(
            id = "me",
            listId = listId.value.toString(),
            postUsersListsReorderRequest = PostUsersListsReorderRequest(
                rank = itemsIds,
            ),
        )
        cacheMarker.invalidate()
    }
}
