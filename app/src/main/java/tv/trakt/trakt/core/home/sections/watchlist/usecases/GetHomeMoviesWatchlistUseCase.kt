package tv.trakt.trakt.core.home.sections.watchlist.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.nowLocalDay
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.fromDto
import tv.trakt.trakt.core.home.sections.watchlist.data.local.HomeWatchlistLocalDataSource
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem
import tv.trakt.trakt.core.user.data.remote.UserRemoteDataSource

private val SortComparator =
    compareByDescending<WatchlistItem> { it.released }
        .thenByDescending { it.listedAt }

internal class GetHomeMoviesWatchlistUseCase(
    private val homeWatchlistLocalSource: HomeWatchlistLocalDataSource,
    private val userRemoteSource: UserRemoteDataSource,
) {
    suspend fun getLocalWatchlist(limit: Int? = null): ImmutableList<WatchlistItem> {
        return homeWatchlistLocalSource.getMovieItems()
            .sortedWith(SortComparator)
            .take(limit ?: Int.MAX_VALUE)
            .toImmutableList()
    }

    suspend fun getWatchlist(limit: Int? = null): ImmutableList<WatchlistItem> {
        val nowDay = nowLocalDay()
        return userRemoteSource.getWatchlistMovies(
            page = 1,
            limit = limit,
            extended = "full,cloud9,colors",
            sort = "released",
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
            .sortedWith(SortComparator)
            .also {
                homeWatchlistLocalSource.setItems(items = it)
            }
            .toImmutableList()
    }
}
