package tv.trakt.trakt.core.home.sections.watchlist.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.nowLocalDay
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem
import tv.trakt.trakt.core.user.usecases.lists.LoadUserWatchlistUseCase

private val SortComparator =
    compareByDescending<WatchlistItem> { it.released }
        .thenBy { it.title }

internal class GetHomeMoviesWatchlistUseCase(
    private val loadUserWatchlistUseCase: LoadUserWatchlistUseCase,
) {
    suspend fun getLocalWatchlist(limit: Int? = null): ImmutableList<WatchlistItem> {
        val nowDay = nowLocalDay()
        return loadUserWatchlistUseCase.loadLocalMovies()
            .filter {
                it.movie.released != null && it.movie.released!! <= nowDay
            }
            .sortedWith(SortComparator)
            .take(limit ?: Int.MAX_VALUE)
            .toImmutableList()
    }

    suspend fun getWatchlist(limit: Int? = null): ImmutableList<WatchlistItem> {
        val nowDay = nowLocalDay()
        return loadUserWatchlistUseCase.loadWatchlist()
            .filterIsInstance<WatchlistItem.MovieItem>()
            .filter {
                it.movie.released != null &&
                    it.movie.released!! <= nowDay
            }
            .sortedWith(SortComparator)
            .take(limit ?: Int.MAX_VALUE)
            .toImmutableList()
    }
}
