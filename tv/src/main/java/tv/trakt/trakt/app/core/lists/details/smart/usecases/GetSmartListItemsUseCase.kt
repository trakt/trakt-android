package tv.trakt.trakt.app.core.lists.details.smart.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.app.core.lists.details.personal.PersonalListConfig.PERSONAL_LIST_PAGE_LIMIT
import tv.trakt.trakt.app.core.lists.details.personal.model.PersonalListItem
import tv.trakt.trakt.common.core.user.data.remote.smartlists.UserSmartListsRemoteDataSource
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.pagination.Pagination

internal class GetSmartListItemsUseCase(
    private val remoteSource: UserSmartListsRemoteDataSource,
) {
    suspend fun getListItems(
        listId: TraktId,
        page: Int = 1,
    ): ImmutableList<PersonalListItem> {
        val pagination = Pagination(
            page = page,
            limit = PERSONAL_LIST_PAGE_LIMIT,
        )

        return remoteSource.getSmartListItems(
            listId = listId,
            pagination = pagination,
        ).mapIndexedNotNull { index, dto ->
            val rank = (page - 1) * PERSONAL_LIST_PAGE_LIMIT + index + 1
            val show = dto.show
            val movie = dto.movie
            when {
                show != null -> PersonalListItem(
                    rank = rank,
                    type = "show",
                    show = Show.fromDto(show),
                )
                movie != null -> PersonalListItem(
                    rank = rank,
                    type = "movie",
                    movie = Movie.fromDto(movie),
                )
                else -> null
            }
        }.toImmutableList()
    }
}
