package tv.trakt.trakt.app.core.lists.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import tv.trakt.trakt.app.core.details.lists.details.media.model.ListMediaItem
import tv.trakt.trakt.common.data.local.MovieLocalSource
import tv.trakt.trakt.common.data.local.ShowLocalSource
import tv.trakt.trakt.common.data.remote.UserWatchlistRemoteSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.sorting.Sorting

internal class GetListsMediaWatchlistUseCase(
    private val remoteSource: UserWatchlistRemoteSource,
    private val localShowSource: ShowLocalSource,
    private val localMovieSource: MovieLocalSource,
    private val getListSortRatingsUseCase: GetListSortRatingsUseCase,
) {
    suspend fun getItems(
        limit: Int,
        sorting: Sorting,
        page: Int? = null,
    ): ImmutableList<ListMediaItem> {
        return coroutineScope {
            val refreshRatings = page == null || page == 1
            val showRatingsAsync = async { getListSortRatingsUseCase.getShowRatings(sorting, refreshRatings) }
            val movieRatingsAsync = async { getListSortRatingsUseCase.getMovieRatings(sorting, refreshRatings) }

            val dtos = remoteSource.getWatchlist(
                page = page,
                limit = limit,
                extended = "full,colors,streaming_ids",
                sorting = sorting,
                filters = null,
            )
            val showRatings = showRatingsAsync.await()
            val movieRatings = movieRatingsAsync.await()

            dtos
                .asyncMap { dto ->
                    val listedAt = dto.listedAt.toInstant()
                    val movieDto = dto.movie
                    val showDto = dto.show

                    when {
                        movieDto != null -> {
                            val movie = Movie.fromDto(movieDto)
                            ListMediaItem.MovieItem(
                                movie = movie,
                                listedAt = listedAt,
                                userRating = movieRatings[movie.ids.trakt],
                            )
                        }

                        showDto != null -> {
                            val show = Show.fromDto(showDto)
                            ListMediaItem.ShowItem(
                                show = show,
                                listedAt = listedAt,
                                userRating = showRatings[show.ids.trakt],
                            )
                        }

                        else -> {
                            error("Watchlist item has unknown type")
                        }
                    }
                }
                .toImmutableList()
                .also { items ->
                    localShowSource.upsertShows(items.filterIsInstance<ListMediaItem.ShowItem>().map { it.show })
                    localMovieSource.upsertMovies(items.filterIsInstance<ListMediaItem.MovieItem>().map { it.movie })
                }
        }
    }
}
