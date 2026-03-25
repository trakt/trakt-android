package tv.trakt.trakt.core.lists.sections.watchlist.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.user.data.remote.watchlist.UserWatchlistRemoteDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem
import tv.trakt.trakt.core.lists.sections.watchlist.model.getWatchlistSorting
import tv.trakt.trakt.core.user.data.local.watchlist.UserWatchlistLocalDataSource

internal class GetMoviesWatchlistUseCase(
    private val remoteSource: UserWatchlistRemoteDataSource,
    private val userWatchlistLocalDataSource: UserWatchlistLocalDataSource,
) {
    suspend fun getLocalWatchlist(
        limit: Int,
        sort: Sorting,
    ): ImmutableList<WatchlistItem> {
        return userWatchlistLocalDataSource.getMovies()
            .sortedWith(getWatchlistSorting(sort))
            .take(limit)
            .toImmutableList()
    }

    suspend fun getRemoteWatchlist(
        page: Int,
        limit: Int,
        sorting: Sorting,
        skipLocal: Boolean = false,
    ): ImmutableList<WatchlistItem> {
        val response = remoteSource.getWatchlistMovies(
            page = page,
            limit = limit,
            sorting = sorting,
            extended = "full,cloud9,colors",
        ).asyncMap {
            val listedAt = it.listedAt.toInstant()
            WatchlistItem.MovieItem(
                movie = Movie.fromDto(it.movie),
                rank = it.rank,
                listedAt = listedAt,
            )
        }

        return response
            .sortedWith(getWatchlistSorting(sorting))
            .toImmutableList()
            .also {
                if (skipLocal) return@also
                with(userWatchlistLocalDataSource) {
                    if (page == 1) {
                        setMovies(response)
                    } else {
                        addMovies(response)
                    }
                }
            }
    }
}
