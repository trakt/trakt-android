package tv.trakt.trakt.core.lists.sheets.create.usecases

import tv.trakt.trakt.common.core.lists.data.remote.ListsRemoteDataSource
import tv.trakt.trakt.common.model.CustomList

internal class CreateListUseCase(
    private val remoteSource: ListsRemoteDataSource,
) {
    suspend fun createList(
        name: String,
        description: String?,
        privacy: CustomList.Privacy,
    ) {
        remoteSource.createList(
            name = name.trim(),
            description = description?.trim(),
            privacy = privacy.value,
        )
    }
}
