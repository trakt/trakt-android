package tv.trakt.trakt.core.lists.features.details.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.MediaType.MOVIE
import tv.trakt.trakt.common.model.MediaType.SHOW
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.core.lists.ListsConfig.LISTS_MAX_PAGE_LIMIT
import tv.trakt.trakt.core.lists.data.remote.ListsRemoteDataSource
import tv.trakt.trakt.core.lists.model.CustomListItem

internal class GetListItemsUseCase(
    private val remoteSource: ListsRemoteDataSource,
) {
    suspend fun getItems(
        listId: TraktId,
        type: List<MediaType>,
        sorting: Sorting,
    ): ImmutableList<CustomListItem> {
        if (type.size == 1 && type[0] == MOVIE) {
            return remoteSource.getMovieListItems(
                listId = listId,
                limit = LISTS_MAX_PAGE_LIMIT.toString(),
                extended = "full,cloud9,colors",
                sorting = sorting,
            ).asyncMap {
                CustomListItem.MovieItem(
                    rank = it.rank,
                    movie = Movie.fromDto(it.movie),
                    listedAt = it.listedAt.toInstant(),
                )
            }.toImmutableList()
        }

        if (type.size == 1 && type[0] == SHOW) {
            return remoteSource.getShowListItems(
                listId = listId,
                limit = LISTS_MAX_PAGE_LIMIT.toString(),
                extended = "full,cloud9,colors",
                sorting = sorting,
            ).asyncMap {
                CustomListItem.ShowItem(
                    rank = it.rank,
                    show = Show.fromDto(it.show),
                    listedAt = it.listedAt.toInstant(),
                )
            }.toImmutableList()
        }

        if (type.containsAll(listOf(MOVIE, SHOW))) {
            return remoteSource.getMediaListItems(
                listId = listId,
                limit = LISTS_MAX_PAGE_LIMIT.toString(),
                extended = "full,cloud9,colors",
                sorting = sorting,
            ).asyncMap {
                when (it.type.value) {
                    MOVIE.value -> CustomListItem.MovieItem(
                        rank = it.rank,
                        movie = Movie.fromDto(it.movie!!),
                        listedAt = it.listedAt.toInstant(),
                    )
                    SHOW.value -> CustomListItem.ShowItem(
                        rank = it.rank,
                        show = Show.fromDto(it.show!!),
                        listedAt = it.listedAt.toInstant(),
                    )
                    else -> throw IllegalStateException("Invalid media type: ${it.type}")
                }
            }.toImmutableList()
        }

        throw IllegalStateException("Invalid media type: $type")
    }
}
