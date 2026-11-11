package tv.trakt.trakt.core.lists.features.smart.usecase

import org.openapitools.client.models.PutUsersSmartListsSmartListUpdateRequest
import tv.trakt.trakt.common.data.local.ListsSmartLocalSource
import tv.trakt.trakt.common.data.remote.UserSmartListsRemoteSource
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.lists.SmartList
import tv.trakt.trakt.common.model.lists.SmartListFilters
import tv.trakt.trakt.common.networking.UpdateSmartListRequestDto

internal class UpdateSmartListUseCase(
    private val remoteSource: UserSmartListsRemoteSource,
    private val localSource: ListsSmartLocalSource,
) {
    suspend fun updateList(
        listId: TraktId,
        name: String,
        filters: SmartListFilters,
    ): SmartList {
        // Keeps saved filters this app cannot edit yet (keywords, people, awards) on the list.
        val savedFilters = remoteSource.getSmartListDetails(listId).filters

        val response = remoteSource.updateSmartList(
            listId = listId,
            request = UpdateSmartListRequestDto(
                name = name.trim(),
                source = PutUsersSmartListsSmartListUpdateRequest.Source.entries
                    .firstOrNull { it.value == filters.source.value },
                mediaType = filters.media.toMediaType(),
                filters = filters.toFiltersDto(base = savedFilters),
            ),
        )

        return SmartList.fromDto(response).also {
            with(localSource) {
                replaceList(it)
                notifyUpdate()
            }
        }
    }
}

private fun MediaMode.toMediaType(): PutUsersSmartListsSmartListUpdateRequest.MediaType {
    return when (this) {
        MediaMode.Media -> PutUsersSmartListsSmartListUpdateRequest.MediaType.MEDIA
        MediaMode.Shows -> PutUsersSmartListsSmartListUpdateRequest.MediaType.SHOWS
        MediaMode.Movies -> PutUsersSmartListsSmartListUpdateRequest.MediaType.MOVIES
    }
}
