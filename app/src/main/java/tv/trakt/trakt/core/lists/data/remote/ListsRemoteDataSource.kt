package tv.trakt.trakt.core.lists.data.remote

import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.networking.ListItemDto
import tv.trakt.trakt.common.networking.ListMovieItemDto
import tv.trakt.trakt.common.networking.ListShowItemDto

internal interface ListsRemoteDataSource {
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
        listId: TraktId,
        showId: TraktId,
    )

    suspend fun removeShowFromList(
        listId: TraktId,
        showId: TraktId,
    )

    suspend fun addMovieToList(
        listId: TraktId,
        movieId: TraktId,
    )

    suspend fun removeMovieFromList(
        listId: TraktId,
        movieId: TraktId,
    )

    suspend fun getAllListItems(
        listId: TraktId,
        extended: String?,
        limit: String?,
    ): List<ListItemDto>

    suspend fun getShowListItems(
        listId: TraktId,
        extended: String?,
        limit: String?,
    ): List<ListShowItemDto>

    suspend fun getMovieListItems(
        listId: TraktId,
        extended: String?,
        limit: String?,
    ): List<ListMovieItemDto>
}
