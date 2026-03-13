package tv.trakt.trakt.core.home.sections.watchlist.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.user.data.remote.UserRemoteDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.nowLocalDay
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.common.model.sorting.SortOrder
import tv.trakt.trakt.common.model.sorting.SortTypeList
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.core.home.sections.watchlist.data.local.HomeWatchlistLocalDataSource
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem

internal class GetHomeMoviesWatchlistUseCase(
    private val homeWatchlistLocalSource: HomeWatchlistLocalDataSource,
    private val userRemoteSource: UserRemoteDataSource,
) {
    suspend fun getLocalWatchlist(limit: Int): ImmutableList<WatchlistItem> {
        return homeWatchlistLocalSource.getMovieItems()
            .take(limit)
            .toImmutableList()
    }

    suspend fun getWatchlist(limit: Int): ImmutableList<WatchlistItem> {
        val nowDay = nowLocalDay()
        return userRemoteSource.getWatchlistMovies(
            page = 1,
            limit = limit,
            extended = "full,cloud9,colors",
            sorting = Sorting(
                type = SortTypeList.RELEASED,
                order = SortOrder.DESCENDING,
            ),
            hide = "unreleased",
        )
            .asyncMap {
                WatchlistItem.MovieItem(
                    movie = Movie.fromDto(it.movie),
                    rank = it.rank,
                    listedAt = it.listedAt.toInstant(),
                )
            }
            .filter {
                it.movie.released != null &&
                    it.movie.released!! <= nowDay
            }
            .also {
                homeWatchlistLocalSource.setMovieItems(items = it)
            }
            .toImmutableList()
    }
}
