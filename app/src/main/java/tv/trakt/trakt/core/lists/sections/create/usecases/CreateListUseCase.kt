package tv.trakt.trakt.core.lists.sections.create.usecases

import tv.trakt.trakt.core.lists.data.remote.ListsRemoteDataSource

internal class CreateListUseCase(
    private val remoteSource: ListsRemoteDataSource,
) {
    suspend fun createList(
        name: String,
        description: String?,
    ) {
        remoteSource.createList(
            name = name.trim(),
            description = description?.trim(),
        )
    }
}
