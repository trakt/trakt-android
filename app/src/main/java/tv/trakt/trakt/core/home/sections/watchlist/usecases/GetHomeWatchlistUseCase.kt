package tv.trakt.trakt.core.home.sections.watchlist.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.nowLocalDay
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem
import tv.trakt.trakt.core.user.usecases.lists.LoadUserWatchlistUseCase

internal class GetHomeWatchlistUseCase(
    val loadUserWatchlistUseCase: LoadUserWatchlistUseCase,
) {
    suspend fun getLocalWatchlist(limit: Int? = null): ImmutableList<WatchlistItem.MovieItem> {
        val nowDay = nowLocalDay()

        return loadUserWatchlistUseCase.loadLocalMovies()
            .filter {
                it.movie.released != null && it.movie.released!! <= nowDay
            }
            .sortedWith(
                compareByDescending<WatchlistItem.MovieItem> { it.movie.released }
                    .thenByDescending { it.rank },
            )
            .take(limit ?: Int.MAX_VALUE)
            .toImmutableList()
    }

    suspend fun getWatchlist(limit: Int? = null): ImmutableList<WatchlistItem.MovieItem> {
        val nowDay = nowLocalDay()

        return loadUserWatchlistUseCase.loadWatchlist()
            .filterIsInstance<WatchlistItem.MovieItem>()
            .filter {
                it.movie.released != null && it.movie.released!! <= nowDay
            }
            .sortedWith(
                compareByDescending<WatchlistItem.MovieItem> { it.movie.released }
                    .thenByDescending { it.rank },
            )
            .take(limit ?: Int.MAX_VALUE)
            .toImmutableList()
    }
}
