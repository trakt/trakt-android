package tv.trakt.trakt.core.lists.sheets.create.usecases

import tv.trakt.trakt.common.data.remote.ListsRemoteSource
import tv.trakt.trakt.common.model.lists.CustomList

internal class CreateListUseCase(
    private val remoteSource: ListsRemoteSource,
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
