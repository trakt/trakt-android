package tv.trakt.trakt.core.lists.data.remote

internal interface ListsRemoteDataSource {
    suspend fun createList(
        name: String,
        description: String?,
    )
}
