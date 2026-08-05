package tv.trakt.trakt.core.lists.sections.smart.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import timber.log.Timber
import tv.trakt.trakt.common.core.user.data.remote.smartlists.UserSmartListsRemoteDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.core.lists.model.SmartListItem

internal class GetSmartListItemsUseCase(
    private val remoteSource: UserSmartListsRemoteDataSource,
) {
    suspend fun getItems(
        listId: TraktId,
        type: String,
        sorting: Sorting = Sorting.Default,
        pagination: Pagination = Pagination.Default,
    ): ImmutableList<SmartListItem> {
        return remoteSource.getSmartListItems(
            listId = listId,
            type = type,
            sorting = sorting,
            pagination = pagination,
        ).asyncMap { dto ->
            val movie = dto.movie
            val show = dto.show
            when {
                show != null -> {
                    SmartListItem.ShowItem(
                        show = Show.fromDto(show),
                    )
                }
                movie != null -> {
                    SmartListItem.MovieItem(
                        movie = Movie.fromDto(movie),
                    )
                }
                else -> {
                    Timber.w("Smart list item missing media: type=%s", dto.type)
                    null
                }
            }
        }
            .filterNotNull()
            .toImmutableList()
    }
}
