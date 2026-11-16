package tv.trakt.trakt.app.core.lists.details.smart.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.app.core.details.lists.details.media.model.ListMediaItem
import tv.trakt.trakt.app.core.details.lists.details.media.model.ListMediaItem.MovieItem
import tv.trakt.trakt.app.core.details.lists.details.media.model.ListMediaItem.ShowItem
import tv.trakt.trakt.app.core.lists.details.personal.PersonalListConfig.PERSONAL_LIST_PAGE_LIMIT
import tv.trakt.trakt.common.data.remote.UserSmartListsRemoteSource
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.pagination.Pagination

internal class GetSmartListItemsUseCase(
    private val remoteSource: UserSmartListsRemoteSource,
) {
    suspend fun getListItems(
        listId: TraktId,
        page: Int = 1,
    ): ImmutableList<ListMediaItem> {
        val pagination = Pagination(
            page = page,
            limit = PERSONAL_LIST_PAGE_LIMIT,
        )

        return remoteSource.getSmartListItems(
            listId = listId,
            pagination = pagination,
        ).mapNotNull { dto ->
            val show = dto.show
            val movie = dto.movie
            when {
                show != null -> ShowItem(show = Show.fromDto(show))
                movie != null -> MovieItem(movie = Movie.fromDto(movie))
                else -> null
            }
        }.toImmutableList()
    }
}
