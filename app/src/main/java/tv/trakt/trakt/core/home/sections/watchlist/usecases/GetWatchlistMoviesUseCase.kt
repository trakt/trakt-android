package tv.trakt.trakt.core.home.sections.watchlist.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.nowLocalDay
import tv.trakt.trakt.core.home.HomeConfig.HOME_WATCHLIST_LIMIT
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem
import tv.trakt.trakt.core.user.usecase.watchlist.LoadUserWatchlistUseCase

internal class GetWatchlistMoviesUseCase(
    val loadUserWatchlistUseCase: LoadUserWatchlistUseCase,
) {
    suspend fun getLocalWatchlist(limit: Int = HOME_WATCHLIST_LIMIT): ImmutableList<WatchlistItem.MovieItem> {
        val nowDay = nowLocalDay()

        return loadUserWatchlistUseCase.loadLocalMovies()
            .filter {
                it.movie.released != null && it.movie.released!! <= nowDay
            }
            .sortedWith(
                compareByDescending<WatchlistItem.MovieItem> { it.movie.released }
                    .thenByDescending { it.rank },
            )
            .take(limit)
            .toImmutableList()
    }

    suspend fun getWatchlist(limit: Int = HOME_WATCHLIST_LIMIT): ImmutableList<WatchlistItem.MovieItem> {
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
            .take(limit)
            .toImmutableList()
    }
}
