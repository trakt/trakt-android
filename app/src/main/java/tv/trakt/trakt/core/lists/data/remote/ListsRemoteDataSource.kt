package tv.trakt.trakt.core.lists.data.remote

import tv.trakt.trakt.common.model.TraktId

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

    suspend fun removeShowFromList(
        listId: TraktId,
        showId: TraktId,
    )

    suspend fun removeMovieFromList(
        listId: TraktId,
        movieId: TraktId,
    )
}
