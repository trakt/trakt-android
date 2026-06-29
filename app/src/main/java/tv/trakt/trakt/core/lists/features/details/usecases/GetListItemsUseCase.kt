package tv.trakt.trakt.core.lists.features.details.usecases

import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.lists.data.remote.ListsRemoteDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.MediaMode.MOVIES
import tv.trakt.trakt.common.model.MediaMode.SHOWS
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Season
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.core.lists.model.CustomListItem
import tv.trakt.trakt.core.user.usecases.ratings.LoadUserRatingsUseCase

internal class GetListItemsUseCase(
    private val remoteSource: ListsRemoteDataSource,
    private val loadUserRatingsUseCase: LoadUserRatingsUseCase,
) {
    suspend fun getItems(
        listId: TraktId,
        type: List<MediaType>,
        sorting: Sorting,
        pagination: Pagination,
        filters: GlobalFilter?,
    ): List<CustomListItem> {
        val (showsRatings, moviesRatings, episodesRatings) =
            loadUserRatingsUseCase.loadAllIfNeeded()

        if (type.size == 1 && type[0] == MediaType.Movie) {
            return remoteSource.getMovieListItems(
                listId = listId,
                extended = "full,cloud9,colors",
                sorting = sorting,
                pagination = pagination,
                filters = filters?.copy(mode = MOVIES),
            ).asyncMap {
                val movie = Movie.fromDto(it.movie)
                CustomListItem.MovieItem(
                    itemId = it.id,
                    rank = it.rank,
                    movie = movie,
                    listedAt = it.listedAt.toInstant(),
                    userRating = moviesRatings[movie.ids.trakt],
                )
            }
        }

        if (type.size == 1 && type[0] == MediaType.Show) {
            return remoteSource.getShowListItems(
                listId = listId,
                extended = "full,cloud9,colors",
                sorting = sorting,
                pagination = pagination,
                filters = filters?.copy(mode = SHOWS),
            ).asyncMap {
                val show = Show.fromDto(it.show)
                CustomListItem.ShowItem(
                    itemId = it.id,
                    rank = it.rank,
                    show = show,
                    listedAt = it.listedAt.toInstant(),
                    userRating = showsRatings[show.ids.trakt],
                )
            }.toImmutableList()
        }

        if (type.containsAll(listOf(Movie, Show))) {
            return remoteSource.getAllListItems(
                listId = listId,
                extended = "full,cloud9,colors",
                sorting = sorting,
                pagination = pagination,
                filters = filters,
            ).asyncMap {
                when (it.type.value) {
                    MediaType.Show.value -> {
                        val show = Show.fromDto(it.show!!)
                        CustomListItem.ShowItem(
                            itemId = it.id,
                            rank = it.rank,
                            show = show,
                            listedAt = it.listedAt.toInstant(),
                            userRating = showsRatings[show.ids.trakt],
                        )
                    }
                    MediaType.Movie.value -> {
                        val movie = Movie.fromDto(it.movie!!)
                        CustomListItem.MovieItem(
                            itemId = it.id,
                            rank = it.rank,
                            movie = movie,
                            listedAt = it.listedAt.toInstant(),
                            userRating = moviesRatings[movie.ids.trakt],
                        )
                    }
                    MediaType.Season.value -> {
                        CustomListItem.SeasonItem(
                            itemId = it.id,
                            rank = it.rank,
                            show = Show.fromDto(it.show!!),
                            season = Season.fromDto(it.season!!),
                            listedAt = it.listedAt.toInstant(),
                            userRating = null,
                        )
                    }
                    MediaType.Episode.value -> {
                        val show = Show.fromDto(it.show!!)
                        val episode = Episode.fromDto(it.episode!!)
                        CustomListItem.EpisodeItem(
                            itemId = it.id,
                            rank = it.rank,
                            show = show,
                            episode = episode,
                            listedAt = it.listedAt.toInstant(),
                            userRating = episodesRatings[episode.ids.trakt],
                        )
                    }
                    else -> {
                        throw IllegalStateException("Invalid media type: ${it.type}")
                    }
                }
            }.toImmutableList()
        }

        throw IllegalStateException("Invalid media type: $type")
    }
}
