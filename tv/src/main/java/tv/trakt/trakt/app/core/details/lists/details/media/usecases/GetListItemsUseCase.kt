package tv.trakt.trakt.app.core.details.lists.details.media.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.openapitools.client.models.GetUsersWatchlistAll200ResponseInner.Type
import tv.trakt.trakt.app.core.details.lists.details.CustomListDetailsConfig.CUSTOM_LIST_PAGE_LIMIT
import tv.trakt.trakt.app.core.details.lists.details.media.model.ListMediaItem
import tv.trakt.trakt.app.core.details.lists.details.media.model.ListMediaItem.MovieItem
import tv.trakt.trakt.app.core.details.lists.details.media.model.ListMediaItem.ShowItem
import tv.trakt.trakt.app.core.lists.usecases.GetListSortRatingsUseCase
import tv.trakt.trakt.common.data.local.MovieLocalSource
import tv.trakt.trakt.common.data.local.ShowLocalSource
import tv.trakt.trakt.common.data.remote.ListsRemoteSource
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.common.model.sorting.Sorting

internal class GetListItemsUseCase(
    private val remoteSource: ListsRemoteSource,
    private val showLocalSource: ShowLocalSource,
    private val movieLocalSource: MovieLocalSource,
    private val getListSortRatingsUseCase: GetListSortRatingsUseCase,
) {
    suspend fun getListItems(
        listId: TraktId,
        sorting: Sorting,
        page: Int = 1,
    ): ImmutableList<ListMediaItem> {
        return coroutineScope {
            val showRatingsAsync = async {
                getListSortRatingsUseCase.getShowRatings(sorting, refresh = page == 1)
            }
            val movieRatingsAsync = async {
                getListSortRatingsUseCase.getMovieRatings(sorting, refresh = page == 1)
            }

            val dtos = remoteSource.getMediaListItems(
                listId = listId,
                extended = "full,streaming_ids",
                pagination = Pagination(page, CUSTOM_LIST_PAGE_LIMIT),
                sorting = sorting,
                filters = null,
            )
            val showRatings = showRatingsAsync.await()
            val movieRatings = movieRatingsAsync.await()

            val items = dtos.mapNotNull { dto ->
                when (dto.type) {
                    Type.SHOW -> {
                        val show = dto.show?.let { Show.fromDto(it) } ?: return@mapNotNull null
                        ShowItem(
                            show = show,
                            listedAt = dto.listedAt.toInstant(),
                            userRating = showRatings[show.ids.trakt],
                        )
                    }

                    Type.MOVIE -> {
                        val movie = dto.movie?.let { Movie.fromDto(it) } ?: return@mapNotNull null
                        MovieItem(
                            movie = movie,
                            listedAt = dto.listedAt.toInstant(),
                            userRating = movieRatings[movie.ids.trakt],
                        )
                    }
                }
            }.toImmutableList()

            items.also {
                showLocalSource.upsertShows(it.filterIsInstance<ShowItem>().map { item -> item.show })
                movieLocalSource.upsertMovies(it.filterIsInstance<MovieItem>().map { item -> item.movie })
            }
        }
    }

    suspend fun getShowListItems(
        listId: TraktId,
        sorting: Sorting,
        page: Int = 1,
    ): ImmutableList<ListMediaItem> {
        return coroutineScope {
            val ratingsAsync = async {
                getListSortRatingsUseCase.getShowRatings(sorting, refresh = page == 1)
            }

            val dtos = remoteSource.getShowListItems(
                listId = listId,
                extended = "full,streaming_ids",
                pagination = Pagination(page, CUSTOM_LIST_PAGE_LIMIT),
                sorting = sorting,
                filters = null,
            )
            val ratings = ratingsAsync.await()

            dtos
                .map {
                    val show = Show.fromDto(it.show)
                    ShowItem(
                        show = show,
                        listedAt = it.listedAt.toInstant(),
                        userRating = ratings[show.ids.trakt],
                    )
                }
                .toImmutableList()
                .also {
                    showLocalSource.upsertShows(it.map { item -> item.show })
                }
        }
    }

    suspend fun getMovieListItems(
        listId: TraktId,
        sorting: Sorting,
        page: Int = 1,
    ): ImmutableList<ListMediaItem> {
        return coroutineScope {
            val ratingsAsync = async {
                getListSortRatingsUseCase.getMovieRatings(sorting, refresh = page == 1)
            }

            val dtos = remoteSource.getMovieListItems(
                listId = listId,
                extended = "full,streaming_ids",
                pagination = Pagination(page, CUSTOM_LIST_PAGE_LIMIT),
                sorting = sorting,
                filters = null,
            )
            val ratings = ratingsAsync.await()

            dtos
                .map {
                    val movie = Movie.fromDto(it.movie)
                    MovieItem(
                        movie = movie,
                        listedAt = it.listedAt.toInstant(),
                        userRating = ratings[movie.ids.trakt],
                    )
                }
                .toImmutableList()
                .also {
                    movieLocalSource.upsertMovies(it.map { item -> item.movie })
                }
        }
    }
}
