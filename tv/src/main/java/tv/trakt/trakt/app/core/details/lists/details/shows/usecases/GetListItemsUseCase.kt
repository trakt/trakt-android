package tv.trakt.trakt.app.core.details.lists.details.shows.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.app.core.details.lists.data.remote.ListsRemoteDataSource
import tv.trakt.trakt.app.core.details.lists.details.CustomListDetailsConfig.CUSTOM_LIST_PAGE_LIMIT
import tv.trakt.trakt.app.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto

internal class GetListItemsUseCase(
    private val remoteSource: ListsRemoteDataSource,
    private val localSource: ShowLocalDataSource,
) {
    suspend fun getListItems(
        listId: TraktId,
        page: Int = 1,
    ): ImmutableList<Show> {
        val shows = remoteSource.getShowListItems(
            listId = listId,
            limit = CUSTOM_LIST_PAGE_LIMIT,
            page = page,
            extended = "full,images",
        )
            .map { Show.fromDto(it.show) }
            .toImmutableList()

        localSource.upsertShows(shows)

        return shows
    }
}
