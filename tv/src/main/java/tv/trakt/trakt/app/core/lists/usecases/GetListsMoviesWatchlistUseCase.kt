package tv.trakt.trakt.app.core.lists.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import tv.trakt.trakt.app.core.details.lists.details.media.model.ListMediaItem
import tv.trakt.trakt.common.data.local.MovieLocalSource
import tv.trakt.trakt.common.data.remote.MoviesSyncRemoteSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.sorting.Sorting

internal class GetListsMoviesWatchlistUseCase(
    private val remoteSyncSource: MoviesSyncRemoteSource,
    private val localMovieSource: MovieLocalSource,
    private val getListSortRatingsUseCase: GetListSortRatingsUseCase,
) {
    suspend fun getMovies(
        limit: Int,
        sorting: Sorting,
        page: Int? = null,
    ): ImmutableList<ListMediaItem.MovieItem> {
        return coroutineScope {
            val ratingsAsync = async {
                getListSortRatingsUseCase.getMovieRatings(sorting, refresh = page == null || page == 1)
            }

            val dtos = remoteSyncSource.getWatchlist(
                page = page,
                limit = limit,
                extended = "full,colors,streaming_ids",
                sorting = sorting,
            )
            val ratings = ratingsAsync.await()

            dtos
                .asyncMap {
                    val movie = Movie.fromDto(it.movie)
                    ListMediaItem.MovieItem(
                        movie = movie,
                        listedAt = it.listedAt.toInstant(),
                        userRating = ratings[movie.ids.trakt],
                    )
                }
                .toImmutableList()
                .also {
                    localMovieSource.upsertMovies(it.map { item -> item.movie })
                }
        }
    }
}
