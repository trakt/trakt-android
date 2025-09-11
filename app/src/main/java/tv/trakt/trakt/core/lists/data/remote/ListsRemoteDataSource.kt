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
}
