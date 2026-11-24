package tv.trakt.trakt.core.lists.features.convert.usecases

import org.openapitools.client.models.PostUsersListsListConvertToSmartListRequest.MediaType
import org.openapitools.client.models.PostUsersListsListConvertToSmartListRequest.Privacy
import org.openapitools.client.models.PostUsersListsListConvertToSmartListRequest.Source
import tv.trakt.trakt.common.data.local.ListsSmartLocalSource
import tv.trakt.trakt.common.data.remote.UserSmartListsRemoteSource
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.lists.CustomList
import tv.trakt.trakt.common.model.lists.SmartList
import tv.trakt.trakt.common.networking.CreateSmartListRequestDto

internal class ConvertToSmartListUseCase(
    private val remoteSource: UserSmartListsRemoteSource,
    private val localSource: ListsSmartLocalSource,
) {
    suspend fun convert(list: CustomList): SmartList {
        val listId = list.ids.trakt
        val draft = checkNotNull(remoteSource.getSmartListDraft(listId)) {
            "No smart list draft for list ${listId.value}"
        }

        val ids = remoteSource.convertToSmartList(
            listId = listId,
            request = CreateSmartListRequestDto(
                name = list.name.trim(),
                source = Source.entries.first { it.value == draft.source },
                mediaType = MediaType.entries.first { it.value == draft.mediaType },
                filters = draft.filters,
                privacy = when (list.privacy) {
                    CustomList.Privacy.Public -> Privacy.PUBLIC
                    else -> Privacy.PRIVATE
                },
            ),
        )

        with(localSource) {
            clear()
            notifyUpdate()
        }

        return SmartList.fromDto(remoteSource.getSmartListDetails(TraktId(ids.trakt)))
    }
}
