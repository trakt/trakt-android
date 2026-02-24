package tv.trakt.trakt.core.lists.features.details.usecases

import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.lists.data.remote.ListsRemoteDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.MediaType.EPISODE
import tv.trakt.trakt.common.model.MediaType.MOVIE
import tv.trakt.trakt.common.model.MediaType.SEASON
import tv.trakt.trakt.common.model.MediaType.SHOW
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Season
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.core.lists.model.CustomListItem

internal class GetListItemsUseCase(
    private val remoteSource: ListsRemoteDataSource,
) {
    suspend fun getItems(
        listId: TraktId,
        type: List<MediaType>,
        sorting: Sorting,
        pagination: Pagination,
    ): List<CustomListItem> {
        if (type.size == 1 && type[0] == MOVIE) {
            return remoteSource.getMovieListItems(
                listId = listId,
                extended = "full,cloud9,colors",
                sorting = sorting,
                pagination = pagination,
            ).asyncMap {
                CustomListItem.MovieItem(
                    rank = it.rank,
                    movie = Movie.fromDto(it.movie),
                    listedAt = it.listedAt.toInstant(),
                )
            }
        }

        if (type.size == 1 && type[0] == SHOW) {
            return remoteSource.getShowListItems(
                listId = listId,
                extended = "full,cloud9,colors",
                sorting = sorting,
                pagination = pagination,
            ).asyncMap {
                CustomListItem.ShowItem(
                    rank = it.rank,
                    show = Show.fromDto(it.show),
                    listedAt = it.listedAt.toInstant(),
                )
            }.toImmutableList()
        }

        if (type.containsAll(listOf(MOVIE, SHOW))) {
            return remoteSource.getAllListItems(
                listId = listId,
                extended = "full,cloud9,colors",
                sorting = sorting,
                pagination = pagination,
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
                    SEASON.value -> CustomListItem.SeasonItem(
                        rank = it.rank,
                        show = Show.fromDto(it.show!!),
                        season = Season.fromDto(it.season!!),
                        listedAt = it.listedAt.toInstant(),
                    )
                    EPISODE.value -> CustomListItem.EpisodeItem(
                        rank = it.rank,
                        show = Show.fromDto(it.show!!),
                        episode = Episode.fromDto(it.episode!!),
                        listedAt = it.listedAt.toInstant(),
                    )
                    else -> throw IllegalStateException("Invalid media type: ${it.type}")
                }
            }.toImmutableList()
        }

        throw IllegalStateException("Invalid media type: $type")
    }
}
