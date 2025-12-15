package tv.trakt.trakt.core.lists.features.details.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.sorting.SortTypeList.DEFAULT
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.core.lists.data.remote.ListsRemoteDataSource
import tv.trakt.trakt.core.lists.model.PersonalListItem

internal class GetListItemsUseCase(
    private val remoteSource: ListsRemoteDataSource,
) {
    suspend fun getItems(
        listId: TraktId,
        type: MediaType?,
        sorting: Sorting,
    ): ImmutableList<PersonalListItem> {
        if (type == MediaType.MOVIE) {
            return remoteSource.getMovieListItems(
                listId = listId,
                limit = "all",
                extended = "full,cloud9,colors",
                sorting = when {
                    sorting.type == DEFAULT -> sorting
                    else -> sorting.copy(order = sorting.order.toggle())
                },
            ).asyncMap {
                PersonalListItem.MovieItem(
                    rank = it.rank,
                    movie = Movie.fromDto(it.movie),
                    listedAt = it.listedAt.toInstant(),
                )
            }.toImmutableList()
        }

        if (type == MediaType.SHOW) {
            return remoteSource.getShowListItems(
                listId = listId,
                limit = "all",
                extended = "full,cloud9,colors",
                sorting = when {
                    sorting.type == DEFAULT -> sorting
                    else -> sorting.copy(order = sorting.order.toggle())
                },
            ).asyncMap {
                PersonalListItem.ShowItem(
                    rank = it.rank,
                    show = Show.fromDto(it.show),
                    listedAt = it.listedAt.toInstant(),
                )
            }.toImmutableList()
        }

        throw IllegalStateException("Invalid media type: $type")
    }
}
