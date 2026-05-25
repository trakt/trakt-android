package tv.trakt.trakt.common.core.lists.data.remote

import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.common.networking.ListItemDto
import tv.trakt.trakt.common.networking.ListMediaItemDto
import tv.trakt.trakt.common.networking.ListMovieItemDto
import tv.trakt.trakt.common.networking.ListShowItemDto

interface ListsRemoteDataSource {
    suspend fun createList(
        name: String,
        description: String?,
    )

    suspend fun editList(
        listId: TraktId,
        name: String,
        description: String?,
    )

    suspend fun deleteList(listId: TraktId)

    suspend fun addShowToList(
        userId: TraktId,
        listId: TraktId,
        showId: TraktId,
    )

    suspend fun removeShowFromList(
        userId: TraktId,
        listId: TraktId,
        showId: TraktId,
    )

    suspend fun addMovieToList(
        userId: TraktId,
        listId: TraktId,
        movieId: TraktId,
    )

    suspend fun removeMovieFromList(
        userId: TraktId,
        listId: TraktId,
        movieId: TraktId,
    )

    suspend fun getAllListItems(
        listId: TraktId,
        extended: String?,
        sorting: Sorting,
        pagination: Pagination,
        filters: GlobalFilter?,
    ): List<ListItemDto>

    suspend fun getMediaListItems(
        listId: TraktId,
        extended: String?,
        sorting: Sorting,
        pagination: Pagination,
        filters: GlobalFilter?,
    ): List<ListMediaItemDto>

    suspend fun getShowListItems(
        listId: TraktId,
        extended: String?,
        sorting: Sorting,
        pagination: Pagination,
        filters: GlobalFilter?,
    ): List<ListShowItemDto>

    suspend fun getMovieListItems(
        listId: TraktId,
        extended: String?,
        sorting: Sorting,
        pagination: Pagination,
        filters: GlobalFilter?,
    ): List<ListMovieItemDto>

    suspend fun addLikedList(listId: TraktId)

    suspend fun removeLikedList(listId: TraktId)
}
