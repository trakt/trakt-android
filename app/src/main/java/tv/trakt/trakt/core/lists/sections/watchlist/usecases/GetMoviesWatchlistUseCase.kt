package tv.trakt.trakt.core.lists.sections.watchlist.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem
import tv.trakt.trakt.core.lists.sections.watchlist.model.getWatchlistSorting
import tv.trakt.trakt.core.user.usecases.lists.LoadUserWatchlistUseCase

internal class GetMoviesWatchlistUseCase(
    private val loadUserWatchlistUseCase: LoadUserWatchlistUseCase,
) {
    suspend fun getLocalWatchlist(
        limit: Int? = null,
        sort: Sorting? = null,
    ): ImmutableList<WatchlistItem> {
        return loadUserWatchlistUseCase
            .loadLocalMovies()
            .sortedWith(getWatchlistSorting(sort))
            .take(limit ?: Int.MAX_VALUE)
            .toImmutableList()
    }

    suspend fun getWatchlist(
        limit: Int? = null,
        sort: Sorting? = null,
    ): ImmutableList<WatchlistItem> {
        return loadUserWatchlistUseCase.loadWatchlist()
            .filterIsInstance<WatchlistItem.MovieItem>()
            .sortedWith(getWatchlistSorting(sort))
            .take(limit ?: Int.MAX_VALUE)
            .toImmutableList()
    }
}
